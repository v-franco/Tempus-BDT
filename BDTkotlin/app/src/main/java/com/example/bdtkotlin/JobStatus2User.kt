package com.example.bdtkotlin

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.bdtkotlin.databinding.ActivityJobStatus2UserBinding

class JobStatus2User : AppCompatActivity() {
    private lateinit var binding: ActivityJobStatus2UserBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityJobStatus2UserBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val title = intent.getStringExtra("title")
        val name = intent.getStringExtra("name")
        val email = intent.getStringExtra("email")
        val phone = intent.getStringExtra("phone")

        binding.textJvJobtitle.text = title
        binding.txtJs2opDynName.text = name
        binding.txtJs2opDynMail.text = email
        binding.txtJs2opDynPhone.text = phone

        binding.btnJs2usrClose.setOnClickListener {
            val intent = Intent(this, HomeView::class.java)
            startActivity(intent)
        }
    }

    override fun onPause() {
        super.onPause()
        finish()
    }
}