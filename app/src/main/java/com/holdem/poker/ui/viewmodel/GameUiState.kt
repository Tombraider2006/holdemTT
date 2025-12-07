package com.holdem.poker.ui.viewmodel

import com.holdem.poker.model.GameState
import com.holdem.poker.model.Player
import com.holdem.poker.model.Card
import com.holdem.poker.strategy.BettingHelper
import com.holdem.poker.strategy.RangeAnalyzer

/**
 * Единое состояние UI для игрового экрана
 * Использует паттерн UiState для атомарных обновлений состояния
 */
data class GameUiState(
    val players: List<Player> = emptyList(),
    val gameState: GameState = GameState.WAITING,
    val communityCards: List<Card> = emptyList(),
    val pot: Int = 0,
    val currentPlayerIndex: Int = 0,
    val currentBet: Int = 0,
    val smallBlind: Int = 10,
    val bigBlind: Int = 20,
    val bettingRecommendation: BettingHelper.BettingRecommendation? = null,
    val opponentRanges: Map<String, RangeAnalyzer.HandRange> = emptyMap(),
    val rangeStrengths: Map<String, RangeAnalyzer.RangeStrength> = emptyMap(),
    val message: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null
) {
    /**
     * Текущий игрок
     */
    val currentPlayer: Player?
        get() = players.getOrNull(currentPlayerIndex)
    
    /**
     * Является ли текущий игрок пользователем
     */
    val isPlayerTurn: Boolean
        get() {
            val player = currentPlayer
            return player?.id == "player1" && !player.isFolded
        }
    
    /**
     * Можно ли делать ставку
     */
    val canBet: Boolean
        get() = isPlayerTurn && gameState != GameState.SHOWDOWN && gameState != GameState.FINISHED
}

