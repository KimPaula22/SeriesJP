package com.example.seriesjp.view

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import com.example.seriesjp.data.SessionPreferences
import com.example.seriesjp.viewmodel.AuthState
import com.example.seriesjp.viewmodel.AuthViewModel
import com.example.seriesjp.viewmodel.AuthViewModelFactory
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.ui.Alignment

@Composable
fun LoginScreen(
    navController: NavController,
    onLoginSuccess: (String) -> Unit
) {
    val context = LocalContext.current
    val sessionPreferences = remember { SessionPreferences(context) }
    val authViewModel: AuthViewModel = viewModel(
        factory = AuthViewModelFactory(sessionPreferences)
    )

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var emailError by remember { mutableStateOf(false) }
    var passwordError by remember { mutableStateOf(false) }
    var keepLoggedIn by remember { mutableStateOf(false) }

    val state by authViewModel.authState.collectAsState()

    LaunchedEffect(state) {
        if (state is AuthState.Success) {
            onLoginSuccess((state as AuthState.Success).user.uid)
        }
    }

    Column(
        Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text("Iniciar sesión", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = email,
            onValueChange = {
                email = it
                emailError = false
            },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth(),
            isError = emailError
        )
        if (emailError) {
            Text("Correo inválido", color = MaterialTheme.colorScheme.error)
        }

        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = password,
            onValueChange = {
                password = it
                passwordError = false
            },
            label = { Text("Contraseña") },
            modifier = Modifier.fillMaxWidth(),
            isError = passwordError,
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                val image = if (passwordVisible)
                    Icons.Default.Visibility
                else Icons.Default.VisibilityOff

                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(imageVector = image, contentDescription = null)
                }
            }
        )
        if (passwordError) {
            Text("La contraseña no puede estar vacía", color = MaterialTheme.colorScheme.error)
        }

        Spacer(Modifier.height(16.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 16.dp)
        ) {
            Checkbox(
                checked = keepLoggedIn,
                onCheckedChange = { keepLoggedIn = it }
            )
            Spacer(Modifier.width(8.dp))
            Text("Mantener sesión iniciada")
        }

        Button(
            onClick = {
                emailError = !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
                passwordError = password.isEmpty()

                if (!emailError && !passwordError) {
                    authViewModel.setKeepLoggedIn(keepLoggedIn)
                    authViewModel.signIn(email.trim(), password)
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Entrar")
        }

        Spacer(Modifier.height(8.dp))

        TextButton(onClick = { navController.navigate("register") }) {
            Text("¿No tienes cuenta? Regístrate")
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
}
