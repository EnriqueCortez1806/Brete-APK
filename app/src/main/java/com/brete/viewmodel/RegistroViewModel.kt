package com.brete.viewmodel

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.brete.data.RetrofitClient
import com.brete.data.UsuarioDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class RegistroViewModel(application: Application) : AndroidViewModel(application) {

    private val sharedPreferences = application.getSharedPreferences("brete_perfil_prefs", Context.MODE_PRIVATE)

    private val _estaCargando = MutableStateFlow(false)
    val estaCargando: StateFlow<Boolean> = _estaCargando.asStateFlow()

    private val _esExitoso = MutableStateFlow(false)
    val esExitoso: StateFlow<Boolean> = _esExitoso.asStateFlow()

    private val _errorMensaje = MutableStateFlow<String?>(null)
    val errorMensaje: StateFlow<String?> = _errorMensaje.asStateFlow()

    fun registrarUsuario(
        nombre: String,
        correo: String,
        contrasena: String,
        lugarTrabajo: String,
        puesto: String,
        telefono: String,
        onRegistroExitoso: () -> Unit
    ) {
        viewModelScope.launch {
            _estaCargando.value = true
            _errorMensaje.value = null
            try {
                val nuevoUsuario = UsuarioDto(
                    id = 0,
                    correo = correo.trim(),
                    contrasena = contrasena,
                    nombre = nombre.trim(),
                    puesto = puesto.ifBlank { "Asesor de Ventas" },
                    lugarTrabajo = lugarTrabajo.trim(),
                    telefono = telefono.ifBlank { "+505 0000 0000" },
                    fotoUri = null,
                    darkMode = false
                )

                val respuesta = RetrofitClient.api.registrar(nuevoUsuario)
                // Asegúrate de que el backend devuelva el mapa con el ID o ajusta si el backend devuelve un objeto UsuarioDto directamente.
                val nuevoId = (respuesta["id"] as? Number)?.toInt() ?: 0

                if (nuevoId > 0) {
                    // LIMPIA por completo la memoria local para evitar mezclar datos con cuentas viejas
                    sharedPreferences.edit().clear().apply()

                    // Guarda de forma limpia y exclusiva los datos de esta nueva cuenta
                    sharedPreferences.edit().apply {
                        putInt("usuario_id", nuevoId)
                        putString("correo", correo.trim())
                        putString("nombre", nombre.trim())
                        putString("lugar_trabajo", lugarTrabajo.trim())
                        putString("puesto", puesto.ifBlank { "Asesor de Ventas" })
                        putString("telefono", telefono.ifBlank { "+505 0000 0000" })
                        apply()
                    }

                    _esExitoso.value = true
                    Log.d("API_TEST", "Usuario registrado exitosamente con ID: $nuevoId")
                } else {
                    _errorMensaje.value = "No se pudo obtener el ID del usuario registrado."
                }
            } catch (e: Exception) {
                Log.e("API_TEST", "Error al registrar: ${e.localizedMessage}")
                _errorMensaje.value = "No se pudo registrar. El correo podría estar en uso."
            } finally {
                _estaCargando.value = false
            }
        }
    }
}