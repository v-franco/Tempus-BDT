package com.example.bdtkotlin

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider.getUriForFile
import com.example.bdtkotlin.databinding.ActivityRegisterViewBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import java.io.ByteArrayOutputStream
import java.io.File

private const val FILE_NAME = "photoOne"
private const val FILE_NAME2 = "photoTwo"
private const val REQUEST_CODE = 42
private lateinit var photoFile: File

class RegisterView : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var storage: FirebaseStorage
    private lateinit var binding: ActivityRegisterViewBinding

    private lateinit var activityForResultLauncher: ActivityResultLauncher<Intent>

    private class STCUserInfo {
        lateinit var name:String
        lateinit var email:String
        lateinit var password:String
    }

    private var userinfo = STCUserInfo()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterViewBinding.inflate(layoutInflater)
        setContentView(binding.root)


        auth = Firebase.auth
        db = Firebase.firestore
        storage = Firebase.storage


        binding.textViewAcceder.setOnClickListener {
            finish()
        }

        binding.termsConditions.setOnClickListener {
            val openTerms =Intent(Intent.ACTION_VIEW)
            openTerms.data =  Uri.parse("https://docs.google.com/document/d/1TSyaGm2982EtXAw2XFHdph3yWw8ZhYc5/edit?usp=sharing&ouid=117497648589784769211&rtpof=true&sd=true")
            startActivity(openTerms)
        }

        //Button Register trigger
        binding.btnRegister.setOnClickListener {
            val _email = binding.inputAccederUser.text.toString().trim()
            val _name = binding.inputNames.text.toString().trim()
            val _pass = binding.inputAccederPassword.text.toString().trim()
            val _rePass = binding.inputAccederRePass.text.toString().trim()
            val _checkBoxState = binding.checkBoxTerms.isChecked

            if(_email.isNotEmpty() and Patterns.EMAIL_ADDRESS.matcher(_email).matches()) {
                userinfo.email = _email

                if(_name.isNotEmpty()) {
                    userinfo.name = _name

                    if(_pass.length >= 8) {
                        if(_pass == _rePass) {
                            userinfo.password = _pass

                            if(_checkBoxState) {
                                createUser(_email, _pass)
                            }
                            else {
                                binding.textErrors.text = "Debe de aceptar los terminos"
                                binding.textErrors.visibility = View.VISIBLE
                            }
                        }
                        else {
                            binding.textErrors.text = "Las contraseñas no coinciden"
                            binding.textErrors.visibility = View.VISIBLE
                        }
                    }
                    else {
                        binding.textErrors.text = "La longitud debe de ser mayor a 8"
                        binding.textErrors.visibility = View.VISIBLE
                    }
                }
                else {
                    binding.textErrors.text = "Especifique un nombre"
                    binding.textErrors.visibility = View.VISIBLE
                }
            }
            else {
                binding.textErrors.text = "Email invalido"
                binding.textErrors.visibility = View.VISIBLE
            }
        }
    }


    //Normal Functions
    private fun createUser(email:String, password:String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d("DBGR", "createUserWithEmail:success")

                    val user = auth.currentUser!!
                    createUserContainer(user.uid)

                    //Verification
                    val intent = Intent(this, RegisterVerificationView::class.java)
                    intent.putExtra("email", userinfo.email)
                    startActivity(intent)
                    finish()

                    //updateUI(user)
                } else {
                    // If sign in fails, display a message to the user.
                    Log.d("DBGR", "createUserWithEmail:failure", task.exception)

                    if(task.exception.localizedMessage.toString() == "The email address is already in use by another account.") {
                        Toast.makeText(baseContext, "Ya esta registrado ese correo", Toast.LENGTH_SHORT).show()
                        binding.inputAccederUser.setText("")
                    }
                    else {
                        Toast.makeText(baseContext, "No se pudo crear el usuario", Toast.LENGTH_SHORT).show()
                    }

                    //updateUI(null)
                }
            }
    }

    //Firestore
    private fun createUserContainer(userUID:String) {
        //Gender: [0] No defined, [1] Men, [2] Women
        val personal = hashMapOf(
            "Email" to userinfo.email,
            "Name" to userinfo.name,
            "Gender" to 0,
            "AP" to "",
            "AM" to "",
            "Phone" to "",
            "Address" to ""
        )

        val home = hashMapOf(
            "Name" to userinfo.name,
            "Credits" to 0,
            "Score" to 0,
            "JobsDone" to 0,
            "Banner" to "",
            "Tags" to arrayListOf("","","")
        )

        val dataSTC = hashMapOf(
            "newUser" to true,
            "Home" to home,
            "Personal" to personal
        )

        db.collection("Users").document(userUID)
            .set(dataSTC)
            .addOnSuccessListener {
                Log.d("DBGR", "Info User successfully written!")
            }
            .addOnFailureListener { e ->
                Log.d("DBGR", "Error writing document", e)
            }
    }


    /*Deprecated
    //Photo functions
    private fun getPhotoFile(fileName: String): File {
        val storageDirectory = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(fileName, ".jpg", storageDirectory)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if(requestCode == REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            //val takenImagePreview = data?.extras?.get("data") as Bitmap
            //val takenImage = BitmapFactory.decodeFile(photoFile.absolutePath)

            val match = photoFile.absolutePath.indexOf("photoOne",0)

            if(match != -1) {
                //Is photo One / Front
                //userinfo.idFront = BitmapToBase64(BitmapFactory.decodeFile(photoFile.absolutePath))
                userinfo.idFront = BitmapFactory.decodeFile(photoFile.absolutePath)
                userinfo.idFrontBool = true
                binding.iconRgINEFState.visibility = View.VISIBLE
            }
            else {
                //Is photo Two / Back
                //userinfo.idBack = BitmapToBase64(BitmapFactory.decodeFile(photoFile.absolutePath))
                userinfo.idBack = BitmapFactory.decodeFile(photoFile.absolutePath)
                userinfo.idBackBool = true
                binding.iconRgINEBState.visibility = View.VISIBLE
            }

        }
        else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }
    */

}