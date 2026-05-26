package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class TimerState { IDLE, RUNNING, PAUSED, BREAK, ACTIVE_RECALL }

class TimerViewModel : ViewModel() {
    private val _timerState = MutableStateFlow(TimerState.IDLE)
    val timerState: StateFlow<TimerState> = _timerState.asStateFlow()

    private val FOCUS_TIME = 25 * 60
    private val BREAK_TIME = 5 * 60

    private val _remainingSeconds = MutableStateFlow(FOCUS_TIME)
    val remainingSeconds: StateFlow<Int> = _remainingSeconds.asStateFlow()

    private var timerJob: Job? = null

    fun startFocusTimer() {
        if (_timerState.value == TimerState.IDLE || _timerState.value == TimerState.PAUSED) {
            _timerState.value = TimerState.RUNNING
            startCountdown()
        }
    }

    fun pauseTimer() {
        if (_timerState.value == TimerState.RUNNING) {
            _timerState.value = TimerState.PAUSED
            timerJob?.cancel()
        }
    }

    fun resetTimer() {
        timerJob?.cancel()
        _timerState.value = TimerState.IDLE
        _remainingSeconds.value = FOCUS_TIME
    }

    private fun startCountdown() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (_remainingSeconds.value > 0) {
                delay(1000L)
                _remainingSeconds.value -= 1
            }
            // Timer finished
            if (_timerState.value == TimerState.RUNNING) {
                // Focus session ended, time for active recall
                _timerState.value = TimerState.ACTIVE_RECALL
            } else if (_timerState.value == TimerState.BREAK) {
                // Break ended, back to idle focus
                resetTimer()
            }
        }
    }

    fun finishActiveRecall(summary: String) {
        // We can save 'summary' to DB here in a later iteration.
        // For now, proceed to break.
        _timerState.value = TimerState.BREAK
        _remainingSeconds.value = BREAK_TIME
        startCountdown()
    }
}
