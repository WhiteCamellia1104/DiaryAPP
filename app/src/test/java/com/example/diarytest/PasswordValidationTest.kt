package com.example.diarytest

import com.example.diarytest.Data.DiaryEntry
import org.junit.Test
import org.junit.Assert.*

class PasswordValidationTest {
    
    @Test
    fun validatePassword_correctPassword() {
        val diaryEntry = DiaryEntry(
            title = "Encrypted diary",
            content = "Encrypted Content",
            date = System.currentTimeMillis().toString(),
            categoryId = 1,
            isProtected = true,
            password = "123456"
        )
        
        val inputPassword = "123456"
        assertEquals(diaryEntry.password, inputPassword)
    }
    
    @Test
    fun validatePassword_incorrectPassword() {
        val diaryEntry = DiaryEntry(
            title = "Encrypted diary",
            content = "Encrypted Content",
            date = System.currentTimeMillis().toString(),
            categoryId = 1,
            isProtected = true,
            password = "123456"
        )
        
        val wrongPassword = "654321"
        assertNotEquals(diaryEntry.password, wrongPassword)
    }
}