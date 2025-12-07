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
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (isCurrentPlayer) Gold.copy(alpha = 0.3f) else Color.Transparent
            )
            .border(
                width = if (isCurrentPlayer) 2.dp else 0.dp,
                color = Gold,
                shape = RoundedCornerShape(12.dp)
            )
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = player.name,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = Color.White
        )
        
        Spacer(modifier = Modifier.height(4.dp))
        
        // Стек игрока (фишки)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "Стек:",
                fontSize = 12.sp,
                color = Color.White.copy(alpha = 0.7f)
            )
            Text(
                text = "${player.chips}",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Gold
            )
        }
        
        // Текущая ставка
        if (player.currentBet > 0) {
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "Ставка:",
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.7f)
                )
                Text(
                    text = "${player.currentBet}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFF6B6B)
                )
            }
        }
        
        // Блайнды
        if (player.isSmallBlind || player.isBigBlind) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = if (player.isSmallBlind) "SB" else "BB",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFFFD93D)
            )
        }
        
        if (player.isFolded) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "FOLDED",
                fontSize = 10.sp,
                color = Color.Red
            )
        }
        
        if (player.isDealer) {
            Spacer(modifier = Modifier.height(4.dp))
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(Color.Blue),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "D",
                    fontSize = 10.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        
        Row(
            modifier = Modifier.padding(top = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            player.hand.forEach { card ->
                CardView(
                    card = if (player.id == "player1") card else null,
                    isFaceUp = player.id == "player1",
                    modifier = Modifier.size(width = 40.dp, height = 56.dp)
                )
            }
        }
    }
}

