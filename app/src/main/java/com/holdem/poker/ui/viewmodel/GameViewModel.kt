package com.holdem.poker.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.holdem.poker.engine.GameEngine
import com.holdem.poker.engine.PokerHandEvaluator
import com.holdem.poker.model.*
import com.holdem.poker.strategy.BettingHelper
import com.holdem.poker.strategy.RangeAnalyzer
import com.holdem.poker.util.WhileUiSubscribed
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class GameViewModel : ViewModel() {
    private val gameEngine = GameEngine(smallBlind = 10, bigBlind = 20)
    private val evaluator = PokerHandEvaluator()
    private val ai = com.holdem.poker.ai.SimpleAI(evaluator)
    private val bettingHelper = BettingHelper(evaluator)
    private val rangeAnalyzer = RangeAnalyzer()
    
    // История действий оппонентов для анализа диапазона
    private val opponentActionsHistory = mutableMapOf<String, MutableList<RangeAnalyzer.OpponentAction>>()
    
    // Внутренние StateFlow для отдельных частей состояния
    private val _players = MutableStateFlow<List<Player>>(emptyList())
    private val _gameState = MutableStateFlow(GameState.WAITING)
    private val _communityCards = MutableStateFlow<List<Card>>(emptyList())
    private val _pot = MutableStateFlow(0)
    private val _currentPlayerIndex = MutableStateFlow(0)
    private val _currentBet = MutableStateFlow(0)
    private val _smallBlind = MutableStateFlow(10)
    private val _bigBlind = MutableStateFlow(20)
    private val _bettingRecommendation = MutableStateFlow<BettingHelper.BettingRecommendation?>(null)
    private val _opponentRanges = MutableStateFlow<Map<String, RangeAnalyzer.HandRange>>(emptyMap())
    private val _rangeStrengths = MutableStateFlow<Map<String, RangeAnalyzer.RangeStrength>>(emptyMap())
    private val _message = MutableStateFlow<String?>(null)
    private val _isLoading = MutableStateFlow(false)
    private val _error = MutableStateFlow<String?>(null)
    
    // Единое состояние UI с оптимизацией WhileUiSubscribed
    val uiState: StateFlow<GameUiState> = combine(
        _players,
        _gameState,
        _communityCards,
        _pot,
        _currentPlayerIndex,
        _currentBet,
        _smallBlind,
        _bigBlind,
        _bettingRecommendation,
        _opponentRanges,
        _rangeStrengths,
        _message,
        _isLoading,
        _error
    ) { players, gameState, communityCards, pot, currentPlayerIndex, 
        currentBet, smallBlind, bigBlind, bettingRecommendation, 
        opponentRanges, rangeStrengths, message, isLoading, error ->
        GameUiState(
            players = players,
            gameState = gameState,
            communityCards = communityCards,
            pot = pot,
            currentPlayerIndex = currentPlayerIndex,
            currentBet = currentBet,
            smallBlind = smallBlind,
            bigBlind = bigBlind,
            bettingRecommendation = bettingRecommendation,
            opponentRanges = opponentRanges,
            rangeStrengths = rangeStrengths,
            message = message,
            isLoading = isLoading,
            error = error
        )
    }.stateIn(
        scope = viewModelScope,
        started = WhileUiSubscribed,
        initialValue = GameUiState()
    )
    
    // Обратная совместимость - оставляем отдельные StateFlow для постепенного перехода
    @Deprecated("Используйте uiState вместо отдельных StateFlow", ReplaceWith("uiState"))
    val players: StateFlow<List<Player>> = _players
    
    @Deprecated("Используйте uiState вместо отдельных StateFlow", ReplaceWith("uiState"))
    val gameState: StateFlow<GameState> = _gameState
    
    @Deprecated("Используйте uiState вместо отдельных StateFlow", ReplaceWith("uiState"))
    val communityCards: StateFlow<List<Card>> = _communityCards
    
    @Deprecated("Используйте uiState вместо отдельных StateFlow", ReplaceWith("uiState"))
    val pot: StateFlow<Int> = _pot
    
    @Deprecated("Используйте uiState вместо отдельных StateFlow", ReplaceWith("uiState"))
    val currentPlayerIndex: StateFlow<Int> = _currentPlayerIndex
    
    @Deprecated("Используйте uiState вместо отдельных StateFlow", ReplaceWith("uiState"))
    val currentBet: StateFlow<Int> = _currentBet
    
    @Deprecated("Используйте uiState вместо отдельных StateFlow", ReplaceWith("uiState"))
    val smallBlind: StateFlow<Int> = _smallBlind
    
    @Deprecated("Используйте uiState вместо отдельных StateFlow", ReplaceWith("uiState"))
    val bigBlind: StateFlow<Int> = _bigBlind
    
    @Deprecated("Используйте uiState вместо отдельных StateFlow", ReplaceWith("uiState"))
    val bettingRecommendation: StateFlow<BettingHelper.BettingRecommendation?> = _bettingRecommendation
    
    @Deprecated("Используйте uiState вместо отдельных StateFlow", ReplaceWith("uiState"))
    val opponentRanges: StateFlow<Map<String, RangeAnalyzer.HandRange>> = _opponentRanges
    
    @Deprecated("Используйте uiState вместо отдельных StateFlow", ReplaceWith("uiState"))
    val rangeStrengths: StateFlow<Map<String, RangeAnalyzer.RangeStrength>> = _rangeStrengths
    
    init {
        startNewGame()
    }
    
    fun startNewGame() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val newPlayers = listOf(
                    Player("player1", "Вы", 1000),
                    Player("ai1", "ИИ 1", 1000),
                    Player("ai2", "ИИ 2", 1000),
                    Player("ai3", "ИИ 3", 1000)
                )
                _players.value = newPlayers
                startNewHand()
            } catch (e: Exception) {
                _error.value = e.message ?: "Ошибка при создании новой игры"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun startNewHand() {
        viewModelScope.launch {
            try {
                // Очищаем историю действий при новой раздаче
                opponentActionsHistory.clear()
                gameEngine.startNewHand(_players.value)
                updateGameState()
            } catch (e: Exception) {
                _error.value = e.message ?: "Ошибка при начале новой раздачи"
            }
        }
    }
    
    fun playerAction(action: PlayerAction, betAmount: Int = 0) {
        viewModelScope.launch {
            val currentPlayer = _players.value.getOrNull(_currentPlayerIndex.value)
            if (currentPlayer?.id != "player1") return@launch
            
            try {
                val success = gameEngine.processPlayerAction(currentPlayer, action, betAmount)
                if (success) {
                    _players.update { it.toList() }
                    updateGameState()
                    processAITurns()
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Ошибка при выполнении действия"
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
                
                val currentPlayer = _players.value.getOrNull(_currentPlayerIndex.value)
                if (currentPlayer?.id == "player1") {
                    // Ход игрока
                    break
                }
                
                // Ход AI
                val aiAction = ai.makeDecision(
                    currentPlayer!!,
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
                _players.update { it.toList() }
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
    
    fun clearError() {
        _error.value = null
    }
    
    fun clearMessage() {
        _message.value = null
    }
}
