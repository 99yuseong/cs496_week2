package com.example.cs496_week2

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.graphics.drawable.toDrawable
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class GroupListAdapter(private val groupList: ArrayList<GroupDT>): RecyclerView.Adapter<GroupListAdapter.MyViewHolder>() {
    inner class MyViewHolder(itemView: View?): RecyclerView.ViewHolder(itemView!!) {
        val groupNameTv = itemView!!.findViewById<TextView>(R.id.gi_group_name)


        fun bind(groupItem: GroupDT, position: Int) {
            groupNameTv.text = groupItem.groupName
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.group_item, parent, false)
        return MyViewHolder(view)
    }

    override fun getItemCount(): Int {
        return groupList.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.bind(groupList[position], position)
    }
}