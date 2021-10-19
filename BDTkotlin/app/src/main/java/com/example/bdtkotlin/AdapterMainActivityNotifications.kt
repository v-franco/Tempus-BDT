package com.example.bdtkotlin

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import kotlinx.android.synthetic.main.list_hm_notification.view.*


class Items(val titulo:String, val desc:String, val iconSelect:Int)

class AdapterMainActivityNotifications(private val mContext: Context, private val listNoti: List<Items>): ArrayAdapter<Items>(mContext, 0, listNoti) {
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val layout = LayoutInflater.from(mContext).inflate(R.layout.list_hm_notification, parent, false)

        val item = listNoti[position]

        layout.text_list_hm_title.text = item.titulo
        layout.text_list_hm_desc.text = item.desc

        if(item.iconSelect == 0) {
            layout.image_list_hm.setImageResource(R.drawable.hm_vector_petition)
        }
        else {
            layout.image_list_hm.setImageResource(R.drawable.hm_vector_friend)
        }

        return layout
    }
}