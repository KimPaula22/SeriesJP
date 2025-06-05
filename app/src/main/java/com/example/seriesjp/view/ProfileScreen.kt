package com.example.seriesjp.view

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.seriesjp.R
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth

@Composable
fun ProfileScreen(
    navController: NavController,
    userId: String,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    var showChangePasswordDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1B1B2F))
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = {
                val popped = navController.popBackStack()
                if (!popped) {
                    navController.navigate("home/$userId") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            }) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Image(
            painter = painterResource(id = R.drawable.avatar_default),
            contentDescription = "Avatar",
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Usuario ID:\n$userId",
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onLogout,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE43F5A))
        ) {
            Text("Cerrar sesión", color = Color.White)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { showChangePasswordDialog = true },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF53354A))
        ) {
            Text("Cambiar contraseña", color = Color.White)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { showDeleteConfirmDialog = true },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF990000))
        ) {
            Text("Eliminar cuenta", color = Color.White)
        }
    }

    if (showChangePasswordDialog) {
        ChangePasswordDialog(
            onDismiss = { showChangePasswordDialog = false },
            onPasswordChange = { oldPass, newPass ->
                val user = FirebaseAuth.getInstance().currentUser
                val email = user?.email
                if (user != null && email != null) {
                    val credential = EmailAuthProvider.getCredential(email, oldPass)
                    user.reauthenticate(credential)
                        .addOnSuccessListener {
                            user.updatePassword(newPass)
                                .addOnSuccessListener {
                                    Toast.makeText(context, "Contraseña actualizada", Toast.LENGTH_SHORT).show()
                                    showChangePasswordDialog = false
                                }
                                .addOnFailureListener {
                                    Toast.makeText(context, "Error al actualizar contraseña", Toast.LENGTH_SHORT).show()
                                }
                        }
                        .addOnFailureListener {
                            Toast.makeText(context, "Contraseña actual incorrecta", Toast.LENGTH_SHORT).show()
                        }
                }
            }
        )
    }

    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = { Text("¿Eliminar cuenta?") },
            text = { Text("¿Estás seguro de que quieres eliminar tu cuenta? Esta acción no se puede deshacer.") },
            confirmButton = {
                TextButton(onClick = {
                    val user = FirebaseAuth.getInstance().currentUser
                    user?.delete()?.addOnCompleteListener {
                        if (it.isSuccessful) {
                            Toast.makeText(context, "Cuenta eliminada", Toast.LENGTH_SHORT).show()
                            onLogout()
                        } else {
                            Toast.makeText(context, "Error al eliminar cuenta", Toast.LENGTH_SHORT).show()
                        }
                    }
                    showDeleteConfirmDialog = false
                }) {
                    Text("Sí", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text("No")
                }
            }
        )
    }
}

@Composable
fun ChangePasswordDialog(
    onDismiss: () -> Unit,
    onPasswordChange: (String, String) -> Unit
) {
    var oldPass by remember { mutableStateOf("") }
    var newPass by remember { mutableStateOf("") }
    var confirmPass by remember { mutableStateOf("") }
    var showOld by remember { mutableStateOf(false) }
    var showNew by remember { mutableStateOf(false) }
    var showConfirm by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Cambiar contraseña")
                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Cerrar")
                }
            }
        },
        text = {
            Column {
                PasswordField("Contraseña actual", oldPass, showOld, {
                    oldPass = it
                }) { showOld = !showOld }

                Spacer(modifier = Modifier.height(8.dp))

                PasswordField("Nueva contraseña", newPass, showNew, {
                    newPass = it
                }) { showNew = !showNew }

                Spacer(modifier = Modifier.height(8.dp))

                PasswordField("Confirmar contraseña", confirmPass, showConfirm, {
                    confirmPass = it
                }) { showConfirm = !showConfirm }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (newPass == confirmPass && newPass.length >= 6) {
                    onPasswordChange(oldPass, newPass)
                }
            }) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        },
        shape = RoundedCornerShape(12.dp),
        containerColor = Color.White
    )
}

@Composable
fun PasswordField(
    label: String,
    password: String,
    isVisible: Boolean,
    onPasswordChange: (String) -> Unit,
    onVisibilityToggle: () -> Unit
) {
    OutlinedTextField(
        value = password,
        onValueChange = onPasswordChange,
        label = { Text(label) },
        singleLine = true,
        visualTransformation = if (isVisible) VisualTransformation.None else PasswordVisualTransformation(),
        trailingIcon = {
            IconButton(onClick = onVisibilityToggle) {
                Icon(
                    imageVector = if (isVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                    contentDescription = null
                )
            }
        },
        modifier = Modifier.fillMaxWidth()
    )
}
