package com.holdem.poker.strategy

import com.holdem.poker.engine.PokerHandEvaluator
import com.holdem.poker.model.*

/**
 * Помощник для расчета оптимальных размеров ставок
 * Основано на GTO (Game Theory Optimal) стратегии 2025
 */
class BettingHelper(
    private val evaluator: PokerHandEvaluator
) {
    
    /**
     * Рекомендация по размеру ставки на основе силы руки и позиции
     */
    data class BettingRecommendation(
        val action: PlayerAction,
        val suggestedAmount: Int,
        val confidence: Float, // 0.0 - 1.0
        val reasoning: String,
        val potOdds: Double? = null,
        val equity: Double? = null
    )
    
    /**
     * Рассчитывает рекомендацию по ставке
     */
    fun getBettingRecommendation(
        player: Player,
        communityCards: List<Card>,
        pot: Int,
        currentBet: Int,
        position: Int, // 0 = early, 1 = middle, 2 = late
        activePlayers: Int
    ): BettingRecommendation {
        val handStrength = evaluateHandStrength(player.hand, communityCards)
        val potOdds = calculatePotOdds(pot, currentBet, player.currentBet)
        val equity = calculateEquity(player.hand, communityCards, activePlayers)
        
        return when {
            // Pre-flop логика
            communityCards.isEmpty() -> getPreFlopRecommendation(
                player, pot, currentBet, position, handStrength
            )
            
            // Post-flop логика с GTO подходами
            else -> getPostFlopRecommendation(
                player, communityCards, pot, currentBet, handStrength, 
                potOdds, equity, position
            )
        }
    }
    
    private fun getPreFlopRecommendation(
        player: Player,
        pot: Int,
        currentBet: Int,
        position: Int,
        handStrength: Float
    ): BettingRecommendation {
        val callAmount = currentBet - player.currentBet
        
        return when {
            // Очень сильные руки - рейз
            handStrength > 0.85 -> BettingRecommendation(
                action = if (currentBet == 0) PlayerAction.BET else PlayerAction.RAISE,
                suggestedAmount = if (currentBet == 0) pot / 3 else currentBet * 2,
                confidence = 0.9f,
                reasoning = "Очень сильная рука - агрессивная игра"
            )
            
            // Сильные руки - умеренный рейз или колл
            handStrength > 0.65 -> {
                if (position >= 1 && callAmount < pot / 4) {
                    BettingRecommendation(
                        action = PlayerAction.CALL,
                        suggestedAmount = callAmount,
                        confidence = 0.7f,
                        reasoning = "Сильная рука, хорошие pot odds"
                    )
                } else {
                    BettingRecommendation(
                        action = if (currentBet == 0) PlayerAction.BET else PlayerAction.RAISE,
                        suggestedAmount = if (currentBet == 0) pot / 4 else (currentBet * 1.5).toInt(),
                        confidence = 0.75f,
                        reasoning = "Сильная рука - умеренная агрессия"
                    )
                }
            }
            
            // Средние руки - колл в поздней позиции
            handStrength > 0.45 && position >= 1 && callAmount < pot / 3 -> {
                BettingRecommendation(
                    action = PlayerAction.CALL,
                    suggestedAmount = callAmount,
                    confidence = 0.6f,
                    reasoning = "Средняя рука, приемлемые pot odds в поздней позиции"
                )
            }
            
            // Слабые руки - фолд
            else -> BettingRecommendation(
                action = PlayerAction.FOLD,
                suggestedAmount = 0,
                confidence = 0.8f,
                reasoning = "Слабая рука - фолд"
            )
        }
    }
    
    private fun getPostFlopRecommendation(
        player: Player,
        communityCards: List<Card>,
        pot: Int,
        currentBet: Int,
        handStrength: Float,
        potOdds: Double,
        equity: Double?,
        position: Int
    ): BettingRecommendation {
        val callAmount = currentBet - player.currentBet
        val evaluation = evaluator.evaluateBestHand(player.hand, communityCards)
        val handRank = evaluation.rank.value / 10.0
        
        // GTO подход: размер ставки зависит от силы руки и текстуры доски
        val boardTexture = analyzeBoardTexture(communityCards)
        
        return when {
            // Монстр руки (фулл-хаус и выше) - большая ставка
            handRank >= 0.7 -> BettingRecommendation(
                action = if (currentBet == 0) PlayerAction.BET else PlayerAction.RAISE,
                suggestedAmount = if (currentBet == 0) (pot * 0.75).toInt() else currentBet * 2,
                confidence = 0.95f,
                reasoning = "Монстр рука - максимальная ценность",
                equity = equity
            )
            
            // Сильные руки (пара+ или стрит/флеш) - средняя ставка
            handRank >= 0.5 -> {
                val betSize = when {
                    boardTexture.isWet -> (pot * 0.5).toInt() // Влажная доска - защита
                    else -> (pot * 0.33).toInt() // Сухая доска - ценность
                }
                
                if (currentBet == 0) {
                    BettingRecommendation(
                        action = PlayerAction.BET,
                        suggestedAmount = betSize,
                        confidence = 0.8f,
                        reasoning = "Сильная рука - извлечение ценности",
                        equity = equity
                    )
                } else if (potOdds < equity ?: 0.0) {
                    BettingRecommendation(
                        action = PlayerAction.CALL,
                        suggestedAmount = callAmount,
                        confidence = 0.7f,
                        reasoning = "Положительные pot odds",
                        potOdds = potOdds,
                        equity = equity
                    )
                } else {
                    BettingRecommendation(
                        action = PlayerAction.FOLD,
                        suggestedAmount = 0,
                        confidence = 0.75f,
                        reasoning = "Отрицательные pot odds",
                        potOdds = potOdds,
                        equity = equity
                    )
                }
            }
            
            // Блеф на влажной доске
            handRank < 0.3 && boardTexture.isWet && position >= 1 -> {
                BettingRecommendation(
                    action = if (currentBet == 0) PlayerAction.BET else PlayerAction.FOLD,
                    suggestedAmount = if (currentBet == 0) (pot * 0.4).toInt() else 0,
                    confidence = 0.5f,
                    reasoning = "Блеф на влажной доске",
                    equity = equity
                )
            }
            
            // Слабые руки - фолд
            else -> BettingRecommendation(
                action = PlayerAction.FOLD,
                suggestedAmount = 0,
                confidence = 0.8f,
                reasoning = "Слабая рука - фолд",
                potOdds = potOdds,
                equity = equity
            )
        }
    }
    
    /**
     * Оценивает силу руки (0.0 - 1.0)
     */
    private fun evaluateHandStrength(hand: List<Card>, communityCards: List<Card>): Float {
        if (hand.isEmpty()) return 0f
        
        return if (communityCards.isEmpty()) {
            // Pre-flop оценка
            evaluatePreFlopStrength(hand)
        } else {
            // Post-flop оценка
            val evaluation = evaluator.evaluateBestHand(hand, communityCards)
            evaluation.rank.value / 10.0f
        }
    }
    
    private fun evaluatePreFlopStrength(hand: List<Card>): Float {
        if (hand.size != 2) return 0f
        
        val card1 = hand[0]
        val card2 = hand[1]
        
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
    
    /**
     * Рассчитывает pot odds
     */
    private fun calculatePotOdds(pot: Int, currentBet: Int, playerBet: Int): Double {
        val callAmount = currentBet - playerBet
        if (callAmount <= 0) return 0.0
        return callAmount.toDouble() / (pot + callAmount)
    }
    
    /**
     * Оценивает equity (вероятность выигрыша)
     */
    private fun calculateEquity(hand: List<Card>, communityCards: List<Card>, activePlayers: Int): Double {
        if (hand.isEmpty() || communityCards.isEmpty()) return 0.5
        
        val evaluation = evaluator.evaluateBestHand(hand, communityCards)
        val baseEquity = evaluation.rank.value / 10.0
        
        // Корректировка на количество игроков
        return when (activePlayers) {
            2 -> baseEquity
            3 -> baseEquity * 0.85
            4 -> baseEquity * 0.75
            else -> baseEquity * 0.65
        }
    }
    
    /**
     * Анализирует текстуру доски
     */
    private fun analyzeBoardTexture(cards: List<Card>): BoardTexture {
        if (cards.isEmpty()) return BoardTexture.DRY
        
        val ranks = cards.map { it.rank.value }.sorted()
        val suits = cards.groupingBy { it.suit }.eachCount()
        
        val isWet = suits.size == 1 || // Флеш-дро
                ranks.zipWithNext().any { it.second - it.first <= 1 } || // Стрит-дро
                ranks.groupingBy { it }.eachCount().values.any { it >= 2 } // Пара на доске
        
        return if (isWet) BoardTexture.WET else BoardTexture.DRY
    }
    
    private data class BoardTexture(val isWet: Boolean) {
        companion object {
            val WET = BoardTexture(true)
            val DRY = BoardTexture(false)
        }
    }
}

