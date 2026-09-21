package com.example.myapplication

import android.app.Activity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.databinding.ActivityMainBinding // Adjust package prefix if needed

private const val KEY_INDEX = "index"
private const val KEY_SCORE = "score"
private const val KEY_ANSWERED = "answered"
private const val KEY_CHEATER_LIST = "cheater_list"

data class Question(val textResId: Int, val answer: Boolean, var isCheater: Boolean = false)

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val questionBank = listOf(
        Question(R.string.question_australia, true),
        Question(R.string.question_oceans, true),
        Question(R.string.question_mideast, false),
        Question(R.string.question_africa, true),
        Question(R.string.question_americas, false),
        Question(R.string.question_asia, true)
    )

    private var currentIndex = 0
    private var correctAnswersCount = 0
    private var answeredCount = 0

    private val cheatLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data
            val didCheat = data?.getBooleanExtra(EXTRA_ANSWER_SHOWN, false) ?: false
            if (didCheat) {
                questionBank[currentIndex].isCheater = true
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Handles and retains phone orientation switches seamlessly
        if (savedInstanceState != null) {
            currentIndex = savedInstanceState.getInt(KEY_INDEX, 0)
            correctAnswersCount = savedInstanceState.getInt(KEY_SCORE, 0)
            answeredCount = savedInstanceState.getInt(KEY_ANSWERED, 0)
            val cheatArray = savedInstanceState.getBooleanArray(KEY_CHEATER_LIST)
            if (cheatArray != null) {
                for (i in cheatArray.indices) {
                    questionBank[i].isCheater = cheatArray[i]
                }
            }
        }

        binding.trueButton.setOnClickListener { checkAnswer(true) }
        binding.falseButton.setOnClickListener { checkAnswer(false) }

        binding.nextButton.setOnClickListener {
            currentIndex = (currentIndex + 1) % questionBank.size
            updateQuestion()
        }

        binding.cheatButton.setOnClickListener {
            val answerIsTrue = questionBank[currentIndex].answer
            val intent = CheatActivity.newIntent(this@MainActivity, answerIsTrue)
            cheatLauncher.launch(intent)
        }

        updateQuestion()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(KEY_INDEX, currentIndex)
        outState.putInt(KEY_SCORE, correctAnswersCount)
        outState.putInt(KEY_ANSWERED, answeredCount)
        val cheatArray = questionBank.map { it.isCheater }.toBooleanArray()
        outState.putBooleanArray(KEY_CHEATER_LIST, cheatArray)
    }

    private fun updateQuestion() {
        val questionTextResId = questionBank[currentIndex].textResId
        binding.questionTextView.setText(questionTextResId)
    }

    private fun checkAnswer(userAnswer: Boolean) {
        val correctAnswer = questionBank[currentIndex].answer

        val messageResId = when {
            questionBank[currentIndex].isCheater -> R.string.judgment_toast
            userAnswer == correctAnswer -> {
                correctAnswersCount++
                R.string.correct_toast
            }
            else -> R.string.incorrect_toast
        }

        Toast.makeText(this, messageResId, Toast.LENGTH_SHORT).show()

        answeredCount++
        if (answeredCount >= questionBank.size) {
            val percentScore = (correctAnswersCount.toDouble() / questionBank.size * 100).toInt()
            Toast.makeText(this, "Quiz Finished! Your Final Score is: $percentScore%", Toast.LENGTH_LONG).show()
            answeredCount = 0
            correctAnswersCount = 0
        }
    }
}
