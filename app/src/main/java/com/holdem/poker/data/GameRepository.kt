package com.holdem.poker.data

import com.holdem.poker.engine.GameEngine
import com.holdem.poker.engine.PokerHandEvaluator
import com.holdem.poker.model.*
import com.holdem.poker.strategy.BettingHelper
import com.holdem.poker.strategy.RangeAnalyzer
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Интерфейс репозитория для игровой логики
 * Единая точка доступа к данным игры
 */
interface GameRepository {
    /**
     * Получить текущее состояние игры
     */
    fun getGameState(): GameState
    
    /**
     * Получить список игроков
     */
    fun getPlayers(): List<Player>
    
    /**
     * Получить общие карты
     */
    fun getCommunityCards(): List<Card>
    
    /**
     * Получить размер банка
     */
    fun getPot(): Int
    
    /**
     * Получить текущую ставку
     */
    fun getCurrentBet(): Int
    
    /**
     * Получить индекс текущего игрока
     */
    fun getCurrentPlayerIndex(): Int
    
    /**
     * Начать новую игру
     */
    suspend fun startNewGame(players: List<Player>)
    
    /**
     * Начать новую раздачу
     */
    suspend fun startNewHand(players: List<Player>)
    
    /**
     * Обработать действие игрока
     */
    suspend fun processPlayerAction(player: Player, action: PlayerAction, betAmount: Int): Boolean
    
    /**
     * Перейти к следующему этапу игры
     */
    suspend fun nextStage(players: List<Player>)
    
    /**
     * Перейти к следующему игроку
     */
    suspend fun nextPlayer(players: List<Player>)
    
    /**
     * Проверить, все ли игроки сделали ставки
     */
    fun allPlayersActed(players: List<Player>): Boolean
    
    /**
     * Получить рекомендацию по ставке
     */
    suspend fun getBettingRecommendation(
        player: Player,
        communityCards: List<Card>,
        pot: Int,
        currentBet: Int,
        position: Int,
        activePlayers: Int
    ): BettingHelper.BettingRecommendation?
    
    /**
     * Проанализировать диапазон оппонента
     */
    suspend fun analyzeOpponentRange(
        opponentActions: List<RangeAnalyzer.OpponentAction>,
        communityCards: List<Card>,
        pot: Int,
        position: Int
    ): RangeAnalyzer.HandRange
    
    /**
     * Оценить силу диапазона оппонента
     */
    suspend fun evaluateRangeStrength(
        range: RangeAnalyzer.HandRange,
        communityCards: List<Card>
    ): RangeAnalyzer.RangeStrength
}

/**
 * Реализация GameRepository
 */
class GameRepositoryImpl(
    private val gameEngine: GameEngine,
    private val evaluator: PokerHandEvaluator,
    private val bettingHelper: BettingHelper,
    private val rangeAnalyzer: RangeAnalyzer
) : GameRepository {
    
    override fun getGameState(): GameState = gameEngine.gameState
    
    override fun getPlayers(): List<Player> = gameEngine.players
    
    override fun getCommunityCards(): List<Card> = gameEngine.communityCards
    
    override fun getPot(): Int = gameEngine.pot
    
    override fun getCurrentBet(): Int = gameEngine.currentBet
    
    override fun getCurrentPlayerIndex(): Int = gameEngine.currentPlayerIndex
    
    override suspend fun startNewGame(players: List<Player>) {
        // Игра уже инициализирована через GameEngine
        // Можно добавить дополнительную логику при необходимости
    }
    
    override suspend fun startNewHand(players: List<Player>) {
        gameEngine.startNewHand(players)
    }
    
    override suspend fun processPlayerAction(
        player: Player,
        action: PlayerAction,
        betAmount: Int
    ): Boolean {
        return gameEngine.processPlayerAction(player, action, betAmount)
    }
    
    override suspend fun nextStage(players: List<Player>) {
        gameEngine.nextStage(players)
    }
    
    override suspend fun nextPlayer(players: List<Player>) {
        gameEngine.nextPlayer(players)
    }
    
    override fun allPlayersActed(players: List<Player>): Boolean {
        return gameEngine.allPlayersActed(players)
    }
    
    override suspend fun getBettingRecommendation(
        player: Player,
        communityCards: List<Card>,
        pot: Int,
        currentBet: Int,
        position: Int,
        activePlayers: Int
    ): BettingHelper.BettingRecommendation? {
        return bettingHelper.getBettingRecommendation(
            player = player,
            communityCards = communityCards,
            pot = pot,
            currentBet = currentBet,
            position = position,
            activePlayers = activePlayers
        )
    }
    
    override suspend fun analyzeOpponentRange(
        opponentActions: List<RangeAnalyzer.OpponentAction>,
        communityCards: List<Card>,
        pot: Int,
        position: Int
    ): RangeAnalyzer.HandRange {
        return rangeAnalyzer.analyzeOpponentRange(
            opponentActions = opponentActions,
            communityCards = communityCards,
            pot = pot,
            position = position
        )
    }
    
    override suspend fun evaluateRangeStrength(
        range: RangeAnalyzer.HandRange,
        communityCards: List<Card>
    ): RangeAnalyzer.RangeStrength {
        return rangeAnalyzer.evaluateRangeStrength(
            range = range,
            communityCards = communityCards
        )
    }
}

