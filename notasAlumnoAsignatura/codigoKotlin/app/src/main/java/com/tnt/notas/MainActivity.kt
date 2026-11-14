// Paquete principal de la aplicacion.
package com.tnt.notas
// Importaciones para navegacion entre pantallas y componentes de Android.
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
// Actividad principal que funciona como menu de la aplicacion.
class MainActivity : AppCompatActivity() {
    // Metodo de ciclo de vida donde se inicializa la pantalla y sus acciones.
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Asocia la actividad con su layout de menu.
        setContentView(R.layout.activity_menu)
        // Navega a la pantalla de ingreso de alumno.
        findViewById<Button>(R.id.btnAlumno).setOnClickListener {
            startActivity(Intent(this, AlumnoActivity::class.java))
        }
        // Navega a la pantalla de ingreso de asignatura.
        findViewById<Button>(R.id.btnAsignatura).setOnClickListener {
            startActivity(Intent(this, AsignaturaActivity::class.java))
        }
        // Navega a la pantalla de ingreso de notas.
        findViewById<Button>(R.id.btnNota).setOnClickListener {
            startActivity(Intent(this, NotaActivity::class.java))
        }
        // Navega a la pantalla de listado de notas.
        findViewById<Button>(R.id.btnListado).setOnClickListener {
            startActivity(Intent(this, ListadoActivity::class.java))
        }
        // Abre la lista de alumnos en modo solo lectura.
        findViewById<Button>(R.id.btnListaAlumnos).setOnClickListener {
            val intent = Intent(this, ListaAlumnosActivity::class.java)
            intent.putExtra("modoEdicion", false)
            startActivity(intent)
        }
        // Abre la lista de alumnos en modo edicion.
        findViewById<Button>(R.id.btnEditarAlumno).setOnClickListener {
            val intent = Intent(this, ListaAlumnosActivity::class.java)
            intent.putExtra("modoEdicion", true)
            startActivity(intent)
        }
        // Abre la lista de asignaturas en modo solo lectura.
        findViewById<Button>(R.id.btnListaAsignaturas).setOnClickListener {
            val intent = Intent(this, ListaAsignaturasActivity::class.java)
            intent.putExtra("modoEdicion", false)
            startActivity(intent)
        }
        // Abre la lista de asignaturas en modo edicion.
        findViewById<Button>(R.id.btnEditarAsignatura).setOnClickListener {
            val intent = Intent(this, ListaAsignaturasActivity::class.java)
            intent.putExtra("modoEdicion", true)
            startActivity(intent)
        }
        // Navega a la pantalla de edicion de notas.
        findViewById<Button>(R.id.btnEditarNota).setOnClickListener {
            startActivity(Intent(this, EditarNotaActivity::class.java))
        }
        // Navega a la pantalla de resumen.
        findViewById<Button>(R.id.btnResumen).setOnClickListener {
            startActivity(Intent(this, ResumenActivity::class.java))
        }
        // Cierra completamente la aplicacion.
        findViewById<Button>(R.id.btnSalir).setOnClickListener {
            finishAffinity()
        }
    }
}
