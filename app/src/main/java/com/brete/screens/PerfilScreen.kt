package com.brete.screens

import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.brete.components.BreteBottomNav
import com.brete.ui.theme.*
import com.brete.viewmodel.PerfilViewModel

@Composable
fun PerfilScreen(
    viewModel: PerfilViewModel,
    onNavigateToEditar: () -> Unit,
    onNavigateToAjustes: () -> Unit,
    onNavigateToEditarSolicitud: (Int) -> Unit = {}, // Mantenido por compatibilidad
    onTabSelected: (Int) -> Unit = {}
) {
    val context = LocalContext.current

    val nombre by viewModel.nombre.collectAsState()
    val puesto by viewModel.puesto.collectAsState()
    val lugarTrabajo by viewModel.lugarTrabajo.collectAsState()
    val telefono by viewModel.telefono.collectAsState()
    val fotoPerfilUri by viewModel.fotoUri.collectAsState()

    val listaMisSolicitudes by viewModel.listaMisSolicitudes.collectAsState()

    var solicitudParaEliminar by remember { mutableStateOf<SolicitudCard?>(null) }

    // --- ESTADOS PARA LA EDICIÓN RÁPIDA ---
    var solicitudParaEditar by remember { mutableStateOf<SolicitudCard?>(null) }
    var editDescripcion by remember { mutableStateOf("") }
    var editPago by remember { mutableStateOf("") }
    var editUbicacion by remember { mutableStateOf("") }

    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        viewModel.cargarPerfil()             // <-- ¡AQUÍ ESTABA EL DETALLE CLAVE! Fuerza la lectura del nuevo usuario
        viewModel.cargarSolicitudesDesdeBD()
        isVisible = true
    }

    Scaffold(
        bottomBar = { BreteBottomNav(selectedTab = 3, onTabSelected = onTabSelected) },
        containerColor = MaterialTheme.colorScheme.background
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
                    .padding(padding)
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Mi Perfil",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    IconButton(onClick = onNavigateToAjustes) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Ajustes",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(90.dp)
                                .clip(CircleShape)
                                .background(Teal600.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            if (fotoPerfilUri != null) {
                                AsyncImage(
                                    model = Uri.parse(fotoPerfilUri),
                                    contentDescription = "Foto de perfil",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Text(
                                    text = if (nombre.isNotEmpty()) nombre.take(2).uppercase() else "BR",
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Teal600
                                )
                            }
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = nombre,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = puesto,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(text = "Ubicación", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(text = lugarTrabajo, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(text = "Teléfono", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(text = telefono, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                            }
                        }

                        Button(
                            onClick = onNavigateToEditar,
                            modifier = Modifier.fillMaxWidth(0.8f),
                            colors = ButtonDefaults.buttonColors(containerColor = Teal600),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Editar Perfil", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Text(
                    text = "Mis Solicitudes Publicadas",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(listaMisSolicitudes) { solicitud ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(text = solicitud.tipo, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                    Text(text = solicitud.descripcion, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2)

                                    val pagoLimpio = solicitud.pago.replace("₡", "").trim()
                                    Text(text = "Pago: $pagoLimpio", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Teal600)
                                }

                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // --- ACCIÓN DEL BOTÓN EDITAR RÁPIDO ---
                                    IconButton(onClick = {
                                        solicitudParaEditar = solicitud
                                        editDescripcion = solicitud.descripcion
                                        editPago = solicitud.pago.replace("₡", "").replace("C$", "").replace("$", "").trim()
                                        editUbicacion = solicitud.rol // 'rol' almacena la ubicación en SolicitudCard
                                    }) {
                                        Icon(
                                            imageVector = Icons.Default.Edit,
                                            contentDescription = "Editar solicitud",
                                            tint = Teal600
                                        )
                                    }

                                    IconButton(onClick = { solicitudParaEliminar = solicitud }) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Eliminar solicitud",
                                            tint = MaterialTheme.colorScheme.error
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // --- VENTANA EMERGENTE (DIÁLOGO) PARA EDITAR LA OFERTA RÁPIDAMENTE ---
    if (solicitudParaEditar != null) {
        AlertDialog(
            onDismissRequest = { solicitudParaEditar = null },
            title = { Text("Editar Oferta", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface) },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = editDescripcion,
                        onValueChange = { editDescripcion = it },
                        label = { Text("Descripción") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3
                    )
                    OutlinedTextField(
                        value = editPago,
                        onValueChange = { editPago = it },
                        label = { Text("Pago / Dinero (ej: 5000)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editUbicacion,
                        onValueChange = { editUbicacion = it },
                        label = { Text("Lugar / Ubicación") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val currentSol = solicitudParaEditar!!
                        solicitudParaEditar = null

                        viewModel.guardarOActualizarSolicitud(
                            idExistente = currentSol.id,
                            titulo = currentSol.tipo,
                            descripcion = editDescripcion,
                            categoria = currentSol.categoriaId,
                            ubicacion = editUbicacion,
                            presupuesto = editPago,
                            esUrgente = currentSol.esUrgente
                        ) { exito, error ->
                            if (exito) {
                                Toast.makeText(context, "Oferta actualizada con éxito", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, error ?: "Error al actualizar", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Teal600),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Guardar", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { solicitudParaEditar = null }) {
                    Text("Cancelar", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(20.dp)
        )
    }

    // --- DIÁLOGO DE ELIMINACIÓN ---
    if (solicitudParaEliminar != null) {
        AlertDialog(
            onDismissRequest = { solicitudParaEliminar = null },
            title = { Text("Eliminar Solicitud", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface) },
            text = { Text("¿Deseas eliminar esta solicitud de forma permanente de la base de datos?", color = MaterialTheme.colorScheme.onSurfaceVariant) },
            confirmButton = {
                Button(
                    onClick = {
                        val idAEliminar = solicitudParaEliminar!!.id
                        solicitudParaEliminar = null

                        viewModel.eliminarSolicitud(idAEliminar) { exito ->
                            if (exito) {
                                Toast.makeText(context, "Solicitud eliminada con éxito", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "Error al eliminar en el servidor", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Eliminar", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { solicitudParaEliminar = null }) {
                    Text("Cancelar", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(20.dp)
        )
    }
}