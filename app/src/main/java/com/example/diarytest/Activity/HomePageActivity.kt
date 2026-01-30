package com.example.diarytest.Activity

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.diarytest.R


class HomePageActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_page)

        val createDiaryButton = findViewById<Button>(R.id.createDiaryButton)
        val viewDiaryButton = findViewById<Button>(R.id.viewDiaryButton)
        val settingsButton = findViewById<Button>(R.id.settingsButton)


        createDiaryButton.setOnClickListener { // Jump to AddDiaryActivity
            val intent = Intent(
                this@HomePageActivity,
                AddDiaryActivity::class.java
            )
            startActivity(intent)
        }

        viewDiaryButton.setOnClickListener { // Jump to ViewDiaryActivity
            val intent = Intent(
                this@HomePageActivity,
                ViewDiaryActivity::class.java
            )
            startActivity(intent)
        }


        settingsButton.setOnClickListener {
            val intent = Intent(
                this,
                AiFunctionActivity::class.java
            )
            startActivity(intent)
        }
    }
}
