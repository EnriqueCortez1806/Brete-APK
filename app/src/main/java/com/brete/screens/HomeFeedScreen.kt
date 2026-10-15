package com.brete.screens

import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.brete.components.BreteBottomNav
import com.brete.data.NotificacionManager // <--- IMPORTANTE: Importar NotificacionManager
import com.brete.ui.theme.*
import com.brete.viewmodel.ChatViewModel
import com.brete.viewmodel.PerfilViewModel

data class SolicitudCard(
    val id: Int,
    val nombre: String,
    val rol: String,
    val tipo: String,
    val categoriaId: Int,
    val pago: String,
    val esUrgente: Boolean,
    val badgeColor: Color,
    val descripcion: String,
    val fecha: String,
    val fotoPerfilUrl: String? = null
) : java.io.Serializable

@Composable
fun HomeFeedScreen(
    viewModelPerfil: PerfilViewModel,
    viewModelChat: ChatViewModel, // <--- ViewModel inyectado para gestionar la creación del chat
    perfilActualId: String,
    onCardClick: (SolicitudCard) -> Unit = {},
    onChatClick: () -> Unit = {},
    onTabSelected: (Int) -> Unit = {}
) {
    val context = LocalContext.current
    val solicitudesBD by viewModelPerfil.listaSolicitudes.collectAsState()
    val totalNoLeidos = 0

    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableIntStateOf(-1) }
    val filters = listOf("Todos", "⚡ Trabajo rápido", "💼 Profesional", "🛠️ Técnico")

    // Estado para manejar el diálogo de reporte
    var solicitudAReportar by remember { mutableStateOf<SolicitudCard?>(null) }

    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        viewModelPerfil.cargarSolicitudesDesdeBD()
        isVisible = true
    }

    val solicitudesFiltradas = solicitudesBD.filter { item ->
        val coincideCategoria = (selectedFilter == -1) || (item.categoriaId == selectedFilter)
        val coincideBusqueda = item.nombre.contains(searchQuery, ignoreCase = true) ||
                item.rol.contains(searchQuery, ignoreCase = true) ||
                item.descripcion.contains(searchQuery, ignoreCase = true) ||
                item.tipo.contains(searchQuery, ignoreCase = true)

        coincideCategoria && coincideBusqueda
    }

    Scaffold(
        bottomBar = { BreteBottomNav(selectedTab = 0, onTabSelected = onTabSelected) }
    ) { padding ->
        AnimatedVisibility(
            visible = isVisible,
            enter = fadeIn(animationSpec = tween(600)) + slideInVertically(
                initialOffsetY = { it / 10 },
                animationSpec = tween(600)
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(padding)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Oportunidades laborales",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    Box {
                        IconButton(onClick = onChatClick) {
                            Icon(
                                imageVector = Icons.Default.Chat,
                                contentDescription = "Chats",
                                tint = MaterialTheme.colorScheme.onBackground
                            )
                        }
                        if (totalNoLeidos > 0) {
                            Badge(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .offset(x = (-4).dp, y = 4.dp),
                                containerColor = Teal600
                            ) {
                                Text("$totalNoLeidos", color = Color.White, fontSize = 9.sp)
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Buscar solicitudes...", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedBorderColor = Color.Transparent,
                        focusedBorderColor = Teal600
                    ),
                    leadingIcon = { Text("🔍", fontSize = 14.sp) },
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    itemsIndexed(filters) { index, label ->
                        val filtroId = index - 1
                        FilterChip(
                            selected = selectedFilter == filtroId,
                            onClick = { selectedFilter = filtroId },
                            label = { Text(label, fontSize = 12.sp) },
                            shape = RoundedCornerShape(20.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Teal600,
                                selectedLabelColor = Color.White,
                                containerColor = MaterialTheme.colorScheme.surface,
                                labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                if (solicitudesFiltradas.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No hay solicitudes registradas en la base de datos",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 6.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(solicitudesFiltradas.size) { index ->
                            val card = solicitudesFiltradas[index]
                            SolicitudCardItem(
                                card = card,
                                onClick = { onCardClick(card) },
                                onReportClick = { solicitudAReportar = card },
                                onAcceptBreteClick = {
                                    // 1. Crea el chat en la Base de Datos
                                    viewModelChat.iniciarChat(
                                        chatId = "0",
                                        perfilId = perfilActualId,
                                        otroIdUsuario = card.categoriaId,
                                        otroNombre = card.nombre,
                                        mensajeInicial = "¡Hola! Me interesa tu oferta de '${card.rol}'."
                                    )

                                    // 2. 🔔 DISPARAR NOTIFICACIÓN AL ACEPTAR EL BRETE
                                    NotificacionManager.agregarNotificacion(
                                        titulo = "¡Brete Aceptado!",
                                        mensaje = "Has aceptado la oferta de '${card.rol}' con ${card.nombre}.",
                                        tipo = "brete"
                                    )

                                    Toast.makeText(context, "Conversación iniciada con ${card.nombre}", Toast.LENGTH_SHORT).show()
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    // Diálogo de confirmación para reportar y eliminar de la Base de Datos
    if (solicitudAReportar != null) {
        AlertDialog(
            onDismissRequest = { solicitudAReportar = null },
            title = { Text("Reportar Oferta Sospechosa", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface) },
            text = { Text("¿Deseas reportar este trabajo por considerarlo de dudosa procedencia? Se eliminará permanentemente de la base de datos.", color = MaterialTheme.colorScheme.onSurfaceVariant) },
            confirmButton = {
                Button(
                    onClick = {
                        val idAEliminar = solicitudAReportar!!.id
                        solicitudAReportar = null

                        viewModelPerfil.eliminarSolicitud(idAEliminar) { exito ->
                            if (exito) {
                                Toast.makeText(context, "Oferta reportada y eliminada de la base de datos", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "Error al eliminar de la base de datos", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Reportar y Borrar", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { solicitudAReportar = null }) {
                    Text("Cancelar", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(20.dp)
        )
    }
}

@Composable
fun SolicitudCardItem(
    card: SolicitudCard,
    onClick: () -> Unit,
    onReportClick: () -> Unit,
    onAcceptBreteClick: () -> Unit
) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f, fill = false),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Teal600.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (!card.fotoPerfilUrl.isNullOrEmpty()) {
                            AsyncImage(
                                model = Uri.parse(card.fotoPerfilUrl),
                                contentDescription = "Foto de perfil del creador",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Text(
                                text = if (card.nombre.isNotEmpty()) card.nombre.take(2).uppercase() else "BR",
                                color = Teal600,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }

                    Column(modifier = Modifier.weight(1f, fill = false)) {
                        Text(
                            text = card.nombre,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = card.rol,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.width(6.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (card.esUrgente) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = BadgeYellow
                        ) {
                            Text(
                                text = "URGENTE",
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.Black
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(99.dp),
                        color = card.badgeColor
                    ) {
                        Text(
                            text = card.tipo,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.Black
                        )
                    }

                    IconButton(
                        onClick = onReportClick,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ReportProblem,
                            contentDescription = "Reportar oferta",
                            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Text(
                text = card.descripcion,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = 18.sp,
                modifier = Modifier.fillMaxWidth()
            )

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 0.8.dp)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "📅 ${card.fecha}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = card.pago
                            .replace("₡", "")
                            .replace(":", "")
                            .replace("C$,C$", "C$")
                            .trim(),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Teal600
                    )
                }

                Button(
                    onClick = {
                        onAcceptBreteClick()
                    },
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Teal600),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "Aceptar brete",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}