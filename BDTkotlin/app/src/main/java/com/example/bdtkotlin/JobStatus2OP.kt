package com.example.bdtkotlin

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.bdtkotlin.databinding.ActivityJobStatus2OpBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class JobStatus2OP : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var binding: ActivityJobStatus2OpBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityJobStatus2OpBinding.inflate(layoutInflater)
        setContentView(binding.root)


        auth = Firebase.auth
        db = Firebase.firestore

        val name = intent.getStringExtra("name")
        val phone = intent.getStringExtra("phone")
        val email = intent.getStringExtra("email")
        val title = intent.getStringExtra("title")
        val desc = intent.getStringExtra("desc")
        val uid = intent.getStringExtra("UID")

        binding.textJvJobtitle.text = title
        binding.txtJs2opDynName.text = name
        binding.txtJs2opDynPhone.text = phone
        binding.txtJs2opDynMail.text = email

        binding.btnPjPublish.setOnClickListener {
            continueOperations(title!!, desc!!, uid!!)
        }

        binding.btnJs2opClose.setOnClickListener {
            val intent = Intent(this, HomeView::class.java)
            startActivity(intent)
        }
    }

    override fun onPause() {
        super.onPause()
        finish()
    }

    private fun continueOperations(title:String, desc:String, uid:String) {
        val user = auth.currentUser

        val containerFireStore = mapOf(
            "state" to 2
        )

        db.collection("Jobs").document(user!!.uid)
            .update(containerFireStore)
            .addOnSuccessListener {
                val intent = Intent(this, JobStatus3OP::class.java)
                intent.putExtra("title", title)
                intent.putExtra("desc", desc)
                intent.putExtra("UID",uid)
                startActivity(intent)
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "No se continuar", Toast.LENGTH_SHORT).show()
            }
    }
}