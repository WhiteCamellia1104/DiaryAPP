package com.example.diarytest

import com.example.diarytest.Data.MediaItem
import org.junit.Test
import org.junit.Assert.*

class MediaTest {
    
    @Test
    fun createMediaItems_isCorrect() {
        val mediaItems = listOf(
            MediaItem(
                diaryEntryId = 1,
                uri = "content://media/external/images/1",
                type = "Image"
            ),
            MediaItem(
                diaryEntryId = 1,
                uri = "content://media/external/video/1",
                type = "Video"
            ),
            MediaItem(
                diaryEntryId = 1,
                uri = "content://media/external/file/1",
                type = "File"
            )
        )
        
        assertEquals(3, mediaItems.size)
        assertEquals("Image", mediaItems[0].type)
        assertEquals("Video", mediaItems[1].type)
        assertEquals("File", mediaItems[2].type)
    }
    
    @Test
    fun validateMediaUri_isCorrect() {
        val mediaItem = MediaItem(
            diaryEntryId = 1,
            uri = "content://media/external/images/1",
            type = "Image"
        )
        
        assertTrue(mediaItem.uri.startsWith("content://"))
        assertTrue(mediaItem.uri.contains("media"))
    }
}