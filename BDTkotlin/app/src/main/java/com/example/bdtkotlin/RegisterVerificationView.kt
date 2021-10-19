package com.example.bdtkotlin

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.example.bdtkotlin.databinding.ActivityRegisterVerificationViewBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.auth.ktx.userProfileChangeRequest
import com.google.firebase.ktx.Firebase

class RegisterVerificationView : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var binding: ActivityRegisterVerificationViewBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterVerificationViewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth
        val user = auth.currentUser

        if (user != null) {
            binding.textRgSendto2.text = user.email.toString()
        }

        //Button Close
        binding.btnUvjusrClose2.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        //Button Send email verification
        binding.btnRgvSendV2.setOnClickListener {
            sendEmailVerification()
            binding.btnRgvSendV2.visibility = View.INVISIBLE
        }

        //Button Continue
        binding.btnRgvContinue2.setOnClickListener {
            val profileUpdates = userProfileChangeRequest { }

            user!!.updateProfile(profileUpdates)
                .addOnCompleteListener { task ->
                    if(task.isSuccessful) {
                        if(user.isEmailVerified) {
                            Toast.makeText(this, "Usuario Verificado", Toast.LENGTH_SHORT).show()
                            auth.signOut()
                            reload()
                        }
                        else {
                            Toast.makeText(this, "Aun no se verifica", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
        }
    }

    private fun sendEmailVerification() {
        val user = auth.currentUser
        user!!.sendEmailVerification()
            .addOnCompleteListener {
                Toast.makeText(this, "Se envio un correo de verificacion", Toast.LENGTH_SHORT).show()
            }
    }

    /*
    public override fun onStart() {
        super.onStart()
        val currentUser = auth.currentUser
        if(currentUser != null) {
            if(currentUser.isEmailVerified) {
                reload()
            }
            else {
                sendEmailVerification()
            }
        }
    }
*/
    private fun reload() {
        val intent = Intent(this, MainActivity::class.java)
        this.startActivity(intent)
    }
}