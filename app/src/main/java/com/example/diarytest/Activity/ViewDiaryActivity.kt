package com.example.diarytest.Activity
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.diarytest.Adapter.ViewDiaryAdapter
import com.example.diarytest.Data.DiaryEntry
import com.example.diarytest.Database.DiaryDatabaseHelper
import com.example.diarytest.R
import com.example.diarytest.utils.EncryptionUtils
import com.google.android.material.floatingactionbutton.FloatingActionButton


class ViewDiaryActivity : AppCompatActivity() {

    private lateinit var titleTextView: TextView
    private lateinit var searchEditText: EditText
    private lateinit var diaryRecyclerView: RecyclerView
    private lateinit var newEntryButton: FloatingActionButton
    private lateinit var searchButton: FloatingActionButton
    private lateinit var diaryList: List<DiaryEntry>
    private lateinit var dbHelper: DiaryDatabaseHelper
    private lateinit var diaryEntriesWithCategories: List<Pair<DiaryEntry, String?>>
    private lateinit var adapter: ViewDiaryAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_diary)

        dbHelper = DiaryDatabaseHelper(this)
        dbHelper.initializeCategories()
        diaryList = dbHelper.getAllDiaries()

        // Initialize view
        titleTextView = findViewById(R.id.titleTextView)
        searchEditText = findViewById(R.id.searchEditText)
        diaryRecyclerView = findViewById(R.id.diaryRecyclerView)
        newEntryButton = findViewById(R.id.newEntryButton)
        // Ensure that the data is retrieved from the database and initialized
        diaryEntriesWithCategories = dbHelper.getAllDiariesWithCategories()
        // Set RecyclerView
        Log.d("ViewDiaryActivity", "Loaded entries count: ${diaryEntriesWithCategories.size}")
        diaryEntriesWithCategories.forEach { (entry, category) ->
            Log.d("ViewDiaryActivity", "Entry ${entry.id} has category: $category")
        }
        diaryRecyclerView.layoutManager = LinearLayoutManager(this)

        // Initializes the adapter and passes the data with the class name
        adapter = ViewDiaryAdapter(diaryEntriesWithCategories,  { diaryEntry ->
            // View details of the click event
            if (diaryEntry.isProtected) {
                showPasswordDialog(diaryEntry) {
                    val intent = Intent(this, DiaryDetailActivity::class.java)
                    intent.putExtra("DIARY_ID", diaryEntry.id)
                    startActivity(intent)
                }
            } else {
                val intent = Intent(this, DiaryDetailActivity::class.java)
                intent.putExtra("DIARY_ID", diaryEntry.id)
                startActivity(intent)
            }
        }, { diaryId ->
            // delete operation
            val diaryEntry = dbHelper.getDiaryById(diaryId)
            if (diaryEntry?.isProtected == true) {
                showPasswordDialog(diaryEntry) {
                    deleteDiaryEntry(diaryId)
                }
            } else {
                deleteDiaryEntry(diaryId)
            }
        }, { diaryId ->
            editDiaryEntry(diaryId)

        }, this)

        diaryRecyclerView.adapter = adapter

        // Set the click event for the new entry button
        newEntryButton.setOnClickListener {
            addNewDiaryEntry()
        }

        searchButton = findViewById(R.id.searchButton)

        // Set button click event listener
        searchButton.setOnClickListener {
            // Gets the contents of the input box and performs a search
            val query = searchEditText.text.toString()
            adapter.filterEntries(query)
        }
    }

    private fun deleteDiaryEntry(diaryId: Long) {
        // Delete a diary entry from the database
        val success = dbHelper.deleteDiaryEntry(diaryId)
        if (success) {
            Toast.makeText(this, "Delete Successfully", Toast.LENGTH_SHORT).show()
            // Update RecyclerView data
            diaryEntriesWithCategories = dbHelper.getAllDiariesWithCategories()
            (diaryRecyclerView.adapter as ViewDiaryAdapter).updateEntries(diaryEntriesWithCategories)
        } else {
            Toast.makeText(this, "Delete failed", Toast.LENGTH_SHORT).show()
        }
    }

    private fun addNewDiaryEntry() {
        val intent = Intent(this, AddDiaryActivity::class.java)
        startActivity(intent)
    }

    private fun editDiaryEntry(diaryId: Long) {
        val intent = Intent(this, EditDiaryActivity::class.java)
        intent.putExtra("DIARY_ID", diaryId)
        startActivity(intent)
    }

    private fun showPasswordDialog(diaryEntry: DiaryEntry, onSuccess: () -> Unit) {
        val builder = AlertDialog.Builder(this)
        val input = EditText(this)
        input.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        builder.setTitle("Please enter password")
            .setView(input)
            .setPositiveButton("Confirm") { _, _ ->
                val inputPassword = input.text.toString()
                if (EncryptionUtils.verifyPassword(inputPassword, diaryEntry.password ?: "")) {
                    onSuccess()
                } else {
                    Toast.makeText(this, "Password incorrect", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.cancel()
            }
        builder.show()
    }

    override fun onResume() {
        super.onResume()
        refreshDiaryList()
    }

    private fun refreshDiaryList() {
        val dbHelper = DiaryDatabaseHelper(this)
        val diariesWithCategories = dbHelper.getAllDiariesWithCategories()
        adapter.updateData(diariesWithCategories)
    }


}
