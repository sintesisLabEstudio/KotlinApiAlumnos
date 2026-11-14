// Paquete principal de la aplicacion.
package com.tnt.notas
// Importaciones para navegacion, visibilidad, componentes de UI y corrutinas.
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
// Actividad que lista alumnos y, segun modo, permite editarlos.
class ListaAlumnosActivity : AppCompatActivity() {
    // Estado de datos y modo de funcionamiento de la pantalla.
    var alumnos: List<AlumnoDto> = emptyList()
    var seleccionado: AlumnoDto? = null
    var modoEdicion: Boolean = false
    // Referencias a elementos de la interfaz.
    lateinit var lvAlumnos: ListView
    lateinit var tvTitulo: TextView
    lateinit var tvCodigo: TextView
    lateinit var etNombres: EditText
    lateinit var etApellidos: EditText
    lateinit var tvResultado: TextView
    lateinit var btnEditarAlumno: Button
    // Inicializa la vista, determina el modo y configura eventos.
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Asocia el layout de listado/edicion de alumnos.
        setContentView(R.layout.activity_lista_alumnos)
        // Boton para volver al menu principal.
        findViewById<Button>(R.id.btnHome).setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            startActivity(intent)
            finish()
        }
        // Lee el modo enviado desde el menu (listar o editar).
        modoEdicion = intent.getBooleanExtra("modoEdicion", false)
        // Vincula las vistas del layout.
        lvAlumnos = findViewById(R.id.lvAlumnos)
        tvTitulo = findViewById(R.id.tvTitulo)
        tvCodigo = findViewById(R.id.tvCodigo)
        etNombres = findViewById(R.id.etNombres)
        etApellidos = findViewById(R.id.etApellidos)
        tvResultado = findViewById(R.id.tvResultado)
        btnEditarAlumno = findViewById(R.id.btnEditarAlumno)
        // Configura la interfaz segun si es modo edicion o solo listado.
        if (modoEdicion) {
            tvTitulo.text = "Editar alumno (toque uno para editar)"
            tvCodigo.visibility = View.VISIBLE
            etNombres.visibility = View.VISIBLE
            etApellidos.visibility = View.VISIBLE
            btnEditarAlumno.visibility = View.VISIBLE
            // Al seleccionar un alumno, carga sus datos en el formulario.
            lvAlumnos.setOnItemClickListener { _, _, posicion, _ ->
                val a = alumnos[posicion]
                seleccionado = a
                tvCodigo.text = "Codigo: " + a.codAlumno
                etNombres.setText(a.nombres)
                etApellidos.setText(a.apellidos)
                tvResultado.text = "Alumno seleccionado: " + a.codAlumno
            }
            // Ejecuta la edicion del alumno seleccionado.
            btnEditarAlumno.setOnClickListener { editar() }
        } else {
            tvTitulo.text = "Listado de alumnos"
            tvCodigo.visibility = View.GONE
            etNombres.visibility = View.GONE
            etApellidos.visibility = View.GONE
            btnEditarAlumno.visibility = View.GONE
            // En modo listado no se permite seleccionar para editar.
            lvAlumnos.setOnItemClickListener(null)
        }
        // Configura la actualizacion manual de la lista.
        findViewById<Button>(R.id.btnActualizar).setOnClickListener { cargar() }
        // Carga inicial de alumnos.
        cargar()
    }
    // Consulta y muestra los alumnos desde la API.
    fun cargar() {
        lifecycleScope.launch {
            try {
                // Solicitud de red en hilo IO.
                val r = withContext(Dispatchers.IO) { RetrofitClient.api.listarAlumnos() }
                if (!r.isSuccessful) {
                    tvResultado.text = "Error " + r.code()
                    return@launch
                }
                // Actualiza la lista y el adaptador con los datos recibidos.
                alumnos = r.body() ?: emptyList()
                val textos = alumnos.map { it.codAlumno.toString() + " - " + it.nombres + " " + it.apellidos }
                lvAlumnos.adapter = ArrayAdapter(this@ListaAlumnosActivity, android.R.layout.simple_list_item_1, textos)
                // Muestra estado segun haya o no registros.
                if (alumnos.isEmpty()) {
                    tvResultado.text = "Aun no hay alumnos ingresados"
                } else {
                    tvResultado.text = "Alumnos: " + alumnos.size
                }
            } catch (e: Exception) {
                // Manejo de errores de conexion.
                tvResultado.text = "No se pudo conectar con la API: " + e.message
            }
        }
    }
    // Valida y envia la edicion del alumno seleccionado (solo en modo edicion).
    fun editar() {
        if (!modoEdicion) {
            return
        }
        val a = seleccionado
        if (a == null) {
            tvResultado.text = "Seleccione un alumno de la lista"
            return
        }
        val nombres = etNombres.text.toString().trim()
        val apellidos = etApellidos.text.toString().trim()
        // Valida campos obligatorios.
        if (nombres == "" || apellidos == "") {
            tvResultado.text = "Complete nombres y apellidos"
            return
        }
        // Envia la actualizacion a la API y refresca la lista al finalizar.
        lifecycleScope.launch {
            tvResultado.text = "Enviando..."
            val mensaje = RetrofitClient.guardar {
                RetrofitClient.api.editarAlumno(AlumnoDto(a.codAlumno, nombres, apellidos))
            }
            tvResultado.text = mensaje
            if (mensaje == "Guardado correctamente") {
                seleccionado = null
                tvCodigo.text = "Codigo: -"
                etNombres.text.clear()
                etApellidos.text.clear()
                cargar()
            }
        }
    }
}
