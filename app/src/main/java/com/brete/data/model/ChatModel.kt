package com.brete.data.model

data class Mensaje(
    val id: String,
    val remitenteId: String,
    val texto: String,
    val hora: String
)

data class ChatItem(
    val id: String,
    val otroUsuarioNombre: String,
    val ultimoMensaje: String,
    val hora: String,
    val noLeidos: Int
)