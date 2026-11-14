package com.tnt.notas
import android.os.Bundle
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
class ListaAlumnosActivity : AppCompatActivity() {
    var alumnos: List<AlumnoDto> = emptyList()
    var seleccionado: AlumnoDto? = null
    lateinit var lvAlumnos: ListView
    lateinit var tvCodigo: TextView
    lateinit var etNombres: EditText
    lateinit var etApellidos: EditText
    lateinit var tvResultado: TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lista_alumnos)
        lvAlumnos = findViewById(R.id.lvAlumnos)
        tvCodigo = findViewById(R.id.tvCodigo)
        etNombres = findViewById(R.id.etNombres)
        etApellidos = findViewById(R.id.etApellidos)
        tvResultado = findViewById(R.id.tvResultado)
        // Al tocar un alumno de la lista, sus datos pasan al formulario de edicion.
        lvAlumnos.setOnItemClickListener { _, _, posicion, _ ->
            val a = alumnos[posicion]
            seleccionado = a
            tvCodigo.text = "Codigo: " + a.codAlumno
            etNombres.setText(a.nombres)
            etApellidos.setText(a.apellidos)
            tvResultado.text = "Editando alumno " + a.codAlumno
        }
        findViewById<Button>(R.id.btnGuardar).setOnClickListener { guardar() }
        findViewById<Button>(R.id.btnActualizar).setOnClickListener { cargar() }
        cargar()
    }
    fun cargar() {
        lifecycleScope.launch {
            try {
                val r = withContext(Dispatchers.IO) { RetrofitClient.api.listarAlumnos() }
                if (!r.isSuccessful) {
                    tvResultado.text = "Error " + r.code()
                    return@launch
                }
                alumnos = r.body() ?: emptyList()
                val textos = alumnos.map { it.codAlumno.toString() + " - " + it.nombres + " " + it.apellidos }
                lvAlumnos.adapter = ArrayAdapter(this@ListaAlumnosActivity, android.R.layout.simple_list_item_1, textos)
                if (alumnos.isEmpty()) tvResultado.text = "Aun no hay alumnos ingresados"
            } catch (e: Exception) {
                tvResultado.text = "No se pudo conectar con la API: " + e.message
            }
        }
    }
    fun guardar() {
        val a = seleccionado
        if (a == null) {
            tvResultado.text = "Seleccione un alumno de la lista"
            return
        }
        val nombres = etNombres.text.toString().trim()
        val apellidos = etApellidos.text.toString().trim()
        if (nombres == "" || apellidos == "") {
            tvResultado.text = "Complete nombres y apellidos"
            return
        }
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