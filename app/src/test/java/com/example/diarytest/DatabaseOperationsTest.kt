package com.example.diarytest

import com.example.diarytest.Data.DiaryEntry
import com.example.diarytest.Data.MediaItem
import org.junit.Test
import org.junit.Assert.*

class DatabaseOperationsTest {
    
    @Test
    fun validateDiaryEntry_isValid() {
        val diaryEntry = DiaryEntry(
            title = "Test Diary",
            content = "Test Content",
            date = System.currentTimeMillis().toString(),
            categoryId = 1
        )
        
        assertNotNull(diaryEntry)
        assertTrue(diaryEntry.title.isNotEmpty())
        assertTrue(diaryEntry.content.isNotEmpty())
        assertTrue(diaryEntry.date.isNotEmpty())
        assertTrue(diaryEntry.categoryId > 0)
    }
    
    @Test
    fun validateMediaItem_isValid() {
        val mediaItem = MediaItem(
            diaryEntryId = 1,
            uri = "content://media/external/images/1",
            type = "Image"
        )
        
        assertTrue(mediaItem.diaryEntryId > 0)
        assertTrue(mediaItem.uri.startsWith("content://"))
        assertTrue(mediaItem.type in listOf("Image", "Video", "File"))
    }
}