package com.holdem.poker.strategy

import com.holdem.poker.model.*

/**
 * Анализатор диапазонов рук оппонентов
 * Основано на современных solver-based алгоритмах 2025
 */
class RangeAnalyzer {
    
    /**
     * Диапазон рук оппонента
     */
    data class HandRange(
        val possibleHands: List<Pair<Card, Card>>,
        val probability: Double,
        val strength: Float // Средняя сила диапазона
    )
    
    /**
     * Анализирует возможные руки оппонента на основе его действий
     */
    fun analyzeOpponentRange(
        opponentActions: List<OpponentAction>,
        communityCards: List<Card>,
        pot: Int,
        position: Int
    ): HandRange {
        // Начинаем с полного диапазона
        var range = getAllPossibleHands()
        
        // Сужаем диапазон на основе действий
        opponentActions.forEach { action ->
            range = narrowRangeByAction(range, action, communityCards, pot, position)
        }
        
        // Учитываем позицию
        range = adjustRangeByPosition(range, position)
        
        val strength = calculateAverageStrength(range, communityCards)
        val probability = calculateRangeProbability(range)
        
        return HandRange(range, probability, strength)
    }
    
    /**
     * Определяет наиболее вероятные руки оппонента
     */
    fun getMostLikelyHands(
        range: HandRange,
        communityCards: List<Card>,
        count: Int = 5
    ): List<Pair<Pair<Card, Card>, Double>> {
        val evaluator = com.holdem.poker.engine.PokerHandEvaluator()
        
        return range.possibleHands.map { hand ->
            val evaluation = if (communityCards.size >= 3) {
                evaluator.evaluateBestHand(listOf(hand.first, hand.second), communityCards)
            } else {
                null
            }
            val strength = evaluation?.rank?.value?.div(10.0) ?: 0.5
            hand to strength
        }
            .sortedByDescending { it.second }
            .take(count)
            .map { it.first to it.second }
    }
    
    /**
     * Оценивает силу диапазона оппонента
     */
    fun evaluateRangeStrength(
        range: HandRange,
        communityCards: List<Card>
    ): RangeStrength {
        val evaluator = com.holdem.poker.engine.PokerHandEvaluator()
        
        val strengths = range.possibleHands.map { hand ->
            if (communityCards.size >= 3) {
                val evaluation = evaluator.evaluateBestHand(
                    listOf(hand.first, hand.second),
                    communityCards
                )
                evaluation.rank.value / 10.0
            } else {
                evaluatePreFlopStrength(hand.first, hand.second).toDouble()
            }
        }
        
        if (strengths.isEmpty()) {
            return RangeStrength(
                average = 0f,
                maximum = 0f,
                minimum = 0f,
                distribution = emptyMap()
            )
        }
        
        val avgStrength = strengths.average()
        val maxStrength = strengths.maxOrNull() ?: 0.0
        val minStrength = strengths.minOrNull() ?: 0.0
        
        return RangeStrength(
            average = avgStrength.toFloat(),
            maximum = maxStrength.toFloat(),
            minimum = minStrength.toFloat(),
            distribution = strengths.groupBy { strength ->
                when {
                    strength >= 0.7 -> "Сильные"
                    strength >= 0.5 -> "Средние"
                    else -> "Слабые"
                }
            }.mapValues { it.value.size.toDouble() / strengths.size }
        )
    }
    
    private fun getAllPossibleHands(): List<Pair<Card, Card>> {
        val allCards = mutableListOf<Card>()
        for (suit in Suit.values()) {
            for (rank in Rank.values()) {
                allCards.add(Card(suit, rank))
            }
        }
        
        val hands = mutableListOf<Pair<Card, Card>>()
        for (i in allCards.indices) {
            for (j in i + 1 until allCards.size) {
                hands.add(allCards[i] to allCards[j])
            }
        }
        return hands
    }
    
    private fun narrowRangeByAction(
        range: List<Pair<Card, Card>>,
        action: OpponentAction,
        communityCards: List<Card>,
        pot: Int,
        position: Int
    ): List<Pair<Card, Card>> {
        return when (action.type) {
            ActionType.FOLD -> emptyList() // Оппонент сфолдил
            ActionType.CHECK -> {
                // Чек обычно означает слабую или среднюю руку
                range.filter { evaluatePreFlopStrength(it.first, it.second) < 0.7 }
            }
            ActionType.CALL -> {
                // Колл - средние руки, которые хотят увидеть следующую карту
                range.filter { 
                    val strength = evaluatePreFlopStrength(it.first, it.second)
                    strength in 0.3..0.7
                }
            }
            ActionType.BET, ActionType.RAISE -> {
                // Ставка/рейз - сильные руки или блеф
                val betSizeRatio = action.amount.toDouble() / pot
                when {
                    betSizeRatio > 0.75 -> {
                        // Большая ставка - очень сильные руки
                        range.filter { evaluatePreFlopStrength(it.first, it.second) > 0.7 }
                    }
                    betSizeRatio > 0.5 -> {
                        // Средняя ставка - сильные руки
                        range.filter { evaluatePreFlopStrength(it.first, it.second) > 0.6 }
                    }
                    else -> {
                        // Малая ставка - может быть блеф или средняя рука
                        range.filter { evaluatePreFlopStrength(it.first, it.second) > 0.4 }
                    }
                }
            }
            ActionType.ALL_IN -> {
                // Олл-ин - очень сильные руки или отчаяние
                range.filter { evaluatePreFlopStrength(it.first, it.second) > 0.75 }
            }
        }
    }
    
    private fun adjustRangeByPosition(
        range: List<Pair<Card, Card>>,
        position: Int
    ): List<Pair<Card, Card>> {
        // В поздней позиции диапазон шире
        return when (position) {
            0 -> range.filter { evaluatePreFlopStrength(it.first, it.second) > 0.5 } // Early - tight
            1 -> range.filter { evaluatePreFlopStrength(it.first, it.second) > 0.4 } // Middle - medium
            else -> range // Late - wide
        }
    }
    
    private fun calculateAverageStrength(
        range: List<Pair<Card, Card>>,
        communityCards: List<Card>
    ): Float {
        if (range.isEmpty()) return 0f
        
        val strengths = range.map { 
            if (communityCards.isEmpty()) {
                evaluatePreFlopStrength(it.first, it.second).toDouble()
            } else {
                val evaluator = com.holdem.poker.engine.PokerHandEvaluator()
                val evaluation = evaluator.evaluateBestHand(
                    listOf(it.first, it.second),
                    communityCards
                )
                evaluation.rank.value / 10.0
            }
        }
        
        return (strengths.average()).toFloat()
    }
    
    private fun calculateRangeProbability(range: HandRange): Double {
        val totalHands = 1326.0 // Всего возможных комбинаций из 2 карт
        return range.possibleHands.size / totalHands
    }
    
    private fun evaluatePreFlopStrength(card1: Card, card2: Card): Float {
        // Пара
        if (card1.rank == card2.rank) {
            return 0.6f + (card1.rank.value - 2) * 0.03f
        }
        
        // Одинаковая масть
        val isSuited = card1.suit == card2.suit
        val highCard = maxOf(card1.rank.value, card2.rank.value)
        val lowCard = minOf(card1.rank.value, card2.rank.value)
        val isConnector = (highCard - lowCard) <= 1
        
        var strength = 0.3f
        if (isSuited) strength += 0.1f
        if (isConnector) strength += 0.1f
        strength += (highCard - 7) * 0.05f
        
        return strength.coerceIn(0f, 1f)
    }
    
    data class OpponentAction(
        val type: ActionType,
        val amount: Int,
        val stage: GameState
    )
    
    enum class ActionType {
        FOLD, CHECK, CALL, BET, RAISE, ALL_IN
    }
    
    data class RangeStrength(
        val average: Float,
        val maximum: Float,
        val minimum: Float,
        val distribution: Map<String, Double>
    )
}

