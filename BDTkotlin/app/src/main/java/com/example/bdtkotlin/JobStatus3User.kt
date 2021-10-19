package com.example.bdtkotlin

import android.content.Intent
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.example.bdtkotlin.databinding.ActivityJobStatus3UserBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage

class JobStatus3User : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var storage: FirebaseStorage
    private lateinit var binding: ActivityJobStatus3UserBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityJobStatus3UserBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth
        db = Firebase.firestore
        storage = Firebase.storage

        val title = intent.getStringExtra("title")
        val desc = intent.getStringExtra("desc")
        val uid = intent.getStringExtra("UID")

        loadInterface(uid!!)

        binding.textJvJobtitle.text = title
        binding.singleJobDescription.text = intent.getStringExtra("desc")

        binding.btnJvusrClose.setOnClickListener {
            val intent = Intent(this, HomeView::class.java)
            startActivity(intent)
        }
    }

    private fun loadInterface(uid:String) {
        val storageRef = storage.reference
        var islandRef = storageRef.child("${uid}/Job.jpg")

        val ONE_MEGABYTE: Int = 1024*1024

        islandRef.getBytes(ONE_MEGABYTE.toLong())
            .addOnSuccessListener { myByteArray ->
                val bitmap = BitmapFactory.decodeByteArray(myByteArray, 0, myByteArray.size)
                binding.imageJobUsr.setImageBitmap(bitmap)
            }

            .addOnFailureListener {
                // Handle any errors
                Log.d("DBGF", "Descarga fallida")
            }
    }

    override fun onPause() {
        super.onPause()
        finish()
    }
}