package com.example.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlin.math.sin
import com.example.ui.theme.TimerBackground
import com.example.ui.theme.OnTimerBackground
import com.example.ui.theme.WaveColor1
import com.example.ui.theme.WaveColor2
import com.example.ui.theme.TimerCardBorder

@Composable
fun TimerScreen(timerViewModel: TimerViewModel) {
    val timerState by timerViewModel.timerState.collectAsStateWithLifecycle()
    val remainingSeconds by timerViewModel.remainingSeconds.collectAsStateWithLifecycle()

    val maxTime = if (timerState == TimerState.BREAK) 5 * 60 else 25 * 60
    val progress = (maxTime - remainingSeconds).toFloat() / maxTime.toFloat()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
    ) {
        // Bento Box Card
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(TimerBackground, RoundedCornerShape(24.dp))
                .border(1.dp, TimerCardBorder, RoundedCornerShape(24.dp))
                .clip(RoundedCornerShape(24.dp))
        ) {
            // Wave Canvas in background
            WaveCanvas(progress = progress, timerState = timerState)

            // Content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
            ) {
                // Top Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column {
                        Text("Dolphin Flow", style = MaterialTheme.typography.titleMedium, color = OnTimerBackground)
                        Text(
                            text = "Focus Mode: ${
                                when (timerState) {
                                    TimerState.IDLE -> "Ready"
                                    TimerState.RUNNING -> "Running"
                                    TimerState.PAUSED -> "Paused"
                                    TimerState.BREAK -> "Break"
                                    TimerState.ACTIVE_RECALL -> "Checkpoint"
                                }
                            }",
                            style = MaterialTheme.typography.labelMedium,
                            color = OnTimerBackground.copy(alpha = 0.7f)
                        )
                    }
                    Text(
                        text = formatTime(remainingSeconds),
                        style = MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.Bold),
                        color = OnTimerBackground
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                // Bottom Buttons
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (timerState == TimerState.IDLE || timerState == TimerState.PAUSED) {
                        IconButton(
                            onClick = { timerViewModel.startFocusTimer() },
                            modifier = Modifier
                                .size(56.dp)
                                .shadow(4.dp, RoundedCornerShape(28.dp))
                                .background(Color.White, RoundedCornerShape(28.dp))
                                .testTag("start_timer_btn")
                        ) {
                            Icon(Icons.Filled.PlayArrow, contentDescription = "Start", tint = OnTimerBackground)
                        }
                    } else if (timerState == TimerState.RUNNING) {
                        IconButton(
                            onClick = { timerViewModel.pauseTimer() },
                            modifier = Modifier
                                .size(56.dp)
                                .shadow(4.dp, RoundedCornerShape(28.dp))
                                .background(Color.White, RoundedCornerShape(28.dp))
                                .testTag("pause_timer_btn")
                        ) {
                            Icon(Icons.Filled.Pause, contentDescription = "Pause", tint = OnTimerBackground)
                        }
                    }
                    
                    Spacer(modifier = Modifier.width(24.dp))

                    if (timerState != TimerState.IDLE) {
                        IconButton(
                            onClick = { timerViewModel.resetTimer() },
                            modifier = Modifier
                                .size(56.dp)
                                .shadow(4.dp, RoundedCornerShape(28.dp))
                                .background(MaterialTheme.colorScheme.error, RoundedCornerShape(28.dp))
                                .testTag("stop_timer_btn")
                        ) {
                            Icon(Icons.Filled.Stop, contentDescription = "Stop", tint = Color.White)
                        }
                    }
                }
            }
        }
    }

    if (timerState == TimerState.ACTIVE_RECALL) {
        ActiveRecallDialog(
            onSave = { summary ->
                timerViewModel.finishActiveRecall(summary)
            }
        )
    }
}

@Composable
fun WaveCanvas(progress: Float, timerState: TimerState) {
    val infiniteTransition = rememberInfiniteTransition()

    // Smooth horizontal shift
    val waveOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * Math.PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )
    
    val amplitudeMultiplier by animateFloatAsState(
        targetValue = if (timerState == TimerState.RUNNING || timerState == TimerState.BREAK) 1f else 0.2f,
        label = "AmplitudeMultiplier"
    )

    Canvas(
        modifier = Modifier.fillMaxSize()
    ) {
        val width = size.width
        val height = size.height
        val waveBaseHeight = height * progress
        val amplitude = 30f * amplitudeMultiplier
        val waveLength = width / 1.5f

        // Draw background wave
        val path1 = Path()
        path1.moveTo(0f, height)
        path1.lineTo(0f, waveBaseHeight + 20f)
        var x1 = 0f
        while (x1 <= width + 10) {
            val y1 = waveBaseHeight + 20f + sin(((x1 / waveLength) * 2 * Math.PI) + waveOffset + Math.PI).toFloat() * amplitude
            path1.lineTo(x1, y1)
            x1 += 10
        }
        path1.lineTo(width, height)
        path1.close()
        drawPath(path = path1, color = WaveColor2.copy(alpha = 0.5f))

        // Draw foreground wave
        val path2 = Path()
        path2.moveTo(0f, height)
        path2.lineTo(0f, waveBaseHeight)
        var x2 = 0f
        while (x2 <= width + 10) {
            val y2 = waveBaseHeight + sin(((x2 / waveLength) * 2 * Math.PI) + waveOffset).toFloat() * amplitude
            path2.lineTo(x2, y2)
            x2 += 10
        }
        path2.lineTo(width, height)
        path2.close()
        drawPath(path = path2, color = WaveColor1.copy(alpha = 0.6f))
    }
}

@Composable
fun ActiveRecallDialog(onSave: (String) -> Unit) {
    var summary by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = { /* Force save/action */ },
        title = { 
            Text("Active Recall Checkpoint", style = MaterialTheme.typography.titleLarge)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(
                    "Close your eyes for 30 seconds, then summarize what you just learned or accomplished below.",
                    style = MaterialTheme.typography.bodyMedium
                )
                OutlinedTextField(
                    value = summary,
                    onValueChange = { summary = it },
                    label = { Text("What did you accomplish?") },
                    modifier = Modifier.fillMaxWidth().height(120.dp).testTag("recall_summary_input"),
                    maxLines = 4
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(summary) },
                enabled = summary.isNotBlank(),
                modifier = Modifier.testTag("save_session_history_btn")
            ) {
                Text("Save to Session History")
            }
        }
    )
}

fun formatTime(seconds: Int): String {
    val mins = seconds / 60
    val secs = seconds % 60
    return String.format("%02d:%02d", mins, secs)
}
