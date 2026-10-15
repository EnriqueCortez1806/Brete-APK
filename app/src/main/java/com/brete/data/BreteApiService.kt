package com.brete.data

import com.google.gson.annotations.SerializedName
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.DELETE
import retrofit2.http.Body
import retrofit2.http.Path
import java.security.SecureRandom
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

// --- DTOS EXISTENTES ---

data class UsuarioDto(
    val id: Int = 0,
    val correo: String,
    val contrasena: String? = null,
    val nombre: String,
    val puesto: String?,
    val lugarTrabajo: String?,
    val telefono: String?,
    val fotoUri: String?,
    val darkMode: Boolean
)

data class LoginDto(
    val correo: String,
    val contrasena: String
)

data class SolicitudDto(
    val id: Int = 0,
    val usuarioId: Int,
    val titulo: String,
    val descripcion: String,
    val categoria: Int,
    val ubicacion: String,
    val presupuesto: String,
    val fechaCreacion: String? = null,
    val estado: String? = "Activa",
    val esUrgente: Boolean = false,
    val nombreUsuario: String? = null,
    val fotoUsuarioUri: String? = null
)

data class NotificacionDto(
    val id: Int = 0,
    val usuarioId: Int,
    val mensaje: String,
    val fechaCreacion: String? = null
)

// --- DTOS DE CHATS Y MENSAJERÍA ACTUALIZADOS ---

data class ChatDto(
    @SerializedName("id") val id: Int,
    @SerializedName("usuario1Id") val usuario1Id: Int,
    @SerializedName("usuario2Id") val usuario2Id: Int,
    @SerializedName("otroUsuarioNombre") val otroUsuarioNombre: String?,
    @SerializedName("otroUsuarioFotoUri") val otroUsuarioFotoUri: String?, // <-- Nuevo campo para la foto
    @SerializedName("ultimoMensaje") val ultimoMensaje: String?,
    @SerializedName("fechaUltimoMensaje") val fechaUltimoMensaje: String?
)

data class MensajeDto(
    @SerializedName("id") val id: Int = 0,
    @SerializedName("chatId") val chatId: Int,
    @SerializedName("remitenteId") val remitenteId: Int,
    @SerializedName("texto") val texto: String,
    @SerializedName("fecha") val fecha: String? = null
)

data class CrearChatRequest(
    @SerializedName("usuario1Id") val usuario1Id: Int,
    @SerializedName("usuario2Id") val usuario2Id: Int
)

// --- INTERFAZ API SERVICE ---

interface BreteApiService {
    @GET("api/usuarios/{id}")
    suspend fun obtenerUsuario(@Path("id") id: Int): UsuarioDto

    @PUT("api/usuarios")
    suspend fun actualizarUsuario(@Body usuario: UsuarioDto)

    @POST("api/usuarios/login")
    suspend fun login(@Body credenciales: LoginDto): UsuarioDto

    @POST("api/usuarios/register")
    suspend fun registrar(@Body usuario: UsuarioDto): Map<String, Any>

    // --- Endpoints de Solicitudes ---
    @GET("api/solicitudes")
    suspend fun obtenerSolicitudes(): List<SolicitudDto>

    @POST("api/solicitudes")
    suspend fun crearSolicitud(@Body solicitud: SolicitudDto)

    @PUT("api/solicitudes/{id}")
    suspend fun actualizarSolicitud(@Path("id") id: Int, @Body solicitud: SolicitudDto)

    @DELETE("api/solicitudes/{id}")
    suspend fun eliminarSolicitud(@Path("id") id: Int)

    // --- Endpoints de Notificaciones ---
    @GET("api/notificaciones/{usuarioId}")
    suspend fun obtenerNotificaciones(@Path("usuarioId") usuarioId: Int): List<NotificacionDto>

    @POST("api/notificaciones")
    suspend fun crearNotificacion(@Body notificacion: NotificacionDto)

    @DELETE("api/notificaciones/{id}")
    suspend fun eliminarNotificacion(@Path("id") id: Int)

    // --- Endpoints de Chats y Mensajería ---
    @GET("api/chats/usuario/{usuarioId}")
    suspend fun obtenerChatsDelUsuario(@Path("usuarioId") usuarioId: Int): List<ChatDto>

    @POST("api/chats/crear")
    suspend fun crearOObtenerChat(@Body request: CrearChatRequest): ChatDto

    @GET("api/chats/{chatId}/mensajes")
    suspend fun obtenerMensajesDelChat(@Path("chatId") chatId: Int): List<MensajeDto>

    @POST("api/chats/mensajes")
    suspend fun enviarMensaje(@Body mensaje: MensajeDto): MensajeDto

    @DELETE("api/chats/{chatId}")
    suspend fun eliminarChat(@Path("chatId") chatId: Int)
}

// --- CLIENTE RETROFIT ---

object RetrofitClient {
    private const val BASE_URL = "http://192.168.1.3:5180/"

    private val unsafeOkHttpClient: OkHttpClient.Builder = try {
        val trustAllCerts = arrayOf<TrustManager>(
            object : X509TrustManager {
                override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
                override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
                override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
            }
        )
        val sslContext = SSLContext.getInstance("SSL")
        sslContext.init(null, trustAllCerts, SecureRandom())
        val sslSocketFactory = sslContext.socketFactory

        OkHttpClient.Builder()
            .sslSocketFactory(sslSocketFactory, trustAllCerts[0] as X509TrustManager)
            .hostnameVerifier { _, _ -> true }
    } catch (e: Exception) {
        OkHttpClient.Builder()
    }

    val api: BreteApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(unsafeOkHttpClient.build())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(BreteApiService::class.java)
    }
}