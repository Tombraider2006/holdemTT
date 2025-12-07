package com.holdem.poker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.holdem.poker.model.Player
import com.holdem.poker.ui.theme.Gold

@Composable
fun PlayerView(
    player: Player,
    isCurrentPlayer: Boolean = false,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .padding(8.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(
                brush = if (isCurrentPlayer) {
                    Brush.linearGradient(
                        colors = listOf(
                            Gold.copy(alpha = 0.4f),
                            Gold.copy(alpha = 0.2f)
                        )
                    )
                } else {
                    Brush.linearGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.1f),
                            Color.Transparent
                        )
                    )
                },
                shape = RoundedCornerShape(16.dp)
            )
            .border(
                width = if (isCurrentPlayer) 3.dp else 1.dp,
                brush = if (isCurrentPlayer) {
                    Brush.linearGradient(
                        colors = listOf(Gold, Gold.copy(alpha = 0.7f))
                    )
                } else {
                    Brush.linearGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.2f),
                            Color.Transparent
                        )
                    )
                },
                shape = RoundedCornerShape(16.dp)
            )
            .padding(14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Имя игрока с улучшенным стилем
        Text(
            text = player.name,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 16.sp,
            color = Color.White,
            letterSpacing = 0.5.sp
        )
        
        Spacer(modifier = Modifier.height(6.dp))
        
        // Стек игрока (фишки) с визуальным улучшением
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier
                .background(
                    color = Color.Black.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(8.dp)
                )
                .padding(horizontal = 10.dp, vertical = 6.dp)
        ) {
            Text(
                text = "💰",
                fontSize = 14.sp
            )
            Text(
                text = "Стек:",
                fontSize = 12.sp,
                color = Color.White.copy(alpha = 0.8f),
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "${player.chips}",
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Gold
            )
        }
        
        // Текущая ставка с улучшенным дизайном
        if (player.currentBet > 0) {
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier
                    .background(
                        color = Color(0xFFFF6B6B).copy(alpha = 0.2f),
                        shape = RoundedCornerShape(8.dp)
                    )
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "📊",
                    fontSize = 14.sp
                )
                Text(
                    text = "Ставка:",
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.8f),
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "${player.currentBet}",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFFFF6B6B)
                )
            }
        }
        
        // Blinds с улучшенным дизайном
        if (player.isSmallBlind || player.isBigBlind) {
            Spacer(modifier = Modifier.height(6.dp))
            Box(
                modifier = Modifier
                    .background(
                        color = Color(0xFFFFD93D).copy(alpha = 0.3f),
                        shape = RoundedCornerShape(6.dp)
                    )
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = if (player.isSmallBlind) "SB" else "BB",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFFD93D)
                )
            }
        }
        
        // Статус сброса
        if (player.isFolded) {
            Spacer(modifier = Modifier.height(6.dp))
            Box(
                modifier = Modifier
                    .background(
                        color = Color.Red.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(6.dp)
                    )
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "✕ СБРОШЕНО",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Red
                )
            }
        }
        
        // Индикатор дилера с улучшенным дизайном
        if (player.isDealer) {
            Spacer(modifier = Modifier.height(6.dp))
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(0xFF2196F3),
                                Color(0xFF1976D2)
                            )
                        )
                    )
                    .border(2.dp, Color.White, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "D",
                    fontSize = 12.sp,
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
        
        // Карты игрока
        Spacer(modifier = Modifier.height(10.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            player.hand.forEach { card ->
                CardView(
                    card = if (player.id == "player1") card else null,
                    isFaceUp = player.id == "player1",
                    modifier = Modifier.size(width = 45.dp, height = 63.dp)
                )
            }
        }
    }
}

