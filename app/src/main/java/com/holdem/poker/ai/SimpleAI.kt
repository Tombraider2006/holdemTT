package com.holdem.poker.ai

import com.holdem.poker.engine.PokerHandEvaluator
import com.holdem.poker.model.*

/**
 * Простой AI для игры в покер
 */
class SimpleAI(private val evaluator: PokerHandEvaluator) {
    
    /**
     * Принимает решение на основе текущей ситуации
     */
    fun makeDecision(
        player: Player,
        communityCards: List<Card>,
        currentBet: Int,
        pot: Int
    ): PlayerAction {
        // Если нет ставки, можно чекнуть
        if (currentBet == 0 || currentBet == player.currentBet) {
            return decideCheckOrBet(player, communityCards, pot)
        }
        
        // Есть ставка - нужно решить: фолд, колл или рейз
        return decideFoldCallRaise(player, communityCards, currentBet, pot)
    }
    
    private fun decideCheckOrBet(player: Player, communityCards: List<Card>, pot: Int): PlayerAction {
        if (communityCards.isEmpty()) {
            // Pre-flop: ставка на основе силы карт
            val handStrength = evaluatePreFlopHand(player.hand)
            return if (handStrength > 0.6) {
                PlayerAction.BET
            } else {
                PlayerAction.CHECK
            }
        }
        
        // Post-flop: оценка комбинации
        val evaluation = evaluator.evaluateBestHand(player.hand, communityCards)
        val handValue = evaluation.rank.value / 10.0
        
        return if (handValue > 0.5) {
            PlayerAction.BET
        } else {
            PlayerAction.CHECK
        }
    }
    
    private fun decideFoldCallRaise(
        player: Player,
        communityCards: List<Card>,
        currentBet: Int,
        pot: Int
    ): PlayerAction {
        val callAmount = currentBet - player.currentBet
        val potOdds = callAmount.toDouble() / (pot + callAmount)
        
        if (communityCards.isEmpty()) {
            // Pre-flop
            val handStrength = evaluatePreFlopHand(player.hand)
            return when {
                handStrength > 0.8 -> PlayerAction.RAISE
                handStrength > 0.5 && potOdds < 0.3 -> PlayerAction.CALL
                else -> PlayerAction.FOLD
            }
        }
        
        // Post-flop
        val evaluation = evaluator.evaluateBestHand(player.hand, communityCards)
        val handValue = evaluation.rank.value / 10.0
        
        return when {
            handValue > 0.7 -> PlayerAction.RAISE
            handValue > 0.4 && potOdds < 0.4 -> PlayerAction.CALL
            else -> PlayerAction.FOLD
        }
    }
    
    /**
     * Оценивает силу руки до флопа
     */
    private fun evaluatePreFlopHand(hand: List<Card>): Double {
        if (hand.size != 2) return 0.0
        
        val card1 = hand[0]
        val card2 = hand[1]
        
        // Пара
        if (card1.rank == card2.rank) {
            return when (card1.rank.value) {
                14 -> 0.95 // AA
                13 -> 0.90 // KK
                12 -> 0.85 // QQ
                11 -> 0.80 // JJ
                else -> 0.60 + (card1.rank.value - 2) * 0.02
            }
        }
        
        // Одинаковая масть
        val isSuited = card1.suit == card2.suit
        val highCard = maxOf(card1.rank.value, card2.rank.value)
        val lowCard = minOf(card1.rank.value, card2.rank.value)
        
        // Коннекторы
        val isConnector = (highCard - lowCard) <= 1
        
        var strength = 0.3
        if (isSuited) strength += 0.1
        if (isConnector) strength += 0.1
        strength += (highCard - 7) * 0.05
        
        return strength.coerceIn(0.0, 1.0)
    }
}

