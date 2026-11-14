package com.tnt.notas

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class AsignaturaActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_asignatura)

        val etNombre = findViewById<EditText>(R.id.etNombre)
        val btnGuardar = findViewById<Button>(R.id.btnGuardar)
        val tvResultado = findViewById<TextView>(R.id.tvResultado)

        btnGuardar.setOnClickListener {
            val nombre = etNombre.text.toString().trim()

            if (nombre == "") {
                tvResultado.text = "Ingrese el nombre de la asignatura"
                return@setOnClickListener
            }

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
