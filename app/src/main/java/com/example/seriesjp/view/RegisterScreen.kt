package com.example.seriesjp.view

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import com.example.seriesjp.data.SessionPreferences
import com.example.seriesjp.viewmodel.AuthState
import com.example.seriesjp.viewmodel.AuthViewModel
import com.example.seriesjp.viewmodel.AuthViewModelFactory

@Composable
fun RegisterScreen(
    navController: NavController,
    onRegisterSuccess: (String) -> Unit
) {
    val context = LocalContext.current
    val sessionPreferences = remember { SessionPreferences(context) }
    val authViewModel: AuthViewModel = viewModel(factory = AuthViewModelFactory(sessionPreferences))

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var showPasswordMismatchDialog by remember { mutableStateOf(false) }

    val state by authViewModel.authState.collectAsState()

    LaunchedEffect(state) {
        if (state is AuthState.Success) {
            onRegisterSuccess((state as AuthState.Success).user.uid)
        }
    }

    Column(
        Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text("Regístrate", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Contraseña") },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation()
        )
        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            label = { Text("Confirmar contraseña") },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation()
        )

        Spacer(Modifier.height(16.dp))

        Button(
            onClick = {
                if (password != confirmPassword) {
                    showPasswordMismatchDialog = true
                } else {
                    authViewModel.signUp(email.trim(), password)
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Crear cuenta")
        }

        Spacer(Modifier.height(8.dp))

        TextButton(onClick = { navController.navigateUp() }) {
            Text("¿Ya tienes cuenta? Inicia sesión")
        }

        when (state) {
            AuthState.Loading -> {
                Spacer(modifier = Modifier.height(16.dp))
                CircularProgressIndicator()
            }
            is AuthState.Error -> {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = (state as AuthState.Error).message,
                    color = MaterialTheme.colorScheme.error
                )
            }
            else -> Unit
        }
    }

    // Diálogo si las contraseñas no coinciden
    if (showPasswordMismatchDialog) {
        AlertDialog(
            onDismissRequest = { showPasswordMismatchDialog = false },
            title = { Text("Error") },
            text = { Text("Las contraseñas no coinciden. Deben ser iguales.") },
            confirmButton = {
                TextButton(onClick = { showPasswordMismatchDialog = false }) {
                    Text("Aceptar")
                }
            }
        )
    }
}
