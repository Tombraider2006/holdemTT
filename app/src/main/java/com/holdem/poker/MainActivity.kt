package com.holdem.poker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.holdem.poker.audio.SoundManager
import com.holdem.poker.audio.rememberSoundManager
import com.holdem.poker.ui.screen.GameScreen
import com.holdem.poker.ui.screen.SettingsScreen
import com.holdem.poker.ui.theme.TexasHoldemTheme
import com.holdem.poker.ui.viewmodel.GameViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TexasHoldemTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val soundManager = rememberSoundManager()
                    var showSettings by remember { mutableStateOf(false) }
                    val viewModel = remember { GameViewModel(soundManager = soundManager) }
                    
                    if (showSettings) {
                        SettingsScreen(
                            onBack = { showSettings = false },
                            soundManager = soundManager
                        )
                    } else {
                        GameScreen(
                            viewModel = viewModel,
                            onSettingsClick = { showSettings = true }
                        )
                    }
                }
            }
        }
    }
}

