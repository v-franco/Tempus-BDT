package com.example.bdtkotlin

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.bdtkotlin.databinding.ActivityJobStatus1OpBinding

class JobStatus1OP : AppCompatActivity() {
    private lateinit var binding: ActivityJobStatus1OpBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityJobStatus1OpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val title = intent.getStringExtra("title")

        binding.textJvJobtitle.text = title

        binding.btnJvopClose.setOnClickListener {
            val intent = Intent(this, HomeView::class.java)
            startActivity(intent)
        }
    }

    override fun onPause() {
        super.onPause()
        finish()
    }
}