package com.example.diarytest.Activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.diarytest.Adapter.MediaAdapter
import com.example.diarytest.Data.DiaryEntry
import com.example.diarytest.Data.MediaItem
import com.example.diarytest.Database.DiaryDatabaseHelper
import android.app.AlertDialog;
import android.content.Context
import android.graphics.Typeface;
import android.widget.SeekBar;
import android.graphics.Color
import android.os.Build
import android.text.TextWatcher
import android.text.Editable
import android.text.InputType
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import androidx.annotation.RequiresApi
import com.example.diarytest.R
import com.example.diarytest.utils.EncryptionUtils

class AddDiaryActivity : AppCompatActivity() {

    private val REQUEST_IMAGE = 1
    private val REQUEST_VIDEO = 2
    private val REQUEST_FILE = 3

    private lateinit var diaryContentEditText: EditText
    private lateinit var diaryTitleEditText: EditText
    private var selectedMediaUri: Uri? = null // Store the selected media URI
    private lateinit var mediaRecyclerView: RecyclerView
    private lateinit var mediaAdapter: MediaAdapter
    private val mediaItemList = mutableListOf<MediaItem>()
    private lateinit var categorySpinner: Spinner
    private var selectedFontSize = 14 // Default font size
    private var selectedFontStyle = "normal" // Default font style
    private var selectedColor = "#000000" // Default font color
    private lateinit var encryptCheckBox: CheckBox
    private lateinit var passwordEditText: EditText


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_diary)

        // Initialize the Spinner
        categorySpinner = findViewById(R.id.category_spinner)
        loadCategories()

        diaryContentEditText = findViewById(R.id.diaryContentEditText)
        diaryTitleEditText = findViewById(R.id.title)

        //Button to set the journal font size and color
        val fontSizeButton: Button = findViewById(R.id.fontSizeButton)
        val fontColorButton: Button = findViewById(R.id.fontColorButton)
        val fontStyleButton: Button = findViewById(R.id.fontStyleButton)
        val addPhotoButton: Button = findViewById(R.id.addPhotoButton)
        val addVideoButton: Button = findViewById(R.id.addVideoButton)
        val addFileButton: Button = findViewById(R.id.addFileButton)
        val saveDiaryButton: Button = findViewById(R.id.saveDiaryButton)
        encryptCheckBox = findViewById(R.id.encryptCheckBox)
        passwordEditText = findViewById(R.id.passwordEditText)

        // Get the generated content
        val generatedContent = intent.getStringExtra("GENERATED_CONTENT")
        val generatedTitle = intent.getStringExtra("GENERATED_TITLE")

        if (generatedContent != null) {
            diaryContentEditText.setText(generatedContent)
        }
        if (generatedTitle != null) {
            diaryTitleEditText.setText(generatedTitle)
        }

        encryptCheckBox.setOnCheckedChangeListener { _, isChecked ->
            passwordEditText.visibility = if (isChecked) View.VISIBLE else View.GONE
        }

        mediaRecyclerView = findViewById(R.id.mediaRecyclerView)
        val layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        mediaRecyclerView.layoutManager = layoutManager
        mediaAdapter = MediaAdapter(this, mediaItemList) { position ->
            // Delete a callback for a media item
            mediaItemList.removeAt(position)
            mediaAdapter.notifyItemRemoved(position)
        }
        mediaRecyclerView.adapter = mediaAdapter


        addPhotoButton.setOnClickListener { openImagePicker() }
        addVideoButton.setOnClickListener { openVideoPicker() }
        addFileButton.setOnClickListener { openFilePicker() }
        //Button to set the journal font size and color
        fontSizeButton.setOnClickListener {
            showFontSizePicker()
        }

        fontColorButton.setOnClickListener {
            showColorPicker()
        }

        fontStyleButton.setOnClickListener {
            showFontStylePicker()
        }

        diaryContentEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // If there is content in the input box, the button is displayed, otherwise it is hidden
                if (s.toString().isNotEmpty()) {
                    fontSizeButton.visibility = View.VISIBLE
                    fontColorButton.visibility = View.VISIBLE
                    fontStyleButton.visibility = View.VISIBLE
                } else {
                    fontSizeButton.visibility = View.GONE
                    fontColorButton.visibility = View.GONE
                    fontStyleButton.visibility = View.GONE
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })
        saveDiaryButton.setOnClickListener {
            if (encryptCheckBox.isChecked && passwordEditText.text.isEmpty()) {
                Toast.makeText(this, "Please enter password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            saveDiaryEntry()
        }

        // Get view component
        val addTagButton = findViewById<Button>(R.id.addTagButton)
        val tagInputEditText = findViewById<EditText>(R.id.tagInputEditText)
        val tagsDisplayLayout = findViewById<LinearLayout>(R.id.tagsDisplayLayout)

        // Handle label add button click events
        addTagButton.setOnClickListener {
            val tag = tagInputEditText.text.toString().trim()

            // Check whether the user entered a label
            if (tag.isNotEmpty()) {
                // Create a new TextView display label
                val tagTextView = TextView(this)
                tagTextView.text = tag
                tagTextView.setTextColor(Color.BLACK)  // Label text color (can be customized)
                tagTextView.setPadding(8, 8, 8, 8)

                // Optionally set a style for the label, such as a background color
                tagTextView.setBackgroundColor(Color.parseColor("#E0E0E0"))

                // Add the label to the LinearLayout
                tagsDisplayLayout.addView(tagTextView)

                // Clear the input box so that the user can move on to the next label
                tagInputEditText.text.clear()
            } else {
                // If no label is entered, a prompt is displayed (optional)
                Toast.makeText(this, "Please enter a tag", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "image/*"
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
        }
        startActivityForResult(intent, REQUEST_IMAGE)
    }

    private fun openVideoPicker() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "video/*"
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
        }
        startActivityForResult(intent, REQUEST_VIDEO)
    }

    private fun openFilePicker() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "*/*"
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
        }
        startActivityForResult(intent, REQUEST_FILE)
    }


    private fun saveDiaryEntry() {
        val title = diaryTitleEditText.text.toString()
        val content = diaryContentEditText.text.toString()
        val date = System.currentTimeMillis().toString()
        val categoryId = categorySpinner.selectedItemPosition
        val fontStyle = selectedFontStyle
        val fontSize = selectedFontSize
        val color = selectedColor
        // Set encryption status and password when saving diary
        val isProtected = encryptCheckBox.isChecked
        val password = if (isProtected) {
            EncryptionUtils.encryptPassword(passwordEditText.text.toString())
        } else null

        // Handles persistent permissions for media files
        for (mediaItem in mediaItemList) {
            try {
                val uri = Uri.parse(mediaItem.uri)
                val takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION or
                        Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                contentResolver.takePersistableUriPermission(uri, takeFlags)
            } catch (e: SecurityException) {
                Log.e("AddDiaryActivity", "Can't get the permission for media files: ${e.message}")
            }
        }


        // Create a DiaryEntry object
        val diaryEntry = DiaryEntry(
            id = 0, // 让数据库自动生成
            title = title,
            content = content,
            date = date,
            categoryId = categoryId,
            fontStyle = fontStyle,
            fontSize = fontSize,
            color = color,
            isProtected = isProtected,
            password = password,
            tags = getTagsFromUI()
        )

        Log.e("save tag", getTagsFromUI().toString())

        // Insert to database
        val dbHelper = DiaryDatabaseHelper(this)
        val result = dbHelper.insertDiaryEntryWithMedia(diaryEntry, mediaItemList)

        if (result != -1L) {
            // Save labels and create associations
            saveTagsAndLinkToEntry(result)
            Toast.makeText(this, "Diary entry saved!", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Failed to save diary entry.", Toast.LENGTH_SHORT).show()
        }

        finish() // Close current activity
    }

    private fun saveTagsAndLinkToEntry(entryId: Long) {
        val tags = getTagsFromUI() // Gets the label entered by the user
        val dbHelper = DiaryDatabaseHelper(this)
        Log.d("SaveDiary", "Saving Tags")
        if (tags.isEmpty()) {
            Log.e("SaveDiary", "No tags selected!")
        }

        for (tagName in tags) {
            // Check whether the label already exists
            var tagId = dbHelper.getTagIdByName(tagName)
            if (tagId == -1L) {
                //If the label does not exist, insert a new label
                Log.d("SaveDiary", "saving new tag:" + tagName)
                tagId = dbHelper.insertTag(tagName)
            }

            // If the label is inserted successfully or already exists, the association is established
            if (tagId != -1L) {
                // Associate the Tag with the DiaryEntry
                dbHelper.insertEntryTag(entryId, tagId)
            }
        }
    }

    // Gets all labels from the UI
    private fun getTagsFromUI(): List<String> {
        val tags = mutableListOf<String>()
        // Let's say you have a LinearLayout that you use to display labels, and here you adjust it to suit your situation
        val tagsLayout = findViewById<LinearLayout>(R.id.tagsDisplayLayout)
        for (i in 0 until tagsLayout.childCount) {
            val tagView = tagsLayout.getChildAt(i) as? TextView
            tagView?.let {
                tags.add(it.text.toString())
            }
        }
        return tags
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && data != null) {
            selectedMediaUri = data.data
            when (requestCode) {
                REQUEST_IMAGE -> {
                    // Here you add the media item and update the adapter
                    selectedMediaUri?.let {
                        mediaItemList.add(MediaItem(0,0,it.toString(), "Image"))
                        mediaAdapter.notifyItemInserted(mediaItemList.size - 1)
                    }
                }
                REQUEST_VIDEO -> {
                    selectedMediaUri?.let {
                        mediaItemList.add(MediaItem(0,0,it.toString(), "Video"))
                        mediaAdapter.notifyItemInserted(mediaItemList.size - 1)
                    }
                }
                REQUEST_FILE -> {
                    selectedMediaUri?.let {
                        mediaItemList.add(MediaItem(0,0,it.toString(), "File"))
                        mediaAdapter.notifyItemInserted(mediaItemList.size - 1)
                    }
                }
            }
        }
    }

    //Set the font size and color of the journal content
    //
    private fun showColorPicker() {
        val colors = mapOf(
            "Black" to "#000000",
            "Red" to "#FF0000",
            "Blue" to "#0000FF",
            "Green" to "#008000",
            "Purple" to "#800080",
            "Pink" to "#FFC0CB",
            "Yellow" to "#FFFF00"
        )

        val builder = AlertDialog.Builder(this)
        builder.setTitle("Choose Color")

        val colorNames = colors.keys.toTypedArray()
        builder.setItems(colorNames) { _, which ->
            val colorHex = colors[colorNames[which]] ?: "#000000"
            selectedColor = colorHex // Save hexadecimal color values

            diaryContentEditText.setTextColor(Color.parseColor(colorHex))
        }
        builder.show()
    }

    private fun showFontStylePicker() {
        val styles = arrayOf("Normal", "bold", "italic")
        val dialogBuilder = AlertDialog.Builder(this)
        dialogBuilder.setTitle("Select Font style")
        dialogBuilder.setItems(styles) { _, which ->
            selectedFontStyle = when (which) {
                1 -> "bold"
                2 -> "italic"
                else -> "normal"
            }
            updateDiaryContentFontStyle()
        }
        dialogBuilder.show()
    }

    private fun showFontSizePicker() {
        val dialogBuilder = AlertDialog.Builder(this)
        dialogBuilder.setTitle("Select font size")

        val seekBar = SeekBar(this)
        seekBar.max = 40
        seekBar.min = 12
        seekBar.progress = selectedFontSize
        dialogBuilder.setView(seekBar)

        dialogBuilder.setPositiveButton("confirm") { _, _ ->
            selectedFontSize = seekBar.progress
            if (selectedFontSize < 12) selectedFontSize = 12
            diaryContentEditText.textSize = selectedFontSize.toFloat()
        }

        dialogBuilder.setNegativeButton("cancel", null)
        dialogBuilder.show()
    }

    private fun updateDiaryContentFontStyle() {
        when (selectedFontStyle) {
            "bold" -> diaryContentEditText.setTypeface(null, Typeface.BOLD)
            "italic" -> diaryContentEditText.setTypeface(null, Typeface.ITALIC)
            else -> diaryContentEditText.setTypeface(null, Typeface.NORMAL)
        }
    }


    private fun loadCategories() {
        val categories = resources.getStringArray(R.array.category_labels).toMutableList()
        categories.add(0, "Select a category") // Add a hint item in the first location
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categories)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        categorySpinner.adapter = adapter
        // Set the initial selection to prompt
        categorySpinner.setSelection(0)
    }

    }
