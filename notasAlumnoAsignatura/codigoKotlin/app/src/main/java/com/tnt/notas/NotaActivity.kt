// Paquete principal de la aplicacion.
package com.tnt.notas
// Importaciones para navegacion, componentes de UI, eventos y corrutinas.
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
// Actividad para ingresar notas, mostrando la nota correspondiente a la seleccion actual.
class NotaActivity : AppCompatActivity() {
    // Listas en memoria con alumnos, asignaturas y notas cargadas desde la API.
    var alumnos: List<AlumnoDto> = emptyList()
    var asignaturas: List<AsignaturaDto> = emptyList()
    var listadoNotas: List<ListadoNota> = emptyList()
    // Referencias a los controles de la interfaz.
    lateinit var spAlumno: Spinner
    lateinit var spAsignatura: Spinner
    lateinit var spNumero: Spinner
    lateinit var etNota: EditText
    lateinit var tvResultado: TextView
    // Inicializa la pantalla, listeners y carga de datos.
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Asocia el layout de ingreso de notas.
        setContentView(R.layout.activity_nota)
        // Boton para volver al menu principal.
        findViewById<Button>(R.id.btnHome).setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            startActivity(intent)
            finish()
        }
        // Vincula variables con los elementos del layout.
        spAlumno = findViewById(R.id.spAlumno)
        spAsignatura = findViewById(R.id.spAsignatura)
        spNumero = findViewById(R.id.spNumero)
        etNota = findViewById(R.id.etNota)
        tvResultado = findViewById(R.id.tvResultado)
        // Configura el selector de numero de nota (1 a 3).
        val numeros = ArrayAdapter(this, android.R.layout.simple_spinner_item, listOf("1", "2", "3"))
        numeros.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spNumero.adapter = numeros
        // Listener comun para actualizar la nota mostrada al cambiar la seleccion.
        val listener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                mostrarNotaCorrespondiente()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }
        spAlumno.onItemSelectedListener = listener
        spAsignatura.onItemSelectedListener = listener
        spNumero.onItemSelectedListener = listener
        // Ejecuta el ingreso de nota al presionar el boton.
        findViewById<Button>(R.id.btnGuardar).setOnClickListener { ingresarNota() }
    }
    // Recarga datos cada vez que la pantalla vuelve a primer plano.
    override fun onResume() {
        super.onResume()
        cargarListas()
    }
    // Obtiene alumnos, asignaturas y listado de notas desde la API.
    fun cargarListas() {
        lifecycleScope.launch {
            try {
                // Solicitudes de red ejecutadas en el hilo IO.
                val respAlumnos = withContext(Dispatchers.IO) { RetrofitClient.api.listarAlumnos() }
                val respAsignaturas = withContext(Dispatchers.IO) { RetrofitClient.api.listarAsignaturas() }
                val respListado = withContext(Dispatchers.IO) { RetrofitClient.api.listado() }
                // Verifica que las listas principales se hayan obtenido correctamente.
                if (!respAlumnos.isSuccessful || !respAsignaturas.isSuccessful) {
                    tvResultado.text = "No se pudieron cargar alumnos y asignaturas"
                    return@launch
                }
                // Guarda los datos recibidos para usarlos en la UI.
                alumnos = respAlumnos.body() ?: emptyList()
                asignaturas = respAsignaturas.body() ?: emptyList()
                listadoNotas = if (respListado.isSuccessful) respListado.body() ?: emptyList() else emptyList()
                // Prepara textos visibles para los spinners.
                val textosAlumnos = alumnos.map { it.codAlumno.toString() + " - " + it.nombres + " " + it.apellidos }
                val textosAsignaturas = asignaturas.map { it.nomAsignatura }
                // Asigna adaptadores a los spinners de alumnos y asignaturas.
                val adA = ArrayAdapter(this@NotaActivity, android.R.layout.simple_spinner_item, textosAlumnos)
                adA.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spAlumno.adapter = adA
                val adS = ArrayAdapter(this@NotaActivity, android.R.layout.simple_spinner_item, textosAsignaturas)
                adS.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spAsignatura.adapter = adS
                // Muestra la nota correspondiente a la seleccion inicial.
                mostrarNotaCorrespondiente()
            } catch (e: Exception) {
                // Informa errores de conexion o carga.
                tvResultado.text = "No se pudo conectar con la API: " + e.message
            }
        }
    }
    // Busca y muestra la nota existente segun alumno, asignatura y numero seleccionados.
    fun mostrarNotaCorrespondiente() {
        if (alumnos.isEmpty() || asignaturas.isEmpty()) {
            return
        }
        val codAlumno = alumnos[spAlumno.selectedItemPosition].codAlumno
        val nomAsignatura = asignaturas[spAsignatura.selectedItemPosition].nomAsignatura
        val nNota = spNumero.selectedItem.toString().toInt()
        // Localiza la fila del listado que coincide con la seleccion.
        val fila = listadoNotas.find { it.codAlumno == codAlumno && it.nomAsignatura == nomAsignatura }
        // Selecciona la nota 1, 2 o 3 segun corresponda.
        val valor = when (nNota) {
            1 -> fila?.nota1
            2 -> fila?.nota2
            3 -> fila?.nota3
            else -> null
        }
        // Actualiza el campo y el mensaje segun exista o no una nota registrada.
        if (valor != null) {
            etNota.setText(valor.toString())
            tvResultado.text = "Nota actual cargada para la seleccion"
        } else {
            etNota.setText("")
            tvResultado.text = "No hay nota registrada para la seleccion"
        }
    }
    // Valida y envia una nueva nota a la API.
    fun ingresarNota() {
        if (alumnos.isEmpty() || asignaturas.isEmpty()) {
            tvResultado.text = "Primero ingrese al menos un alumno y una asignatura"
            return
        }
        val valor = etNota.text.toString().replace(",", ".").toDoubleOrNull()
        // Valida el rango permitido de la nota.
        if (valor == null || valor < 1.0 || valor > 7.0) {
            tvResultado.text = "La nota debe estar entre 1.0 y 7.0"
            return
        }
        // Construye el objeto nota con la seleccion actual.
        val nota = NotaDto(
            alumnos[spAlumno.selectedItemPosition].codAlumno,
            asignaturas[spAsignatura.selectedItemPosition].idAsignatura,
            spNumero.selectedItem.toString().toInt(),
            valor
        )
        // Envia la solicitud de creacion y refresca la nota mostrada si fue exitosa.
        lifecycleScope.launch {
            tvResultado.text = "Enviando..."
            val mensaje = RetrofitClient.guardar {
                RetrofitClient.api.crearNota(nota)
            }
            tvResultado.text = mensaje
            if (mensaje == "Guardado correctamente") {
                val respListado = withContext(Dispatchers.IO) { RetrofitClient.api.listado() }
                if (respListado.isSuccessful) {
                    listadoNotas = respListado.body() ?: emptyList()
                    mostrarNotaCorrespondiente()
                }
            }
        }
    }
}
