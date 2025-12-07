package com.holdem.poker.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.holdem.poker.audio.SoundManager
import com.holdem.poker.audio.rememberSoundManager

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    soundManager: SoundManager = rememberSoundManager()
) {
    var soundsEnabled by remember { mutableStateOf(soundManager.soundsEnabled) }
    var cardSoundsEnabled by remember { mutableStateOf(soundManager.cardSoundsEnabled) }
    var chipSoundsEnabled by remember { mutableStateOf(soundManager.chipSoundsEnabled) }
    var winLoseSoundsEnabled by remember { mutableStateOf(soundManager.winLoseSoundsEnabled) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Заголовок
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "⚙️ Настройки",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            TextButton(onClick = onBack) {
                Text("✕", color = Color.White, fontSize = 20.sp)
            }
        }
        
        Divider(color = Color.White.copy(alpha = 0.3f))
        
        // Настройки звука
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF1E3A5F).copy(alpha = 0.95f)
            ),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "🔊 Звуки",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                
                // Общий переключатель звуков
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Включить звуки",
                        fontSize = 16.sp,
                        color = Color.White
                    )
                    Switch(
                        checked = soundsEnabled,
                        onCheckedChange = {
                            soundsEnabled = it
                            soundManager.soundsEnabled = it
                        }
                    )
                }
                
                if (soundsEnabled) {
                    Divider(color = Color.White.copy(alpha = 0.2f))
                    
                    // Звуки раздачи карт
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Звуки раздачи карт",
                                fontSize = 14.sp,
                                color = Color.White
                            )
                            Text(
                                text = "Воспроизводить звук при раздаче",
                                fontSize = 12.sp,
                                color = Color.White.copy(alpha = 0.7f)
                            )
                        }
                        Switch(
                            checked = cardSoundsEnabled,
                            onCheckedChange = {
                                cardSoundsEnabled = it
                                soundManager.cardSoundsEnabled = it
                            }
                        )
                    }
                    
                    // Звуки фишек
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Звуки фишек",
                                fontSize = 14.sp,
                                color = Color.White
                            )
                            Text(
                                text = "Воспроизводить звук при ставках",
                                fontSize = 12.sp,
                                color = Color.White.copy(alpha = 0.7f)
                            )
                        }
                        Switch(
                            checked = chipSoundsEnabled,
                            onCheckedChange = {
                                chipSoundsEnabled = it
                                soundManager.chipSoundsEnabled = it
                            }
                        )
                    }
                    
                    // Звуки победы/поражения
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Звуки победы/поражения",
                                fontSize = 14.sp,
                                color = Color.White
                            )
                            Text(
                                text = "Воспроизводить звук при выигрыше/проигрыше",
                                fontSize = 12.sp,
                                color = Color.White.copy(alpha = 0.7f)
                            )
                        }
                        Switch(
                            checked = winLoseSoundsEnabled,
                            onCheckedChange = {
                                winLoseSoundsEnabled = it
                                soundManager.winLoseSoundsEnabled = it
                            }
                        )
                    }
                }
            }
        }
        
        // Кнопка тестирования звуков
        if (soundsEnabled) {
            Button(
                onClick = {
                    soundManager.playCardDealSound()
                    android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                        soundManager.playChipSound()
                    }, 300)
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF4CAF50)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "🎵 Тест звуков",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

