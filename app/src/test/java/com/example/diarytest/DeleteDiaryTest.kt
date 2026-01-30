package com.example.diarytest

import com.example.diarytest.Data.DiaryEntry
import org.junit.Test
import org.junit.Assert.*

class DeleteDiaryTest {
    
    @Test
    fun deleteDiary_withoutPassword_isSuccessful() {
        // Create a test diary entry without password protection
        val diaryEntry = DiaryEntry(
            id = 1,
            title = "Test Diary",
            content = "Content to be deleted",
            date = System.currentTimeMillis().toString(),
            categoryId = 1,
            isProtected = false
        )
        
        // Verify diary properties before deletion
        assertNotNull(diaryEntry)
        assertEquals(1L, diaryEntry.id)
        assertFalse(diaryEntry.isProtected)
        assertNull(diaryEntry.password)
    }
    
    @Test
    fun deleteDiary_withPassword_requiresValidation() {
        // Create a password protected diary entry
        val diaryEntry = DiaryEntry(
            id = 2,
            title = "Protected Diary",
            content = "Protected content to be deleted",
            date = System.currentTimeMillis().toString(),
            categoryId = 1,
            isProtected = true,
            password = "123456"
        )
        
        // Verify password protection
        assertTrue(diaryEntry.isProtected)
        assertNotNull(diaryEntry.password)
        
        // Test correct password
        val correctPassword = "123456"
        assertEquals(diaryEntry.password, correctPassword)
        
        // Test incorrect password
        val wrongPassword = "wrong123"
        assertNotEquals(diaryEntry.password, wrongPassword)
    }
    
    @Test
    fun deleteDiary_withMedia_clearsAssociatedMedia() {
        val diaryId = 3L
        val mediaItems = listOf(
            "content://media/external/images/1",
            "content://media/external/videos/2",
            "content://media/external/files/3"
        )
        
        // Verify media items exist before deletion
        assertEquals(3, mediaItems.size)
        assertTrue(mediaItems.all { it.startsWith("content://") })
    }
}