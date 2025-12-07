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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.holdem.poker.model.Card
import com.holdem.poker.model.CardColor
import com.holdem.poker.ui.theme.CardBlack
import com.holdem.poker.ui.theme.CardRed

/**
 * Компонент для отображения игральной карты
 * Реалистичный дизайн как у настоящих карт
 */
@Composable
fun CardView(
    card: Card?,
    modifier: Modifier = Modifier,
    isFaceUp: Boolean = true,
    size: CardSize = CardSize.MEDIUM
) {
    val cardWidth = when (size) {
        CardSize.SMALL -> 50.dp
        CardSize.MEDIUM -> 70.dp
        CardSize.LARGE -> 90.dp
    }
    val cardHeight = (cardWidth * 1.4f)
    
    Box(
        modifier = modifier
            .size(width = cardWidth, height = cardHeight)
            .shadow(
                elevation = 6.dp,
                shape = RoundedCornerShape(8.dp),
                spotColor = Color.Black.copy(alpha = 0.4f)
            )
            .clip(RoundedCornerShape(8.dp))
            .then(
                if (isFaceUp && card != null) {
                    Modifier.background(Color.White, RoundedCornerShape(8.dp))
                } else {
                    Modifier.background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color(0xFF1A237E),
                                Color(0xFF283593),
                                Color(0xFF1A237E)
                            )
                        ),
                        shape = RoundedCornerShape(8.dp)
                    )
                }
            )
            .border(1.dp, Color.Black.copy(alpha = 0.3f), RoundedCornerShape(8.dp)),
        contentAlignment = Alignment.Center
    ) {
        if (isFaceUp && card != null) {
            val textColor = when (card.suit.color) {
                CardColor.RED -> CardRed
                CardColor.BLACK -> CardBlack
            }
            
            // Верхний левый угол - ранг и масть
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(horizontal = 6.dp, vertical = 4.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.Start,
                    verticalArrangement = Arrangement.spacedBy(0.dp)
                ) {
                    Text(
                        text = card.rank.symbol,
                        color = textColor,
                        fontSize = when (size) {
                            CardSize.SMALL -> 14.sp
                            CardSize.MEDIUM -> 18.sp
                            CardSize.LARGE -> 22.sp
                        },
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.SansSerif
                    )
                    Text(
                        text = card.suit.symbol,
                        color = textColor,
                        fontSize = when (size) {
                            CardSize.SMALL -> 12.sp
                            CardSize.MEDIUM -> 16.sp
                            CardSize.LARGE -> 20.sp
                        },
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            // Центр - большая масть
            Text(
                text = card.suit.symbol,
                color = textColor,
                fontSize = when (size) {
                    CardSize.SMALL -> 28.sp
                    CardSize.MEDIUM -> 40.sp
                    CardSize.LARGE -> 52.sp
                },
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.Center)
            )
            
            // Нижний правый угол - ранг и масть (перевернутые)
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(horizontal = 6.dp, vertical = 4.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(0.dp)
                ) {
                    Text(
                        text = card.suit.symbol,
                        color = textColor,
                        fontSize = when (size) {
                            CardSize.SMALL -> 12.sp
                            CardSize.MEDIUM -> 16.sp
                            CardSize.LARGE -> 20.sp
                        },
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = card.rank.symbol,
                        color = textColor,
                        fontSize = when (size) {
                            CardSize.SMALL -> 14.sp
                            CardSize.MEDIUM -> 18.sp
                            CardSize.LARGE -> 22.sp
                        },
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.SansSerif
                    )
                }
            }
        } else if (!isFaceUp) {
            // Рубашка карты с декоративным узором
            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                // Фоновый узор
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceEvenly
                ) {
                    repeat(3) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            repeat(2) {
                                Text(
                                    text = "♠",
                                    color = Color.White.copy(alpha = 0.15f),
                                    fontSize = when (size) {
                                        CardSize.SMALL -> 12.sp
                                        CardSize.MEDIUM -> 16.sp
                                        CardSize.LARGE -> 20.sp
                                    },
                                    modifier = Modifier.weight(1f),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
                
                // Центральный логотип
                Box(
                    modifier = Modifier
                        .size(cardWidth * 0.4f)
                        .align(Alignment.Center)
                        .background(
                            color = Color.White.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(4.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "♠",
                        color = Color.White.copy(alpha = 0.3f),
                        fontSize = when (size) {
                            CardSize.SMALL -> 20.sp
                            CardSize.MEDIUM -> 28.sp
                            CardSize.LARGE -> 36.sp
                        }
                    )
                }
            }
        }
    }
}

enum class CardSize {
    SMALL, MEDIUM, LARGE
}
