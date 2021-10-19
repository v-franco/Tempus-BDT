package com.example.bdtkotlin

import android.content.Intent
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.example.bdtkotlin.databinding.ActivityJobStatus3OpBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage

class JobStatus3OP : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var storage: FirebaseStorage
    private lateinit var binding: ActivityJobStatus3OpBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityJobStatus3OpBinding.inflate(layoutInflater)
        setContentView(binding.root)


        auth = Firebase.auth
        db = Firebase.firestore
        storage = Firebase.storage

        loadInterface()

        val title = intent.getStringExtra("title")
        val uid = intent.getStringExtra("UID")

        binding.textJvJobtitle.text = title
        binding.singleJobDescription2.text = intent.getStringExtra("desc")

        binding.btnUjvContact.setOnClickListener {
            continueOperations(title!!, uid!!)
        }

        binding.btnJvClose.setOnClickListener {
            val intent = Intent(this, HomeView::class.java)
            startActivity(intent)
        }

    }

    private fun loadInterface() {
        val user = auth.currentUser

        val storageRef = storage.reference
        var islandRef = storageRef.child("${user?.uid}/Job.jpg")

        val ONE_MEGABYTE: Int = 1024*1024

        islandRef.getBytes(ONE_MEGABYTE.toLong())
            .addOnSuccessListener { myByteArray ->
                val bitmap = BitmapFactory.decodeByteArray(myByteArray, 0, myByteArray.size)
                binding.imageUvj.setImageBitmap(bitmap)
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

    private fun continueOperations(title:String, uid:String) {
        val user = auth.currentUser

        val containerFireStore = mapOf(
            "state" to 3
        )

        db.collection("Jobs").document(user!!.uid)
            .update(containerFireStore)
            .addOnSuccessListener {
                val intent = Intent(this, JobStatus4OP::class.java)
                intent.putExtra("title", title)
                intent.putExtra("UID", uid)
                startActivity(intent)
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "No se continuar", Toast.LENGTH_SHORT).show()
            }
    }
}