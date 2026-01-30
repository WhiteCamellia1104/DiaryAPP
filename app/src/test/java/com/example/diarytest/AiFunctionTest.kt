package com.example.diarytest

import org.junit.Test
import org.junit.Assert.*

class AiFunctionTest {
    
    @Test
    fun validateAiPrompt_isCorrect() {
        val events = "Today I went to the park and had a picnic with friends."
        val prompt = "Please create a short diary summary based on the following: " +
                "1. Language should be lively " +
                "2. 3. Write 100 words or less. " +
                "content：$events"
        
        assertTrue(prompt.contains("Language should be lively"))
        assertTrue(prompt.contains("100 words or less"))
        assertTrue(prompt.contains(events))
    }

    @Test
    fun validateSystemMessage_isCorrect() {
        val systemMessage = "You're a good diary keeper。"
        
        assertNotNull(systemMessage)
        assertTrue(systemMessage.isNotEmpty())
        assertTrue(systemMessage.contains("diary keeper"))
    }
}