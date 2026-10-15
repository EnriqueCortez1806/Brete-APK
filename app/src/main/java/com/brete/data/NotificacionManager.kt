package com.brete.data

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class NotificacionItem(
    val id: String,
    val titulo: String,
    val mensaje: String,
    val tiempo: String,
    val tipo: String,
    val leida: Boolean = false
)

object NotificacionManager {
    // Lista inicial de notificaciones
    private val _notificaciones = MutableStateFlow<List<NotificacionItem>>(
        listOf(
            NotificacionItem("1", "¡Nuevo Brete Publicado!", "Tu solicitud ha sido visible para la comunidad.", "Hace 10 min", "brete", leida = false),
            NotificacionItem("2", "Mensaje Recibido", "Tienes una nueva consulta sobre tu oferta de trabajo.", "Hace 1 hora", "chat", leida = false)
        )
    )
    val notificaciones: StateFlow<List<NotificacionItem>> = _notificaciones.asStateFlow()

    fun agregarNotificacion(titulo: String, mensaje: String, tipo: String = "brete") {
        val nuevaNoti = NotificacionItem(
            id = System.currentTimeMillis().toString(),
            titulo = titulo,
            mensaje = mensaje,
            tiempo = "Hace un momento",
            tipo = tipo,
            leida = false // Nace como no leída para mostrar el punto rojo
        )
        // Agrega la nueva notificación al inicio de la lista
        _notificaciones.value = listOf(nuevaNoti) + _notificaciones.value
    }

    // Función para borrar una notificación específica por su ID
    fun eliminarNotificacion(id: String) {
        _notificaciones.value = _notificaciones.value.filter { it.id != id }
    }

    // Función para marcar todas las notificaciones como leídas (oculta el punto rojo)
    fun marcarTodasComoLeidas() {
        _notificaciones.value = _notificaciones.value.map { it.copy(leida = true) }
    }
}