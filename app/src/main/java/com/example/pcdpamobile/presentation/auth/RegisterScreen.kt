package com.example.pcdpamobile.presentation.auth

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.pcdpamobile.data.remote.FirebaseAuthManager
import kotlinx.coroutines.launch

@Composable
fun RegisterScreen(
    navController: NavController,
    onNavigateBack: () -> Unit = {},          // Para volver al Login
    onRegisterSuccess: () -> Unit = {}       // Para navegar a la pantalla principal tras registrarse
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    var isPasswordVisible by remember { mutableStateOf(false) }
    var isConfirmPasswordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    // Fondo con el mismo degradado elegante del Login
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF6A11CB), // Púrpura intenso
                        Color(0xFF2575FC)  // Azul brillante
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.88f)
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(horizontal = 24.dp, vertical = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Crea una cuenta",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E293B)
                )

                Text(
                    text = "Regístrate para empezar",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
                )

                // Campo: Nombre
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre completo") },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Person, contentDescription = "Nombre")
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Campo: Correo Electrónico
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Correo electrónico") },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Email, contentDescription = "Email")
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Campo: Contraseña
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Contraseña") },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Lock, contentDescription = "Password")
                    },
                    trailingIcon = {
                        IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                            Icon(
                                imageVector = if (isPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = "Toggle password"
                            )
                        }
                    },
                    singleLine = true,
                    visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Campo: Confirmar Contraseña
                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = { Text("Confirmar contraseña") },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Lock, contentDescription = "Confirm Password")
                    },
                    trailingIcon = {
                        IconButton(onClick = { isConfirmPasswordVisible = !isConfirmPasswordVisible }) {
                            Icon(
                                imageVector = if (isConfirmPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = "Toggle confirm password"
                            )
                        }
                    },
                    singleLine = true,
                    visualTransformation = if (isConfirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Botón de Registro
                Button(
                    onClick = {
                        // Validaciones básicas antes de llamar a Firebase
                        if (name.isBlank() || email.isBlank() || password.isBlank()) {
                            Toast.makeText(context, "Por favor completa todos los campos", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        if (password != confirmPassword) {
                            Toast.makeText(context, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        if (password.length < 6) {
                            Toast.makeText(context, "La contraseña debe tener al menos 6 caracteres", Toast.LENGTH_SHORT).show()
                            return@Button
                        }

                        // Lanzar corrutina para ejecutar el registro con tu FirebaseAuthManager
                        coroutineScope.launch {
                            isLoading = true
                            val result = FirebaseAuthManager.registerUser(name.trim(), email.trim(), password)
                            isLoading = false

                            result.onSuccess {
                                Toast.makeText(context, "¡Registro exitoso!", Toast.LENGTH_SHORT).show()

                                // Opción A: Si quieres limpiar la pantalla de registro de la pila para que al dar "atrás" no vuelva a ella
                                navController.navigate("login") {
                                    popUpTo("register") { inclusive = true }
                                }

                                // O simplemente si prefieres usar el callback que ya venía preparado:
                                // onNavigateBack()
                            }.onFailure { exception ->
                                Toast.makeText(context, "Error: ${exception.localizedMessage}", Toast.LENGTH_LONG).show()
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2575FC)),
                    enabled = !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text = "Registrarse",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Volver al Login
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "¿Ya tienes una cuenta?", fontSize = 13.sp, color = Color.Gray)
                    TextButton(onClick = { onNavigateBack() }) {
                        Text(
                            text = "Inicia sesión",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF6A11CB)
                        )
                    }
                }
            }
        }
    }
}