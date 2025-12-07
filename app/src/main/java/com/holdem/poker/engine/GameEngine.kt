package com.holdem.poker.engine

import com.holdem.poker.model.*

/**
 * Игровой движок техасского холдема
 */
class GameEngine(
    private val smallBlind: Int = 10,
    private val bigBlind: Int = 20
) {
    private val deck = Deck()
    private val evaluator = PokerHandEvaluator()
    
    var players = mutableListOf<Player>()
        private set
    
    var gameState = GameState.WAITING
        private set
    
    var communityCards = mutableListOf<Card>()
        private set
    
    var pot = 0
        private set
    
    var currentBet = 0
    
    var currentPlayerIndex = 0
        private set
    
    private var dealerIndex = 0
    
    /**
     * Начинает новую раздачу
     */
    fun startNewHand(players: List<Player>) {
        if (players.size < 2) {
            throw IllegalArgumentException("Нужно минимум 2 игрока")
        }
        
        // Сохраняем игроков
        this.players.clear()
        this.players.addAll(players)
        
        // Сброс состояния
        deck.reset()
        communityCards.clear()
        pot = 0
        currentBet = 0
        gameState = GameState.PRE_FLOP
        
        // Сброс игроков
        players.forEach { it.resetForNewHand() }
        
        // Определяем дилера, малый и большой блайнды
        dealerIndex = (dealerIndex + 1) % players.size
        val smallBlindIndex = (dealerIndex + 1) % players.size
        val bigBlindIndex = (dealerIndex + 2) % players.size
        
        players[dealerIndex].isDealer = true
        players[smallBlindIndex].isSmallBlind = true
        players[bigBlindIndex].isBigBlind = true
        
        // Раздаем блайнды
        players[smallBlindIndex].bet(smallBlind)
        players[bigBlindIndex].bet(bigBlind)
        pot += smallBlind + bigBlind
        currentBet = bigBlind
        
        // Раздаем карты игрокам
        players.forEach { player ->
            if (player.isActive && !player.isFolded) {
                player.hand = deck.dealCards(2)
            }
        }
        
        currentPlayerIndex = (bigBlindIndex + 1) % players.size
    }
    
    /**
     * Обрабатывает действие игрока
     */
    fun processPlayerAction(player: Player, action: PlayerAction, betAmount: Int = 0): Boolean {
        when (action) {
            PlayerAction.FOLD -> {
                player.fold()
            }
            PlayerAction.CHECK -> {
                if (currentBet > player.currentBet) {
                    return false // Нельзя чекнуть, если есть ставка
                }
            }
            PlayerAction.CALL -> {
                val callAmount = currentBet - player.currentBet
                if (callAmount > 0) {
                    val betBefore = player.currentBet
                    if (callAmount > player.chips) {
                        player.bet(player.chips) // All-in
                    } else {
                        player.bet(callAmount)
                    }
                    pot += (player.currentBet - betBefore)
                }
            }
            PlayerAction.BET, PlayerAction.RAISE -> {
                val minBet = if (action == PlayerAction.RAISE) currentBet * 2 else bigBlind
                val actualBet = maxOf(betAmount, minBet)
                val totalNeeded = actualBet - player.currentBet
                
                if (totalNeeded > 0) {
                    val betBefore = player.currentBet
                    if (totalNeeded > player.chips) {
                        player.bet(player.chips) // All-in
                    } else {
                        player.bet(totalNeeded)
                    }
                    pot += (player.currentBet - betBefore)
                    currentBet = player.currentBet
                }
            }
            PlayerAction.ALL_IN -> {
                val betBefore = player.currentBet
                player.bet(player.chips)
                pot += (player.currentBet - betBefore)
                if (player.currentBet > currentBet) {
                    currentBet = player.currentBet
                }
            }
        }
        
        return true
    }
    
    /**
     * Переходит к следующему этапу игры
     */
    fun nextStage(players: List<Player>) {
        when (gameState) {
            GameState.PRE_FLOP -> {
                // Флоп - раздаем 3 карты
                communityCards.addAll(deck.dealCards(3))
                gameState = GameState.FLOP
            }
            GameState.FLOP -> {
                // Терн - раздаем 1 карту
                communityCards.add(deck.dealCard())
                gameState = GameState.TURN
            }
            GameState.TURN -> {
                // Ривер - раздаем последнюю карту
                communityCards.add(deck.dealCard())
                gameState = GameState.RIVER
            }
            GameState.RIVER -> {
                // Вскрытие карт
                gameState = GameState.SHOWDOWN
                determineWinner(players)
            }
            else -> {}
        }
        
        // Сбрасываем ставки игроков для нового раунда
        players.forEach { it.currentBet = 0 }
        currentBet = 0
    }
    
    /**
     * Определяет победителя
     */
    private fun determineWinner(players: List<Player>) {
        val activePlayers = players.filter { !it.isFolded && it.isActive }
        
        if (activePlayers.size == 1) {
            activePlayers[0].chips += pot
            pot = 0
            return
        }
        
        // Оцениваем руки всех активных игроков
        val evaluations = activePlayers.map { player ->
            player to evaluator.evaluateBestHand(player.hand, communityCards)
        }
        
        // Находим лучшую руку
        val bestEvaluation = evaluations.maxByOrNull { it.second }?.second
        val winners = evaluations.filter { it.second == bestEvaluation }.map { it.first }
        
        // Делим банк между победителями
        val winAmount = pot / winners.size
        winners.forEach { it.chips += winAmount }
        pot = 0
    }
    
    /**
     * Переходит к следующему игроку
     */
    fun nextPlayer(players: List<Player>) {
        do {
            currentPlayerIndex = (currentPlayerIndex + 1) % players.size
        } while (players[currentPlayerIndex].isFolded || !players[currentPlayerIndex].isActive)
    }
    
    /**
     * Проверяет, все ли игроки сделали ставки в текущем раунде
     */
    fun allPlayersActed(players: List<Player>): Boolean {
        val activePlayers = players.filter { !it.isFolded && it.isActive }
        if (activePlayers.isEmpty()) return true
        
        // Все игроки должны иметь одинаковую ставку или быть all-in
        val maxBet = activePlayers.maxOfOrNull { it.currentBet } ?: 0
        return activePlayers.all { 
            it.currentBet == maxBet || it.isAllIn 
        }
    }
}

