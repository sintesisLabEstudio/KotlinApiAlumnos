package com.tnt.notas

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT

interface ApiService {

    @GET("api/alumnos")
    suspend fun listarAlumnos(): Response<List<AlumnoDto>>

    @POST("api/alumnos")
    suspend fun crearAlumno(@Body alumno: AlumnoDto): Response<ResponseBody>

    @GET("api/asignaturas")
    suspend fun listarAsignaturas(): Response<List<AsignaturaDto>>

    @POST("api/asignaturas")
    suspend fun crearAsignatura(@Body asignatura: AsignaturaNueva): Response<ResponseBody>

    @POST("api/notas")
    suspend fun crearNota(@Body nota: NotaDto): Response<ResponseBody>

    @PUT("api/notas")
    suspend fun editarNota(@Body nota: NotaDto): Response<ResponseBody>

    @GET("api/notas/listado")
    suspend fun listado(): Response<List<ListadoNota>>

    @PUT("api/alumnos")
    suspend fun editarAlumno(@Body alumno: AlumnoDto): Response<ResponseBody>

    @PUT("api/asignaturas")
    suspend fun editarAsignatura(@Body asignatura: AsignaturaDto): Response<ResponseBody>
}
