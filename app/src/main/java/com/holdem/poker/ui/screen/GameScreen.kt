package com.holdem.poker.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.holdem.poker.audio.rememberSoundManager
import com.holdem.poker.model.PlayerAction
import com.holdem.poker.strategy.RangeAnalyzer
import com.holdem.poker.ui.components.BettingHints
import com.holdem.poker.ui.components.CardView
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
    // Используем единое состояние UI с оптимизацией collectAsStateWithLifecycle
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val soundManager = rememberSoundManager()
    
    // Извлекаем значения из uiState для удобства
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
        // Скроллируемый контент
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 80.dp), // Отступ для фиксированных кнопок
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Обработка ошибок с улучшенным дизайном
            uiState.error?.let { error ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.Red.copy(alpha = 0.95f)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("⚠️", fontSize = 20.sp)
                            Text(
                                text = error,
                                color = Color.White,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        TextButton(onClick = { viewModel.clearError() }) {
                            Text("✕", color = Color.White, fontSize = 18.sp)
                        }
                    }
                }
            }
            
            // Обработка сообщений с улучшенным дизайном
            uiState.message?.let { message ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Gold.copy(alpha = 0.95f)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("ℹ️", fontSize = 20.sp)
                            Text(
                                text = message,
                                color = Color.Black,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        TextButton(onClick = { viewModel.clearMessage() }) {
                            Text("✕", color = Color.Black, fontSize = 18.sp)
                        }
                    }
                }
            }
            
            // Кнопка настроек в правом верхнем углу
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(
                    onClick = onSettingsClick,
                    modifier = Modifier
                        .background(
                            color = Color(0xFF1E3A5F).copy(alpha = 0.8f),
                            shape = RoundedCornerShape(8.dp)
                        )
                ) {
                    Text("⚙️", fontSize = 20.sp)
                }
            }
            
            // Индикатор загрузки
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.padding(16.dp),
                    color = Gold
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            
            // Верхние игроки (AI)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
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
            
            // Общие карты с улучшенным дизайном
            Column(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Общие карты",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White.copy(alpha = 0.7f),
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    communityCards.forEach { card ->
                        CardView(card = card, isFaceUp = true)
                    }
                    // Показываем пустые места для будущих карт
                    repeat(5 - communityCards.size) {
                        CardView(card = null, isFaceUp = false)
                    }
                }
            }
            
            // Pot и информация о ставках с улучшенным дизайном
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    Gold.copy(alpha = 0.4f),
                                    Gold.copy(alpha = 0.2f)
                                )
                            ),
                            shape = RoundedCornerShape(16.dp)
                        )
                        .border(
                            width = 2.dp,
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    Gold.copy(alpha = 0.8f),
                                    Gold.copy(alpha = 0.4f)
                                )
                            ),
                            shape = RoundedCornerShape(16.dp)
                        )
                        .padding(horizontal = 28.dp, vertical = 16.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "💰",
                                fontSize = 28.sp
                            )
                            Text(
                                text = "Pot: $pot",
                                fontSize = 28.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White,
                                letterSpacing = 1.sp
                            )
                        }
                        if (currentBet > 0) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "📊",
                                    fontSize = 16.sp
                                )
                                Text(
                                    text = "Текущая ставка: $currentBet",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White.copy(alpha = 0.9f)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier
                                .background(
                                    color = Color.Black.copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "SB: $smallBlind",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                            Text(
                                text = "•",
                                fontSize = 13.sp,
                                color = Color.White.copy(alpha = 0.5f)
                            )
                            Text(
                                text = "BB: $bigBlind",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Нижний игрок (игрок)
            if (players.isNotEmpty()) {
                PlayerView(
                    player = players[0],
                    isCurrentPlayer = currentPlayerIndex == 0,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            
            if (players.size > 3) {
                PlayerView(
                    player = players[3],
                    isCurrentPlayer = currentPlayerIndex == 3,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Подсказки по ставкам
            if (currentPlayerIndex == 0 && players.isNotEmpty() && !players[0].isFolded) {
                bettingRecommendation?.let { recommendation ->
                    BettingHints(recommendation = recommendation)
                }
            }
            
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
            
            // Дополнительный отступ внизу для скроллинга
            Spacer(modifier = Modifier.height(20.dp))
        }
        
        // Фиксированные кнопки действий внизу экрана
        if (currentPlayerIndex == 0 && players.isNotEmpty() && !players[0].isFolded) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                GreenTable.copy(alpha = 0.95f)
                            )
                        )
                    )
                    .padding(horizontal = 16.dp, vertical = 12.dp)
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
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Слайдер для выбора суммы ставки (drag and drop)
        if (currentBet == 0 || currentBet == playerCurrentBet) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF1E3A5F).copy(alpha = 0.8f)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "💰 Сумма ставки: $betAmount",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
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
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Мин: $minBet",
                            fontSize = 10.sp,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                        Text(
                            text = "Макс: $maxBet",
                            fontSize = 10.sp,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
        // Кнопка Сбросить
        Button(
            onClick = { viewModel.playerAction(PlayerAction.FOLD) },
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFDC3545)
            ),
            modifier = Modifier
                .weight(1f)
                .heightIn(min = 48.dp, max = 56.dp),
            shape = RoundedCornerShape(12.dp),
            elevation = ButtonDefaults.buttonElevation(
                defaultElevation = 4.dp,
                pressedElevation = 2.dp
            )
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text("✕", fontSize = 16.sp)
                Text(
                    "Сбросить",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        
        if (currentBet == 0 || currentBet == playerCurrentBet) {
            // Кнопка Чек
            Button(
                onClick = { viewModel.playerAction(PlayerAction.CHECK) },
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF6C757D)
                ),
                shape = RoundedCornerShape(12.dp),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 4.dp,
                    pressedElevation = 2.dp
                )
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text("✓", fontSize = 16.sp)
                    Text(
                        "Чек",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            // Кнопка Ставка
            Button(
                onClick = { viewModel.playerAction(PlayerAction.BET, betAmount) },
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF2196F3)
                ),
                shape = RoundedCornerShape(12.dp),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 4.dp,
                    pressedElevation = 2.dp
                )
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text("💰", fontSize = 16.sp)
                    Text(
                        "Ставка",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        } else {
            val callAmount = currentBet - playerCurrentBet
            // Кнопка Колл
            Button(
                onClick = { viewModel.playerAction(PlayerAction.CALL) },
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF4CAF50)
                ),
                shape = RoundedCornerShape(12.dp),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 4.dp,
                    pressedElevation = 2.dp
                )
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text("📞", fontSize = 16.sp)
                    Text(
                        "Колл",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "$callAmount",
                        fontSize = 9.sp,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                }
            }
            // Кнопка Рейз
            Button(
                onClick = { 
                    val raiseAmount = maxOf(currentBet * 2, betAmount)
                    viewModel.playerAction(PlayerAction.RAISE, raiseAmount) 
                },
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFF9800)
                ),
                shape = RoundedCornerShape(12.dp),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 4.dp,
                    pressedElevation = 2.dp
                )
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text("📈", fontSize = 16.sp)
                    Text(
                        "Рейз",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "до ${currentBet * 2}",
                        fontSize = 9.sp,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                }
            }
        }
        
        // Кнопка Олл-ин
        Button(
            onClick = { viewModel.playerAction(PlayerAction.ALL_IN) },
            colors = ButtonDefaults.buttonColors(
                containerColor = Gold
            ),
            modifier = Modifier
                .weight(1f)
                .heightIn(min = 48.dp, max = 56.dp),
            shape = RoundedCornerShape(12.dp),
            elevation = ButtonDefaults.buttonElevation(
                defaultElevation = 6.dp,
                pressedElevation = 3.dp
            )
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text("🔥", fontSize = 16.sp)
                Text(
                    "Олл-ин",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.Black
                )
            }
        }
        }
    }
}

