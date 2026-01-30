package com.example.diarytest

import com.example.diarytest.Data.DiaryEntry
import org.junit.Test
import org.junit.Assert.*

class DiaryEntryTest {
    
    @Test
    fun createDiaryEntry_isCorrect() {
        val diaryEntry = DiaryEntry(
            id = 1,
            title = "Test Diary",
            content = "Test Content",
            date = "1698047531000",
            categoryId = 1,
            fontStyle = "bold",
            fontSize = 16,
            color = "#FF0000",
            isProtected = true,
            password = "123456"
        )
        
        assertEquals(1L, diaryEntry.id)
        assertEquals("Test Diary", diaryEntry.title)
        assertEquals("Test Content", diaryEntry.content)
        assertEquals("1698047531000", diaryEntry.date)
        assertEquals(1, diaryEntry.categoryId)
        assertEquals("bold", diaryEntry.fontStyle)
        assertEquals(16, diaryEntry.fontSize)
        assertEquals("#FF0000", diaryEntry.color)
        assertTrue(diaryEntry.isProtected)
        assertEquals("123456", diaryEntry.password)
    }

    @Test
    fun defaultValues_areCorrect() {
        val diaryEntry = DiaryEntry(
            id = 1,
            title = "Test Diary",
            content = "Test Content",
            date = "1698047531000",
            categoryId = 1
        )
        
        assertEquals("normal", diaryEntry.fontStyle)
        assertEquals(14, diaryEntry.fontSize)
        assertEquals("#000000", diaryEntry.color)
        assertFalse(diaryEntry.isProtected)
        assertNull(diaryEntry.password)
    }
}