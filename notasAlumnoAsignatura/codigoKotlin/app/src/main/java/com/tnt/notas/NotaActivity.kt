package com.tnt.notas

import android.os.Bundle
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

class NotaActivity : AppCompatActivity() {

    var alumnos: List<AlumnoDto> = emptyList()
    var asignaturas: List<AsignaturaDto> = emptyList()

    lateinit var spAlumno: Spinner
    lateinit var spAsignatura: Spinner
    lateinit var spNumero: Spinner
    lateinit var etNota: EditText
    lateinit var tvResultado: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nota)

        spAlumno = findViewById(R.id.spAlumno)
        spAsignatura = findViewById(R.id.spAsignatura)
        spNumero = findViewById(R.id.spNumero)
        etNota = findViewById(R.id.etNota)
        tvResultado = findViewById(R.id.tvResultado)

        val numeros = ArrayAdapter(this, android.R.layout.simple_spinner_item, listOf("1", "2", "3"))
        numeros.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spNumero.adapter = numeros

        findViewById<Button>(R.id.btnGuardar).setOnClickListener { enviar(false) }
        findViewById<Button>(R.id.btnEditar).setOnClickListener { enviar(true) }
    }

    override fun onResume() {
        super.onResume()
        cargarListas()
    }

    fun cargarListas() {
        lifecycleScope.launch {
            try {
                // Red en el hilo IO
                val respAlumnos = withContext(Dispatchers.IO) { RetrofitClient.api.listarAlumnos() }
                val respAsignaturas = withContext(Dispatchers.IO) { RetrofitClient.api.listarAsignaturas() }

                // De vuelta en el hilo principal para tocar la pantalla
                if (!respAlumnos.isSuccessful || !respAsignaturas.isSuccessful) {
                    tvResultado.text = "No se pudieron cargar alumnos y asignaturas"
                    return@launch
                }

                alumnos = respAlumnos.body() ?: emptyList()
                asignaturas = respAsignaturas.body() ?: emptyList()

                val textosAlumnos = alumnos.map { it.codAlumno.toString() + " - " + it.nombres + " " + it.apellidos }
                val textosAsignaturas = asignaturas.map { it.nomAsignatura }

                val adA = ArrayAdapter(this@NotaActivity, android.R.layout.simple_spinner_item, textosAlumnos)
                adA.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spAlumno.adapter = adA

                val adS = ArrayAdapter(this@NotaActivity, android.R.layout.simple_spinner_item, textosAsignaturas)
                adS.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spAsignatura.adapter = adS

            } catch (e: Exception) {
                tvResultado.text = "No se pudo conectar con la API: " + e.message
            }
        }
    }

    fun enviar(esEdicion: Boolean) {
        if (alumnos.isEmpty() || asignaturas.isEmpty()) {
            tvResultado.text = "Primero ingrese al menos un alumno y una asignatura"
            return
        }

        val valor = etNota.text.toString().replace(",", ".").toDoubleOrNull()
        if (valor == null || valor < 1.0 || valor > 7.0) {
            tvResultado.text = "La nota debe estar entre 1.0 y 7.0"
            return
        }

        val nota = NotaDto(
            alumnos[spAlumno.selectedItemPosition].codAlumno,
            asignaturas[spAsignatura.selectedItemPosition].idAsignatura,
            spNumero.selectedItem.toString().toInt(),
            valor
        )

        lifecycleScope.launch {
            tvResultado.text = "Enviando..."
            val mensaje = RetrofitClient.guardar {
                if (esEdicion) RetrofitClient.api.editarNota(nota)
                else RetrofitClient.api.crearNota(nota)
            }
            tvResultado.text = mensaje
        }
    }
}
