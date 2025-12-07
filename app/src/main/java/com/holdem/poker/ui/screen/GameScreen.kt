package com.holdem.poker.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.holdem.poker.model.PlayerAction
import com.holdem.poker.strategy.RangeAnalyzer
import com.holdem.poker.ui.components.BettingHints
import com.holdem.poker.ui.components.CardView
import com.holdem.poker.ui.components.OpponentRangeView
import com.holdem.poker.ui.components.PlayerView
import com.holdem.poker.ui.theme.GreenTable
import com.holdem.poker.ui.theme.Gold
import com.holdem.poker.ui.viewmodel.GameViewModel

@Composable
fun GameScreen(viewModel: GameViewModel = viewModel()) {
    val players by viewModel.players.collectAsState()
    val gameState by viewModel.gameState.collectAsState()
    val communityCards by viewModel.communityCards.collectAsState()
    val pot by viewModel.pot.collectAsState()
    val currentBet by viewModel.currentBet.collectAsState()
    val currentPlayerIndex by viewModel.currentPlayerIndex.collectAsState()
    val smallBlind by viewModel.smallBlind.collectAsState()
    val bigBlind by viewModel.bigBlind.collectAsState()
    val bettingRecommendation by viewModel.bettingRecommendation.collectAsState()
    val opponentRanges by viewModel.opponentRanges.collectAsState()
    val rangeStrengths by viewModel.rangeStrengths.collectAsState()
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(GreenTable)
    ) {
        // Игровой стол
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(20.dp))
            
            // Верхние игроки (AI)
            Row(
                modifier = Modifier.fillMaxWidth(),
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
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // Общие карты
            Row(
                modifier = Modifier.padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                communityCards.forEach { card ->
                    CardView(card = card, isFaceUp = true)
                }
                // Показываем пустые места для будущих карт
                repeat(5 - communityCards.size) {
                    CardView(card = null, isFaceUp = false)
                }
            }
            
            // Банк и информация о ставках
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .background(
                            color = Gold.copy(alpha = 0.3f),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 24.dp, vertical = 12.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Банк: $pot",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        if (currentBet > 0) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Текущая ставка: $currentBet",
                                fontSize = 16.sp,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "SB: $smallBlind",
                                fontSize = 12.sp,
                                color = Color.White.copy(alpha = 0.7f)
                            )
                            Text(
                                text = "BB: $bigBlind",
                                fontSize = 12.sp,
                                color = Color.White.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
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
            
            Spacer(modifier = Modifier.height(20.dp))
            
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
            
            // Кнопки действий
            if (currentPlayerIndex == 0 && players.isNotEmpty() && !players[0].isFolded) {
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
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Button(
            onClick = { viewModel.playerAction(PlayerAction.FOLD) },
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Red
            ),
            modifier = Modifier.weight(1f)
        ) {
            Text("Сбросить")
        }
        
        if (currentBet == 0 || currentBet == playerCurrentBet) {
            Button(
                onClick = { viewModel.playerAction(PlayerAction.CHECK) },
                modifier = Modifier.weight(1f)
            ) {
                Text("Чек")
            }
            Button(
                onClick = { viewModel.playerAction(PlayerAction.BET, 50) },
                modifier = Modifier.weight(1f)
            ) {
                Text("Ставка")
            }
        } else {
            val callAmount = currentBet - playerCurrentBet
            Button(
                onClick = { viewModel.playerAction(PlayerAction.CALL) },
                modifier = Modifier.weight(1f)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Колл")
                    Text(
                        text = "$callAmount",
                        fontSize = 10.sp
                    )
                }
            }
            Button(
                onClick = { viewModel.playerAction(PlayerAction.RAISE, currentBet * 2) },
                modifier = Modifier.weight(1f)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Рейз")
                    Text(
                        text = "до ${currentBet * 2}",
                        fontSize = 10.sp
                    )
                }
            }
        }
        
        Button(
            onClick = { viewModel.playerAction(PlayerAction.ALL_IN) },
            colors = ButtonDefaults.buttonColors(
                containerColor = Gold
            ),
            modifier = Modifier.weight(1f)
        ) {
            Text("Олл-ин")
        }
    }
}

