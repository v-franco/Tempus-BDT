package com.example.bdtkotlin

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import kotlinx.android.synthetic.main.list_hm_jobs.view.*
import kotlinx.android.synthetic.main.list_hm_notification.view.*
import java.io.Serializable





class ItemsJobs(val titulo: String, val state:String):Serializable

class AdapterMainActivityJobs(private val mContext: Context, private val listNoti: List<ItemsJobs>, private val typeOP: Int): ArrayAdapter<ItemsJobs>(mContext, 0, listNoti) {
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val layout = LayoutInflater.from(mContext).inflate(R.layout.list_hm_jobs, parent, false)

        val item = listNoti[position]
        val state = item.state.toInt()


        if(typeOP == 1) {
            layout.image_list_hm_jobs.setImageResource(R.drawable.menu_vector_search)
            layout.text_list_hm_jobs_title.text = "Tu trabajo"
            layout.text_list_hm_jobs_title.setTextSize(16F)

            if(state == 0) {
                layout.text_list_hm_jobs_desc.text = "Esperando..."
                layout.text_list_hm_jobs_desc.setTextSize(14F)
            }
            else if(state == 1) {
                layout.text_list_hm_jobs_desc.text = "Informacion del trabajador"
                layout.text_list_hm_jobs_desc.setTextSize(14F)
            }
            else if(state == 2){
                layout.text_list_hm_jobs_desc.text = "Trabajando..."
                layout.text_list_hm_jobs_desc.setTextSize(14F)
            }
            else if(state == 3) {
                layout.text_list_hm_jobs_desc.text = "Califca el trabajo"
                layout.text_list_hm_jobs_desc.setTextSize(14F)
            }
        }
        else {
            layout.image_list_hm_jobs.setImageResource(R.drawable.menu_vector_myjobs)
            layout.text_list_hm_jobs_title.text = "Trabajo tomado"
            layout.text_list_hm_jobs_title.setTextSize(16F)

            if(state == 0) {
                layout.text_list_hm_jobs_desc.text = "Esperando..."
            }
            else if(state == 1) {
                layout.text_list_hm_jobs_desc.text = "Informacion del trabajo"
                layout.text_list_hm_jobs_desc.setTextSize(14F)
            }
            else if(state == 2){
                layout.text_list_hm_jobs_desc.text = "Trabajando..."
                layout.text_list_hm_jobs_desc.setTextSize(14F)
            }
            else if(state == 3) {
                layout.text_list_hm_jobs_desc.text = "Esperando la calificacion"
                layout.text_list_hm_jobs_desc.setTextSize(14F)
            }
        }

        return layout
    }
}