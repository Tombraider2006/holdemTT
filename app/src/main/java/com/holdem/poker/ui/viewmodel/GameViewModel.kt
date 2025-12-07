package com.holdem.poker.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.holdem.poker.engine.GameEngine
import com.holdem.poker.engine.PokerHandEvaluator
import com.holdem.poker.model.*
import com.holdem.poker.strategy.BettingHelper
import com.holdem.poker.strategy.RangeAnalyzer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class GameViewModel : ViewModel() {
    private val gameEngine = GameEngine(smallBlind = 10, bigBlind = 20)
    private val evaluator = PokerHandEvaluator()
    private val ai = com.holdem.poker.ai.SimpleAI(evaluator)
    private val bettingHelper = BettingHelper(evaluator)
    private val rangeAnalyzer = RangeAnalyzer()
    
    // История действий оппонентов для анализа диапазона
    private val opponentActionsHistory = mutableMapOf<String, MutableList<RangeAnalyzer.OpponentAction>>()
    
    private val _players = MutableStateFlow<List<Player>>(emptyList())
    val players: StateFlow<List<Player>> = _players.asStateFlow()
    
    private val _gameState = MutableStateFlow(GameState.WAITING)
    val gameState: StateFlow<GameState> = _gameState.asStateFlow()
    
    private val _communityCards = MutableStateFlow<List<Card>>(emptyList())
    val communityCards: StateFlow<List<Card>> = _communityCards.asStateFlow()
    
    private val _pot = MutableStateFlow(0)
    val pot: StateFlow<Int> = _pot.asStateFlow()
    
    private val _currentPlayerIndex = MutableStateFlow(0)
    val currentPlayerIndex: StateFlow<Int> = _currentPlayerIndex.asStateFlow()
    
    private val _currentBet = MutableStateFlow(0)
    val currentBet: StateFlow<Int> = _currentBet.asStateFlow()
    
    private val _smallBlind = MutableStateFlow(10)
    val smallBlind: StateFlow<Int> = _smallBlind.asStateFlow()
    
    private val _bigBlind = MutableStateFlow(20)
    val bigBlind: StateFlow<Int> = _bigBlind.asStateFlow()
    
    private val _bettingRecommendation = MutableStateFlow<BettingHelper.BettingRecommendation?>(null)
    val bettingRecommendation: StateFlow<BettingHelper.BettingRecommendation?> = _bettingRecommendation.asStateFlow()
    
    private val _opponentRanges = MutableStateFlow<Map<String, RangeAnalyzer.HandRange>>(emptyMap())
    val opponentRanges: StateFlow<Map<String, RangeAnalyzer.HandRange>> = _opponentRanges.asStateFlow()
    
    private val _rangeStrengths = MutableStateFlow<Map<String, RangeAnalyzer.RangeStrength>>(emptyMap())
    val rangeStrengths: StateFlow<Map<String, RangeAnalyzer.RangeStrength>> = _rangeStrengths.asStateFlow()
    
    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()
    
    init {
        startNewGame()
    }
    
    fun startNewGame() {
        viewModelScope.launch {
            val newPlayers = listOf(
                Player("player1", "Вы", 1000),
                Player("ai1", "ИИ 1", 1000),
                Player("ai2", "ИИ 2", 1000),
                Player("ai3", "ИИ 3", 1000)
            )
            _players.value = newPlayers
            startNewHand()
        }
    }
    
    fun startNewHand() {
        viewModelScope.launch {
            // Очищаем историю действий при новой раздаче
            opponentActionsHistory.clear()
            gameEngine.startNewHand(_players.value)
            updateGameState()
        }
    }
    
    fun playerAction(action: PlayerAction, betAmount: Int = 0) {
        viewModelScope.launch {
            val currentPlayer = _players.value[_currentPlayerIndex.value]
            if (currentPlayer.id != "player1") return@launch
            
            val success = gameEngine.processPlayerAction(currentPlayer, action, betAmount)
            if (success) {
                _players.value = _players.value.toList()
                updateGameState()
                processAITurns()
            }
        }
    }
    
    private fun processAITurns() {
        viewModelScope.launch {
            while (true) {
                if (gameEngine.allPlayersActed(_players.value)) {
                    // Все сделали ставки, переходим к следующему этапу
                    if (_gameState.value != GameState.SHOWDOWN && _gameState.value != GameState.FINISHED) {
                        gameEngine.nextStage(_players.value)
                        updateGameState()
                    }
                    break
                }
                
                val currentPlayer = _players.value[_currentPlayerIndex.value]
                if (currentPlayer.id == "player1") {
                    // Ход игрока
                    break
                }
                
                // Ход AI
                val aiAction = ai.makeDecision(
                    currentPlayer,
                    _communityCards.value,
                    gameEngine.currentBet,
                    _pot.value
                )
                
                val betAmount = when (aiAction) {
                    PlayerAction.BET -> 50
                    PlayerAction.RAISE -> 100
                    else -> 0
                }
                
                gameEngine.processPlayerAction(currentPlayer, aiAction, betAmount)
                // Записываем действие AI для анализа диапазона
                recordOpponentAction(currentPlayer.id, aiAction, betAmount)
                _players.value = _players.value.toList()
                gameEngine.nextPlayer(_players.value)
                _currentPlayerIndex.value = gameEngine.currentPlayerIndex
                
                // Небольшая задержка для визуализации
                kotlinx.coroutines.delay(1000)
            }
        }
    }
    
    private fun updateGameState() {
        _gameState.value = gameEngine.gameState
        _communityCards.value = gameEngine.communityCards
        _pot.value = gameEngine.pot
        _currentBet.value = gameEngine.currentBet
        _currentPlayerIndex.value = gameEngine.currentPlayerIndex
        
        // Обновляем подсказки по ставкам для текущего игрока
        updateBettingRecommendation()
        
        // Анализируем диапазоны оппонентов
        analyzeOpponentRanges()
    }
    
    private fun updateBettingRecommendation() {
        val currentPlayer = _players.value.getOrNull(_currentPlayerIndex.value) ?: return
        if (currentPlayer.id != "player1" || currentPlayer.isFolded) {
            _bettingRecommendation.value = null
            return
        }
        
        val position = calculatePosition(_currentPlayerIndex.value, _players.value.size)
        val activePlayers = _players.value.count { !it.isFolded && it.isActive }
        
        val recommendation = bettingHelper.getBettingRecommendation(
            player = currentPlayer,
            communityCards = _communityCards.value,
            pot = _pot.value,
            currentBet = _currentBet.value,
            position = position,
            activePlayers = activePlayers
        )
        
        _bettingRecommendation.value = recommendation
    }
    
    private fun analyzeOpponentRanges() {
        val ranges = mutableMapOf<String, RangeAnalyzer.HandRange>()
        val strengths = mutableMapOf<String, RangeAnalyzer.RangeStrength>()
        
        _players.value.forEach { player ->
            if (player.id != "player1" && !player.isFolded) {
                val actions = opponentActionsHistory[player.id] ?: emptyList()
                if (actions.isNotEmpty()) {
                    val position = calculatePosition(
                        _players.value.indexOf(player),
                        _players.value.size
                    )
                    
                    val range = rangeAnalyzer.analyzeOpponentRange(
                        opponentActions = actions,
                        communityCards = _communityCards.value,
                        pot = _pot.value,
                        position = position
                    )
                    
                    val strength = rangeAnalyzer.evaluateRangeStrength(
                        range = range,
                        communityCards = _communityCards.value
                    )
                    
                    ranges[player.id] = range
                    strengths[player.id] = strength
                }
            }
        }
        
        _opponentRanges.value = ranges
        _rangeStrengths.value = strengths
    }
    
    private fun calculatePosition(playerIndex: Int, totalPlayers: Int): Int {
        // 0 = early, 1 = middle, 2 = late
        val positionRatio = playerIndex.toDouble() / totalPlayers
        return when {
            positionRatio < 0.33 -> 0 // Early
            positionRatio < 0.66 -> 1 // Middle
            else -> 2 // Late
        }
    }
    
    fun recordOpponentAction(playerId: String, action: PlayerAction, amount: Int) {
        val actionType = when (action) {
            PlayerAction.FOLD -> RangeAnalyzer.ActionType.FOLD
            PlayerAction.CHECK -> RangeAnalyzer.ActionType.CHECK
            PlayerAction.CALL -> RangeAnalyzer.ActionType.CALL
            PlayerAction.BET -> RangeAnalyzer.ActionType.BET
            PlayerAction.RAISE -> RangeAnalyzer.ActionType.RAISE
            PlayerAction.ALL_IN -> RangeAnalyzer.ActionType.ALL_IN
        }
        
        val history = opponentActionsHistory.getOrPut(playerId) { mutableListOf() }
        history.add(
            RangeAnalyzer.OpponentAction(
                type = actionType,
                amount = amount,
                stage = _gameState.value
            )
        )
        
        // Ограничиваем историю последними 10 действиями
        if (history.size > 10) {
            history.removeAt(0)
        }
    }
}

