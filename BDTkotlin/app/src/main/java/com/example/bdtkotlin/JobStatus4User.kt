package com.example.bdtkotlin

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.bdtkotlin.databinding.ActivityJobStatus3UserBinding
import com.example.bdtkotlin.databinding.ActivityJobStatus4UserBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class JobStatus4User : AppCompatActivity() {


    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var binding: ActivityJobStatus4UserBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityJobStatus4UserBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth
        db = Firebase.firestore

        val title = intent.getStringExtra("title")

        binding.textJvJobtitle.text = title

        binding.btnUvjusrClose.setOnClickListener {
            val intent = Intent(this, HomeView::class.java)
            startActivity(intent)
        }
    }

    override fun onPause() {
        super.onPause()
        finish()
    }
}