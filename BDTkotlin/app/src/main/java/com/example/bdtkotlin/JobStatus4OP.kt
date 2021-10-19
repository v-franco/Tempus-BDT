package com.example.bdtkotlin

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.Observer
import com.example.bdtkotlin.databinding.ActivityJobStatus4OpBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.runBlocking

class JobStatus4OP : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var binding: ActivityJobStatus4OpBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityJobStatus4OpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth
        db = Firebase.firestore


        val uid = intent.getStringExtra("UID")!!
        binding.textJvJobtitle.text = intent.getStringExtra("title")


        binding.btnUjvContact.setOnClickListener {
            updateScore(binding.ratingBarUjv.rating, uid)
        }

        binding.btnUvjopClose.setOnClickListener {
            val intent = Intent(this, HomeView::class.java)
            startActivity(intent)
        }

    }

    override fun onPause() {
        super.onPause()
        finish()
    }

    private fun updateScore(rating: Float, uid:String) {
        //Update score to the idTaker
        val docRef = db.collection("Users").document(uid)
        docRef.get()
            .addOnSuccessListener { document ->
                val personal = document.data?.get("Home") as HashMap<String, Object>
                val _score = personal["Score"].toString().toFloat()
                val _jobsDone = personal["JobsDone"].toString().toInt()

                val resultScore = ((_score*_jobsDone)+rating)/(_jobsDone+1)
                val roundedScore = java.lang.String.format("%.1f", resultScore)

                val tags = personal["Tags"] as ArrayList<String>
                val containerFireStore = mapOf(
                    "Banner" to personal["Banner"],
                    "Credits" to 1,
                    "Name" to personal["Name"],
                    "Score" to roundedScore.toFloat(),
                    "JobsDone" to _jobsDone+1,
                    "Tags" to arrayListOf(tags[0], tags[1], tags[2])
                )

                val dataSTC = mapOf(
                    "Home" to containerFireStore
                )

                db.collection("Users").document(uid)
                    .update(dataSTC)
                    .addOnSuccessListener {
                        continueOperations()
                    }
                    .addOnFailureListener { e ->
                        Log.d("DBG", "Error fetching document ${e.localizedMessage}")
                    }
            }
            .addOnFailureListener { e ->
                Log.d("DBG", "Error fetching document ${e.localizedMessage}")
            }
    }

    private fun continueOperations() {
        val user = auth.currentUser

        val containerFireStore = mapOf(
            "state" to -1
        )

        db.collection("Jobs").document(user!!.uid)
            .update(containerFireStore)
            .addOnSuccessListener {
                Toast.makeText(this, "Trabajo finalizado", Toast.LENGTH_SHORT).show()

                val intent = Intent(this, HomeView::class.java)
                startActivity(intent)
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "No se puede continuar", Toast.LENGTH_SHORT).show()
            }
    }
}