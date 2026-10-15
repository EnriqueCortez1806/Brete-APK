package com.brete.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.brete.components.BreteTextField
import com.brete.data.NotificacionManager // <--- IMPORTANTE: Importar el manager
import com.brete.ui.theme.*
import com.brete.viewmodel.PerfilViewModel
import java.io.File
import java.io.FileOutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditarPerfilScreen(
    viewModel: PerfilViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current

    val nombreGuardado by viewModel.nombre.collectAsState()
    val puestoGuardado by viewModel.puesto.collectAsState()
    val lugarGuardado by viewModel.lugarTrabajo.collectAsState()
    val telefonoGuardado by viewModel.telefono.collectAsState()
    val fotoGuardadaUri by viewModel.fotoUri.collectAsState()

    var tempNombre by remember { mutableStateOf(nombreGuardado) }
    var tempPuesto by remember { mutableStateOf(puestoGuardado) }
    var tempLugar by remember { mutableStateOf(lugarGuardado) }
    var tempTelefono by remember { mutableStateOf(telefonoGuardado) }
    var tempFotoUri by remember { mutableStateOf(fotoGuardadaUri) }
    var isLoading by remember { mutableStateOf(false) }

    var showFullImageDialog by remember { mutableStateOf(false) }

    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        isVisible = true
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { sourceUri ->
            try {
                val idUsuarioActual = viewModel.idUsuario.value
                val file = File(context.filesDir, "perfil_foto_$idUsuarioActual.jpg")

                val inputStream = context.contentResolver.openInputStream(sourceUri)
                val outputStream = FileOutputStream(file)
                inputStream?.copyTo(outputStream)
                inputStream?.close()
                outputStream.close()

                tempFotoUri = Uri.fromFile(file).toString()
            } catch (e: Exception) {
                e.printStackTrace()
                tempFotoUri = sourceUri.toString()
            }
        }
    }

    val ejecutarGuardado = {
        if (!isLoading) {
            isLoading = true
            viewModel.actualizarPerfil(
                nuevoNombre = tempNombre,
                nuevoPuesto = tempPuesto,
                nuevoLugar = tempLugar,
                nuevoTelefono = tempTelefono,
                nuevaFotoUri = tempFotoUri
            ) { exito, mensaje ->
                isLoading = false
                // DISPARAR NOTIFICACIÓN AL EDITAR PERFIL (Independientemente si es éxito remoto o local)
                NotificacionManager.agregarNotificacion(
                    titulo = "Perfil Actualizado",
                    mensaje = "Has actualizado la información de tu perfil con éxito.",
                    tipo = "perfil"
                )

                if (exito) {
                    Toast.makeText(context, "Los datos se han guardado con éxito", Toast.LENGTH_SHORT).show()
                    onBack()
                } else {
                    Toast.makeText(context, mensaje ?: "Guardado localmente", Toast.LENGTH_LONG).show()
                    onBack()
                }
            }
        }
    }

    if (showFullImageDialog && !tempFotoUri.isNullOrEmpty()) {
        AlertDialog(
            onDismissRequest = { showFullImageDialog = false },
            confirmButton = {
                TextButton(onClick = { showFullImageDialog = false }) {
                    Text("Cerrar", color = Teal600, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = {
                        showFullImageDialog = false
                        imagePickerLauncher.launch("image/*")
                    }) {
                        Text("Cambiar", color = MaterialTheme.colorScheme.onSurface)
                    }
                    TextButton(onClick = {
                        tempFotoUri = null
                        showFullImageDialog = false
                        Toast.makeText(context, "Foto eliminada. Guarda los cambios para actualizar.", Toast.LENGTH_SHORT).show()
                    }) {
                        Text("Eliminar foto", color = MaterialTheme.colorScheme.error)
                    }
                }
            },
            text = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(320.dp),
                    contentAlignment = Alignment.Center
                ) {
                    AsyncImage(
                        model = Uri.parse(tempFotoUri),
                        contentDescription = "Foto de perfil ampliada",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )
                }
            },
            containerColor = MaterialTheme.colorScheme.surface
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Editar Perfil", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Regresar", tint = MaterialTheme.colorScheme.onBackground)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(Teal600.copy(alpha = 0.2f))
                        .clickable {
                            if (!tempFotoUri.isNullOrEmpty()) {
                                showFullImageDialog = true
                            } else {
                                imagePickerLauncher.launch("image/*")
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (!tempFotoUri.isNullOrEmpty()) {
                        AsyncImage(
                            model = Uri.parse(tempFotoUri),
                            contentDescription = "Foto de perfil",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Text(
                            text = if (tempNombre.isNotEmpty()) tempNombre.take(2).uppercase() else "BR",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = Teal600
                        )
                    }
                }

                Text(
                    text = "Toque la imagen para verla en grande o cambiarla",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(8.dp))

                BreteTextField(
                    value = tempNombre,
                    onValueChange = { tempNombre = it },
                    label = "Nombre Completo",
                    keyboardType = KeyboardType.Text
                )

                BreteTextField(
                    value = tempPuesto,
                    onValueChange = { tempPuesto = it },
                    label = "Puesto",
                    keyboardType = KeyboardType.Text
                )

                BreteTextField(
                    value = tempLugar,
                    onValueChange = { tempLugar = it },
                    label = "Lugar de Trabajo",
                    keyboardType = KeyboardType.Text
                )

                BreteTextField(
                    value = tempTelefono,
                    onValueChange = { tempTelefono = it },
                    label = "Teléfono",
                    keyboardType = KeyboardType.Phone
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = ejecutarGuardado,
                    enabled = !isLoading,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Teal600),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                    } else {
                        Text("Guardar Cambios", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}