package com.danix.ai

import android.Manifest
import android.os.Bundle
import android.speech.tts.TextToSpeech
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale

class MainActivity : ComponentActivity(), TextToSpeech.OnInitListener {
    private lateinit var voiceManager: VoiceRecorderManager
    private lateinit var router: DanixRouter
    private var tts: TextToSpeech? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        voiceManager = VoiceRecorderManager(this)
        router = DanixRouter(this)
        tts = TextToSpeech(this, this)

        setContent {
            var statusText by remember { mutableStateOf("Ready. Tap mic to speak.") }
            var responseText by remember { mutableStateOf("DANIX AI Core Active.") }
            var isListening by remember { mutableStateOf(false) }

            val permissionLauncher = rememberLauncherForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) { granted ->
                if (granted) {
                    isListening = true
                    statusText = "Listening..."
                    voiceManager.startListening(
                        onResult = { result ->
                            isListening = false
                            statusText = "Heard: $result"
                            val answer = router.processCommand(result)
                            responseText = answer
                            speak(answer)
                        },
                        onError = { err ->
                            isListening = false
                            statusText = err
                        }
                    )
                } else {
                    statusText = "Microphone permission denied."
                }
            }

            Surface(modifier = Modifier.fillMaxSize(), color = Color(0xFF090D16)) {
                Column(
                    modifier = Modifier.fillMaxSize().padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("DANIX AI", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)

                    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(220.dp)) {
                        VoiceOrb(isListening = isListening)
                    }

                    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = statusText, color = Color(0xFF8E9BAE), fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(12.dp))
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF131B2A)),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text(text = responseText, color = Color.White, fontSize = 16.sp, modifier = Modifier.padding(16.dp))
                        }
                        Spacer(modifier = Modifier.height(20.dp))
                        Button(
                            onClick = {
                                if (voiceManager.hasMicPermission()) {
                                    isListening = true
                                    statusText = "Listening..."
                                    voiceManager.startListening(
                                        onResult = { res ->
                                            isListening = false
                                            statusText = "Heard: $res"
                                            val ans = router.processCommand(res)
                                            responseText = ans
                                            speak(ans)
                                        },
                                        onError = { err ->
                                            isListening = false
                                            statusText = err
                                        }
                                    )
                                } else {
                                    permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(54.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF)),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Text(text = if (isListening) "Listening..." else "Tap to Speak", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    private fun speak(text: String) {
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "DanixVoice")
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts?.language = Locale.US
        }
    }

    override fun onDestroy() {
        voiceManager.stop()
        tts?.stop()
        tts?.shutdown()
        super.onDestroy()
    }
}

@Composable
fun VoiceOrb(isListening: Boolean) {
    val transition = rememberInfiniteTransition(label = "pulse")
    val scale by transition.animateFloat(
        initialValue = 0.85f,
        targetValue = if (isListening) 1.25f else 0.95f,
        animationSpec = infiniteRepeatable(animation = tween(800, easing = FastOutSlowInEasing), repeatMode = RepeatMode.Reverse),
        label = "scale"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val center = Offset(size.width / 2, size.height / 2)
        val radius = (size.minDimension / 3.5f) * scale
        drawCircle(
            brush = Brush.radialGradient(colors = listOf(Color(0xFF00E5FF).copy(alpha = 0.5f), Color.Transparent), center = center, radius = radius * 1.6f),
            radius = radius * 1.6f,
            center = center
        )
        drawCircle(color = Color(0xFF00E5FF), radius = radius * 0.8f, center = center)
    }
}
