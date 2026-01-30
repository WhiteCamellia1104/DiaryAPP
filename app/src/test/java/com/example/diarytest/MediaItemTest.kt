package com.example.diarytest

import com.example.diarytest.Data.MediaItem
import org.junit.Test
import org.junit.Assert.*

class MediaItemTest {
    
    @Test
    fun createMediaItem_isCorrect() {
        val mediaItem = MediaItem(
            id = 1,
            diaryEntryId = 1,
            uri = "content://media/external/images/1",
            type = "Image"
        )
        
        assertEquals(1L, mediaItem.id)
        assertEquals(1L, mediaItem.diaryEntryId)
        assertEquals("content://media/external/images/1", mediaItem.uri)
        assertEquals("Image", mediaItem.type)
    }
}