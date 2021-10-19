package com.example.bdtkotlin

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bdtkotlin.databinding.ActivitySearchViewBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_search_view.*
import org.w3c.dom.Text

class SearchView : AppCompatActivity() {
    private lateinit var binding: ActivitySearchViewBinding
    private lateinit var auth: FirebaseAuth

    private var searchList: List<SearchModel> = ArrayList()
    private val searchListAdapter = SearchListAdapter(searchList)
    private val firebaseFirestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchViewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth


        binding.menuStFab.setOnItemClickListener { menuButton ->
            if(menuButton == 1) {
                val intent = Intent(this, JobsMain::class.java)
                startActivity(intent)
            }
            if(menuButton == 2) {
                val intent = Intent(this, HomeView::class.java)
                startActivity(intent)
            }
            if(menuButton == 3) {
                val intent = Intent(this, SettingsMain::class.java)
                startActivity(intent)
            }
        }

        SearchJobsRecycleView.hasFixedSize()
        SearchJobsRecycleView.layoutManager = LinearLayoutManager(this)
        SearchJobsRecycleView.adapter = searchListAdapter

        txt_se_search_bar.addTextChangedListener(object: TextWatcher{
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }


            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                val searchText: String = txt_se_search_bar.text.toString()

                searchInFirestore(searchText)
            }

            override fun afterTextChanged(p0: Editable?) {
            }

        })

    }

    private fun searchInFirestore(searchText: String) {
        // Query

        val user = auth.currentUser

        firebaseFirestore.collection("Jobs").whereEqualTo("isLocked",false).orderBy("title").startAt(searchText)
            .endAt("$searchText\uf8ff").get().addOnCompleteListener {
                if (it.isSuccessful){
                    searchList = it.result!!.toObjects(SearchModel::class.java)
                    searchListAdapter.searchList = searchList
                    searchListAdapter.notifyDataSetChanged()

                    val fetched = ClassesContainer().processJobsData(it.result, user!!.uid)

                    val recyclerView = findViewById<RecyclerView>(R.id.SearchJobsRecycleView)
                    val adapter = JobCustomAdapter(fetched)

                    recyclerView.layoutManager = LinearLayoutManager(this)
                    recyclerView.adapter = adapter
                    //OnClick for each item in the list
                    adapter.setOnItemClickListener(object : JobCustomAdapter.onItemClickListener{
                        override fun onItemClick(position: Int) {
                            val intent = Intent(this@SearchView, JobUserView::class.java)
                            intent.putExtra("uid", fetched[0][position]) //Who created
                            intent.putExtra("titles", fetched[1][position])
                            intent.putExtra("descs", fetched[5][position])
                            startActivity(intent)
                        }
                    })
                }
            }
    }


}