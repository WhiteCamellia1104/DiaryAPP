package com.example.diarytest.Activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.alibaba.dashscope.aigc.generation.Generation
import com.alibaba.dashscope.aigc.generation.GenerationParam
import com.alibaba.dashscope.common.Message
import com.alibaba.dashscope.common.Role
import com.example.diarytest.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext



class AiFunctionActivity : AppCompatActivity() {
    private lateinit var inputText: EditText
    private lateinit var outputText: TextView
    private lateinit var generateButton: Button
    private lateinit var editButton: Button
    private var generatedContent: String = ""
    private val scope = CoroutineScope(Dispatchers.Main + Job())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ai_function)

        inputText = findViewById(R.id.inputText)
        outputText = findViewById(R.id.outputText)
        generateButton = findViewById(R.id.generateButton)
        editButton = findViewById(R.id.editButton)

        generateButton.setOnClickListener {
            val userInput = inputText.text.toString()
            if (userInput.isNotEmpty()) {
                generateDiary(userInput)
            } else {
                Toast.makeText(this, "Please enter today's events", Toast.LENGTH_SHORT).show()
            }
        }

        editButton.setOnClickListener {
            val intent = Intent(this, AddDiaryActivity::class.java).apply {
                putExtra("GENERATED_CONTENT", generatedContent)
                putExtra("GENERATED_TITLE", "Summary of the day")
            }
            startActivity(intent)
        }
    }

    private fun generateDiary(events: String) {
        scope.launch {
            try {
                generateButton.isEnabled = false
                val prompt = "Please create a short diary summary based on the following: 1. Language should be lively 2. 3. Write 100 words or less. content：$events"

                val result = withContext(Dispatchers.IO) {
                    callQianwen(prompt)
                }
                generatedContent = result
                outputText.text = result
                editButton.isEnabled = true
                generateButton.isEnabled = true
            } catch (e: Exception) {
                generateButton.isEnabled = true
            }
        }
    }

    private fun callQianwen(text: String): String {
        try {
            val gen = com.alibaba.dashscope.aigc.generation.Generation()
            val systemMsg = Message.builder()
                .role(Role.SYSTEM.getValue())
                .content("You're a good diary keeper。")
                .build()

            val userMsg = Message.builder()
                .role(Role.USER.getValue())
                .content(text)
                .build()

            val param = GenerationParam.builder()
                .apiKey("sk-71897c83444546cf930af8f9e42ce2d5")
                .model("qwen-plus")
                .messages(listOf(systemMsg, userMsg))
                .resultFormat(GenerationParam.ResultFormat.MESSAGE)
                .build()

            val result = gen.call(param)
            return result.output.choices[0].message.content

        } catch (e: Exception) {
            Log.e("AiFunctionActivity", "Error calling AI: ${e.message}")
            throw e
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }
}