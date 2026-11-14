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
// Actividad que lista asignaturas y, segun modo, permite editarlas.
class ListaAsignaturasActivity : AppCompatActivity() {
    // Estado de datos y modo de funcionamiento de la pantalla.
    var asignaturas: List<AsignaturaDto> = emptyList()
    var seleccionada: AsignaturaDto? = null
    var modoEdicion: Boolean = false
    // Referencias a elementos de la interfaz.
    lateinit var lvAsignaturas: ListView
    lateinit var tvTitulo: TextView
    lateinit var tvId: TextView
    lateinit var etNombre: EditText
    lateinit var tvResultado: TextView
    lateinit var btnEditarAsignatura: Button
    // Inicializa la vista, determina el modo y configura eventos.
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Asocia el layout de listado/edicion de asignaturas.
        setContentView(R.layout.activity_lista_asignaturas)
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
        lvAsignaturas = findViewById(R.id.lvAsignaturas)
        tvTitulo = findViewById(R.id.tvTitulo)
        tvId = findViewById(R.id.tvId)
        etNombre = findViewById(R.id.etNombre)
        tvResultado = findViewById(R.id.tvResultado)
        btnEditarAsignatura = findViewById(R.id.btnEditarAsignatura)
        // Configura la interfaz segun si es modo edicion o solo listado.
        if (modoEdicion) {
            tvTitulo.text = "Editar asignatura (toque una para editar)"
            tvId.visibility = View.VISIBLE
            etNombre.visibility = View.VISIBLE
            btnEditarAsignatura.visibility = View.VISIBLE
            // Al seleccionar una asignatura, carga sus datos en el formulario.
            lvAsignaturas.setOnItemClickListener { _, _, posicion, _ ->
                val s = asignaturas[posicion]
                seleccionada = s
                tvId.text = "Id: " + s.idAsignatura
                etNombre.setText(s.nomAsignatura)
                tvResultado.text = "Asignatura seleccionada: " + s.idAsignatura
            }
            // Ejecuta la edicion de la asignatura seleccionada.
            btnEditarAsignatura.setOnClickListener { editar() }
        } else {
            tvTitulo.text = "Listado de asignaturas"
            tvId.visibility = View.GONE
            etNombre.visibility = View.GONE
            btnEditarAsignatura.visibility = View.GONE
            // En modo listado no se permite seleccionar para editar.
            lvAsignaturas.setOnItemClickListener(null)
        }
        // Configura la actualizacion manual de la lista.
        findViewById<Button>(R.id.btnActualizar).setOnClickListener { cargar() }
        // Carga inicial de asignaturas.
        cargar()
    }
    // Consulta y muestra las asignaturas desde la API.
    fun cargar() {
        lifecycleScope.launch {
            try {
                // Solicitud de red en hilo IO.
                val r = withContext(Dispatchers.IO) { RetrofitClient.api.listarAsignaturas() }
                if (!r.isSuccessful) {
                    tvResultado.text = "Error " + r.code()
                    return@launch
                }
                // Actualiza la lista y el adaptador con los datos recibidos.
                asignaturas = r.body() ?: emptyList()
                val textos = asignaturas.map { it.idAsignatura.toString() + " - " + it.nomAsignatura }
                lvAsignaturas.adapter = ArrayAdapter(this@ListaAsignaturasActivity, android.R.layout.simple_list_item_1, textos)
                // Muestra estado segun haya o no registros.
                if (asignaturas.isEmpty()) {
                    tvResultado.text = "Aun no hay asignaturas ingresadas"
                } else {
                    tvResultado.text = "Asignaturas: " + asignaturas.size
                }
            } catch (e: Exception) {
                // Manejo de errores de conexion.
                tvResultado.text = "No se pudo conectar con la API: " + e.message
            }
        }
    }
    // Valida y envia la edicion de la asignatura seleccionada (solo en modo edicion).
    fun editar() {
        if (!modoEdicion) {
            return
        }
        val s = seleccionada
        if (s == null) {
            tvResultado.text = "Seleccione una asignatura de la lista"
            return
        }
        val nombre = etNombre.text.toString().trim()
        // Valida campo obligatorio.
        if (nombre == "") {
            tvResultado.text = "Ingrese el nombre de la asignatura"
            return
        }
        // Envia la actualizacion a la API y refresca la lista al finalizar.
        lifecycleScope.launch {
            tvResultado.text = "Enviando..."
            val mensaje = RetrofitClient.guardar {
                RetrofitClient.api.editarAsignatura(AsignaturaDto(s.idAsignatura, nombre))
            }
            tvResultado.text = mensaje
            if (mensaje == "Guardado correctamente") {
                seleccionada = null
                tvId.text = "Id: -"
                etNombre.text.clear()
                cargar()
            }
        }
    }
}
