package com.brete.viewmodel

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.brete.data.RetrofitClient
import com.brete.data.SolicitudDto
import com.brete.data.UsuarioDto
import com.brete.screens.SolicitudCard
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import android.util.Log

class PerfilViewModel(application: Application) : AndroidViewModel(application) {

    private val sharedPreferences = getApplication<Application>().applicationContext.getSharedPreferences("brete_perfil_prefs", Context.MODE_PRIVATE)

    private val _idUsuario = MutableStateFlow(0)
    val idUsuario: StateFlow<Int> = _idUsuario.asStateFlow()

    private val _nombre = MutableStateFlow("")
    val nombre: StateFlow<String> = _nombre.asStateFlow()

    private val _puesto = MutableStateFlow("")
    val puesto: StateFlow<String> = _puesto.asStateFlow()

    private val _lugarTrabajo = MutableStateFlow("")
    val lugarTrabajo: StateFlow<String> = _lugarTrabajo.asStateFlow()

    private val _telefono = MutableStateFlow("")
    val telefono: StateFlow<String> = _telefono.asStateFlow()

    private val _fotoUri = MutableStateFlow<String?>(null)
    val fotoUri: StateFlow<String?> = _fotoUri.asStateFlow()

    // --- ESTADOS PARA LA BASE DE DATOS ---
    private val _listaSolicitudesBD = MutableStateFlow<List<SolicitudDto>>(emptyList())

    // Lista general para el Feed (todas las solicitudes)
    val listaSolicitudes: StateFlow<List<SolicitudCard>> = _listaSolicitudesBD.map { listaDto ->
        listaDto.map { dto ->
            val (tipoTexto, colorBadge) = when (dto.categoria) {
                0 -> Pair("Trabajo rápido", com.brete.ui.theme.BadgeYellow)
                1 -> Pair("Profesional", com.brete.ui.theme.Teal600)
                else -> Pair("Técnico", com.brete.ui.theme.TextSecondary)
            }

            val presupuestoCrudo = dto.presupuesto?.trim() ?: "0"
            val pagoLimpio = presupuestoCrudo.replace("₡", "").replace("C$", "").replace("$", "").trim()
            val formatoFinalPago = when {
                presupuestoCrudo.contains("C$") || presupuestoCrudo.contains("$") -> presupuestoCrudo
                else -> "₡$pagoLimpio"
            }

            SolicitudCard(
                id = dto.id,
                nombre = if (!dto.nombreUsuario.isNullOrBlank()) dto.nombreUsuario else "Usuario #${dto.usuarioId}",
                rol = dto.ubicacion,
                tipo = tipoTexto,
                categoriaId = dto.categoria,
                pago = formatoFinalPago,
                esUrgente = dto.esUrgente,
                badgeColor = colorBadge,
                descripcion = dto.descripcion,
                fecha = dto.fechaCreacion ?: "Reciente",
                fotoPerfilUrl = dto.fotoUsuarioUri
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Lista exclusiva filtrada solo para las solicitudes del usuario actual logueado
    val listaMisSolicitudes: StateFlow<List<SolicitudCard>> = combine(
        _listaSolicitudesBD,
        _idUsuario
    ) { listaDto, idActual ->
        listaDto.filter { it.usuarioId == idActual }.map { dto ->
            val (tipoTexto, colorBadge) = when (dto.categoria) {
                0 -> Pair("Trabajo rápido", com.brete.ui.theme.BadgeYellow)
                1 -> Pair("Profesional", com.brete.ui.theme.Teal600)
                else -> Pair("Técnico", com.brete.ui.theme.TextSecondary)
            }

            val presupuestoCrudo = dto.presupuesto?.trim() ?: "0"
            val pagoLimpio = presupuestoCrudo.replace("₡", "").replace("C$", "").replace("$", "").trim()
            val formatoFinalPago = when {
                presupuestoCrudo.contains("C$") || presupuestoCrudo.contains("$") -> presupuestoCrudo
                else -> "₡$pagoLimpio"
            }

            SolicitudCard(
                id = dto.id,
                nombre = if (!dto.nombreUsuario.isNullOrBlank()) dto.nombreUsuario else "Usuario #${dto.usuarioId}",
                rol = dto.ubicacion,
                tipo = tipoTexto,
                categoriaId = dto.categoria,
                pago = formatoFinalPago,
                esUrgente = dto.esUrgente,
                badgeColor = colorBadge,
                descripcion = dto.descripcion,
                fecha = dto.fechaCreacion ?: "Reciente",
                fotoPerfilUrl = dto.fotoUsuarioUri
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    init {
        cargarPerfil()
        cargarSolicitudesDesdeBD()
    }

    fun cargarPerfil() {
        val idGuardado = sharedPreferences.getInt("id_usuario", 0)
        _idUsuario.value = if (idGuardado <= 0) sharedPreferences.getInt("usuario_id", 0) else idGuardado

        _nombre.value = sharedPreferences.getString("nombre", "") ?: ""
        _puesto.value = sharedPreferences.getString("puesto", "") ?: ""
        _lugarTrabajo.value = sharedPreferences.getString("lugar_trabajo", "") ?: ""
        _telefono.value = sharedPreferences.getString("telefono", "") ?: ""

        // Validamos si la foto guardada localmente sigue existiendo físicamente en el dispositivo
        val fotoUriGuardada = sharedPreferences.getString("foto_uri", null)
        val fotoValida = if (!fotoUriGuardada.isNullOrEmpty() && fotoUriGuardada.startsWith("file://")) {
            val file = File(Uri.parse(fotoUriGuardada).path ?: "")
            if (file.exists()) fotoUriGuardada else null
        } else {
            fotoUriGuardada
        }
        _fotoUri.value = fotoValida

        // Sincronización en segundo plano con la base de datos
        if (_idUsuario.value > 0) {
            viewModelScope.launch {
                try {
                    val usuarioDto = RetrofitClient.api.obtenerUsuario(_idUsuario.value)

                    _nombre.value = usuarioDto.nombre ?: ""
                    _puesto.value = usuarioDto.puesto ?: ""
                    _lugarTrabajo.value = usuarioDto.lugarTrabajo ?: ""
                    _telefono.value = usuarioDto.telefono ?: ""

                    val fotoRemota = usuarioDto.fotoUri
                    val fotoRemotaValida = if (!fotoRemota.isNullOrEmpty() && fotoRemota.startsWith("file://")) {
                        val fileRemoto = File(Uri.parse(fotoRemota).path ?: "")
                        if (fileRemoto.exists()) fotoRemota else null
                    } else {
                        fotoRemota
                    }
                    _fotoUri.value = fotoRemotaValida

                    sharedPreferences.edit().apply {
                        putInt("id_usuario", _idUsuario.value)
                        putString("nombre", usuarioDto.nombre ?: "")
                        putString("puesto", usuarioDto.puesto ?: "")
                        putString("lugar_trabajo", usuarioDto.lugarTrabajo ?: "")
                        putString("telefono", usuarioDto.telefono ?: "")
                        if (fotoRemotaValida != null) {
                            putString("foto_uri", fotoRemotaValida)
                        } else {
                            remove("foto_uri")
                        }
                        apply()
                    }
                } catch (e: Exception) {
                    Log.e("DEBUG_PERFIL", "Error al sincronizar perfil: ${e.localizedMessage}")
                }
            }
        }
    }

    fun actualizarPerfil(
        nuevoNombre: String,
        nuevoPuesto: String,
        nuevoLugar: String,
        nuevoTelefono: String,
        nuevaFotoUri: String?,
        onResultado: (Boolean, String?) -> Unit
    ) {
        val appContext = getApplication<Application>().applicationContext
        var rutaFinalFoto: String? = _fotoUri.value
        val idActual = _idUsuario.value

        if (nuevaFotoUri.isNullOrEmpty()) {
            rutaFinalFoto = null
        } else if (nuevaFotoUri != _fotoUri.value) {
            try {
                if (nuevaFotoUri.startsWith("content://")) {
                    val sourceUri = Uri.parse(nuevaFotoUri)
                    val inputStream = appContext.contentResolver.openInputStream(sourceUri)
                    val file = File(appContext.filesDir, "perfil_foto_$idActual.jpg")
                    val outputStream = FileOutputStream(file)
                    inputStream?.copyTo(outputStream)
                    inputStream?.close()
                    outputStream.close()
                    rutaFinalFoto = Uri.fromFile(file).toString()
                } else {
                    rutaFinalFoto = nuevaFotoUri
                }
            } catch (e: Exception) {
                Log.e("DEBUG_PERFIL", "Error copiando imagen: ${e.localizedMessage}")
            }
        }

        sharedPreferences.edit().apply {
            putString("nombre", nuevoNombre)
            putString("puesto", nuevoPuesto)
            putString("lugar_trabajo", nuevoLugar)
            putString("telefono", nuevoTelefono)
            if (rutaFinalFoto == null) {
                remove("foto_uri")
            } else {
                putString("foto_uri", rutaFinalFoto)
            }
            apply()
        }
        cargarPerfil()

        viewModelScope.launch {
            try {
                if (idActual <= 0) {
                    onResultado(false, "Error: ID de usuario es 0. Vuelve a iniciar sesión.")
                    return@launch
                }

                val usuarioActual = UsuarioDto(
                    id = idActual,
                    correo = "",
                    contrasena = null,
                    nombre = nuevoNombre,
                    puesto = nuevoPuesto,
                    lugarTrabajo = nuevoLugar,
                    telefono = nuevoTelefono,
                    fotoUri = rutaFinalFoto,
                    darkMode = false
                )

                RetrofitClient.api.actualizarUsuario(usuarioActual)
                onResultado(true, null)
            } catch (e: Exception) {
                e.printStackTrace()
                onResultado(false, "Fallo al conectar: ${e.localizedMessage}")
            }
        }
    }

    // --- MÉTODOS DE BASE DE DATOS PARA SOLICITUDES ---

    fun cargarSolicitudesDesdeBD() {
        viewModelScope.launch {
            try {
                val listaApi = RetrofitClient.api.obtenerSolicitudes()
                _listaSolicitudesBD.value = listaApi
            } catch (e: Exception) {
                Log.e("DEBUG_SOLICITUDES", "Error al cargar solicitudes: ${e.localizedMessage}")
            }
        }
    }

    fun guardarOActualizarSolicitud(
        idExistente: Int?,
        titulo: String,
        descripcion: String,
        categoria: Int,
        ubicacion: String,
        presupuesto: String,
        esUrgente: Boolean,
        onResultado: (Boolean, String?) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val solicitudDto = SolicitudDto(
                    id = idExistente ?: 0,
                    usuarioId = _idUsuario.value,
                    titulo = titulo,
                    descripcion = descripcion,
                    categoria = categoria,
                    ubicacion = ubicacion,
                    presupuesto = presupuesto,
                    esUrgente = esUrgente
                )

                if (idExistente == null || idExistente == 0) {
                    RetrofitClient.api.crearSolicitud(solicitudDto)
                } else {
                    RetrofitClient.api.actualizarSolicitud(idExistente, solicitudDto)
                }

                cargarSolicitudesDesdeBD()
                onResultado(true, null)
            } catch (e: Exception) {
                e.printStackTrace()
                onResultado(false, "Error: ${e.localizedMessage}")
            }
        }
    }

    fun eliminarSolicitud(id: Int, onResultado: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                RetrofitClient.api.eliminarSolicitud(id)
                cargarSolicitudesDesdeBD()
                onResultado(true)
            } catch (e: Exception) {
                e.printStackTrace()
                onResultado(false)
            }
        }
    }
}