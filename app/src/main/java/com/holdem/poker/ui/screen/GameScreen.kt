package com.holdem.poker.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.holdem.poker.audio.rememberSoundManager
import com.holdem.poker.model.PlayerAction
import com.holdem.poker.ui.components.BettingHints
import com.holdem.poker.ui.components.CardView
import com.holdem.poker.ui.components.CardSize
import com.holdem.poker.ui.components.OpponentRangeView
import com.holdem.poker.ui.components.PlayerView
import com.holdem.poker.ui.theme.GreenTable
import com.holdem.poker.ui.theme.Gold
import com.holdem.poker.ui.viewmodel.GameViewModel
import kotlin.math.roundToInt

@Composable
fun GameScreen(
    viewModel: GameViewModel = viewModel(),
    onSettingsClick: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val soundManager = rememberSoundManager()
    
    val players = uiState.players
    val gameState = uiState.gameState
    val communityCards = uiState.communityCards
    val pot = uiState.pot
    val currentBet = uiState.currentBet
    val currentPlayerIndex = uiState.currentPlayerIndex
    val smallBlind = uiState.smallBlind
    val bigBlind = uiState.bigBlind
    val bettingRecommendation = uiState.bettingRecommendation
    val opponentRanges = uiState.opponentRanges
    val rangeStrengths = uiState.rangeStrengths
    
    val configuration = LocalConfiguration.current
    val isPortrait = configuration.screenHeightDp > configuration.screenWidthDp
    val scrollState = rememberScrollState()
    
    // Адаптивные размеры
    val cardSize = if (isPortrait) CardSize.SMALL else CardSize.MEDIUM
    // Увеличенный отступ для кнопок (слайдер + кнопки могут быть высокими)
    val bottomPadding = if (isPortrait) 220.dp else 280.dp
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        GreenTable,
                        Color(0xFF0F5132),
                        GreenTable
                    )
                )
            )
    ) {
        // Основной контент
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(bottom = bottomPadding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Верхняя панель с настройками
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(
                    onClick = onSettingsClick,
                    modifier = Modifier
                        .background(
                            color = Color.Black.copy(alpha = 0.3f),
                            shape = RoundedCornerShape(8.dp)
                        )
                ) {
                    Text("⚙️", fontSize = 20.sp)
                }
            }
            
            // Сообщения об ошибках
            uiState.error?.let { error ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.Red.copy(alpha = 0.9f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "⚠️ $error",
                            color = Color.White,
                            fontWeight = FontWeight.Medium
                        )
                        TextButton(onClick = { viewModel.clearError() }) {
                            Text("✕", color = Color.White)
                        }
                    }
                }
            }
            
            // Сообщения
            uiState.message?.let { message ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Gold.copy(alpha = 0.9f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "ℹ️ $message",
                            color = Color.Black,
                            fontWeight = FontWeight.Medium
                        )
                        TextButton(onClick = { viewModel.clearMessage() }) {
                            Text("✕", color = Color.Black)
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Верхние игроки (AI)
            if (players.size > 2) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    if (players.size > 2) {
                        PlayerView(
                            player = players[1],
                            isCurrentPlayer = currentPlayerIndex == 1,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    if (players.size > 3) {
                        PlayerView(
                            player = players[2],
                            isCurrentPlayer = currentPlayerIndex == 2,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }
            
            // Общие карты на столе
            Column(
                modifier = Modifier.padding(vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Общие карты",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    communityCards.forEach { card ->
                        CardView(
                            card = card,
                            isFaceUp = true,
                            size = cardSize
                        )
                    }
                    repeat(5 - communityCards.size) {
                        CardView(
                            card = null,
                            isFaceUp = false,
                            size = cardSize
                        )
                    }
                }
            }
            
            // Pot и текущая ставка
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.Black.copy(alpha = 0.4f)
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("💰", fontSize = 24.sp)
                        Text(
                            text = "Pot: $pot",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Gold
                        )
                    }
                    
                    if (currentBet > 0) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text("📊", fontSize = 16.sp)
                            Text(
                                text = "Текущая ставка: $currentBet",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                        }
                    }
                    
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier
                            .background(
                                color = Color.White.copy(alpha = 0.1f),
                                shape = RoundedCornerShape(8.dp)
                            )
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "SB: $smallBlind",
                            fontSize = 14.sp,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                        Text("•", color = Color.White.copy(alpha = 0.5f))
                        Text(
                            text = "BB: $bigBlind",
                            fontSize = 14.sp,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Нижние игроки
            if (players.isNotEmpty()) {
                PlayerView(
                    player = players[0],
                    isCurrentPlayer = currentPlayerIndex == 0,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            
            if (players.size > 3) {
                Spacer(modifier = Modifier.height(8.dp))
                PlayerView(
                    player = players[3],
                    isCurrentPlayer = currentPlayerIndex == 3,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            
            // Подсказки по ставкам - показываем СРАЗУ ПОСЛЕ POT, чтобы были заметны
            val isPlayerTurn = currentPlayerIndex == 0 && players.isNotEmpty() && !players[0].isFolded
            if (isPlayerTurn) {
                Spacer(modifier = Modifier.height(12.dp))
                bettingRecommendation?.let { recommendation ->
                    BettingHints(recommendation = recommendation)
                } ?: run {
                    // Показываем заглушку, если подсказка еще не готова
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFF1E3A5F).copy(alpha = 0.7f)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "💡 Подсказка загружается...",
                                fontSize = 14.sp,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Анализ диапазонов оппонентов
            if (opponentRanges.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    opponentRanges.forEach { (playerId, range) ->
                        val player = players.find { it.id == playerId }
                        val strength = rangeStrengths[playerId]
                        if (player != null && strength != null) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "${player.name}:",
                                    fontSize = 12.sp,
                                    color = Color.White,
                                    modifier = Modifier.padding(bottom = 4.dp)
                                )
                                OpponentRangeView(
                                    rangeAnalysis = range,
                                    rangeStrength = strength,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(20.dp))
        }
        
        // Фиксированные кнопки действий внизу
        if (currentPlayerIndex == 0 && players.isNotEmpty() && !players[0].isFolded) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                GreenTable.copy(alpha = 0.95f)
                            )
                        )
                    )
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                ActionButtons(
                    viewModel = viewModel,
                    currentBet = currentBet,
                    playerChips = players[0].chips,
                    playerCurrentBet = players[0].currentBet
                )
            }
        }
    }
}

@Composable
fun ActionButtons(
    viewModel: GameViewModel,
    currentBet: Int,
    playerChips: Int,
    playerCurrentBet: Int
) {
    var betAmount by remember { mutableStateOf(50) }
    val minBet = if (currentBet > 0) currentBet * 2 else 20
    val maxBet = playerChips
    
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // Слайдер для выбора суммы ставки
        if (currentBet == 0 || currentBet == playerCurrentBet) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color.Black.copy(alpha = 0.5f)
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "💰 $betAmount",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "$minBet",
                                fontSize = 9.sp,
                                color = Color.White.copy(alpha = 0.7f)
                            )
                            Text("|", fontSize = 9.sp, color = Color.White.copy(alpha = 0.5f))
                            Text(
                                text = "$maxBet",
                                fontSize = 9.sp,
                                color = Color.White.copy(alpha = 0.7f)
                            )
                        }
                    }
                    Slider(
                        value = betAmount.toFloat(),
                        onValueChange = { betAmount = it.roundToInt().coerceIn(minBet, maxBet) },
                        valueRange = minBet.toFloat()..maxBet.toFloat(),
                        modifier = Modifier.fillMaxWidth(),
                        colors = SliderDefaults.colors(
                            thumbColor = Gold,
                            activeTrackColor = Gold,
                            inactiveTrackColor = Color.White.copy(alpha = 0.3f)
                        )
                    )
                }
            }
        }
        
        // Кнопки действий
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Сбросить
            Button(
                onClick = { viewModel.playerAction(PlayerAction.FOLD) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFDC3545)
                ),
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("❌ Сбросить", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
            
            if (currentBet == 0 || currentBet == playerCurrentBet) {
                // Чек
                Button(
                    onClick = { viewModel.playerAction(PlayerAction.CHECK) },
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF6C757D)
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("✅ Чек", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
                // Ставка
                Button(
                    onClick = { viewModel.playerAction(PlayerAction.BET, betAmount) },
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF2196F3)
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("💰 Ставка", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            } else {
                val callAmount = currentBet - playerCurrentBet
                // Колл (уравнять ставку)
                Button(
                    onClick = { viewModel.playerAction(PlayerAction.CALL) },
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4CAF50)
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("⚖️ Уравнять", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Text("$callAmount", fontSize = 8.sp)
                    }
                }
                // Рейз
                Button(
                    onClick = { 
                        val raiseAmount = maxOf(currentBet * 2, betAmount)
                        viewModel.playerAction(PlayerAction.RAISE, raiseAmount) 
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFF9800)
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("⬆️ Рейз", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Text("до ${currentBet * 2}", fontSize = 8.sp)
                    }
                }
            }
            
            // Олл-ин
            Button(
                onClick = { viewModel.playerAction(PlayerAction.ALL_IN) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Gold
                ),
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("💎 Олл-ин", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Black)
            }
        }
    }
}
