package com.tnt.notas

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class AlumnoActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_alumno)

        val etCodigo = findViewById<EditText>(R.id.etCodigo)
        val etNombres = findViewById<EditText>(R.id.etNombres)
        val etApellidos = findViewById<EditText>(R.id.etApellidos)
        val btnGuardar = findViewById<Button>(R.id.btnGuardar)
        val tvResultado = findViewById<TextView>(R.id.tvResultado)

        btnGuardar.setOnClickListener {
            val codigo = etCodigo.text.toString().toIntOrNull()
            val nombres = etNombres.text.toString().trim()
            val apellidos = etApellidos.text.toString().trim()

            if (codigo == null || nombres == "" || apellidos == "") {
                tvResultado.text = "Complete código, nombres y apellidos"
                return@setOnClickListener
            }

            val alumno = AlumnoDto(codigo, nombres, apellidos)

            // lifecycleScope corre en Dispatchers.Main; la red va en Dispatchers.IO
            lifecycleScope.launch {
                tvResultado.text = "Enviando..."
                val mensaje = RetrofitClient.guardar { RetrofitClient.api.crearAlumno(alumno) }
                tvResultado.text = mensaje
            }
        }
    }
}
