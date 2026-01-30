package com.example.diarytest.Activity

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.diarytest.Adapter.MediaAdapter
import com.example.diarytest.Data.DiaryEntry
import com.example.diarytest.Data.MediaItem
import com.example.diarytest.Database.DiaryDatabaseHelper
import com.example.diarytest.R
import com.example.diarytest.utils.DateUtils.formatTimestamp

class DiaryDetailActivity : AppCompatActivity() {
    private lateinit var mediaRecyclerView: RecyclerView
    private lateinit var mediaAdapter: MediaAdapter
    private var mediaItemList = mutableListOf<MediaItem>()
    private lateinit var dbHelper: DiaryDatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_diary_detail)

        //Gets the passed diary ID and converts it to type Long
        val diaryId = intent.getLongExtra("DIARY_ID", -1)

        // Initializes the database helper
        dbHelper = DiaryDatabaseHelper(this)

        // Initialize RecyclerView
        mediaRecyclerView = findViewById(R.id.mediaRecyclerView)
        mediaRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        mediaAdapter = MediaAdapter(this, mediaItemList) { position ->
        }
        mediaRecyclerView.adapter = mediaAdapter

        // Check ID validity and load data
        if (diaryId != -1L) {
            loadDiaryDetails(diaryId)
        } else {
            showErrorMessage("Invalid ID")
        }
    }

    private fun loadDiaryDetails(diaryId: Long) {
        val diaryEntry = dbHelper.getDiaryById(diaryId)
        diaryEntry?.let {
            val titleTextView = findViewById<TextView>(R.id.diaryTitleTextView)
            val dateTextView = findViewById<TextView>(R.id.diaryDateTextView)
            val contentTextView = findViewById<TextView>(R.id.diaryContentTextView)

            titleTextView.text = it.title

            // Formatted date display
            val formattedDate = formatTimestamp(it.date)
            dateTextView.text = formattedDate

            // Set contents
            contentTextView.text = it.content

            // Apply font style
            when (it.fontStyle) {
                "bold" -> contentTextView.setTypeface(null, Typeface.BOLD)
                "italic" -> contentTextView.setTypeface(null, Typeface.ITALIC)
                else -> contentTextView.setTypeface(null, Typeface.NORMAL)
            }

            // Applied font size
            contentTextView.textSize = it.fontSize.toFloat()

            // Applied font color
            try {
                contentTextView.setTextColor(Color.parseColor(it.color))
            } catch (e: IllegalArgumentException) {
                Log.e("DiaryDetailActivity", "Invalid color code: ${it.color}")
                contentTextView.setTextColor(Color.BLACK)
            }

            // Loading media content
            mediaItemList.clear()
            mediaItemList.addAll(dbHelper.getMediaItemsForDiaryEntry(diaryId))
            mediaAdapter.notifyDataSetChanged()
            // Gets and displays the label
            val tags = dbHelper.getTagsForDiaryEntry(diaryId)
            if (tags.isNotEmpty()) {
                val tagsText = tags.joinToString(", ")  // Connect labels with commas
                findViewById<TextView>(R.id.diaryTagsTextView).text = "Tags: $tagsText"
            } else {
                findViewById<TextView>(R.id.diaryTagsTextView).text = "No tags"
            }
        }
    }

    private fun showErrorMessage(message: String) {
        findViewById<TextView>(R.id.diaryTitleTextView).text = message
    }


}

