package com.holdem.poker.engine

import com.holdem.poker.model.*

/**
 * Оценка покерных рук
 * Основано на логике из PokerServer
 */
class PokerHandEvaluator {
    
    /**
     * Оценивает лучшую комбинацию из 7 карт (2 карты игрока + 5 общих)
     */
    fun evaluateBestHand(playerCards: List<Card>, communityCards: List<Card>): HandEvaluation {
        val allCards = (playerCards + communityCards).toList()
        if (allCards.size < 5) {
            throw IllegalArgumentException("Нужно минимум 5 карт для оценки")
        }
        
        // Генерируем все возможные комбинации из 5 карт
        val combinations = generateCombinations(allCards, 5)
        
        // Оцениваем каждую комбинацию и находим лучшую
        return combinations.map { evaluateHand(it) }.maxOrNull() ?: 
            HandEvaluation(HandRank.HIGH_CARD, allCards.take(5))
    }
    
    /**
     * Оценивает комбинацию из 5 карт
     */
    fun evaluateHand(cards: List<Card>): HandEvaluation {
        if (cards.size != 5) {
            throw IllegalArgumentException("Нужно ровно 5 карт")
        }
        
        val sortedByRank = cards.sortedByDescending { it.rank.value }
        val rankCounts = cards.groupingBy { it.rank }.eachCount()
        val suitCounts = cards.groupingBy { it.suit }.eachCount()
        
        val isFlush = suitCounts.size == 1
        val isStraight = isStraight(sortedByRank)
        val isRoyal = isRoyalFlush(sortedByRank, isFlush)
        
        // Royal Flush
        if (isRoyal) {
            return HandEvaluation(HandRank.ROYAL_FLUSH, sortedByRank)
        }
        
        // Straight Flush
        if (isStraight && isFlush) {
            return HandEvaluation(HandRank.STRAIGHT_FLUSH, sortedByRank, listOf(sortedByRank[0].rank.value))
        }
        
        // Four of a Kind
        val fourOfAKind = rankCounts.entries.find { it.value == 4 }
        if (fourOfAKind != null) {
            val kicker = rankCounts.entries.find { it.value == 1 }?.key?.value ?: 0
            return HandEvaluation(HandRank.FOUR_OF_A_KIND, sortedByRank, listOf(fourOfAKind.key.value, kicker))
        }
        
        // Full House
        val threeOfAKind = rankCounts.entries.find { it.value == 3 }
        val pair = rankCounts.entries.find { it.value == 2 }
        if (threeOfAKind != null && pair != null) {
            return HandEvaluation(HandRank.FULL_HOUSE, sortedByRank, listOf(threeOfAKind.key.value, pair.key.value))
        }
        
        // Flush
        if (isFlush) {
            return HandEvaluation(HandRank.FLUSH, sortedByRank, sortedByRank.map { it.rank.value })
        }
        
        // Straight
        if (isStraight) {
            return HandEvaluation(HandRank.STRAIGHT, sortedByRank, listOf(sortedByRank[0].rank.value))
        }
        
        // Three of a Kind
        if (threeOfAKind != null) {
            val kickers = sortedByRank.filter { it.rank != threeOfAKind.key }
                .map { it.rank.value }
                .sortedDescending()
            return HandEvaluation(HandRank.THREE_OF_A_KIND, sortedByRank, listOf(threeOfAKind.key.value) + kickers)
        }
        
        // Two Pair
        val pairs = rankCounts.entries.filter { it.value == 2 }.sortedByDescending { it.key.value }
        if (pairs.size >= 2) {
            val kicker = sortedByRank.find { it.rank != pairs[0].key && it.rank != pairs[1].key }?.rank?.value ?: 0
            return HandEvaluation(HandRank.TWO_PAIR, sortedByRank, listOf(pairs[0].key.value, pairs[1].key.value, kicker))
        }
        
        // Pair
        if (pair != null) {
            val kickers = sortedByRank.filter { it.rank != pair.key }
                .map { it.rank.value }
                .sortedDescending()
            return HandEvaluation(HandRank.PAIR, sortedByRank, listOf(pair.key.value) + kickers)
        }
        
        // High Card
        return HandEvaluation(HandRank.HIGH_CARD, sortedByRank, sortedByRank.map { it.rank.value })
    }
    
    private fun isStraight(cards: List<Card>): Boolean {
        val ranks = cards.map { it.rank.value }.sorted()
        
        // Обычный стрит
        var isNormalStraight = true
        for (i in 1 until ranks.size) {
            if (ranks[i] != ranks[i - 1] + 1) {
                isNormalStraight = false
                break
            }
        }
        
        // Стрит с тузом как 1 (A-2-3-4-5)
        val isLowAceStraight = ranks == listOf(2, 3, 4, 5, 14)
        
        return isNormalStraight || isLowAceStraight
    }
    
    private fun isRoyalFlush(cards: List<Card>, isFlush: Boolean): Boolean {
        if (!isFlush) return false
        val ranks = cards.map { it.rank.value }.sorted()
        return ranks == listOf(10, 11, 12, 13, 14)
    }
    
    private fun generateCombinations(cards: List<Card>, size: Int): List<List<Card>> {
        if (size == 0) return listOf(emptyList())
        if (cards.isEmpty()) return emptyList()
        
        val result = mutableListOf<List<Card>>()
        for (i in cards.indices) {
            val first = cards[i]
            val rest = cards.subList(i + 1, cards.size)
            for (combo in generateCombinations(rest, size - 1)) {
                result.add(listOf(first) + combo)
            }
        }
        return result
    }
}

