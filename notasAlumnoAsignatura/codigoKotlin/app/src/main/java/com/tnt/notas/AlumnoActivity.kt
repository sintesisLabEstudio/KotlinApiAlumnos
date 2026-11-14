// Paquete principal de la aplicacion.
package com.tnt.notas
// Importaciones para navegacion, interfaz de usuario y corrutinas.
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
// Actividad para ingresar un nuevo alumno.
class AlumnoActivity : AppCompatActivity() {
    // Inicializa la vista y configura los eventos de la pantalla.
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Carga el layout asociado a esta actividad.
        setContentView(R.layout.activity_alumno)
        // Boton para volver al menu principal.
        findViewById<Button>(R.id.btnHome).setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            startActivity(intent)
            finish()
        }
        // Referencias a los campos de entrada y elementos de resultado.
        val etCodigo = findViewById<EditText>(R.id.etCodigo)
        val etNombres = findViewById<EditText>(R.id.etNombres)
        val etApellidos = findViewById<EditText>(R.id.etApellidos)
        val btnGuardar = findViewById<Button>(R.id.btnGuardar)
        val tvResultado = findViewById<TextView>(R.id.tvResultado)
        // Accion del boton guardar para validar y enviar datos a la API.
        btnGuardar.setOnClickListener {
            val codigo = etCodigo.text.toString().toIntOrNull()
            val nombres = etNombres.text.toString().trim()
            val apellidos = etApellidos.text.toString().trim()
            // Valida que todos los campos requeridos esten completos.
            if (codigo == null || nombres == "" || apellidos == "") {
                tvResultado.text = "Complete codigo, nombres y apellidos"
                return@setOnClickListener
            }
            // Construye el objeto alumno con los datos ingresados.
            val alumno = AlumnoDto(codigo, nombres, apellidos)
            // Ejecuta la solicitud de creacion en segundo plano y muestra el resultado.
            lifecycleScope.launch {
                tvResultado.text = "Enviando..."
                val mensaje = RetrofitClient.guardar { RetrofitClient.api.crearAlumno(alumno) }
                tvResultado.text = mensaje
            }
        }
    }
}
