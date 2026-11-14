// Paquete principal de la aplicacion.
package com.tnt.notas
// Importaciones para detectar entorno, corrutinas, cliente HTTP y Retrofit.
import android.os.Build
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
// Objeto singleton que centraliza la configuracion y uso del cliente de la API.
object RetrofitClient {
    // Define el host segun si la app corre en emulador o en dispositivo fisico.
    private val host: String = if (esEmulador()) "10.0.2.2" else "127.0.0.1"
    // URL base de la API REST.
    val BASE_URL = "http://$host:50555/"
    // Detecta si el entorno actual corresponde a un emulador Android.
    private fun esEmulador(): Boolean {
        return Build.FINGERPRINT.startsWith("generic")
                || Build.FINGERPRINT.startsWith("unknown")
                || Build.MODEL.contains("google_sdk")
                || Build.MODEL.contains("Emulator")
                || Build.MODEL.contains("Android SDK built for x86")
                || Build.MANUFACTURER.contains("Genymotion")
                || (Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic"))
                || "google_sdk" == Build.PRODUCT
    }
    // Cliente OkHttp con tiempos de espera configurados.
    private val cliente: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(8, TimeUnit.SECONDS)
        .readTimeout(8, TimeUnit.SECONDS)
        .build()
    // Instancia de ApiService creada por Retrofit con conversor Gson.
    val api: ApiService = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(cliente)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(ApiService::class.java)
    // Ejecuta operaciones de guardado/edicion en hilo IO y retorna un mensaje para la UI.
    suspend fun guardar(llamada: suspend () -> Response<ResponseBody>): String {
        return withContext(Dispatchers.IO) {
            try {
                // Invoca la llamada HTTP recibida como parametro.
                val r = llamada()
                // Interpreta la respuesta exitosa o construye un mensaje de error.
                if (r.isSuccessful) {
                    "Guardado correctamente"
                } else {
                    val error = r.errorBody()?.string() ?: ""
                    if (error == "") "Error " + r.code() else error.trim('"')
                }
            } catch (e: Exception) {
                // Maneja errores de conexion u otros fallos de red.
                "No se pudo conectar con la API: " + e.message
            }
        }
    }
}
