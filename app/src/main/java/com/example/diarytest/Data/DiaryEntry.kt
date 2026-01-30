package com.example.diarytest.Data

import java.net.URL

data class DiaryEntry(
    val id: Long = 0,                // The default value is 0, indicating that the database automatically generates the ID
    val title: String,               // Journal title
    val content: String,             // Journal content
    val date: String,                // Create a date and store the date using String (the format is yyyy-MM-dd)
    val categoryId: Int,             // category ID
    val fontStyle: String = "normal",
    val fontSize: Int = 14,
    val color: String = "#000000", // Use hexadecimal color values
    val isProtected: Boolean = false, // Encrypted or not: The default value is not encrypted
    val password: String? = null,     // Password, no password by default
    val tags: List<String> = listOf() // Label list, default empty
)
