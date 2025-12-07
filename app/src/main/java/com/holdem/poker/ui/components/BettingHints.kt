package com.holdem.poker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.holdem.poker.model.PlayerAction
import com.holdem.poker.strategy.BettingHelper

/**
 * Компонент для отображения подсказок по ставкам
 */
@Composable
fun BettingHints(
    recommendation: BettingHelper.BettingRecommendation,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1E3A5F).copy(alpha = 0.95f)
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "💡 Подсказка",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                
                // Индикатор уверенности
                ConfidenceIndicator(recommendation.confidence)
            }
            
            Divider(color = Color.White.copy(alpha = 0.3f))
            
            // Рекомендуемое действие
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ActionBadge(recommendation.action)
                if (recommendation.suggestedAmount > 0) {
                    Text(
                        text = "${recommendation.suggestedAmount}",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFFD700)
                    )
                }
            }
            
            // Обоснование
            Text(
                text = recommendation.reasoning,
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.9f)
            )
            
            // Дополнительная информация
            if (recommendation.potOdds != null || recommendation.equity != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    recommendation.potOdds?.let { odds ->
                        InfoBadge(
                            label = "Pot Odds",
                            value = "${(odds * 100).toInt()}%",
                            color = Color(0xFF4CAF50)
                        )
                    }
                    recommendation.equity?.let { equity ->
                        InfoBadge(
                            label = "Вероятность",
                            value = "${(equity * 100).toInt()}%",
                            color = Color(0xFF2196F3)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ActionBadge(action: PlayerAction) {
    val (text, color) = when (action) {
        PlayerAction.FOLD -> "СБРОСИТЬ" to Color.Red
        PlayerAction.CHECK -> "ЧЕК" to Color.Gray
        PlayerAction.CALL -> "КОЛЛ" to Color(0xFF4CAF50)
        PlayerAction.BET -> "СТАВКА" to Color(0xFF2196F3)
        PlayerAction.RAISE -> "РЕЙЗ" to Color(0xFFFF9800)
        PlayerAction.ALL_IN -> "ОЛЛ-ИН" to Color(0xFFFFD700)
    }
    
    Box(
        modifier = Modifier
            .background(color, RoundedCornerShape(6.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = text,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }
}

@Composable
private fun ConfidenceIndicator(confidence: Float) {
    val color = when {
        confidence >= 0.8f -> Color(0xFF4CAF50)
        confidence >= 0.6f -> Color(0xFFFF9800)
        else -> Color(0xFFF44336)
    }
    
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = "Уверенность:",
            fontSize = 12.sp,
            color = Color.White.copy(alpha = 0.7f)
        )
        Box(
            modifier = Modifier
                .width(60.dp)
                .height(8.dp)
                .background(Color.Gray.copy(alpha = 0.3f), RoundedCornerShape(4.dp))
        ) {
            Box(
                modifier = Modifier
                    .width(60.dp * confidence)
                    .height(8.dp)
                    .background(color, RoundedCornerShape(4.dp))
            )
        }
        Text(
            text = "${(confidence * 100).toInt()}%",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

@Composable
private fun InfoBadge(label: String, value: String, color: Color) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            fontSize = 10.sp,
            color = Color.White.copy(alpha = 0.7f)
        )
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

