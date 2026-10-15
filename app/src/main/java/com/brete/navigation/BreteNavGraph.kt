package com.brete.navigation

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.brete.data.NotificacionDto
import com.brete.data.RetrofitClient
import com.brete.screens.*
import com.brete.ui.theme.Teal600
import com.brete.viewmodel.LoginViewModel
import com.brete.viewmodel.PerfilViewModel
import com.brete.viewmodel.RegistroViewModel
import com.brete.viewmodel.ChatViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@Composable
fun BreteNavGraph(
    isDarkMode: Boolean = false,
    onDarkModeChanged: (Boolean) -> Unit = {}
) {
    val navController = rememberNavController()
    val perfilViewModel: PerfilViewModel = viewModel()
    val chatViewModel: ChatViewModel = viewModel()
    val context = LocalContext.current

    var mostrarDialogoSalir by remember { mutableStateOf(false) }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    if (currentRoute == "home" || currentRoute == "login") {
        BackHandler {
            mostrarDialogoSalir = true
        }
    }

    NavHost(navController = navController, startDestination = "splash") {
        composable("splash") {
            SplashScreen(onSplashFinished = {
                navController.navigate("login") {
                    popUpTo("splash") { inclusive = true }
                }
            })
        }

        composable("login") {
            val loginViewModel: LoginViewModel = viewModel()
            LoginScreen(
                onLoginExitoso = {
                    navController.navigate("home") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                onIrARegistro = {
                    navController.navigate("registro")
                },
                viewModel = loginViewModel
            )
        }

        composable("registro") {
            val registroViewModel: RegistroViewModel = viewModel()
            RegistroScreen(
                onRegistroExitoso = {
                    navController.navigate("home") {
                        popUpTo("registro") { inclusive = true }
                    }
                },
                onVolverLogin = {
                    navController.popBackStack()
                },
                viewModel = registroViewModel
            )
        }

        composable("home") {
            val idUsuarioActual by perfilViewModel.idUsuario.collectAsState()

            HomeFeedScreen(
                viewModelPerfil = perfilViewModel,
                viewModelChat = chatViewModel,
                perfilActualId = idUsuarioActual.toString(),
                onCardClick = { card ->
                    navController.currentBackStackEntry?.savedStateHandle?.set("solicitud_seleccionada", card)
                    navController.navigate("detalle")
                },
                onChatClick = {
                    navController.navigate("lista_chats")
                },
                onTabSelected = { tab ->
                    when (tab) {
                        0 -> {}
                        1 -> navController.navigate("crear")
                        2 -> navController.navigate("notificaciones")
                        3 -> navController.navigate("perfil")
                    }
                }
            )
        }

        composable("crear") {
            CrearSolicitudScreen(
                viewModelPerfil = perfilViewModel,
                onClose = { navController.popBackStack() },
                onPublicarExitoso = { navController.popBackStack() },
                onTabSelected = { tab ->
                    when (tab) {
                        0 -> navController.navigate("home") { popUpTo("home") { inclusive = true } }
                        1 -> {}
                        2 -> navController.navigate("notificaciones")
                        3 -> navController.navigate("perfil")
                    }
                }
            )
        }

        composable("editar_solicitud/{solicitudId}") { backStackEntry ->
            val solicitudIdStr = backStackEntry.arguments?.getString("solicitudId") ?: "0"
            val solicitudId = solicitudIdStr.toIntOrNull() ?: 0

            navController.currentBackStackEntry?.savedStateHandle?.set("id_solicitud_editar", solicitudId)

            CrearSolicitudScreen(
                viewModelPerfil = perfilViewModel,
                onClose = { navController.popBackStack() },
                onPublicarExitoso = { navController.popBackStack() },
                onTabSelected = { tab ->
                    when (tab) {
                        0 -> navController.navigate("home") { popUpTo("home") { inclusive = true } }
                        1 -> {}
                        2 -> navController.navigate("notificaciones")
                        3 -> navController.navigate("perfil")
                    }
                }
            )
        }

        composable("detalle") {
            val solicitud = navController.previousBackStackEntry
                ?.savedStateHandle
                ?.get<SolicitudCard>("solicitud_seleccionada")

            val idUsuarioActual by perfilViewModel.idUsuario.collectAsState()
            val coroutineScope = rememberCoroutineScope()

            if (solicitud != null) {
                DetalleSolicitudScreen(
                    solicitud = solicitud,
                    onBackClick = { navController.popBackStack() },
                    onSolicitudEnviada = { card ->
                        val chatIdGenerado = "chat_${card.id}_${idUsuarioActual}"
                        val mensajeInicialTexto = "¡Hola! Me interesa tu oferta de '${card.rol}'."

                        chatViewModel.iniciarChat(
                            chatId = chatIdGenerado,
                            perfilId = idUsuarioActual.toString(),
                            otroIdUsuario = card.id,
                            otroNombre = card.nombre,
                            mensajeInicial = mensajeInicialTexto
                        )

                        coroutineScope.launch(Dispatchers.IO) {
                            try {
                                val notificacion = NotificacionDto(
                                    usuarioId = card.id,
                                    mensaje = "¡Han aceptado tu oferta de trabajo: ${card.rol}!"
                                )
                                RetrofitClient.api.crearNotificacion(notificacion)
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }

                        // Codificamos la foto de perfil para enviarla de forma segura por la ruta
                        val fotoUrlEncoded = URLEncoder.encode(card.fotoPerfilUrl ?: "", StandardCharsets.UTF_8.toString())

                        navController.navigate("chat_detalle/$chatIdGenerado/${card.nombre}/$fotoUrlEncoded") {
                            popUpTo("home")
                        }
                    }
                )
            }
        }

        composable("notificaciones") {
            NotificacionesScreen(
                onTabSelected = { tab ->
                    when (tab) {
                        0 -> navController.navigate("home") { popUpTo("home") { inclusive = true } }
                        1 -> navController.navigate("crear")
                        2 -> {}
                        3 -> navController.navigate("perfil")
                    }
                }
            )
        }

        composable("perfil") {
            PerfilScreen(
                viewModel = perfilViewModel,
                onNavigateToEditar = { navController.navigate("editar_perfil") },
                onNavigateToAjustes = { navController.navigate("ajustes") },
                onNavigateToEditarSolicitud = { idSolicitud ->
                    navController.navigate("editar_solicitud/$idSolicitud")
                },
                onTabSelected = { tab ->
                    when (tab) {
                        0 -> navController.navigate("home") { popUpTo("home") { inclusive = true } }
                        1 -> navController.navigate("crear")
                        2 -> navController.navigate("notificaciones")
                        3 -> {}
                    }
                }
            )
        }

        composable("editar_perfil") {
            EditarPerfilScreen(
                viewModel = perfilViewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable(route = "ajustes") {
            AjustesScreen(
                isDarkMode = isDarkMode,
                onDarkModeChanged = { nuevoValor ->
                    onDarkModeChanged(nuevoValor)
                },
                onNavigateToEditarPerfil = { navController.navigate(route = "editar_perfil") },
                onCerrarSesion = {
                    navController.navigate(route = "login") {
                        popUpTo(navController.graph.startDestinationId) { inclusive = true }
                    }
                },
                onTabSelected = { _ -> },
                onBack = { navController.popBackStack() }
            )
        }

        composable("lista_chats") {
            val idUsuarioActual by perfilViewModel.idUsuario.collectAsState()

            ChatsScreen(
                perfilActualId = idUsuarioActual.toString(),
                viewModel = chatViewModel,
                onChatClick = { chatId, otroUsuarioNombre, fotoUri ->
                    val fotoUrlEncoded = URLEncoder.encode(fotoUri ?: "", StandardCharsets.UTF_8.toString())
                    navController.navigate("chat_detalle/$chatId/$otroUsuarioNombre/$fotoUrlEncoded")
                },
                onBackClick = { navController.popBackStack() }
            )
        }

        composable("chat_detalle/{chatId}/{otroUsuarioNombre}/{fotoUri}") { backStackEntry ->
            val chatId = backStackEntry.arguments?.getString("chatId") ?: ""
            val otroUsuarioNombre = backStackEntry.arguments?.getString("otroUsuarioNombre") ?: ""
            val fotoUri = backStackEntry.arguments?.getString("fotoUri") ?: ""
            val idUsuarioActual by perfilViewModel.idUsuario.collectAsState()

            ChatDetalleScreen(
                chatId = chatId,
                otroUsuarioNombre = otroUsuarioNombre,
                otroUsuarioFotoUri = fotoUri,
                perfilActualId = idUsuarioActual.toString(),
                viewModel = chatViewModel,
                onBackClick = { navController.popBackStack() }
            )
        }
    }

    if (mostrarDialogoSalir) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoSalir = false },
            title = {
                Text(
                    text = "¿Enserio quieres dejar de buscar brete?",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        mostrarDialogoSalir = false
                        (context as? Activity)?.finish()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Teal600),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Sí, sacame de una vez", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { mostrarDialogoSalir = false }
                ) {
                    Text("No, no quiero", color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Medium)
                }
            },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(20.dp)
        )
    }
}