package com.example.bdtkotlin

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.QuerySnapshot


class JobCustomAdapter(dataPass: Array<ArrayList<String>>): RecyclerView.Adapter<JobCustomAdapter.ViewHolder>(){
    //0 UIDS
    //1 Titles
    //2 Tag 1
    //3 Tag 2
    //4 Tag 3
    //5 Descripcion

    val titles = dataPass[1]
    val tag1 = dataPass[2]
    val tag2 = dataPass[3]
    val tag3 = dataPass[4]

    //OnClick listener for each job

    private lateinit var mListener: onItemClickListener

    interface onItemClickListener{
        fun onItemClick(position: Int)
    }

    fun setOnItemClickListener(listener:onItemClickListener){
        mListener = listener
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): ViewHolder {
        val v = LayoutInflater.from(viewGroup.context).inflate(R.layout.job_card_layout, viewGroup, false)
        return ViewHolder(v, mListener)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, i: Int) {
        viewHolder.itemJobTitle.text = titles[i]
        viewHolder.itemJobTag1.text = tag1[i]
        viewHolder.itemJobTag2.text = tag2[i]
        viewHolder.itemJobTag3.text = tag3[i]
    }

    override fun getItemCount(): Int {
        return titles.size
    }

    inner class ViewHolder(itemView: View, listener: onItemClickListener): RecyclerView.ViewHolder(itemView){
        var itemJobTitle: TextView
        var itemJobTag1: TextView
        var itemJobTag2: TextView
        var itemJobTag3: TextView

        init{
            itemJobTitle = itemView.findViewById(R.id.job_title)
            itemJobTag1 = itemView.findViewById(R.id.job_tag1)
            itemJobTag2 = itemView.findViewById(R.id.job_tag2)
            itemJobTag3 = itemView.findViewById(R.id.job_tag3)
            itemView.setOnClickListener {
                listener.onItemClick(adapterPosition)
            }
        }
    }
}