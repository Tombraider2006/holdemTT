package com.holdem.poker.ui.components

import androidx.compose.foundation.background
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
import com.holdem.poker.strategy.RangeAnalyzer

/**
 * Компонент для отображения анализа диапазона рук оппонента
 */
@Composable
fun OpponentRangeView(
    rangeAnalysis: RangeAnalyzer.HandRange,
    rangeStrength: RangeAnalyzer.RangeStrength,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF2D1B4E).copy(alpha = 0.9f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "📊 Анализ диапазона оппонента",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            
            Divider(color = Color.White.copy(alpha = 0.3f))
            
            // Вероятность диапазона
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Вероятность диапазона:",
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.8f)
                )
                Text(
                    text = "${(rangeAnalysis.probability * 100).toInt()}%",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFFD700)
                )
            }
            
            // Сила диапазона
            Column {
                Text(
                    text = "Сила диапазона:",
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.8f),
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                StrengthBar(
                    label = "Средняя",
                    value = rangeStrength.average,
                    color = Color(0xFF4CAF50)
                )
                StrengthBar(
                    label = "Максимальная",
                    value = rangeStrength.maximum,
                    color = Color(0xFF2196F3)
                )
                StrengthBar(
                    label = "Минимальная",
                    value = rangeStrength.minimum,
                    color = Color(0xFFF44336)
                )
            }
            
            // Распределение силы
            if (rangeStrength.distribution.isNotEmpty()) {
                Column {
                    Text(
                        text = "Распределение:",
                        fontSize = 14.sp,
                        color = Color.White.copy(alpha = 0.8f),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    rangeStrength.distribution.forEach { (category, percentage) ->
                        DistributionRow(
                            category = category,
                            percentage = percentage
                        )
                    }
                }
            }
            
            // Количество возможных рук
            Text(
                text = "Возможных комбинаций: ${rangeAnalysis.possibleHands.size}",
                fontSize = 12.sp,
                color = Color.White.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
private fun StrengthBar(label: String, value: Float, color: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = Color.White.copy(alpha = 0.7f),
            modifier = Modifier.width(80.dp)
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .height(12.dp)
                .background(Color.Gray.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(value.coerceIn(0f, 1f))
                    .background(color, RoundedCornerShape(6.dp))
            )
        }
        Text(
            text = "${(value * 100).toInt()}%",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = color,
            modifier = Modifier.width(40.dp)
        )
    }
}

@Composable
private fun DistributionRow(category: String, percentage: Double) {
    val color = when (category) {
        "Сильные" -> Color(0xFF4CAF50)
        "Средние" -> Color(0xFFFF9800)
        else -> Color(0xFFF44336)
    }
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = category,
            fontSize = 12.sp,
            color = Color.White.copy(alpha = 0.8f)
        )
        Text(
            text = "${(percentage * 100).toInt()}%",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

