package com.example.diarytest

import com.example.diarytest.utils.DateUtils
import org.junit.Test
import org.junit.Assert.*
import java.text.SimpleDateFormat
import java.util.*

class DateUtilsTest {
    
    @Test
    fun formatTimestamp_validTimestamp_isCorrect() {
        val timestamp = "1698047531000" // 2023-10-23 14:25:31
        val formatted = DateUtils.formatTimestamp(timestamp)

        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        val expected = sdf.format(Date(timestamp.toLong()))
        
        assertEquals(expected, formatted)
    }
    
    @Test
    fun formatTimestamp_invalidTimestamp_returnsOriginal() {
        val invalidTimestamp = "invalid"
        val formatted = DateUtils.formatTimestamp(invalidTimestamp)
        assertEquals(invalidTimestamp, formatted)
    }
}