package com.example.diarytest.Database

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import com.example.diarytest.Data.Category
import com.example.diarytest.Data.DiaryEntry
import com.example.diarytest.Data.MediaItem
import com.example.diarytest.R

class DiaryDatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    private val appContext = context.applicationContext

    companion object {


        const val DATABASE_NAME = "DiaryApp.db"
        const val DATABASE_VERSION = 1
    }

    override fun onCreate(db: SQLiteDatabase) {
        // Create DiaryEntry table
        db.execSQL("""
            CREATE TABLE DiaryEntry (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                title TEXT NOT NULL,
                content TEXT,
                date TEXT NOT NULL,
                font_style TEXT,
                font_size INTEGER,
                category_id INTEGER,
                color TEXT,
                is_protected INTEGER DEFAULT 0,
                password TEXT,
                FOREIGN KEY (category_id) REFERENCES Category(id)
            );
        """)

        // Create Media表 table
        db.execSQL("""
            CREATE TABLE Media (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                diaryEntryId INTEGER,
                uri TEXT,
                type TEXT,
                FOREIGN KEY(diaryEntryId) REFERENCES DiaryEntry(id) ON DELETE CASCADE
            );
        """)

        // Create Tag table
        db.execSQL("""
            CREATE TABLE Tag (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL
            );
        """)

        // Create EntryTag table
        db.execSQL("""
            CREATE TABLE EntryTag (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                entry_id INTEGER,
                tag_id INTEGER,
                FOREIGN KEY (entry_id) REFERENCES DiaryEntry(id),
                FOREIGN KEY (tag_id) REFERENCES Tag(id)
            );
        """)



        // Create category table
        db.execSQL("""
            CREATE TABLE Category (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL
            );
        """)

        // insert categories
        insertCategoryLabels(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) {
            db.execSQL("ALTER TABLE DiaryEntry ADD COLUMN fontStyle ")
        }
        db.execSQL("DROP TABLE IF EXISTS DiaryEntry")
        db.execSQL("DROP TABLE IF EXISTS Category")
        db.execSQL("DROP TABLE IF EXISTS Media")
        db.execSQL("DROP TABLE IF EXISTS Tag")
        db.execSQL("DROP TABLE IF EXISTS EntryTag")
        onCreate(db)
    }

    // Insert labels into the Category table
    private fun insertCategoryLabels(db: SQLiteDatabase) {
        val categoryLabels = appContext.resources.getStringArray(R.array.category_labels)
        for (label in categoryLabels) {
            val values = ContentValues().apply {
                put("name", label)
            }
            db.insert("Category", null, values)
        }
    }

    // Insert DiaryEntry and related MediaItem list
    fun insertDiaryEntryWithMedia(diaryEntry: DiaryEntry, mediaItems: List<MediaItem>): Long {
        val db = writableDatabase
        var diaryEntryId: Long = -1

        db.beginTransaction()
        try {
            // Insert DiaryEntry
            val diaryEntryValues = ContentValues().apply {
                put("title", diaryEntry.title)
                put("content", diaryEntry.content)
                put("date", diaryEntry.date)
                put("category_id", diaryEntry.categoryId)
                put("font_style", diaryEntry.fontStyle)
                put("font_size", diaryEntry.fontSize)
                put("color", diaryEntry.color)
                put("is_protected", if (diaryEntry.isProtected) 1 else 0)
                put("password", diaryEntry.password)
            }

            diaryEntryId = db.insert("DiaryEntry", null, diaryEntryValues)

            // Insert each MediaItem
            if (diaryEntryId != -1L) {
                for (media in mediaItems) {
                    val mediaValues = ContentValues().apply {
                        put("diaryEntryId", diaryEntryId)
                        put("uri", media.uri)
                        put("type", media.type)
                    }
                    db.insert("Media", null, mediaValues)
                }
            }

            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
        return diaryEntryId
    }

    // Get the related MediaItems from the DiaryEntry ID
    fun getMediaItemsForDiaryEntry(diaryEntryId: Long): List<MediaItem> {
        val db = this.readableDatabase
        val cursor = db.query(
            "Media",
            arrayOf("id", "diaryEntryId", "uri", "type"),
            "diaryEntryId = ?",
            arrayOf(diaryEntryId.toString()),
            null, null, null
        )

        val mediaItems = mutableListOf<MediaItem>()
        while (cursor.moveToNext()) {
            val idIndex = cursor.getColumnIndex("id")
            val diaryEntryIdIndex = cursor.getColumnIndex("diaryEntryId")
            val uriIndex = cursor.getColumnIndex("uri")
            val typeIndex = cursor.getColumnIndex("type")

            // Check whether the index is valid
            if (idIndex != -1 && diaryEntryIdIndex != -1 && uriIndex != -1 && typeIndex != -1) {
                val mediaItem = MediaItem(
                    id = cursor.getLong(idIndex),
                    diaryEntryId = cursor.getLong(diaryEntryIdIndex),
                    uri = cursor.getString(uriIndex),
                    type = cursor.getString(typeIndex)
                )
                mediaItems.add(mediaItem)
            } else {
                Log.e("DatabaseError", "Invalid column index.")
            }
        }
        cursor.close()
        return mediaItems
    }


    //Get all Diaries
    fun getAllDiaries(): List<DiaryEntry> {
        val db = readableDatabase
        val query = """
            SELECT d.*, c.name as category_name 
            FROM DiaryEntry d 
            LEFT JOIN Category c ON d.category_id = c.id 
            ORDER BY d.date DESC
        """
        val cursor = db.rawQuery(query, null)
        val entries = mutableListOf<DiaryEntry>()

        while (cursor.moveToNext()) {
            entries.add(
                DiaryEntry(
                    id = cursor.getLong(cursor.getColumnIndexOrThrow("id")),
                    title = cursor.getString(cursor.getColumnIndexOrThrow("title")),
                    content = cursor.getString(cursor.getColumnIndexOrThrow("content")),
                    date = cursor.getString(cursor.getColumnIndexOrThrow("date")),
                    categoryId = cursor.getInt(cursor.getColumnIndexOrThrow("category_id")),
                    fontStyle = cursor.getString(cursor.getColumnIndexOrThrow("font_style")),
                    fontSize = cursor.getInt(cursor.getColumnIndexOrThrow("font_size")),
                    color = cursor.getString(cursor.getColumnIndexOrThrow("color")),
                    isProtected = cursor.getInt(cursor.getColumnIndexOrThrow("is_protected")) == 1,
                    password = cursor.getString(cursor.getColumnIndexOrThrow("password"))
                )
            )
        }
        cursor.close()
        return entries
    }

    // Gets the journal entry for the specified ID
    fun getDiaryById(diaryId: Long): DiaryEntry? {
        val db = readableDatabase
        val query = """
    SELECT d.*, c.name as category_name 
    FROM DiaryEntry d 
    LEFT JOIN Category c ON d.category_id = c.id 
    WHERE d.id = ?
    """
        val cursor = db.rawQuery(query, arrayOf(diaryId.toString()))

        var diaryEntry: DiaryEntry? = null
        if (cursor.moveToFirst()) {
            diaryEntry = DiaryEntry(
                id = cursor.getLong(cursor.getColumnIndexOrThrow("id")),
                title = cursor.getString(cursor.getColumnIndexOrThrow("title")),
                content = cursor.getString(cursor.getColumnIndexOrThrow("content")),
                date = cursor.getString(cursor.getColumnIndexOrThrow("date")),
                categoryId = cursor.getInt(cursor.getColumnIndexOrThrow("category_id")),
                fontStyle = cursor.getString(cursor.getColumnIndexOrThrow("font_style")),
                fontSize = cursor.getInt(cursor.getColumnIndexOrThrow("font_size")),
                color = cursor.getString(cursor.getColumnIndexOrThrow("color")),
                isProtected = cursor.getInt(cursor.getColumnIndexOrThrow("is_protected")) == 1,
                password = cursor.getString(cursor.getColumnIndexOrThrow("password"))
            )
        }
        cursor.close()
        return diaryEntry
    }

    // Gets all diary entries and their category names
    fun getAllDiariesWithCategories(): List<Pair<DiaryEntry, String?>> {
        val db = readableDatabase
        val query = """
        SELECT d.*, c.name as category_name 
        FROM DiaryEntry d 
        LEFT JOIN Category c ON d.category_id = c.id 
        ORDER BY d.date DESC
    """
        val cursor = db.rawQuery(query, null)
        val entries = mutableListOf<Pair<DiaryEntry, String?>>()
        Log.d("DiaryDatabaseHelper", "Query executed, cursor count: ${cursor.count}")

        while (cursor.moveToNext()) {
            val diaryEntry = DiaryEntry(
                id = cursor.getLong(cursor.getColumnIndexOrThrow("id")),
                title = cursor.getString(cursor.getColumnIndexOrThrow("title")),
                content = cursor.getString(cursor.getColumnIndexOrThrow("content")),
                date = cursor.getString(cursor.getColumnIndexOrThrow("date")),
                categoryId = cursor.getInt(cursor.getColumnIndexOrThrow("category_id")),
                fontStyle = cursor.getString(cursor.getColumnIndexOrThrow("font_style")),
                fontSize = cursor.getInt(cursor.getColumnIndexOrThrow("font_size")),
                color = cursor.getString(cursor.getColumnIndexOrThrow("color")),
                isProtected = cursor.getInt(cursor.getColumnIndexOrThrow("is_protected")) == 1,
                password = cursor.getString(cursor.getColumnIndexOrThrow("password"))
            )

            // Get category name
            val categoryName = cursor.getString(cursor.getColumnIndexOrThrow("category_name"))


            Log.d("DiaryDatabaseHelper", "Category name from database: $categoryName")
            entries.add(Pair(diaryEntry, categoryName))
        }
        cursor.close()
        return entries
    }

    fun deleteDiaryEntry(id: Long): Boolean {
        val db = writableDatabase
        val deletedRows = db.delete("DiaryEntry", "id = ?", arrayOf(id.toString()))
        return deletedRows > 0
    }

    fun updateDiaryEntry(diaryEntry: DiaryEntry): Boolean {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put("title", diaryEntry.title)
            put("content", diaryEntry.content)
            put("date", diaryEntry.date)
            put("category_id", diaryEntry.categoryId)
            put("font_style", diaryEntry.fontStyle)
            put("font_size", diaryEntry.fontSize)
            put("color", diaryEntry.color)
            put("is_protected", if (diaryEntry.isProtected) 1 else 0)
            put("password", diaryEntry.password)
        }
        val rowsAffected = db.update("DiaryEntry", values, "id = ?", arrayOf(diaryEntry.id.toString()))
        return rowsAffected > 0
    }


    // Check and initialize the classification data
    fun initializeCategories() {
        val db = readableDatabase
        // Check whether the Category table is empty
        val cursor = db.rawQuery("SELECT COUNT(*) FROM Category", null)
        cursor.moveToFirst()
        val count = cursor.getInt(0)
        cursor.close()

        if (count == 0) {
            Log.d("DiaryDatabaseHelper", "Category table is empty, initializing...")
            val writableDb = writableDatabase
            val categoryLabels = appContext.resources.getStringArray(R.array.category_labels)
            for (label in categoryLabels) {
                val values = ContentValues().apply {
                    put("name", label)
                }
                writableDb.insert("Category", null, values)
                Log.d("DiaryDatabaseHelper", "Inserted category: $label")
            }
        } else {
            Log.d("DiaryDatabaseHelper", "Category table already has $count entries")
        }
    }


    fun insertMediaItem(mediaItem: MediaItem): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("diaryEntryId", mediaItem.diaryEntryId)
            put("uri", mediaItem.uri)
            put("type", mediaItem.type)
        }
        return db.insert("Media", null, values)
    }

    //Gets the ID of the tag and returns -1 if the tag does not exist
    fun getTagIdByName(tagName: String): Long {
        val db = writableDatabase
        val cursor = db.query(
            "Tag",
            arrayOf("id"),
            "name = ?",
            arrayOf(tagName),
            null, null, null
        )

        // Check that the column index is valid
        val idColumnIndex = cursor.getColumnIndex("id")
        if (idColumnIndex == -1) {
            Log.e("Database", "Column 'id' not found in the 'Tag' table")
            return -1  // If the column is not found, return -1
        }

        return if (cursor.moveToFirst()) {
            cursor.getLong(idColumnIndex)
        } else {
            -1
        }
    }


    // Insert new tag
    fun insertTag(tagName: String): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("name", tagName)
        }
        return db.insert("Tag", null, values)
    }

    // Insert an associated entry in the EntryTag table
    fun insertEntryTag(entryId: Long, tagId: Long): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("entry_id", entryId)
            put("tag_id", tagId)
        }
        return db.insert("EntryTag", null, values)
    }

    // Method of obtaining database instance
    fun getReadableDatabaseInstance(): SQLiteDatabase {
        return this.readableDatabase
    }

    fun getTagsForDiaryEntry(diaryId: Long): List<String> {
        val tags = mutableListOf<String>()
        val db = writableDatabase

        // Run the query with JOIN to fetch tag_name from the Tag table
        val cursor = db.rawQuery("""
        SELECT DISTINCT Tag.name
        FROM EntryTag
        INNER JOIN Tag ON EntryTag.tag_id = Tag.id
        WHERE EntryTag.entry_id = ?
    """, arrayOf(diaryId.toString()))

        // Get and log the column names for debugging
        val columnCount = cursor.columnCount
        for (i in 0 until columnCount) {
            Log.d("Database", "Column ${i}: ${cursor.getColumnName(i)}")
        }

        // Move to the first row
        while (cursor.moveToNext()) {
            val columnIndex = cursor.getColumnIndex("name")
            if (columnIndex != -1) {  // Check if column exists
                tags.add(cursor.getString(columnIndex))
            } else {
                Log.w("Database", "Column 'tag_name' not found!")
            }
        }

        cursor.close()
        return tags
    }


    fun searchDiaryEntriesByTagTitleContent(query: String): List<Pair<DiaryEntry, String?>> {
        val db = writableDatabase
        val cursor = db.rawQuery("""
        SELECT DISTINCT DiaryEntry.*, Tag.name AS tag_name
        FROM DiaryEntry
        LEFT JOIN EntryTag ON DiaryEntry.id = EntryTag.entry_id
        LEFT JOIN Tag ON Tag.id = EntryTag.tag_id
        WHERE DiaryEntry.title LIKE ? 
           OR DiaryEntry.content LIKE ? 
           OR Tag.name LIKE ?;
    """, arrayOf("%$query%", "%$query%", "%$query%"))

        val entriesWithTags = mutableListOf<Pair<DiaryEntry, String?>>()

        if (cursor != null && cursor.moveToFirst()) {
            val idIndex = cursor.getColumnIndex("id")
            val titleIndex = cursor.getColumnIndex("title")
            val contentIndex = cursor.getColumnIndex("content")
            val dateIndex = cursor.getColumnIndex("date")
            val categoryIdIndex = cursor.getColumnIndex("category_id")
            val fontStyleIndex = cursor.getColumnIndex("font_style")
            val fontSizeIndex = cursor.getColumnIndex("font_size")
            val colorIndex = cursor.getColumnIndex("color")
            val isProtectedIndex = cursor.getColumnIndex("is_protected")
            val passwordIndex = cursor.getColumnIndex("password")
            val tagNameIndex = cursor.getColumnIndex("tag_name") // The tag name column alias

            do {
                val id = cursor.getLong(idIndex)
                val title = cursor.getString(titleIndex)
                val content = cursor.getString(contentIndex)
                val date = cursor.getString(dateIndex)
                val categoryId = cursor.getInt(categoryIdIndex)
                val fontStyle = cursor.getString(fontStyleIndex)
                val fontSize = cursor.getInt(fontSizeIndex)
                val color = cursor.getString(colorIndex)
                val isProtected = cursor.getInt(isProtectedIndex) == 1
                val password = cursor.getString(passwordIndex)
                val tagName = cursor.getString(tagNameIndex)

                val diaryEntry = DiaryEntry(id, title, content, date, categoryId, fontStyle, fontSize, color, isProtected, password)
                entriesWithTags.add(Pair(diaryEntry, tagName)) // Pair the DiaryEntry with the tag name
            } while (cursor.moveToNext())
        }

        cursor.close()
        return entriesWithTags
    }







}
