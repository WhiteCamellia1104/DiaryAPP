package com.example.diarytest

import com.example.diarytest.Data.DiaryEntry
import org.junit.Test
import org.junit.Assert.*

class DiaryOperationsTest {
    
    @Test
    fun createDiary_withAllFields_isSuccessful() {
        val diaryEntry = DiaryEntry(
            title = "My First Diary",
            content = "Today was a great day!",
            date = System.currentTimeMillis().toString(),
            categoryId = 1,
            fontStyle = "bold",
            fontSize = 16,
            color = "#0000FF",
            isProtected = true,
            password = "secret123"
        )
        
        assertNotNull(diaryEntry)
        assertEquals("My First Diary", diaryEntry.title)
        assertEquals("Today was a great day!", diaryEntry.content)
        assertEquals("bold", diaryEntry.fontStyle)
        assertEquals(16, diaryEntry.fontSize)
        assertEquals("#0000FF", diaryEntry.color)
        assertTrue(diaryEntry.isProtected)
        assertEquals("secret123", diaryEntry.password)
    }

    @Test
    fun modifyDiary_contentAndFormatting_isSuccessful() {
        val originalDiary = DiaryEntry(
            id = 1,
            title = "Original Title",
            content = "Original content",
            date = System.currentTimeMillis().toString(),
            categoryId = 1
        )
        
        // Simulate diary modification
        val modifiedDiary = originalDiary.copy(
            title = "Updated Title",
            content = "Updated content",
            fontSize = 18,
            fontStyle = "italic",
            color = "#FF0000"
        )
        
        assertNotEquals(originalDiary.title, modifiedDiary.title)
        assertNotEquals(originalDiary.content, modifiedDiary.content)
        assertNotEquals(originalDiary.fontSize, modifiedDiary.fontSize)
        assertNotEquals(originalDiary.fontStyle, modifiedDiary.fontStyle)
        assertNotEquals(originalDiary.color, modifiedDiary.color)
    }
}