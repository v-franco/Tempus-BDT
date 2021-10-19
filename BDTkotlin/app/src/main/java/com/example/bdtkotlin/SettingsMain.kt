package com.example.bdtkotlin

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import com.example.bdtkotlin.databinding.ActivitySettingsMainBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class SettingsMain : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var binding: ActivitySettingsMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding =  ActivitySettingsMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        auth = Firebase.auth


        binding.btnStProfile.setOnClickListener{
            val intent = Intent(this, SettingsProfile::class.java)
            startActivity(intent)
        }


        binding.btnStLogout.setOnClickListener {
            logout()
        }

        binding.menuStFab.setOnItemClickListener { menuButton ->
            if(menuButton == 1) {
                val intent = Intent(this, JobsMain::class.java)
                startActivity(intent)
            }
            if(menuButton == 2) {
                val intent = Intent(this, HomeView::class.java)
                startActivity(intent)
            }
            if(menuButton == 0) {
                val intent = Intent(this, SearchView::class.java)
                startActivity(intent)
            }
        }
    }

    private fun logout() {
        val database = AppDatabase.getDatabase(this)

        CoroutineScope(Dispatchers.IO).launch {
            database.userDataPersonal().nuke()
            database.userDataHome().nuke()
        }

        val sharedPreferences = getSharedPreferences("sharedPrefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.apply {
            putBoolean("isLogged", false)
        }.apply()

        auth.signOut()

        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intent)
        finish()
    }
}