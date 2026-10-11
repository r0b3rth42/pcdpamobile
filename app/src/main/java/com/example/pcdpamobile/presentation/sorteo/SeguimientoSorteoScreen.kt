package com.example.pcdpamobile.presentation.sorteo

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeguimientoSorteoScreen(navController: NavController) {
    var userNumbers by remember { mutableStateOf<List<Int>>(emptyList()) }
    var latestSorteoName by remember { mutableStateOf("Sorteo Activo") }
    val drawnBalls = remember { mutableStateListOf<Int>() }
    var isDrawing by remember { mutableStateOf(false) }
    var drawFinished by remember { mutableStateOf(false) }
    var isLoadingUserNumbers by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    val scope = rememberCoroutineScope()
    val auth = FirebaseAuth.getInstance()
    val firestore = FirebaseFirestore.getInstance()

    // Cargar la última jugada del historial del usuario desde Firestore al iniciar
    LaunchedEffect(Unit) {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            try {
                val querySnapshot = firestore.collection("jugadas")
                    .whereEqualTo("userId", userId)
                    .get()
                    .await()

                if (!querySnapshot.isEmpty) {
                    val documents = querySnapshot.documents.sortedByDescending { 
                        it.getLong("createdAt") ?: 0L 
                    }
                    val latestDoc = documents.first()
                    val numbers = latestDoc.get("numbers") as? List<Long>
                    val sorteoName = latestDoc.getString("sorteoName") ?: "Sorteo Activo"
                    if (numbers != null) {
                        userNumbers = numbers.map { it.toInt() }
                        latestSorteoName = sorteoName
                    }
                }
            } catch (e: Exception) {
                errorMessage = "Error al cargar tu jugada: ${e.localizedMessage}"
            }
        }
        isLoadingUserNumbers = false
    }

    // Listener en Tiempo Real (Real-Time) de Firebase para el Sorteo en Vivo
    DisposableEffect(Unit) {
        val docRef = firestore.collection("sorteo_activo").document("actual")
        val listener = docRef.addSnapshotListener { snapshot, e ->
            if (e != null) return@addSnapshotListener
            if (snapshot != null && snapshot.exists()) {
                val balls = (snapshot.get("drawnBalls") as? List<*>)?.mapNotNull { (it as? Long)?.toInt() } ?: emptyList()
                val drawing = snapshot.getBoolean("isDrawing") ?: false
                val finished = snapshot.getBoolean("drawFinished") ?: false

                drawnBalls.clear()
                drawnBalls.addAll(balls)
                isDrawing = drawing
                drawFinished = finished
            }
        }
        onDispose {
            listener.remove()
        }
    }

    // Función para iniciar el sorteo en tiempo real (Sincronizado globalmente en Firestore)
    fun startDraw() {
        if (userNumbers.isEmpty()) {
            errorMessage = "No tienes una jugada registrada para comparar."
            return
        }
        if (isDrawing) return

        scope.launch {
            errorMessage = null
            val docRef = firestore.collection("sorteo_activo").document("actual")

            val availablePool = (1..45).shuffled().take(6)
            val currentDrawn = mutableListOf<Int>()

            // Inicializar sorteo en Firestore para todos los usuarios conectados
            docRef.set(
                mapOf(
                    "isDrawing" to true,
                    "drawFinished" to false,
                    "drawnBalls" to emptyList<Int>(),
                    "updatedAt" to System.currentTimeMillis()
                )
            ).await()

            for (ball in availablePool) {
                delay(1500L) // 1.5 segundos por bolilla en tiempo real
                currentDrawn.add(ball)
                docRef.update(
                    mapOf(
                        "drawnBalls" to currentDrawn.toList(),
                        "isDrawing" to true,
                        "drawFinished" to false,
                        "updatedAt" to System.currentTimeMillis()
                    )
                ).await()
            }

            // Marcar como finalizado en Firestore
            docRef.update(
                mapOf(
                    "isDrawing" to false,
                    "drawFinished" to true,
                    "updatedAt" to System.currentTimeMillis()
                )
            ).await()

            // Guardar resultado del sorteo en la colección "ResultadoSorteo" en Firebase para este usuario
            val userId = auth.currentUser?.uid
            if (userId != null) {
                try {
                    val matches = currentDrawn.intersect(userNumbers.toSet())
                    val matchCount = matches.size
                    val resultStatus = when {
                        matchCount == 6 -> "GANADOR"
                        matchCount >= 4 -> "¡GRAN PREMIO!"
                        else -> "SIGUE INTENTANDO"
                    }

                    val resultadoMap = mapOf(
                        "userId" to userId,
                        "sorteoName" to latestSorteoName,
                        "userNumbers" to userNumbers,
                        "drawnBalls" to currentDrawn.toList(),
                        "matchCount" to matchCount,
                        "resultStatus" to resultStatus,
                        "createdAt" to System.currentTimeMillis()
                    )

                    firestore.collection("ResultadoSorteo")
                        .add(resultadoMap)
                        .await()
                } catch (_: Exception) {}
            }
        }
    }

    // Cálculo de aciertos
    val matches = drawnBalls.intersect(userNumbers.toSet())
    val matchCount = matches.size

    val resultStatus = when {
        !drawFinished && drawnBalls.isEmpty() -> "Sorteo listo para iniciar"
        isDrawing -> "Sorteo en curso..."
        matchCount == 6 -> "¡GANADOR!"
        matchCount >= 4 -> "¡GRAN PREMIO!"
        else -> "SIGUE INTENTANDO"
    }

    val resultColor = when {
        matchCount == 6 -> Color(0xFF10B981)
        matchCount >= 4 -> Color(0xFF2575FC)
        drawFinished -> Color(0xFFEF4444)
        else -> Color(0xFF64748B)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Seguimiento de Sorteo", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Regresar")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF1E293B),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF0F172A),
                            Color(0xFF1E293B)
                        )
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Tarjeta de Resultado Principal (GANADOR / SIGUE INTENTANDO)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF334155)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "ESTADO DEL SORTEO",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF94A3B8)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = resultStatus,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = resultColor,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Aciertos: $matchCount / 6",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Mis Números Jugados (Última Jugada del Historial)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Última Jugada Registrada",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = latestSorteoName,
                            fontSize = 13.sp,
                            color = Color(0xFF38BDF8),
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        if (isLoadingUserNumbers) {
                            CircularProgressIndicator(modifier = Modifier.size(28.dp), color = Color(0xFF38BDF8))
                        } else if (userNumbers.isEmpty()) {
                            Text(
                                text = "No tienes jugadas registradas en tu historial.",
                                fontSize = 13.sp,
                                color = Color(0xFFEF4444),
                                textAlign = TextAlign.Center
                            )
                        } else {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                userNumbers.forEach { num ->
                                    val isMatched = drawnBalls.contains(num)
                                    Box(
                                        modifier = Modifier
                                            .size(42.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (isMatched) Color(0xFF10B981) else Color(0xFF475569)
                                            )
                                            .border(
                                                width = 2.dp,
                                                color = if (isMatched) Color(0xFF34D399) else Color(0xFF64748B),
                                                shape = CircleShape
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = num.toString(),
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Bolillas Sorteadas en Orden de Salida
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Bolillas en Orden de Salida",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        if (drawnBalls.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Presiona 'Iniciar Sorteo' para comenzar el seguimiento en tiempo real.",
                                    fontSize = 14.sp,
                                    color = Color(0xFF94A3B8),
                                    textAlign = TextAlign.Center
                                )
                            }
                        } else {
                            LazyRow(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 12.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                items(drawnBalls) { ball ->
                                    val isUserMatch = userNumbers.contains(ball)
                                    AnimatedVisibility(
                                        visible = true,
                                        enter = scaleIn() + fadeIn()
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(56.dp)
                                                .clip(CircleShape)
                                                .background(
                                                    if (isUserMatch) Color(0xFF10B981) else Color(0xFF3B82F6)
                                                )
                                                .border(
                                                    width = 3.dp,
                                                    color = if (isUserMatch) Color(0xFF34D399) else Color(0xFF60A5FA),
                                                    shape = CircleShape
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = ball.toString(),
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 20.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        if (isDrawing) {
                            Spacer(modifier = Modifier.height(8.dp))
                            LinearProgressIndicator(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = Color(0xFF38BDF8),
                                trackColor = Color(0xFF334155)
                            )
                        }
                    }
                }

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = errorMessage!!,
                        color = Color(0xFFEF4444),
                        fontSize = 13.sp
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Botón Iniciar / Reiniciar Sorteo
                Button(
                    onClick = { startDraw() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF3B82F6)
                    ),
                    enabled = !isDrawing && userNumbers.isNotEmpty()
                ) {
                    Icon(
                        imageVector = if (drawFinished) Icons.Default.Refresh else Icons.Default.PlayArrow,
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = if (drawFinished) "Reiniciar Sorteo" else "Iniciar Sorteo en Vivo",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}
