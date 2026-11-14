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
// Actividad para editar la nota correspondiente a la seleccion actual.
class EditarNotaActivity : AppCompatActivity() {
    // Listas en memoria con datos cargados desde la API.
    var alumnos: List<AlumnoDto> = emptyList()
    var asignaturas: List<AsignaturaDto> = emptyList()
    var listadoNotas: List<ListadoNota> = emptyList()
    // Referencias a los controles de la interfaz.
    lateinit var spAlumno: Spinner
    lateinit var spAsignatura: Spinner
    lateinit var spNumero: Spinner
    lateinit var etNota: EditText
    lateinit var tvResultado: TextView
    // Inicializa la pantalla, listeners y carga inicial de datos.
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Asocia el layout de edicion de notas.
        setContentView(R.layout.activity_editar_nota)
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
        // Listener para actualizar la nota mostrada al cambiar cualquier seleccion.
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
        // Ejecuta la edicion de la nota al presionar el boton.
        findViewById<Button>(R.id.btnEditarNota).setOnClickListener { editarNota() }
        // Carga inicial de datos desde la API.
        cargarDatos()
    }
    // Obtiene alumnos, asignaturas y listado de notas desde la API.
    fun cargarDatos() {
        lifecycleScope.launch {
            try {
                // Solicitudes de red en hilo IO.
                val respAlumnos = withContext(Dispatchers.IO) { RetrofitClient.api.listarAlumnos() }
                val respAsignaturas = withContext(Dispatchers.IO) { RetrofitClient.api.listarAsignaturas() }
                val respListado = withContext(Dispatchers.IO) { RetrofitClient.api.listado() }
                // Verifica disponibilidad de alumnos y asignaturas.
                if (!respAlumnos.isSuccessful || !respAsignaturas.isSuccessful) {
                    tvResultado.text = "No se pudieron cargar alumnos y asignaturas"
                    return@launch
                }
                // Almacena los datos obtenidos.
                alumnos = respAlumnos.body() ?: emptyList()
                asignaturas = respAsignaturas.body() ?: emptyList()
                listadoNotas = if (respListado.isSuccessful) respListado.body() ?: emptyList() else emptyList()
                // Prepara textos para los spinners.
                val textosAlumnos = alumnos.map { it.codAlumno.toString() + " - " + it.nombres + " " + it.apellidos }
                val textosAsignaturas = asignaturas.map { it.nomAsignatura }
                // Asigna adaptadores a los spinners.
                val adA = ArrayAdapter(this@EditarNotaActivity, android.R.layout.simple_spinner_item, textosAlumnos)
                adA.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spAlumno.adapter = adA
                val adS = ArrayAdapter(this@EditarNotaActivity, android.R.layout.simple_spinner_item, textosAsignaturas)
                adS.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spAsignatura.adapter = adS
                // Muestra la nota correspondiente a la seleccion inicial.
                mostrarNotaCorrespondiente()
            } catch (e: Exception) {
                // Manejo de errores de conexion.
                tvResultado.text = "No se pudo conectar con la API: " + e.message
            }
        }
    }
    // Carga en el campo la nota existente segun la combinacion seleccionada.
    fun mostrarNotaCorrespondiente() {
        if (alumnos.isEmpty() || asignaturas.isEmpty()) {
            return
        }
        val codAlumno = alumnos[spAlumno.selectedItemPosition].codAlumno
        val nomAsignatura = asignaturas[spAsignatura.selectedItemPosition].nomAsignatura
        val nNota = spNumero.selectedItem.toString().toInt()
        // Busca la fila correspondiente en el listado consolidado.
        val fila = listadoNotas.find { it.codAlumno == codAlumno && it.nomAsignatura == nomAsignatura }
        // Obtiene el valor de la nota segun el numero seleccionado.
        val valor = when (nNota) {
            1 -> fila?.nota1
            2 -> fila?.nota2
            3 -> fila?.nota3
            else -> null
        }
        // Refleja en pantalla la nota encontrada o indica que no existe.
        if (valor != null) {
            etNota.setText(valor.toString())
            tvResultado.text = "Nota actual cargada para la seleccion"
        } else {
            etNota.setText("")
            tvResultado.text = "No hay nota registrada para la seleccion"
        }
    }
    // Valida y envia la edicion de la nota seleccionada.
    fun editarNota() {
        if (alumnos.isEmpty() || asignaturas.isEmpty()) {
            tvResultado.text = "Primero ingrese al menos un alumno y una asignatura"
            return
        }
        val valor = etNota.text.toString().replace(",", ".").toDoubleOrNull()
        // Valida que la nota este dentro del rango permitido.
        if (valor == null || valor < 1.0 || valor > 7.0) {
            tvResultado.text = "La nota debe estar entre 1.0 y 7.0"
            return
        }
        // Construye el objeto nota con los datos seleccionados.
        val nota = NotaDto(
            alumnos[spAlumno.selectedItemPosition].codAlumno,
            asignaturas[spAsignatura.selectedItemPosition].idAsignatura,
            spNumero.selectedItem.toString().toInt(),
            valor
        )
        // Envia la actualizacion y refresca los datos mostrados si es exitosa.
        lifecycleScope.launch {
            tvResultado.text = "Enviando..."
            val mensaje = RetrofitClient.guardar {
                RetrofitClient.api.editarNota(nota)
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
