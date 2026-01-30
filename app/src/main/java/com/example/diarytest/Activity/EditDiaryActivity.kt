package com.example.diarytest.Activity

import android.annotation.SuppressLint
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
import android.content.pm.PackageManager
import android.graphics.Typeface;
import android.widget.SeekBar;
import android.graphics.Color
import android.text.TextWatcher
import android.text.Editable
import android.text.InputType
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.CheckBox
import android.widget.Spinner
import com.example.diarytest.R
import com.example.diarytest.utils.EncryptionUtils


class EditDiaryActivity : AppCompatActivity() {

    private val REQUEST_IMAGE = 1
    private val REQUEST_VIDEO = 2
    private val REQUEST_FILE = 3

    private lateinit var diaryContentEditText: EditText
    private lateinit var diaryTitleEditText: EditText
    private lateinit var mediaRecyclerView: RecyclerView
    private lateinit var mediaAdapter: MediaAdapter
    private val mediaItemList = mutableListOf<MediaItem>()
    private lateinit var categorySpinner: Spinner
    private lateinit var diaryDatabaseHelper: DiaryDatabaseHelper
    private var diaryId: Long = -1
    private var selectedFontSize = 14
    private var selectedFontStyle = "normal"
    private var selectedColor = "#000000"
    private lateinit var encryptCheckBox: CheckBox
    private lateinit var passwordEditText: EditText
    private var originalIsProtected = false
    private var originalPassword: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_diary)

        categorySpinner = findViewById(R.id.category_spinner)
        val dbHelper = DiaryDatabaseHelper(this)
        dbHelper.initializeCategories()
        loadCategories()

        diaryContentEditText = findViewById(R.id.diaryContentEditText)
        diaryTitleEditText = findViewById(R.id.title)

        diaryDatabaseHelper = DiaryDatabaseHelper(this)

        val fontSizeButton: Button = findViewById(R.id.fontSizeButton)
        val fontColorButton: Button = findViewById(R.id.fontColorButton)
        val fontStyleButton: Button = findViewById(R.id.fontStyleButton)
        val addPhotoButton: Button = findViewById(R.id.addPhotoButton)
        val addVideoButton: Button = findViewById(R.id.addVideoButton)
        val addFileButton: Button = findViewById(R.id.addFileButton)
        val saveDiaryButton: Button = findViewById(R.id.saveDiaryButton)

        // Initializes encryption-related controls
        encryptCheckBox = findViewById(R.id.encryptCheckBox)
        passwordEditText = findViewById(R.id.passwordEditText)

        // Set the encryption check box listener
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
        fontSizeButton.setOnClickListener { showFontSizePicker() }
        fontColorButton.setOnClickListener { showColorPicker() }
        fontStyleButton.setOnClickListener { showFontStylePicker() }

        diaryContentEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
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


        diaryId = intent.getLongExtra("DIARY_ID", -1)
        if (diaryId != -1L) {
            loadDiaryEntry(diaryId)
        }


        saveDiaryButton.setOnClickListener {
            try {
                saveDiaryEntry()
            } catch (e: Exception) {
                Log.e("SaveDiaryError", "Error saving diary: ${e.message}")
                e.printStackTrace()
                Toast.makeText(this, "An error occurred while saving the diary.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun loadDiaryEntry(diaryId: Long) {
        val diaryEntry = diaryDatabaseHelper.getDiaryById(diaryId)
        if (diaryEntry != null) {
            // Check if the diary is encrypted
            if (diaryEntry.isProtected) {
                showPasswordDialog(this, diaryEntry) {
                    // Load the content after the password is correct
                    loadDiaryContent(diaryEntry)
                }
            } else {
                loadDiaryContent(diaryEntry)
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun loadDiaryContent(diaryEntry: DiaryEntry) {
        diaryTitleEditText.setText(diaryEntry.title)
        diaryContentEditText.setText(diaryEntry.content)
        categorySpinner.setSelection(diaryEntry.categoryId)
        // Set font style
        selectedFontStyle = diaryEntry.fontStyle
        selectedFontSize = diaryEntry.fontSize
        selectedColor = diaryEntry.color

        // Apply font style
        when (selectedFontStyle) {
            "bold" -> diaryContentEditText.setTypeface(null, Typeface.BOLD)
            "italic" -> diaryContentEditText.setTypeface(null, Typeface.ITALIC)
            else -> diaryContentEditText.setTypeface(null, Typeface.NORMAL)
        }

        // Applied font size
        diaryContentEditText.textSize = selectedFontSize.toFloat()

        // Applied font color
        try {
            diaryContentEditText.setTextColor(Color.parseColor(selectedColor))
        } catch (e: IllegalArgumentException) {
            diaryContentEditText.setTextColor(Color.BLACK)
            selectedColor = "#000000"
        }

        // Set encryption status
        originalIsProtected = diaryEntry.isProtected
        originalPassword = diaryEntry.password
        encryptCheckBox.isChecked = diaryEntry.isProtected
        if (diaryEntry.isProtected) {
            passwordEditText.setText(EncryptionUtils.decryptPassword(diaryEntry.password.toString()))
            passwordEditText.visibility = View.INVISIBLE
        }

        diaryContentEditText.setTypeface(null, when (diaryEntry.fontStyle) {
            "normal" -> Typeface.NORMAL
            "bold" -> Typeface.BOLD
            "italic" -> Typeface.ITALIC
            else -> Typeface.NORMAL
        })
        diaryContentEditText.textSize = diaryEntry.fontSize.toFloat()
        diaryContentEditText.setTextColor(Color.parseColor(diaryEntry.color))

        // Load media item
        mediaItemList.clear()
        val mediaItems = diaryDatabaseHelper.getMediaItemsForDiaryEntry(diaryId)
        mediaItems.forEach { mediaItem ->
            try {
                // Try to get permission on the URI
                val uri = Uri.parse(mediaItem.uri)
                val takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION or
                        Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                contentResolver.takePersistableUriPermission(uri, takeFlags)
            } catch (e: SecurityException) {
                Log.e("EditDiaryActivity", "Can't get permission for media files: ${e.message}")
            }
        }
        mediaItemList.addAll(mediaItems)
        mediaAdapter.notifyDataSetChanged()

    }

    private fun saveDiaryEntry() {

        val title = diaryTitleEditText.text.toString()
        val content = diaryContentEditText.text.toString()
        val date = System.currentTimeMillis().toString()
        val categoryId = categorySpinner.selectedItemPosition
        val fontStyle = selectedFontStyle
        val fontSize = selectedFontSize
        val color = selectedColor
        val isProtected = encryptCheckBox.isChecked
        val password = if (isProtected) {
            EncryptionUtils.encryptPassword(passwordEditText.text.toString())
        } else null

        val updatedDiaryEntry = DiaryEntry(
            id = diaryId,
            title = title,
            content = content,
            date = date,
            categoryId = categoryId,
            fontStyle = fontStyle,
            fontSize = fontSize,
            color = color,
            isProtected = isProtected,
            password = password,
        )

        try {
            val success = diaryDatabaseHelper.updateDiaryEntry(updatedDiaryEntry)
            if (success) {
                Toast.makeText(this, "Diary saved successfully。", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this, "Failed to update diary。", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Log.e("SaveDiaryError", "Error saving diary: ${e.message}")
            Toast.makeText(this, "An error occurred while saving the diary。", Toast.LENGTH_SHORT).show()
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


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && data != null) {
            val uri = data.data
            uri?.let {
                try {
                    // Obtain persistent permission
                    contentResolver.takePersistableUriPermission(
                        it,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )

                    when (requestCode) {
                        REQUEST_IMAGE, REQUEST_VIDEO, REQUEST_FILE -> {
                            val mediaType = when (requestCode) {
                                REQUEST_IMAGE -> "Image"
                                REQUEST_VIDEO -> "Video"
                                else -> "File"
                            }
                            // Create a new MediaItem and save it to the database
                            val mediaItem = MediaItem(0, diaryId, it.toString(), mediaType)
                            val mediaId = diaryDatabaseHelper.insertMediaItem(mediaItem)
                            if (mediaId != -1L) {
                                mediaItemList.add(mediaItem)
                                mediaAdapter.notifyItemInserted(mediaItemList.size - 1)
                            } else {
                                Log.d("EditDiaryActivity", "Failed to store media resource")
                            }
                        }
                        else -> {
                            Log.d("EditDiaryActivity", "Unprocessed request code: $requestCode")
                        }
                    }
                } catch (e: SecurityException) {
                    Toast.makeText(this, "Failed to obtain file access permission", Toast.LENGTH_SHORT).show()
                    Log.e("EditDiaryActivity", "Permission error: ${e.message}")
                }
            }
        }
    }

    private fun showFontSizePicker() {
        val dialogBuilder = AlertDialog.Builder(this)
        dialogBuilder.setTitle("Select Font Size")
        val seekBar = SeekBar(this)
        seekBar.max = 100
        seekBar.progress = selectedFontSize
        dialogBuilder.setView(seekBar)
        dialogBuilder.setPositiveButton("OK") { _, _ ->
            selectedFontSize = seekBar.progress
            diaryContentEditText.textSize = selectedFontSize.toFloat()
        }
        dialogBuilder.setNegativeButton("Cancel", null)
        dialogBuilder.show()
    }

    private fun showColorPicker() {
        val colors = mapOf(
            "Red" to Color.RED,
            "Yellow" to Color.YELLOW,
            "Blue" to Color.BLUE,
            "Green" to Color.GREEN,
            "Purple" to Color.parseColor("#800080"),
            "Black" to Color.BLACK,
            "Pink" to Color.parseColor("#FFC0CB")
        )
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Choose a Color")
        val colorNames = colors.keys.toTypedArray()
        builder.setItems(colorNames) { _, which ->
            selectedColor = colorNames[which]
            diaryContentEditText.setTextColor(colors[selectedColor] ?: Color.BLACK)
        }
        builder.show()
    }

    private fun showFontStylePicker() {
        val styles = arrayOf("Normal", "Bold", "Italic")
        val dialogBuilder = AlertDialog.Builder(this)
        dialogBuilder.setTitle("Select Font Style")
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

    private fun updateDiaryContentFontStyle() {
        diaryContentEditText.setTypeface(null, when (selectedFontStyle) {
            "bold" -> Typeface.BOLD
            "italic" -> Typeface.ITALIC
            else -> Typeface.NORMAL
        })
    }

    private fun loadCategories() {
        val categories = resources.getStringArray(R.array.category_labels).toMutableList()
        categories.add(0, "Select a category")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categories)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        categorySpinner.adapter = adapter
        categorySpinner.setSelection(0)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_READ_STORAGE) {
            if (!(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                // The permission is denied, prompting the user
                Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    companion object {
        private const val REQUEST_READ_STORAGE = 1
    }


    fun showPasswordDialog(context: Context, diaryEntry: DiaryEntry, onPasswordCorrect: () -> Unit) {
        val builder = AlertDialog.Builder(context)
        val input = EditText(context)
        input.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD

        builder.setTitle("Please enter password")
            .setView(input)
            .setPositiveButton("Confirm") { _, _ ->
                val password = input.text.toString()
                if (EncryptionUtils.verifyPassword(password, diaryEntry.password ?: "")) {
                    onPasswordCorrect()
                } else {
                    Toast.makeText(context, "Password incorrect", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.cancel()
            }

        builder.show()
    }

}

