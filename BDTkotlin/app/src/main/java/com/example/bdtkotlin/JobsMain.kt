package com.example.bdtkotlin

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bdtkotlin.databinding.ActivityJobsMainBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import kotlinx.android.synthetic.main.activity_jobs_main.*
import java.lang.reflect.Array
import java.util.ArrayList

class JobsMain : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var storage: FirebaseStorage
    private lateinit var binding: ActivityJobsMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.supportActionBar?.hide()
        binding = ActivityJobsMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        auth = Firebase.auth
        db = Firebase.firestore
        storage = Firebase.storage



        loadInterface()



        binding.addJobButton.setOnClickListener { addJobButton ->
            val intent = Intent(this, CreateJob::class.java)
            startActivity(intent)
        }

        binding.menuStFab.setOnItemClickListener { menuButton ->
            if(menuButton == 2) {
                val intent = Intent(this, HomeView::class.java)
                startActivity(intent)
            }
            if(menuButton == 3) {
                val intent = Intent(this, SettingsMain::class.java)
                startActivity(intent)
            }
            if(menuButton == 0) {
                val intent = Intent(this, SearchView::class.java)
                startActivity(intent)
            }

        }
    }

    override fun onResume() {
        super.onResume()
        loadInterface()
    }

    private fun loadInterface() {
        val user = auth.currentUser

        db.collection("Jobs")
            .get()
            .addOnSuccessListener { result ->
                //Fetch data FireStore
                val fetched = ClassesContainer().processJobsData(result, user!!.uid)

                val recyclerView = findViewById<RecyclerView>(R.id.JobsRecycleView)
                val adapter = JobCustomAdapter(fetched)

                recyclerView.layoutManager = LinearLayoutManager(this)
                recyclerView.adapter = adapter
                //OnClick for each item in the list
                adapter.setOnItemClickListener(object : JobCustomAdapter.onItemClickListener{
                    override fun onItemClick(position: Int) {
                        val intent = Intent(this@JobsMain, JobUserView::class.java)
                        intent.putExtra("uid", fetched[0][position]) //Who created
                        intent.putExtra("titles", fetched[1][position])
                        intent.putExtra("descs", fetched[5][position])
                        startActivity(intent)
                    }
                })
            }
            .addOnFailureListener { exception ->
                val dialog = AlertDialog.Builder(this)
                    .setTitle("Error")
                    .setMessage("Error al cargar datos")
                    .setNeutralButton("OK") { view, _ ->
                        view.dismiss()
                        val intent = Intent(this, HomeView::class.java)
                        startActivity(intent)
                        finish()
                    }
                    .setCancelable(false)
                    .create()
                dialog.show()
            }
    }
}