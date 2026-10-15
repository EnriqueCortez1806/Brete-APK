package com.brete.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.brete.components.BreteBottomNav
import com.brete.components.BretePrimaryButton
import com.brete.data.NotificacionManager // <--- IMPORTANTE: Importar el manager
import com.brete.ui.theme.*
import com.brete.viewmodel.PerfilViewModel

data class CategoriaTrabajo(
    val id: Int,
    val titulo: String,
    val descripcion: String,
    val icono: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrearSolicitudScreen(
    viewModelPerfil: PerfilViewModel,
    onClose: () -> Unit = {},
    onPublicarExitoso: () -> Unit = {},
    onTabSelected: (Int) -> Unit = {}
) {
    val context = LocalContext.current

    val categorias = listOf(
        CategoriaTrabajo(0, "Trabajo rápido", "Sustituciones, tareas cortas o ayuda inmediata", "⚡"),
        CategoriaTrabajo(1, "Profesional", "Cargos administrativos, gestión o áreas técnicas avanzadas", "💼"),
        CategoriaTrabajo(2, "Técnico", "Mantenimiento, reparación o especialidades operativas", "🛠️")
    )

    var categoriaSeleccionada by remember { mutableIntStateOf(0) }
    var tituloTrabajo by remember { mutableStateOf("") }
    var ubicacion by remember { mutableStateOf("") }
    var pagoOfrecido by remember { mutableStateOf("") }

    var monedaSeleccionada by remember { mutableStateOf("C$") }
    var expandedMoneda by remember { mutableStateOf(false) }
    val monedas = listOf("C$", "$")

    var descripcionDetallada by remember { mutableStateOf("") }
    var esUrgente by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        isVisible = true
    }

    Scaffold(
        bottomBar = { BreteBottomNav(selectedTab = 1, onTabSelected = onTabSelected) },
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
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.surface) {
                        IconButton(onClick = onClose) {
                            Text("✕", fontSize = 18.sp, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                        }
                    }
                    Text(
                        text = "Crear Solicitud de Brete",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(Modifier.width(48.dp))
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Tipo de solicitud de trabajo",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        categorias.forEach { cat ->
                            val estaSeleccionada = categoriaSeleccionada == cat.id
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(
                                        width = if (estaSeleccionada) 2.dp else 1.dp,
                                        color = if (estaSeleccionada) Teal600 else MaterialTheme.colorScheme.outlineVariant,
                                        shape = RoundedCornerShape(16.dp)
                                    )
                                    .clickable { categoriaSeleccionada = cat.id },
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (estaSeleccionada) Teal600.copy(alpha = 0.05f) else MaterialTheme.colorScheme.surface
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = if (estaSeleccionada) Teal600 else MaterialTheme.colorScheme.background,
                                        modifier = Modifier.size(44.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(cat.icono, fontSize = 20.sp)
                                        }
                                    }

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = cat.titulo,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (estaSeleccionada) Teal600 else MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = cat.descripcion,
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            lineHeight = 16.sp
                                        )
                                    }

                                    RadioButton(
                                        selected = estaSeleccionada,
                                        onClick = { categoriaSeleccionada = cat.id },
                                        colors = RadioButtonDefaults.colors(selectedColor = Teal600)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Detalles de la publicación",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    OutlinedTextField(
                        value = tituloTrabajo,
                        onValueChange = { tituloTrabajo = it },
                        label = { Text("Título del puesto / solicitud", fontSize = 13.sp) },
                        placeholder = { Text("Ej: Cobertura de turno en caja", fontSize = 13.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        textStyle = TextStyle.Default.copy(fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                            focusedBorderColor = Teal600,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                        )
                    )

                    OutlinedTextField(
                        value = ubicacion,
                        onValueChange = { ubicacion = it },
                        label = { Text("Lugar / Sucursal", fontSize = 13.sp) },
                        placeholder = { Text("Ej: Managua", fontSize = 13.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        textStyle = TextStyle.Default.copy(fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                            focusedBorderColor = Teal600,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                        )
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ExposedDropdownMenuBox(
                            expanded = expandedMoneda,
                            onExpandedChange = { expandedMoneda = !expandedMoneda },
                            modifier = Modifier.width(100.dp)
                        ) {
                            OutlinedTextField(
                                value = monedaSeleccionada,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Moneda", fontSize = 13.sp) },
                                textStyle = TextStyle.Default.copy(fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface),
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedMoneda) },
                                modifier = Modifier.menuAnchor(),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                                    focusedBorderColor = Teal600,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                                )
                            )
                            ExposedDropdownMenu(
                                expanded = expandedMoneda,
                                onDismissRequest = { expandedMoneda = false }
                            ) {
                                monedas.forEach { moneda ->
                                    DropdownMenuItem(
                                        text = { Text(moneda, fontWeight = FontWeight.Bold, fontSize = 14.sp) },
                                        onClick = {
                                            monedaSeleccionada = moneda
                                            expandedMoneda = false
                                        }
                                    )
                                }
                            }
                        }

                        OutlinedTextField(
                            value = pagoOfrecido,
                            onValueChange = { pagoOfrecido = it },
                            label = { Text("Monto ofrecido", fontSize = 13.sp) },
                            placeholder = { Text("Ej: 500", fontSize = 13.sp) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true,
                            textStyle = TextStyle.Default.copy(fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = MaterialTheme.colorScheme.surface,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                                focusedBorderColor = Teal600,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                            )
                        )
                    }

                    OutlinedTextField(
                        value = descripcionDetallada,
                        onValueChange = { descripcionDetallada = it },
                        label = { Text("Descripción detallada de la oferta", fontSize = 13.sp) },
                        placeholder = { Text("Explica brevemente los requisitos...", fontSize = 13.sp) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp),
                        shape = RoundedCornerShape(12.dp),
                        textStyle = TextStyle.Default.copy(fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                            focusedBorderColor = Teal600,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                        )
                    )

                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "¿Publicación urgente?",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Destaca esta oferta en la pantalla principal",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = esUrgente,
                                onCheckedChange = { esUrgente = it },
                                colors = SwitchDefaults.colors(checkedTrackColor = Teal600)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 12.dp)
                ) {
                    BretePrimaryButton(
                        text = if (isLoading) "Publicando..." else "Publicar oferta de trabajo",
                        onClick = {
                            if (!isLoading) {
                                isLoading = true
                                val presupuestoFinal = if (pagoOfrecido.isNotBlank()) "$monedaSeleccionada $pagoOfrecido" else ""

                                viewModelPerfil.guardarOActualizarSolicitud(
                                    idExistente = null,
                                    titulo = tituloTrabajo,
                                    descripcion = descripcionDetallada,
                                    categoria = categoriaSeleccionada,
                                    ubicacion = ubicacion,
                                    presupuesto = presupuestoFinal,
                                    esUrgente = esUrgente
                                ) { exito, mensaje ->
                                    isLoading = false

                                    // DISPARAR NOTIFICACIÓN AL CREAR UNA PUBLICACIÓN NUEVA
                                    NotificacionManager.agregarNotificacion(
                                        titulo = "¡Nuevo Brete Publicado!",
                                        mensaje = "Tu solicitud '$tituloTrabajo' ya está visible para la comunidad.",
                                        tipo = "brete"
                                    )

                                    if (exito) {
                                        Toast.makeText(context, "¡Publicado con éxito!", Toast.LENGTH_SHORT).show()
                                        onPublicarExitoso()
                                    } else {
                                        Toast.makeText(context, mensaje ?: "Publicado localmente", Toast.LENGTH_SHORT).show()
                                        onPublicarExitoso()
                                    }
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}