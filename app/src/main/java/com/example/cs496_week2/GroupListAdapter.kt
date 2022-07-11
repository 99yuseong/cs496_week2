package com.example.cs496_week2

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class GroupListAdapter(var groupList: ArrayList<GroupDT>): RecyclerView.Adapter<GroupListAdapter.MyViewHolder>() {
    private lateinit var itemClickListener : OnItemClickListener

    inner class MyViewHolder(itemView: View?): RecyclerView.ViewHolder(itemView!!) {
        val groupNameTv = itemView!!.findViewById<TextView>(R.id.gi_group_name)

        fun bind(groupItem: GroupDT, position: Int) {
            groupNameTv.text = groupItem.groupName
            val pos = adapterPosition
            if(pos!= RecyclerView.NO_POSITION)
            {
                itemView.setOnClickListener {
                    itemClickListener?.onClick(itemView,pos)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        var view = LayoutInflater.from(parent.context).inflate(R.layout.group_item, parent, false)
        return MyViewHolder(view)
    }

    override fun getItemCount(): Int {
        return groupList.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.bind(groupList[position], position)
    }

    interface OnItemClickListener {
        fun onClick(v: View, position: Int)
    }

    fun setItemClickListener(onItemClickListener: OnItemClickListener) {
        this.itemClickListener = onItemClickListener
    }

}