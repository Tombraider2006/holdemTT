package com.holdem.poker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
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

/**
 * Компонент для отображения игрока
 * Чистый и понятный дизайн
 */
@Composable
fun PlayerView(
    player: Player,
    isCurrentPlayer: Boolean = false,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.padding(horizontal = 8.dp, vertical = 4.dp),
        colors = androidx.compose.material3.CardDefaults.cardColors(
            containerColor = if (isCurrentPlayer) {
                Gold.copy(alpha = 0.3f)
            } else {
                Color.White.copy(alpha = 0.15f)
            }
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = androidx.compose.material3.CardDefaults.cardElevation(
            defaultElevation = if (isCurrentPlayer) 8.dp else 4.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Имя игрока и индикатор дилера
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (player.isDealer) {
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF2196F3))
                            .padding(4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "D",
                            fontSize = 10.sp,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                }
                Text(
                    text = player.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color.White
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Карты игрока
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                player.hand.forEach { card ->
                    CardView(
                        card = if (player.id == "player1") card else null,
                        isFaceUp = player.id == "player1",
                        size = CardSize.SMALL
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Информация об игроке
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // Стек
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .background(
                            color = Color.Black.copy(alpha = 0.3f),
                            shape = RoundedCornerShape(6.dp)
                        )
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "💰",
                        fontSize = 14.sp
                    )
                    Text(
                        text = "${player.chips}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Gold
                    )
                }
                
                // Ставка
                if (player.currentBet > 0) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .background(
                                color = Color(0xFFFF6B6B).copy(alpha = 0.3f),
                                shape = RoundedCornerShape(6.dp)
                            )
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "📊",
                            fontSize = 14.sp
                        )
                        Text(
                            text = "${player.currentBet}",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFF6B6B)
                        )
                    }
                }
            }
            
            // Статусы
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (player.isSmallBlind) {
                    Badge(
                        containerColor = Color(0xFFFFD93D).copy(alpha = 0.8f),
                        contentColor = Color.Black
                    ) {
                        Text("SB", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
                if (player.isBigBlind) {
                    if (player.isSmallBlind) Spacer(modifier = Modifier.width(4.dp))
                    Badge(
                        containerColor = Color(0xFFFFD93D).copy(alpha = 0.8f),
                        contentColor = Color.Black
                    ) {
                        Text("BB", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
                if (player.isFolded) {
                    if (player.isSmallBlind || player.isBigBlind) Spacer(modifier = Modifier.width(4.dp))
                    Badge(
                        containerColor = Color.Red.copy(alpha = 0.8f),
                        contentColor = Color.White
                    ) {
                        Text("СБРОШЕНО", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
