// Paquete principal de la aplicacion.
package com.tnt.notas
// Importaciones necesarias para manejar respuestas HTTP y anotaciones de Retrofit.
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
// Interfaz que define los endpoints de la API REST consumidos por la app.
interface ApiService {
    // Obtiene la lista completa de alumnos.
    @GET("api/alumnos")
    suspend fun listarAlumnos(): Response<List<AlumnoDto>>
    // Crea un nuevo alumno enviando sus datos en el cuerpo de la solicitud.
    @POST("api/alumnos")
    suspend fun crearAlumno(@Body alumno: AlumnoDto): Response<ResponseBody>
    // Obtiene la lista completa de asignaturas.
    @GET("api/asignaturas")
    suspend fun listarAsignaturas(): Response<List<AsignaturaDto>>
    // Crea una nueva asignatura enviando su nombre.
    @POST("api/asignaturas")
    suspend fun crearAsignatura(@Body asignatura: AsignaturaNueva): Response<ResponseBody>
    // Registra una nueva nota para un alumno y asignatura.
    @POST("api/notas")
    suspend fun crearNota(@Body nota: NotaDto): Response<ResponseBody>
    // Actualiza una nota existente.
    @PUT("api/notas")
    suspend fun editarNota(@Body nota: NotaDto): Response<ResponseBody>
    // Obtiene el listado consolidado de notas por alumno y asignatura.
    @GET("api/notas/listado")
    suspend fun listado(): Response<List<ListadoNota>>
    // Actualiza los datos de un alumno existente.
    @PUT("api/alumnos")
    suspend fun editarAlumno(@Body alumno: AlumnoDto): Response<ResponseBody>
    // Actualiza los datos de una asignatura existente.
    @PUT("api/asignaturas")
    suspend fun editarAsignatura(@Body asignatura: AsignaturaDto): Response<ResponseBody>
}
