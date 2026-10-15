package com.brete.viewmodel

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.brete.data.LoginDto
import com.brete.data.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LoginViewModel(application: Application) : AndroidViewModel(application) {

    private val sharedPreferences = application.getSharedPreferences("brete_perfil_prefs", Context.MODE_PRIVATE)

    private val _email = MutableStateFlow("")
    val email: StateFlow<String> = _email.asStateFlow()

    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password.asStateFlow()

    private val _estaCargando = MutableStateFlow(false)
    val estaCargando: StateFlow<Boolean> = _estaCargando.asStateFlow()

    private val _esExitoso = MutableStateFlow(false)
    val esExitoso: StateFlow<Boolean> = _esExitoso.asStateFlow()

    private val _errorMensaje = MutableStateFlow<String?>(null)
    val errorMensaje: StateFlow<String?> = _errorMensaje.asStateFlow()

    val esEmailValido: Boolean
        get() = android.util.Patterns.EMAIL_ADDRESS.matcher(_email.value).matches()

    val esPasswordValida: Boolean
        get() = _password.value.length >= 6

    val esFormularioValido: Boolean
        get() = esEmailValido && esPasswordValida

    fun onEmailChanged(nuevoEmail: String) {
        _email.value = nuevoEmail
        _errorMensaje.value = null
    }

    fun onPasswordChanged(nuevaPassword: String) {
        _password.value = nuevaPassword
        _errorMensaje.value = null
    }

    fun iniciarSesion() {
        if (!esFormularioValido) return

        viewModelScope.launch {
            _estaCargando.value = true
            _errorMensaje.value = null
            try {
                val credenciales = LoginDto(
                    correo = _email.value.trim(),
                    contrasena = _password.value
                )

                // Conexión real con el endpoint de login en SQL Server
                val usuario = RetrofitClient.api.login(credenciales)

                if (usuario != null) {
                    // LIMPIA por completo los datos y la caché de cualquier usuario anterior
                    sharedPreferences.edit().clear().apply()

                    // Guarda de forma limpia los datos utilizando tanto "id_usuario" (clave maestra del PerfilViewModel)
                    // como "usuario_id" para mantener total compatibilidad con ambos esquemas.
                    sharedPreferences.edit().apply {
                        putInt("id_usuario", usuario.id)
                        putInt("usuario_id", usuario.id)
                        putString("correo", usuario.correo)
                        putString("nombre", usuario.nombre)
                        putString("puesto", usuario.puesto ?: "Asesor de Ventas")
                        putString("lugar_trabajo", usuario.lugarTrabajo ?: "Sucursal Central")
                        putString("telefono", usuario.telefono ?: "+505 0000 0000")
                        putBoolean("dark_mode", usuario.darkMode)
                        usuario.fotoUri?.let { putString("foto_uri", it) }
                        apply()
                    }

                    _esExitoso.value = true
                    Log.d("API_TEST", "Login exitoso y sesión limpia guardada para ID: ${usuario.id} (${usuario.correo})")
                } else {
                    _errorMensaje.value = "Credenciales incorrectas"
                }
            } catch (e: Exception) {
                Log.e("API_TEST", "Error en login: ${e.localizedMessage}")
                _errorMensaje.value = "Error al conectar con el servidor"
            } finally {
                _estaCargando.value = false
            }
        }
    }
}