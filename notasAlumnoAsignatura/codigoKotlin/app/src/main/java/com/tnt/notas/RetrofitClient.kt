package com.tnt.notas

import android.os.Build
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.security.SecureRandom
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit
import javax.net.ssl.HostnameVerifier
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

object RetrofitClient {

    // Si es Emulador usa 10.0.2.2. Si es Teléfono Físico (vía adb reverse) usa 127.0.0.1
    private val host: String = if (esEmulador()) "10.0.2.2" else "127.0.0.1"

    // El puerto 50554 de la API es HTTPS (el 50555 es HTTP). El certificado autofirmado
    // se acepta con trustAllCerts y hostnameVerifier de más abajo, así que se usa https.
    val BASE_URL = "https://$host:50554/"

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

    private fun crearOkHttpClient(): OkHttpClient {
        val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
            override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
            override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
            override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
        })

        val sslContext = SSLContext.getInstance("TLS")
        sslContext.init(null, trustAllCerts, SecureRandom())

        return OkHttpClient.Builder()
            .sslSocketFactory(sslContext.socketFactory, trustAllCerts[0] as X509TrustManager)
            .hostnameVerifier(HostnameVerifier { _, _ -> true })
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .build()
    }

    val api: ApiService = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(crearOkHttpClient())
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(ApiService::class.java)

    // Ejecuta una llamada que guarda datos (POST/PUT) en el hilo de red
    // y devuelve el mensaje que se muestra en pantalla.
    suspend fun guardar(llamada: suspend () -> Response<ResponseBody>): String {
        return withContext(Dispatchers.IO) {
            try {
                val r = llamada()
                if (r.isSuccessful) {
                    "Guardado correctamente"
                } else {
                    val error = r.errorBody()?.string() ?: ""
                    if (error == "") "Error " + r.code() else error.trim('"')
                }
            } catch (e: Exception) {
                "No se pudo conectar con la API: " + e.message
            }
        }
    }
}