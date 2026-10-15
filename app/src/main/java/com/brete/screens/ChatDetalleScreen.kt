package com.brete.screens

import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.brete.viewmodel.ChatViewModel
import com.brete.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatDetalleScreen(
    chatId: String,
    otroUsuarioNombre: String,
    otroUsuarioFotoUri: String?, // <--- Recibe la foto de perfil del otro usuario
    perfilActualId: String,
    viewModel: ChatViewModel,
    onBackClick: () -> Unit
) {
    // 1. Extraemos el ID numérico real (Ejemplo: "chat_1_24" pasa a ser "1")
    val idChatLimpio = chatId.replace("chat_", "").split("_").firstOrNull() ?: chatId

    val listaMensajes by viewModel.mensajes.collectAsState()

    // 2. Filtramos comparando contra el ID limpio o el chatId completo
    val mensajesDelChat = listaMensajes.filter {
        it.chatId == chatId || it.chatId == idChatLimpio
    }

    var textoActual by remember { mutableStateOf("") }

    // Inicia el polling para traer mensajes de la base de datos en tiempo real
    LaunchedEffect(chatId) {
        viewModel.iniciarEscuchaMensajes(chatId)
    }

    DisposableEffect(chatId) {
        onDispose {
            viewModel.detenerEscuchaMensajes()
        }
    }

    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        isVisible = true
    }

    Scaffold(
        contentWindowInsets = WindowInsets.ime,
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Imagen de perfil real con Coil o iniciales de respaldo
                        Surface(
                            modifier = Modifier.size(38.dp),
                            shape = CircleShape,
                            color = Teal600
                        ) {
                            if (!otroUsuarioFotoUri.isNullOrEmpty()) {
                                AsyncImage(
                                    model = Uri.parse(otroUsuarioFotoUri),
                                    contentDescription = "Foto de perfil de $otroUsuarioNombre",
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = otroUsuarioNombre.take(1).uppercase(),
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Text(
                            text = otroUsuarioNombre,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Regresar",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        bottomBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 10.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .imePadding()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = textoActual,
                        onValueChange = { textoActual = it },
                        placeholder = {
                            Text(
                                "Escribe un mensaje...",
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        modifier = Modifier
                            .weight(1f)
                            .defaultMinSize(minHeight = 50.dp),
                        shape = RoundedCornerShape(24.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedContainerColor = MaterialTheme.colorScheme.background,
                            focusedContainerColor = MaterialTheme.colorScheme.background,
                            unfocusedBorderColor = Color.Transparent,
                            focusedBorderColor = Teal600
                        ),
                        maxLines = 4
                    )

                    IconButton(
                        onClick = {
                            if (textoActual.isNotBlank()) {
                                viewModel.enviarMensaje(
                                    chatId = chatId,
                                    textoMensaje = textoActual,
                                    remitenteId = perfilActualId
                                )
                                textoActual = ""
                            }
                        },
                        modifier = Modifier
                            .size(46.dp)
                            .background(Teal600, shape = RoundedCornerShape(23.dp))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = "Enviar",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        AnimatedVisibility(
            visible = isVisible,
            enter = fadeIn(animationSpec = tween(600)) + slideInVertically(
                initialOffsetY = { it / 10 },
                animationSpec = tween(600)
            )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                if (mensajesDelChat.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Inicia la conversación con $otroUsuarioNombre",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 14.sp
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(vertical = 16.dp),
                        reverseLayout = false
                    ) {
                        items(mensajesDelChat) { mensaje ->
                            BurbujaMensajeEstetica(mensaje = mensaje)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BurbujaMensajeEstetica(mensaje: com.brete.viewmodel.Mensaje) {
    val esMio = mensaje.esMio
    val alineacion = if (esMio) Alignment.End else Alignment.Start
    val colorFondo = if (esMio) Teal600 else MaterialTheme.colorScheme.surface
    val colorTexto = if (esMio) Color.White else MaterialTheme.colorScheme.onSurface

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = alineacion
    ) {
        Surface(
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (esMio) 16.dp else 4.dp,
                bottomEnd = if (esMio) 4.dp else 16.dp
            ),
            color = colorFondo,
            shadowElevation = 1.dp,
            modifier = Modifier.widthIn(max = 280.dp)
        ) {
            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                Text(
                    text = mensaje.texto,
                    fontSize = 14.sp,
                    color = colorTexto
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = mensaje.hora,
                    fontSize = 10.sp,
                    color = if (esMio) Color.White.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.align(Alignment.End)
                )
            }
        }
    }
}