package com.example.bdtkotlin

import android.content.Intent
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.example.bdtkotlin.databinding.ActivityJobUserViewBinding
import com.example.bdtkotlin.databinding.ActivityJobsMainBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage

class JobUserView : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var storage: FirebaseStorage
    private lateinit var binding: ActivityJobUserViewBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityJobUserViewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth
        db = Firebase.firestore
        storage = Firebase.storage

        val title = intent.getStringExtra("titles")
        val descs = intent.getStringExtra("descs")
        val uid = intent.getStringExtra("uid")

        loadInterface(uid!!)

        binding.textJvJobtitle.text = title
        binding.singleJobDescription.text = descs

        binding.btnUvjusrClose.setOnClickListener {
            val intent = Intent(this, JobsMain::class.java)
            startActivity(intent)
        }

        binding.btnUjvContact.setOnClickListener {
            contact(uid!!)
        }
    }

    private fun loadInterface(uid:String) {
        val storageRef = storage.reference
        var islandRef = storageRef.child("$uid/Job.jpg")

        val ONE_MEGABYTE: Int = 1024*1024

        islandRef.getBytes(ONE_MEGABYTE.toLong())
            .addOnSuccessListener { myByteArray ->
                val bitmap = BitmapFactory.decodeByteArray(myByteArray, 0, myByteArray.size)
                binding.singleJobImage.setImageBitmap(bitmap)
            }

            .addOnFailureListener {
                // Handle any errors
                Log.d("DBGF", "Descarga fallida")
            }
    }

    private fun contact(UID:String) {
        val user = auth.currentUser

        val containerFireStore = mapOf(
            "idTaker" to user!!.uid,
            "isLocked" to true,
            "state" to 1
        )

        db.collection("Jobs").document(UID)
            .update(containerFireStore)
            .addOnSuccessListener {
                Toast.makeText(this, "Trabajo tomado", Toast.LENGTH_SHORT).show()

                val intent = Intent(this, HomeView::class.java)
                startActivity(intent)
                finish()
            }
            .addOnFailureListener { e ->
                Log.d("DBGR", "Error writing document", e)
                Toast.makeText(this, "No se pudo tomar el trabajo", Toast.LENGTH_SHORT).show()
            }
    }
}