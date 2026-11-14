// Paquete principal de la aplicacion.
package com.tnt.notas
// Importaciones para navegacion, interfaz de usuario y corrutinas.
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
// Actividad que muestra el listado de notas obtenido desde la API.
class ListadoActivity : AppCompatActivity() {
    // Referencia al texto donde se despliega el listado.
    lateinit var tvListado: TextView
    // Inicializa la pantalla y dispara la carga inicial.
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Asocia el layout de listado.
        setContentView(R.layout.activity_listado)
        // Boton para volver al menu principal.
        findViewById<Button>(R.id.btnHome).setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            startActivity(intent)
            finish()
        }
        // Configura elementos de la interfaz y evento de actualizacion.
        tvListado = findViewById(R.id.tvListado)
        findViewById<Button>(R.id.btnActualizar).setOnClickListener { cargar() }
        cargar()
    }
    // Consulta el listado de notas y lo presenta en formato legible.
    fun cargar() {
        lifecycleScope.launch {
            tvListado.text = "Cargando..."
            try {
                // Solicitud al endpoint de listado en hilo IO.
                val respuesta = withContext(Dispatchers.IO) { RetrofitClient.api.listado() }
                // Manejo de respuesta no exitosa.
                if (!respuesta.isSuccessful) {
                    tvListado.text = "Error " + respuesta.code()
                    return@launch
                }
                // Obtiene filas del listado o informa si esta vacio.
                val filas = respuesta.body() ?: emptyList()
                if (filas.isEmpty()) {
                    tvListado.text = "Aun no hay notas ingresadas"
                    return@launch
                }
                // Construye el texto con los datos de cada registro.
                var texto = ""
                for (f in filas) {
                    texto += "Alumno: " + f.codAlumno + " - " + f.nombreCompleto + "\n"
                    texto += "Asignatura: " + f.nomAsignatura + "\n"
                    if (f.nota1 != null) texto += "Nota 1: " + f.nota1 + "\n"
                    if (f.nota2 != null) texto += "Nota 2: " + f.nota2 + "\n"
                    if (f.nota3 != null) texto += "Nota 3: " + f.nota3 + "\n"
                    if (f.promedio != null) texto += "Promedio: " + f.promedio + "\n"
                    texto += "\n----------------------\n\n"
                }
                tvListado.text = texto
            } catch (e: Exception) {
                // Manejo de errores de conexion.
                tvListado.text = "No se pudo conectar con la API: " + e.message
            }
        }
    }
}
