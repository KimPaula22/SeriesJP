package com.example.seriesjp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.seriesjp.data.SessionPreferences
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

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

    // Exponemos la preferencia keepLoggedIn como StateFlow
    val keepLoggedIn: StateFlow<Boolean> = sessionPreferences.keepLoggedIn
        .stateIn(viewModelScope, SharingStarted.Lazily, false)

    // Función para actualizar la preferencia
    fun setKeepLoggedIn(value: Boolean) {
        viewModelScope.launch {
            sessionPreferences.setKeepLoggedIn(value)
        }
    }

    fun signUp(email: String, password: String) {
        _authState.value = AuthState.Loading
        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { result ->
                result.user?.let { _authState.value = AuthState.Success(it) }
            }
            .addOnFailureListener { e ->
                _authState.value = AuthState.Error(e.localizedMessage ?: "Error desconocido")
            }
    }

    fun signIn(email: String, password: String) {
        _authState.value = AuthState.Loading
        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener { result ->
                result.user?.let { _authState.value = AuthState.Success(it) }
            }
            .addOnFailureListener { e ->
                _authState.value = AuthState.Error(e.localizedMessage ?: "Error desconocido")
            }
    }

    fun signOut() {
        auth.signOut()
        _authState.value = AuthState.Idle
        // se puede limpiar preferencia si quieres que no mantenga sesión tras cerrar
        setKeepLoggedIn(false)
    }
}
