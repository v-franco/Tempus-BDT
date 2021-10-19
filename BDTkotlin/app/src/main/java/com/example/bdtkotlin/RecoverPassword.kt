package com.example.bdtkotlin

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.widget.Toast
import com.example.bdtkotlin.databinding.ActivityRecoverPasswordBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class RecoverPassword : AppCompatActivity() {
    private lateinit var binding: ActivityRecoverPasswordBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRecoverPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnRcRecover.setOnClickListener {
            val email = binding.inputUser.text.trim().toString()

            if(email.isNotEmpty() and Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                    .addOnSuccessListener {
                        binding.btnRcRecover.isEnabled = false
                        Toast.makeText(this, "Correo de recuperación enviado", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                    .addOnFailureListener { e ->
                        Log.d("DBG", "Error | ${e.localizedMessage}")
                        Toast.makeText(this, "No se pudo enviar el correo", Toast.LENGTH_SHORT).show()
                    }
            }
            else {
                Toast.makeText(this, "Ingrese un correo valido", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnRcClose.setOnClickListener {
            finish()
        }
    }

    override fun onPause() {
        super.onPause()
        finish()
    }
}