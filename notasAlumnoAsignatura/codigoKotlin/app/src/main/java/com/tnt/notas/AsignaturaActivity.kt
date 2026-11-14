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
// Actividad para ingresar una nueva asignatura.
class AsignaturaActivity : AppCompatActivity() {
    // Inicializa la vista y configura los eventos de la pantalla.
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Carga el layout asociado a esta actividad.
        setContentView(R.layout.activity_asignatura)
        // Boton para volver al menu principal.
        findViewById<Button>(R.id.btnHome).setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            startActivity(intent)
            finish()
        }
        // Referencias a los elementos de la interfaz.
        val etNombre = findViewById<EditText>(R.id.etNombre)
        val btnGuardar = findViewById<Button>(R.id.btnGuardar)
        val tvResultado = findViewById<TextView>(R.id.tvResultado)
        // Accion del boton guardar para validar y enviar la asignatura.
        btnGuardar.setOnClickListener {
            val nombre = etNombre.text.toString().trim()
            // Valida que el nombre no este vacio.
            if (nombre == "") {
                tvResultado.text = "Ingrese el nombre de la asignatura"
                return@setOnClickListener
            }
            // Envia la solicitud de creacion a la API y muestra el resultado.
            lifecycleScope.launch {
                tvResultado.text = "Enviando..."
                val mensaje = RetrofitClient.guardar {
                    RetrofitClient.api.crearAsignatura(AsignaturaNueva(nombre))
                }
                tvResultado.text = mensaje
            }
        }
    }
}
