package com.example.bdtkotlin

import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.*
import java.io.ByteArrayOutputStream
import java.io.File
import kotlin.collections.ArrayList

class ClassesContainer {
    fun uploadImage(storage:FirebaseStorage ,userUID:String, picName:String, picture:Bitmap, showToast:Boolean, context: AppCompatActivity) {
        val data = bitmapToByteArray(picture)

        val storagePath = storage.reference.child("$userUID/$picName.jpg")
        val uploadTask = storagePath.putBytes(data)

        uploadTask
            .addOnSuccessListener { takeSnapshot ->
                Log.d("DBGR", "Image Upload completed")
                if(showToast) {
                    Toast.makeText(context, "Archivo cargado con exito", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Log.d("DBGR","Image Upload Failed ${e.localizedMessage}")
                if(showToast) {
                    Toast.makeText(context, "No se pudo cargar el archivo", Toast.LENGTH_SHORT).show()
                }
            }
    }

    fun uploadFile(storage:FirebaseStorage, userUID: String, fileName:String, path:Uri, showToast: Boolean, context: AppCompatActivity) {
        //var file = Uri.fromFile(File(path))

        val riversRef = storage.reference.child("$userUID/$fileName.pdf")
        val uploadTask = riversRef.putFile(path)

        uploadTask
            .addOnSuccessListener { taskSnapshot ->
                Log.d("DBGR", "File Upload completed")
                if(showToast) {
                    Toast.makeText(context, "Archivo cargado con exito", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Log.d("DBGR","File Upload Failed ${e.localizedMessage}")
                if(showToast) {
                    Toast.makeText(context, "No se pudo cargar el archivo", Toast.LENGTH_SHORT).show()
                }
            }
    }

    //Deprecated
    fun downloadFile(storage:FirebaseStorage, userUID: String, picName: String): String {
        var myByteArray: ByteArray = byteArrayOf()

        CoroutineScope(Dispatchers.IO).launch {
            val storageRef = storage.reference
            var islandRef = storageRef.child("$userUID/$picName.jpg")

            val ONE_MEGABYTE: Int = 1024*1024

            islandRef.getBytes(ONE_MEGABYTE.toLong())
                .addOnSuccessListener {
                    myByteArray = it
                    //bitmap = BitmapFactory.decodeByteArray(myByteArray, 0, myByteArray.size)
                    Log.d("DBGF", "Imagen descargada")
                }

                .addOnFailureListener {
                    // Handle any errors
                    Log.d("DBGF", "Descarga fallida")
                }
        }
        Log.d("DBGF", "Termina funcion")
        return myByteArray.toString()
    }

    fun bitmapToByteArray(picture: Bitmap): ByteArray {
        val baos = ByteArrayOutputStream()
        picture.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        return baos.toByteArray()
    }

    fun processJobsData(dataPass: QuerySnapshot, uidThis:String): Array<ArrayList<String>> {
        //0 UIDS
        //1 Titles
        //2 Tag 1
        //3 Tag 2
        //4 Tag 3
        //5 Descripcion

        val uids = arrayListOf<String>()
        val titles = arrayListOf<String>()
        val tag1 = arrayListOf<String>()
        val tag2 = arrayListOf<String>()
        val tag3 = arrayListOf<String>()
        val desc = arrayListOf<String>()

        for (document in dataPass) {
            val state:Int = document.data["state"].toString().toInt()
            val isLocked:Boolean = document.data["isLocked"].toString().toBoolean()

            if(document.data["idCreator"] != uidThis) {
                if(!isLocked) {
                    if(state != -1) {
                        uids.add(document.id)
                        titles.add(document.data["title"].toString())
                        tag1.add(document.data["tag1"].toString())
                        tag2.add(document.data["tag2"].toString())
                        tag3.add(document.data["tag3"].toString())
                        desc.add(document.data["description"].toString())
                    }
                }
            }
        }

        return arrayOf(uids, titles, tag1, tag2, tag3, desc)
    }

    class CASTERProcessDynamicsJobs(val items: ArrayList<ItemsJobs>, val dataArray: Array<ArrayList<String>>)

    fun processDynamicsJobs(dataPass: QuerySnapshot): CASTERProcessDynamicsJobs {
        val items = arrayListOf<ItemsJobs>()

        val titles = arrayListOf<String>()      //0
        val names = arrayListOf<String>()       //1
        val emails = arrayListOf<String>()      //2
        val phones = arrayListOf<String>()      //3
        val descs = arrayListOf<String>()       //4
        val states = arrayListOf<String>()      //5
        val idTaker = arrayListOf<String>()     //6
        val idCreator = arrayListOf<String>()   //7

        for(document in dataPass) {
            val state:Int = document.data["state"].toString().toInt()

            if(state != -1) {
                items.add(ItemsJobs(document.data["name"].toString(), document.data["state"].toString()))
                titles.add(document.data["title"].toString())
                names.add(document.data["name"].toString())
                emails.add(document.data["email"].toString())
                phones.add(document.data["phone"].toString())
                descs.add(document.data["description"].toString())
                states.add(document.data["state"].toString())
                idTaker.add(document.data["idTaker"].toString())
                idCreator.add(document.data["idCreator"].toString())
            }
        }

        return CASTERProcessDynamicsJobs(items, arrayOf(titles, names, emails, phones, descs, states, idTaker, idCreator))
    }
}