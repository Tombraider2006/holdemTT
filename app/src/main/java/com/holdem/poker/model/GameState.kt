package com.holdem.poker.model

/**
 * Состояние игры
 */
enum class GameState {
    WAITING,           // Ожидание начала
    PRE_FLOP,          // До флопа
    FLOP,              // После флопа (3 карты)
    TURN,              // После терна (4 карты)
    RIVER,             // После ривера (5 карт)
    SHOWDOWN,          // Вскрытие карт
    FINISHED            // Игра завершена
}

/**
 * Действие игрока
 */
enum class PlayerAction {
    FOLD,
    CHECK,
    CALL,
    BET,
    RAISE,
    ALL_IN
}

