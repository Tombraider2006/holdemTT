package com.holdem.poker.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.holdem.poker.audio.SoundManager
import com.holdem.poker.data.GameRepository
import com.holdem.poker.data.GameRepositoryImpl
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

class GameViewModel(
    private val repository: GameRepository = GameRepositoryImpl(
        gameEngine = GameEngine(smallBlind = 10, bigBlind = 20),
        evaluator = PokerHandEvaluator(),
        bettingHelper = BettingHelper(PokerHandEvaluator()),
        rangeAnalyzer = RangeAnalyzer()
    ),
    private val soundManager: SoundManager? = null
) : ViewModel() {
    private val evaluator = PokerHandEvaluator()
    private val ai = com.holdem.poker.ai.SimpleAI(evaluator)
    
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
    // Используем вложенные combine, так как combine поддерживает максимум 5-6 параметров
    val uiState: StateFlow<GameUiState> = combine(
        combine(
            _players,
            _gameState,
            _communityCards,
            _pot,
            _currentPlayerIndex
        ) { players, gameState, communityCards, pot, currentPlayerIndex ->
            Triple(Triple(players, gameState, communityCards), Pair(pot, currentPlayerIndex), Unit)
        },
        combine(
            _currentBet,
            _smallBlind,
            _bigBlind,
            _bettingRecommendation,
            _opponentRanges
        ) { currentBet, smallBlind, bigBlind, bettingRecommendation, opponentRanges ->
            Triple(currentBet, smallBlind, Pair(bigBlind, Pair(bettingRecommendation, opponentRanges)))
        },
        combine(
            _rangeStrengths,
            _message,
            _isLoading,
            _error
        ) { rangeStrengths, message, isLoading, error ->
            Pair(Pair(rangeStrengths, message), Pair(isLoading, error))
        }
    ) { gameData, bettingData, uiData ->
        val (players, gameState, communityCards) = gameData.first
        val (pot, currentPlayerIndex) = gameData.second
        val (currentBet, smallBlind, bigBlindData) = bettingData
        val (bigBlind, recommendationData) = bigBlindData
        val (bettingRecommendation, opponentRanges) = recommendationData
        val (rangeStrengths, message) = uiData.first
        val (isLoading, error) = uiData.second
        
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
                repository.startNewHand(_players.value)
                // Воспроизводим звук раздачи карт
                soundManager?.playCardDealSound()
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
                val success = repository.processPlayerAction(currentPlayer, action, betAmount)
                if (success) {
                    // Воспроизводим звук фишек при ставке
                    if (action == PlayerAction.BET || action == PlayerAction.RAISE || 
                        action == PlayerAction.CALL || action == PlayerAction.ALL_IN) {
                        soundManager?.playChipSound()
                    }
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
                if (repository.allPlayersActed(_players.value)) {
                    // Все сделали ставки, переходим к следующему этапу
                    if (_gameState.value != GameState.SHOWDOWN && _gameState.value != GameState.FINISHED) {
                        val previousState = _gameState.value
                        repository.nextStage(_players.value)
                        updateGameState()
                        
                        // Воспроизводим звук раздачи карт при переходе на новый этап
                        if (_gameState.value != previousState && 
                            (_gameState.value == GameState.FLOP || 
                             _gameState.value == GameState.TURN || 
                             _gameState.value == GameState.RIVER)) {
                            soundManager?.playCardDealSound()
                        }
                        
                        // Проверяем победу/поражение при showdown
                        if (_gameState.value == GameState.SHOWDOWN) {
                            checkWinLose()
                        }
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
                    repository.getCurrentBet(),
                    _pot.value
                )
                
                val betAmount = when (aiAction) {
                    PlayerAction.BET -> 50
                    PlayerAction.RAISE -> 100
                    else -> 0
                }
                
                repository.processPlayerAction(currentPlayer, aiAction, betAmount)
                // Записываем действие AI для анализа диапазона
                recordOpponentAction(currentPlayer.id, aiAction, betAmount)
                _players.update { it.toList() }
                repository.nextPlayer(_players.value)
                _currentPlayerIndex.value = repository.getCurrentPlayerIndex()
                
                // Небольшая задержка для визуализации
                kotlinx.coroutines.delay(1000)
            }
        }
    }
    
    private fun updateGameState() {
        _gameState.value = repository.getGameState()
        _communityCards.value = repository.getCommunityCards()
        _pot.value = repository.getPot()
        _currentBet.value = repository.getCurrentBet()
        _currentPlayerIndex.value = repository.getCurrentPlayerIndex()
        
        // Обновляем подсказки по ставкам для текущего игрока
        updateBettingRecommendation()
        
        // Анализируем диапазоны оппонентов
        analyzeOpponentRanges()
    }
    
    private fun updateBettingRecommendation() {
        viewModelScope.launch {
            val currentPlayer = _players.value.getOrNull(_currentPlayerIndex.value) ?: return@launch
            if (currentPlayer.id != "player1" || currentPlayer.isFolded) {
                _bettingRecommendation.value = null
                return@launch
            }
            
            val position = calculatePosition(_currentPlayerIndex.value, _players.value.size)
            val activePlayers = _players.value.count { !it.isFolded && it.isActive }
            
            val recommendation = repository.getBettingRecommendation(
                player = currentPlayer,
                communityCards = _communityCards.value,
                pot = _pot.value,
                currentBet = _currentBet.value,
                position = position,
                activePlayers = activePlayers
            )
            
            _bettingRecommendation.value = recommendation
        }
    }
    
    private fun analyzeOpponentRanges() {
        viewModelScope.launch {
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
                        
                        val range = repository.analyzeOpponentRange(
                            opponentActions = actions,
                            communityCards = _communityCards.value,
                            pot = _pot.value,
                            position = position
                        )
                        
                        val strength = repository.evaluateRangeStrength(
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
    
    /**
     * Проверяет победу или поражение игрока
     */
    private fun checkWinLose() {
        viewModelScope.launch {
            val player = _players.value.find { it.id == "player1" }
            if (player != null) {
                // Определяем, выиграл ли игрок
                // Это упрощенная проверка - в реальной игре нужно сравнивать с другими игроками
                val activePlayers = _players.value.filter { !it.isFolded && it.isActive }
                if (activePlayers.size == 1 && activePlayers[0].id == "player1") {
                    soundManager?.playWinSound()
                    _message.value = "🎉 Вы выиграли!"
                } else if (player.isFolded) {
                    soundManager?.playLoseSound()
                    _message.value = "😔 Вы проиграли"
                } else {
                    // Проверяем, увеличились ли фишки игрока (упрощенная проверка)
                    // В реальной игре нужно сравнивать руки
                    soundManager?.playWinSound()
                }
            }
        }
    }
}
