package com.example.seriesjp.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.seriesjp.datastore.SessionPreferences
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Success(val user: FirebaseUser) : AuthState()
    data class Error(val message: String) : AuthState()
}

class AuthViewModel(
    private val sessionPreferences: SessionPreferences
) : ViewModel() {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState

    /**
     * Exponer la preferencia keepLoggedIn como StateFlow
     */
    val keepLoggedIn: StateFlow<Boolean> = sessionPreferences.keepLoggedIn
        .stateIn(viewModelScope, SharingStarted.Lazily, false)

    /**
     * Guardar la preferencia de “mantener sesión iniciada”.
     */
    fun setKeepLoggedIn(value: Boolean) {
        viewModelScope.launch {
            try {
                sessionPreferences.setKeepLoggedIn(value)
                Log.d("AuthViewModel", "Preferencia keepLoggedIn guardada: $value")
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Error guardando keepLoggedIn: ${e.localizedMessage}")
            }
        }
    }

    /**
     * Comprobar al arrancar la app si debe hacer auto-login:
     * - Si keepLoggedIn = true y FirebaseAuth.currentUser != null ⇒ onAutoLoginSuccess(uid)
     * - En otro caso, limpiar preferencia y onAutoLoginFail()
     */
    fun checkAutoLogin(
        onAutoLoginSuccess: (String) -> Unit,
        onAutoLoginFail: () -> Unit
    ) {
        viewModelScope.launch {
            try {
                val keep = sessionPreferences.keepLoggedIn.first()
                Log.d("AuthViewModel", "checkAutoLogin: keepLoggedIn = $keep")
                if (keep) {
                    val currentUser = auth.currentUser
                    Log.d("AuthViewModel", "checkAutoLogin: currentUser = $currentUser")
                    if (currentUser != null) {
                        onAutoLoginSuccess(currentUser.uid)
                    } else {
                        // No hay usuario activo: limpiar preferencia y fallback a login
                        sessionPreferences.setKeepLoggedIn(false)
                        Log.d("AuthViewModel", "No hay usuario activo, limpia preferencia y va a Login")
                        onAutoLoginFail()
                    }
                } else {
                    onAutoLoginFail()
                }
            } catch (e: Exception) {
                // En caso de error leyendo DataStore, tratar como no mantener sesión
                Log.e("AuthViewModel", "Error en checkAutoLogin: ${e.localizedMessage}")
                sessionPreferences.setKeepLoggedIn(false)
                onAutoLoginFail()
            }
        }
    }

    /**
     * Registro con email/contraseña.
     */
    fun signUp(email: String, password: String) {
        _authState.value = AuthState.Loading
        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { result ->
                result.user?.let {
                    _authState.value = AuthState.Success(it)
                    // Guardar preferencia para auto-login
                    setKeepLoggedIn(true)
                } ?: run {
                    _authState.value = AuthState.Error("Usuario creado pero no disponible")
                }
            }
            .addOnFailureListener { e ->
                _authState.value = AuthState.Error(e.localizedMessage ?: "Error desconocido")
            }
    }

    /**
     * Login con email/contraseña.
     */
    fun signIn(email: String, password: String) {
        _authState.value = AuthState.Loading
        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener { result ->
                result.user?.let {
                    _authState.value = AuthState.Success(it)
                    // Guardar preferencia para auto-login
                    setKeepLoggedIn(true)
                } ?: run {
                    _authState.value = AuthState.Error("Login exitoso pero usuario no disponible")
                }
            }
            .addOnFailureListener { e ->
                _authState.value = AuthState.Error(e.localizedMessage ?: "Error desconocido")
            }
    }

    /**
     * Cerrar sesión: FirebaseAuth.signOut() y limpiar preferencia.
     */
    fun signOut() {
        auth.signOut()
        _authState.value = AuthState.Idle
        // Limpiar preferencia para que no auto-login después de cerrar sesión
        setKeepLoggedIn(false)
        Log.d("AuthViewModel", "Usuario cerrado sesión y preferencia limpiada")
    }

    /**
     * Login con Google: recibe el idToken desde UI.
     */
    fun signInWithGoogle(idToken: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            try {
                val result = auth.signInWithCredential(credential).await()
                result.user?.let {
                    _authState.value = AuthState.Success(it)
                    // Guardar preferencia para auto-login
                    setKeepLoggedIn(true)
                } ?: run {
                    _authState.value = AuthState.Error("Login con Google exitoso pero usuario no disponible")
                }
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Error al iniciar sesión con Google: ${e.localizedMessage}")
                _authState.value = AuthState.Error("Error al iniciar sesión con Google")
            }
        }
    }

    /**
     * Obtener GoogleSignInClient. Reemplaza "TU_WEB_CLIENT_ID" por tu Web Client ID de Firebase Console.
     */
    fun getGoogleSignInClient(context: Context): GoogleSignInClient {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("647230863809-43lj5aeq0pk38gq24s0ipcmtu3v0ndlb.apps.googleusercontent.com") // <- Sustituir por tu ID real
            .requestEmail()
            .build()
        return GoogleSignIn.getClient(context, gso)
    }
}
