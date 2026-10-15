package com.brete.viewmodel

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.brete.data.CrearChatRequest
import com.brete.data.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class ChatItem(
    val id: String,
    val perfilActualId: String,
    val otroUsuarioNombre: String,
    val otroUsuarioFotoUrl: String?,
    val ultimoMensaje: String,
    val hora: String,
    val noLeidos: Int = 0,
    val esArchivado: Boolean = false,
    val esBloqueado: Boolean = false
)

data class Mensaje(
    val id: String,
    val chatId: String = "",
    val remitenteId: String,
    val texto: String,
    val hora: String,
    val esMio: Boolean = true
)

class ChatViewModel(application: Application) : AndroidViewModel(application) {

    private val sharedPreferences = application.getSharedPreferences("brete_perfil_prefs", Context.MODE_PRIVATE)

    private val _chats = MutableStateFlow<List<ChatItem>>(emptyList())
    val chats: StateFlow<List<ChatItem>> = _chats.asStateFlow()

    // Los mensajes ahora residen únicamente en la memoria de la app (memoria RAM del ViewModel)
    private val _mensajes = MutableStateFlow<List<Mensaje>>(emptyList())
    val mensajes: StateFlow<List<Mensaje>> = _mensajes.asStateFlow()

    // Almacén temporal en memoria para guardar los mensajes de cada chat sin tocar la base de datos
    private val repositorioMensajesMemoria = mutableMapOf<String, MutableList<Mensaje>>()

    fun obtenerUsuarioActualId(): Int {
        val idGuardado = sharedPreferences.getInt("id_usuario", 0)
        return if (idGuardado <= 0) sharedPreferences.getInt("usuario_id", 0) else idGuardado
    }

    // --- CARGAR CHATS DESDE LA BASE DE DATOS (Solo la bandeja) ---
    fun cargarChats() {
        val usuarioId = obtenerUsuarioActualId()
        if (usuarioId <= 0) return

        viewModelScope.launch {
            try {
                val chatsDto = RetrofitClient.api.obtenerChatsDelUsuario(usuarioId)

                _chats.value = chatsDto.map { dto ->
                    val otroId = if (dto.usuario1Id == usuarioId) dto.usuario2Id else dto.usuario1Id
                    val chatIdStr = dto.id.toString()

                    // Buscamos si hay un último mensaje en la memoria local de la app
                    val listaChatMemoria = repositorioMensajesMemoria[chatIdStr]
                    val ultimoMsgMemoria = listaChatMemoria?.lastOrNull()

                    ChatItem(
                        id = chatIdStr,
                        perfilActualId = usuarioId.toString(),
                        otroUsuarioNombre = dto.otroUsuarioNombre ?: "Usuario #$otroId",
                        otroUsuarioFotoUrl = dto.otroUsuarioFotoUri,
                        // Si hay mensaje local se muestra, de lo contrario se queda por defecto
                        ultimoMensaje = ultimoMsgMemoria?.texto ?: (dto.ultimoMensaje ?: "Inicia una conversación"),
                        hora = ultimoMsgMemoria?.hora ?: formatearHora(dto.fechaUltimoMensaje)
                    )
                }
            } catch (e: Exception) {
                Log.e("DEBUG_CHAT", "Error al cargar chats: ${e.localizedMessage}")
            }
        }
    }

    fun obtenerChatsPorPerfil(perfilId: String): List<ChatItem> {
        return _chats.value.filter { it.perfilActualId == perfilId && !it.esBloqueado }
    }

    fun iniciarChat(chatId: String, perfilId: String, otroIdUsuario: Int, otroNombre: String, mensajeInicial: String) {
        val usuarioActualId = obtenerUsuarioActualId()
        if (usuarioActualId <= 0 || otroIdUsuario <= 0) return

        viewModelScope.launch {
            try {
                val chatCreado = RetrofitClient.api.crearOObtenerChat(
                    CrearChatRequest(usuario1Id = usuarioActualId, usuario2Id = otroIdUsuario)
                )

                val chatIdStr = chatCreado.id.toString()

                if (!mensajeInicial.isBlank()) {
                    val mensajeObj = Mensaje(
                        id = System.currentTimeMillis().toString(),
                        chatId = chatIdStr,
                        remitenteId = usuarioActualId.toString(),
                        texto = mensajeInicial,
                        hora = formatearHora(null),
                        esMio = true
                    )

                    val listaChat = repositorioMensajesMemoria.getOrPut(chatIdStr) { mutableListOf() }
                    listaChat.add(mensajeObj)
                }

                cargarChats()
            } catch (e: Exception) {
                Log.e("DEBUG_CHAT", "Error al iniciar chat: ${e.localizedMessage}")
            }
        }
    }

    // --- CARGAR MENSAJES DESDE LA MEMORIA LOCAL (Sin tocar API ni Base de Datos) ---
    fun iniciarEscuchaMensajes(chatId: String) {
        val chatIdLimio = chatId.trim()
        val listaChat = repositorioMensajesMemoria.getOrPut(chatIdLimio) { mutableListOf() }
        _mensajes.value = listaChat.toList()
    }

    fun detenerEscuchaMensajes() {
        // Ya no se requiere polling con la API de mensajes
    }

    // --- ENVIAR MENSAJE 100% LOCAL (No se guarda en SQL Server) ---
    fun enviarMensaje(chatId: String, textoMensaje: String, remitenteId: String = "") {
        if (textoMensaje.isBlank()) return

        val chatIdLimio = chatId.trim()
        val usuarioActual = obtenerUsuarioActualId()
        val remitenteIdInt = if (usuarioActual > 0) usuarioActual else (remitenteId.toIntOrNull() ?: 1)
        val miIdStr = remitenteIdInt.toString()

        val horaActual = formatearHora(null)

        val nuevoMensaje = Mensaje(
            id = System.currentTimeMillis().toString(),
            chatId = chatIdLimio,
            remitenteId = miIdStr,
            texto = textoMensaje,
            hora = horaActual,
            esMio = true
        )

        // Guardamos el mensaje en la memoria interna de la app
        val listaChat = repositorioMensajesMemoria.getOrPut(chatIdLimio) { mutableListOf() }
        listaChat.add(nuevoMensaje)

        // Actualizamos los flujos para que la pantalla del chat y la bandeja se refresquen al instante
        _mensajes.value = listaChat.toList()

        // Actualizamos la vista de la bandeja localmente para mostrar el último mensaje enviado
        _chats.value = _chats.value.map { chat ->
            if (chat.id == chatIdLimio) {
                chat.copy(ultimoMensaje = textoMensaje, hora = horaActual)
            } else {
                chat
            }
        }
    }

    fun eliminarChat(chatId: String) {
        val chatIdInt = chatId.toIntOrNull() ?: return

        viewModelScope.launch {
            try {
                // Borra el chat de la base de datos (mantiene la bandeja limpia) y limpia su memoria local
                RetrofitClient.api.eliminarChat(chatIdInt)
                repositorioMensajesMemoria.remove(chatId)
                cargarChats()
            } catch (e: Exception) {
                Log.e("DEBUG_CHAT", "Error al eliminar chat: ${e.localizedMessage}")
            }
        }
    }

    fun archivarChat(chatId: String) {
        val nuevaLista = _chats.value.map { chat ->
            if (chat.id == chatId) chat.copy(esArchivado = !chat.esArchivado) else chat
        }
        _chats.value = nuevaLista
    }

    fun bloquearChat(chatId: String) {
        val nuevaLista = _chats.value.map { chat ->
            if (chat.id == chatId) chat.copy(esBloqueado = true) else chat
        }
        _chats.value = nuevaLista
    }

    private fun formatearHora(fechaIso: String?): String {
        if (fechaIso.isNullOrEmpty()) {
            val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
            return sdf.format(Date())
        }
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            val date = inputFormat.parse(fechaIso)
            val outputFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
            if (date != null) outputFormat.format(date) else fechaIso
        } catch (e: Exception) {
            fechaIso.takeLast(8).take(5)
        }
    }

    override fun onCleared() {
        super.onCleared()
        detenerEscuchaMensajes()
    }
}