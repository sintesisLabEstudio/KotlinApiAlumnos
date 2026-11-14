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
// Actividad que muestra el resumen solicitado de alumnos, asignaturas y notas.
class ResumenActivity : AppCompatActivity() {
    // Referencia al texto donde se despliega el resumen.
    lateinit var tvResumen: TextView
    // Inicializa la pantalla y dispara la carga inicial.
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Asocia el layout de resumen.
        setContentView(R.layout.activity_resumen)
        // Boton para volver al menu principal.
        findViewById<Button>(R.id.btnHome).setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            startActivity(intent)
            finish()
        }
        // Configura elementos de la interfaz y evento de actualizacion.
        tvResumen = findViewById(R.id.tvResumen)
        findViewById<Button>(R.id.btnActualizar).setOnClickListener { cargar() }
        cargar()
    }
    // Consulta el listado y lo formatea con los campos requeridos.
    fun cargar() {
        lifecycleScope.launch {
            tvResumen.text = "Cargando..."
            try {
                // Solicitud al endpoint de listado en hilo IO.
                val respuesta = withContext(Dispatchers.IO) { RetrofitClient.api.listado() }
                // Manejo de respuesta no exitosa.
                if (!respuesta.isSuccessful) {
                    tvResumen.text = "Error " + respuesta.code()
                    return@launch
                }
                // Obtiene filas o informa si no hay datos.
                val filas = respuesta.body() ?: emptyList()
                if (filas.isEmpty()) {
                    tvResumen.text = "Aun no hay notas ingresadas"
                    return@launch
                }
                // Construye el resumen con el formato solicitado.
                var texto = ""
                for (f in filas) {
                    texto += "COD_ALUMNO: " + (f.codAlumno?.toString() ?: "-") + "\n"
                    texto += "NOMBRE_COMPLETO: " + (f.nombreCompleto ?: "-") + "\n"
                    texto += "NOM_ASIGNATURA: " + (f.nomAsignatura ?: "-") + "\n"
                    texto += "N_NOTA_1: 1\n"
                    texto += "NOTA_1: " + (f.nota1?.toString() ?: "-") + "\n"
                    texto += "N_NOTA2: 2\n"
                    texto += "NOTA_2: " + (f.nota2?.toString() ?: "-") + "\n"
                    texto += "N_NOTA_3: 3\n"
                    texto += "NOTA_3: " + (f.nota3?.toString() ?: "-") + "\n"
                    texto += "PROMEDIO: " + (f.promedio?.toString() ?: "-") + "\n"
                    texto += "----------------------\n"
                }
                tvResumen.text = texto
            } catch (e: Exception) {
                // Manejo de errores de conexion.
                tvResumen.text = "No se pudo conectar con la API: " + e.message
            }
        }
    }
}
