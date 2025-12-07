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
            .clip(RoundedCornerShape(8.dp))
            .background(if (isFaceUp) Color.White else Color(0xFF1B5E20))
            .border(1.dp, Color.Black, RoundedCornerShape(8.dp)),
        contentAlignment = Alignment.Center
    ) {
        if (isFaceUp && card != null) {
            val textColor = when (card.suit.color) {
                CardColor.RED -> CardRed
                CardColor.BLACK -> CardBlack
            }
            
            Column(
                modifier = Modifier.padding(4.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = card.rank.symbol,
                    color = textColor,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = card.suit.symbol,
                    color = textColor,
                    fontSize = 24.sp
                )
            }
        } else if (!isFaceUp) {
            // Рубашка карты
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF1B5E20))
            )
        }
    }
}

@Composable
fun CardBack(modifier: Modifier = Modifier) {
    CardView(card = null, isFaceUp = false, modifier = modifier)
}

