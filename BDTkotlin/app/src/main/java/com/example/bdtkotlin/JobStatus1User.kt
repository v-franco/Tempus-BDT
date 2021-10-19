package com.example.bdtkotlin

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.bdtkotlin.databinding.ActivityJobStatus1UserBinding

class JobStatus1User : AppCompatActivity() {
    private lateinit var binding: ActivityJobStatus1UserBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityJobStatus1UserBinding.inflate(layoutInflater)
        setContentView(binding.root)


    }
}