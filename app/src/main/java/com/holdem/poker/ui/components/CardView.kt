package com.holdem.poker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.holdem.poker.model.Card
import com.holdem.poker.model.CardColor
import com.holdem.poker.ui.theme.CardBlack
import com.holdem.poker.ui.theme.CardRed

@Composable
fun CardView(
    card: Card?,
    modifier: Modifier = Modifier,
    isFaceUp: Boolean = true
) {
    Box(
        modifier = modifier
            .size(width = 60.dp, height = 84.dp)
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(10.dp),
                spotColor = Color.Black.copy(alpha = 0.3f)
            )
            .clip(RoundedCornerShape(10.dp))
            .background(
                if (isFaceUp) {
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.White,
                            Color(0xFFF5F5F5)
                        )
                    )
                } else {
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF2E7D32),
                            Color(0xFF1B5E20)
                        )
                    )
                }
            )
            .border(2.dp, Color.Black.copy(alpha = 0.2f), RoundedCornerShape(10.dp)),
        contentAlignment = Alignment.Center
    ) {
        if (isFaceUp && card != null) {
            val textColor = when (card.suit.color) {
                CardColor.RED -> CardRed
                CardColor.BLACK -> CardBlack
            }
            
            Column(
                modifier = Modifier.padding(6.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = card.rank.symbol,
                    color = textColor,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = card.suit.symbol,
                    color = textColor,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        } else if (!isFaceUp) {
            // Рубашка карты с узором
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                Color(0xFF388E3C).copy(alpha = 0.8f),
                                Color(0xFF1B5E20)
                            )
                        )
                    )
            ) {
                // Декоративный узор на рубашке
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp)
                ) {
                    Text(
                        text = "♠",
                        color = Color.White.copy(alpha = 0.1f),
                        fontSize = 40.sp,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }
        }
    }
}

@Composable
fun CardBack(modifier: Modifier = Modifier) {
    CardView(card = null, isFaceUp = false, modifier = modifier)
}

