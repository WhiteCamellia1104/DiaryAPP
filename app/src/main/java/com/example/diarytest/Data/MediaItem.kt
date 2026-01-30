package com.example.diarytest.Data

data class MediaItem(
    val id: Long = 0,
    val diaryEntryId: Long, // Foreign key，point to id in DiaryEntry table
    val uri: String,
    val type: String // Media type: Image, video, file
)

