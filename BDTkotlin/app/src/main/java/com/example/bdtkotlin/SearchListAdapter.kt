package com.example.bdtkotlin

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.job_card_layout.view.*

class SearchListAdapter (var searchList: List<SearchModel>): RecyclerView.Adapter<SearchListAdapter.SearchListViewHolder>() {

    class SearchListViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        fun bind(searchModel: SearchModel) {
            itemView.job_title.text = searchModel.title
            ///*
            itemView.job_tag1.text = searchModel.tag1
            itemView.job_tag2.text = searchModel.tag2
            itemView.job_tag3.text = searchModel.tag3
            //*/
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): SearchListAdapter.SearchListViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.job_card_layout, parent, false)
        return SearchListViewHolder(view)
    }

    override fun onBindViewHolder(holder: SearchListAdapter.SearchListViewHolder, position: Int) {
        holder.bind(searchList[position])
    }

    override fun getItemCount(): Int {
        return searchList.size
    }

}