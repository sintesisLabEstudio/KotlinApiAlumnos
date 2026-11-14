// Paquete principal de la aplicacion.
package com.tnt.notas
// Importacion para mapear nombres JSON distintos a los del modelo Kotlin.
import com.google.gson.annotations.SerializedName
// Modelo de datos para representar un alumno.
data class AlumnoDto(
    val codAlumno: Int,
    val nombres: String,
    val apellidos: String
)
// Modelo usado al crear una nueva asignatura.
data class AsignaturaNueva(
    val nomAsignatura: String
)
// Modelo de datos para representar una asignatura existente.
data class AsignaturaDto(
    val idAsignatura: Int,
    val nomAsignatura: String
)
// Modelo de datos para representar una nota.
data class NotaDto(
    val codAlumno: Int,
    val idAsignatura: Int,
    val nNota: Int,
    val nota: Double
)
// Modelo para el listado consolidado de notas devuelto por la API.
data class ListadoNota(
    @SerializedName("COD_ALUMNO") val codAlumno: Int?,
    @SerializedName("NOMBRE_COMPLETO") val nombreCompleto: String?,
    @SerializedName("NOM_ASIGNATURA") val nomAsignatura: String?,
    @SerializedName("NOTA_1") val nota1: Double?,
    @SerializedName("NOTA_2") val nota2: Double?,
    @SerializedName("NOTA_3") val nota3: Double?,
    @SerializedName("PROMEDIO") val promedio: Double?
)
