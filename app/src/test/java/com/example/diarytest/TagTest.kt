package com.example.diarytest

import com.example.diarytest.Data.Tag
import org.junit.Test
import org.junit.Assert.*

class TagTest {
    
    @Test
    fun createTag_isCorrect() {
        val tag = Tag(1, "Job")
        
        assertEquals(1L, tag.id)
        assertEquals("Job", tag.name)
    }
    
    @Test
    fun createTagWithDefaultId_isCorrect() {
        val tag = Tag(name = "Life")
        
        assertEquals(0L, tag.id)
        assertEquals("Life", tag.name)
    }
}