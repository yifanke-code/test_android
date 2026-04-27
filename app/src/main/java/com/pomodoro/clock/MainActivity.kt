package com.pomodoro.clock

import android.os.Bundle
import android.os.CountDownTimer
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar

class MainActivity : AppCompatActivity() {

    private lateinit var timerText: TextView
    private lateinit var startButton: Button
    private lateinit var resetButton: Button
    private lateinit var sessionTypeText: TextView

    private var countDownTimer: CountDownTimer? = null
    private var isTimerRunning = false
    private var remainingTimeMillis: Long = 0L

    private var workDurationMillis: Long = 25 * 60 * 1000L
    private var shortBreakMillis: Long = 5 * 60 * 1000L
    private var longBreakMillis: Long = 15 * 60 * 1000L

    private var currentSessionType: SessionType = SessionType.WORK
    private var completedPomodoros: Int = 0

    enum class SessionType {
        WORK, SHORT_BREAK, LONG_BREAK
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        timerText = findViewById(R.id.timerText)
        startButton = findViewById(R.id.startButton)
        resetButton = findViewById(R.id.resetButton)
        sessionTypeText = findViewById(R.id.sessionTypeText)

        updateTimerDisplay(workDurationMillis)
        updateSessionTypeDisplay()

        startButton.setOnClickListener {
            if (isTimerRunning) {
                pauseTimer()
            } else {
                startTimer()
            }
        }

        resetButton.setOnClickListener {
            resetTimer()
        }
    }

    private fun startTimer() {
        if (remainingTimeMillis == 0L) {
            remainingTimeMillis = when (currentSessionType) {
                SessionType.WORK -> workDurationMillis
                SessionType.SHORT_BREAK -> shortBreakMillis
                SessionType.LONG_BREAK -> longBreakMillis
            }
        }

        countDownTimer = object : CountDownTimer(remainingTimeMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                remainingTimeMillis = millisUntilFinished
                updateTimerDisplay(millisUntilFinished)
            }

            override fun onFinish() {
                isTimerRunning = false
                updateStartButton()
                onTimerFinished()
            }
        }.start()

        isTimerRunning = true
        updateStartButton()
    }

    private fun pauseTimer() {
        countDownTimer?.cancel()
        isTimerRunning = false
        updateStartButton()
    }

    private fun resetTimer() {
        countDownTimer?.cancel()
        isTimerRunning = false
        remainingTimeMillis = 0L

        currentSessionType = SessionType.WORK
        completedPomodoros = 0

        updateTimerDisplay(workDurationMillis)
        updateSessionTypeDisplay()
        updateStartButton()
    }

    private fun onTimerFinished() {
        when (currentSessionType) {
            SessionType.WORK -> {
                completedPomodoros++
                val message = "Work session complete! Great job!"
                Snackbar.make(timerText, message, Snackbar.LENGTH_LONG).show()

                currentSessionType = if (completedPomodoros % 4 == 0) {
                    SessionType.LONG_BREAK
                } else {
                    SessionType.SHORT_BREAK
                }
                remainingTimeMillis = 0L
                updateTimerDisplay(
                    when (currentSessionType) {
                        SessionType.LONG_BREAK -> longBreakMillis
                        SessionType.SHORT_BREAK -> shortBreakMillis
                        else -> workDurationMillis
                    }
                )
            }
            SessionType.SHORT_BREAK, SessionType.LONG_BREAK -> {
                Snackbar.make(timerText, "Break over! Ready to work?", Snackbar.LENGTH_LONG).show()
                currentSessionType = SessionType.WORK
                remainingTimeMillis = 0L
                updateTimerDisplay(workDurationMillis)
            }
        }
        updateSessionTypeDisplay()
    }

    private fun updateTimerDisplay(millisUntilFinished: Long) {
        val minutes = (millisUntilFinished / 1000) / 60
        val seconds = (millisUntilFinished / 1000) % 60
        timerText.text = String.format("%02d:%02d", minutes, seconds)
    }

    private fun updateSessionTypeDisplay() {
        sessionTypeText.text = when (currentSessionType) {
            SessionType.WORK -> "Work Session"
            SessionType.SHORT_BREAK -> "Short Break"
            SessionType.LONG_BREAK -> "Long Break"
        }
    }

    private fun updateStartButton() {
        startButton.text = if (isTimerRunning) "Pause" else "Start"
    }

    override fun onDestroy() {
        super.onDestroy()
        countDownTimer?.cancel()
    }
}