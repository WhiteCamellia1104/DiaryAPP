package com.example.diarytest.Adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.diarytest.Data.DiaryEntry
import com.example.diarytest.Database.DiaryDatabaseHelper
import com.example.diarytest.R
import com.example.diarytest.utils.DateUtils.formatTimestamp


class ViewDiaryAdapter(
    private var diaryEntriesWithCategories: List<Pair<DiaryEntry, String?>>,
    private val onItemClick: (DiaryEntry) -> Unit,
    private val onDeleteClick: (Long) -> Unit,
    private val onEditClick: (Long) -> Unit,
    private val context: Context
) : RecyclerView.Adapter<ViewDiaryAdapter.DiaryViewHolder>() {
    private var diaryEntries: List<Pair<DiaryEntry, String?>> = emptyList()

    private val dbHelper: DiaryDatabaseHelper = DiaryDatabaseHelper(context)

    class DiaryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titleTextView: TextView = itemView.findViewById(R.id.titleTextView)
        private val dateTextView: TextView = itemView.findViewById(R.id.dateTextView)
        private val categoryTextView: TextView = itemView.findViewById(R.id.categoryTextView)
        private val entryTextView: TextView = itemView.findViewById(R.id.entryTextView)
        private val viewDetailButton: Button = itemView.findViewById(R.id.viewDetailButton)
        private val deleteButton: Button = itemView.findViewById(R.id.deleteButton)
        private val editButton: Button = itemView.findViewById(R.id.editButton)
        private val tagsTextView: TextView = itemView.findViewById(R.id.tagsTextView)

        fun bind(diaryEntryWithCategory: Pair<DiaryEntry, String?>,
                 tags: List<String>,
                 onItemClick: (DiaryEntry) -> Unit,
                 onDeleteClick: (Long) -> Unit,
                 onEditClick: (Long) -> Unit) {
            val diaryEntry = diaryEntryWithCategory.first
            val category = diaryEntryWithCategory.second
            var diaryId = diaryEntry.id

            if (diaryEntry.isProtected) {
                titleTextView.text = "🔒 Encryped Diray"
                dateTextView.text = formatTimestamp(diaryEntry.date)
                categoryTextView.text = "Category：${category ?: "No Category"}"
                entryTextView.text = "******"
                tagsTextView.visibility = View.GONE
            } else {
                titleTextView.text = diaryEntry.title
                dateTextView.text = formatTimestamp(diaryEntry.date)
                categoryTextView.text = "Category：${category ?: "No Category"}"
                entryTextView.text = diaryEntry.content.take(100)
                tagsTextView.visibility = View.VISIBLE

                // Handle tags visibility and text
                if (tags.isNotEmpty()) {
                    tagsTextView.visibility = View.VISIBLE
                    val tagsString = tags.joinToString(", ")
                    tagsTextView.text = "Tags: $tagsString"
                } else {
                    tagsTextView.visibility = View.GONE // Hide if no tags
                }
            }

            viewDetailButton.setOnClickListener {
                onItemClick(diaryEntry)
            }

            deleteButton.setOnClickListener {
                onDeleteClick(diaryEntry.id)
            }

            editButton.setOnClickListener {
                onEditClick(diaryEntry.id)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DiaryViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_diary_entry, parent, false)
        return DiaryViewHolder(view)
    }

    override fun onBindViewHolder(holder: DiaryViewHolder, position: Int) {
        val diaryEntryWithCategory = diaryEntriesWithCategories[position]
        val tags = dbHelper.getTagsForDiaryEntry(diaryEntryWithCategory.first.id) // Gets the tag associated with the current journal entry

        holder.bind(diaryEntryWithCategory, tags, onItemClick, onDeleteClick, onEditClick)
    }


    override fun getItemCount(): Int = diaryEntriesWithCategories.size

    fun updateEntries(newEntriesWithCategories: List<Pair<DiaryEntry, String?>>) {
        diaryEntriesWithCategories = newEntriesWithCategories
        notifyDataSetChanged()
    }

    // Method to filter entries
    fun filterEntries(query: String) {
        val filteredList = if (query.isEmpty()) {
            diaryEntriesWithCategories
        } else {
            val dbHelper = DiaryDatabaseHelper(context)

            val filteredEntries = dbHelper.searchDiaryEntriesByTagTitleContent(query)

            // Apply the filtering logic (no additional filtering needed, as it's already handled in the query)
            filteredEntries
        }

        diaryEntriesWithCategories = filteredList
        notifyDataSetChanged()

        if (filteredList.isNotEmpty()) {
            Toast.makeText(context, "Search Successfully", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "No content found", Toast.LENGTH_SHORT).show()
        }
    }


    fun updateData(newDiaries: List<Pair<DiaryEntry, String?>>) {
        diaryEntriesWithCategories = newDiaries
        notifyDataSetChanged()
    }
}

