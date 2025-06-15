package com.example.seriesjp.view

import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.seriesjp.R
import com.example.seriesjp.datastore.SessionPreferences
import com.example.seriesjp.viewmodel.AuthState
import com.example.seriesjp.viewmodel.AuthViewModel
import com.example.seriesjp.viewmodel.AuthViewModelFactory
import com.google.android.gms.auth.api.signin.GoogleSignIn
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    navController: NavController,
    onLoginSuccess: (String) -> Unit
) {
    val context = LocalContext.current
    val sessionPreferences = remember { SessionPreferences(context) }
    val authViewModel: AuthViewModel = viewModel(factory = AuthViewModelFactory(sessionPreferences))

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var emailError by remember { mutableStateOf(false) }
    var passwordError by remember { mutableStateOf(false) }
    var keepLoggedIn by remember { mutableStateOf(false) }
    var checkingAutoLogin by remember { mutableStateOf(true) }
    var googleSignInError by remember { mutableStateOf<String?>(null) }

    val state by authViewModel.authState.collectAsState()

    LaunchedEffect(Unit) {
        withTimeoutOrNull(6000L) {
            try {
                keepLoggedIn = sessionPreferences.keepLoggedIn.first()
            } catch (_: Exception) {
                keepLoggedIn = false
            }

            authViewModel.checkAutoLogin(
                onAutoLoginSuccess = { uid ->
                    checkingAutoLogin = false
                    onLoginSuccess(uid)
                },
                onAutoLoginFail = {
                    checkingAutoLogin = false
                }
            )
        } ?: run {
            checkingAutoLogin = false // fallback si timeout
        }
    }

    LaunchedEffect(state) {
        if (state is AuthState.Success) {
            onLoginSuccess((state as AuthState.Success).user.uid)
        }
    }

    if (checkingAutoLogin || state is AuthState.Loading) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = Color.White)
        }
    } else {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFF8AB8D1), Color(0xFF1A1A1A))
                    )
                )
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.align(Alignment.Center),
                verticalArrangement = Arrangement.Center
            ) {
                Text("Iniciar sesión", style = MaterialTheme.typography.headlineMedium, color = Color.White)
                Spacer(Modifier.height(16.dp))

                OutlinedTextField(
                    value = email,
                    onValueChange = {
                        email = it
                        emailError = false
                    },
                    label = { Text("Email") },
                    isError = emailError,
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        cursorColor = Color.White,
                        focusedBorderColor = Color.White,
                        unfocusedBorderColor = Color.LightGray,
                        focusedLabelColor = Color.White,
                        unfocusedLabelColor = Color.LightGray,
                        errorLabelColor = MaterialTheme.colorScheme.error,
                        errorBorderColor = MaterialTheme.colorScheme.error
                    )
                )
                if (emailError) Text("Correo inválido", color = MaterialTheme.colorScheme.error)

                Spacer(Modifier.height(8.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = {
                        password = it
                        passwordError = false
                    },
                    label = { Text("Contraseña") },
                    isError = passwordError,
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = null,
                                tint = Color.White
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        cursorColor = Color.White,
                        focusedBorderColor = Color.White,
                        unfocusedBorderColor = Color.LightGray,
                        focusedLabelColor = Color.White,
                        unfocusedLabelColor = Color.LightGray,
                        errorLabelColor = MaterialTheme.colorScheme.error,
                        errorBorderColor = MaterialTheme.colorScheme.error
                    )
                )
                if (passwordError) Text("La contraseña no puede estar vacía", color = MaterialTheme.colorScheme.error)

                Spacer(Modifier.height(16.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    Checkbox(
                        checked = keepLoggedIn,
                        onCheckedChange = { keepLoggedIn = it },
                        colors = CheckboxDefaults.colors(
                            checkedColor = Color.White,
                            uncheckedColor = Color.LightGray
                        )
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Mantener sesión iniciada", color = Color.White)
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

                val googleSignInClient = remember { authViewModel.getGoogleSignInClient(context) }

                val launcher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.StartActivityForResult()
                ) { result ->
                    val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                    try {
                        val account = task.getResult(com.google.android.gms.common.api.ApiException::class.java)
                        account?.idToken?.let { token ->
                            authViewModel.setKeepLoggedIn(keepLoggedIn)
                            authViewModel.signInWithGoogle(token)
                        }
                    } catch (e: Exception) {
                        googleSignInError = "Error al iniciar sesión con Google"
                    }
                }

                Button(
                    onClick = { launcher.launch(googleSignInClient.signInIntent) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_google_logo),
                        contentDescription = "Google Sign-In",
                        tint = Color.Unspecified
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Iniciar sesión con Google", color = Color.Black)
                }

                Spacer(Modifier.height(8.dp))

                TextButton(onClick = { navController.navigate("register") }) {
                    Text("¿No tienes cuenta? Regístrate", color = Color.White)
                }

                when (state) {
                    is AuthState.Error -> {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = (state as AuthState.Error).message,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                    else -> Unit
                }

                googleSignInError?.let {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = it, color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}
