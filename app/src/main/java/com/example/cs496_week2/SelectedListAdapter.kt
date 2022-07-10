package com.example.cs496_week2

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class SelectedListAdapter(private val selectedList: ArrayList<UserDT>): RecyclerView.Adapter<SelectedListAdapter.MyViewHolder>() {
    inner class MyViewHolder(itemView: View?): RecyclerView.ViewHolder(itemView!!) {
        val nameTv = itemView!!.findViewById<TextView>(R.id.si_name)
        fun bind(userItem: UserDT, position: Int) {
            nameTv.text = userItem.name
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.selected_friend_item, parent, false)
        return MyViewHolder(view)
    }

    override fun getItemCount(): Int {
        return selectedList.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.bind(selectedList[position], position)
//        holder.itemView.setOnClickListener {
//            itemClickListener.onClick(it, position)
//        }
    }

//    interface OnItemClickListener {
//        fun onClick(v: View, position: Int)
//    }

//    fun setItemClickListener(onItemClickListener: OnItemClickListener) {
//        this.itemClickListener = onItemClickListener
//    }
}
//
//class GalleryAdapter(private val galleryList: ArrayList<GalleryItem>): RecyclerView.Adapter<GalleryAdapter.MyViewHolder>() {
//    private lateinit var itemClickListener : OnItemClickListener
//
//    inner class MyViewHolder(itemView: View?): RecyclerView.ViewHolder(itemView!!) {
//        val image = itemView!!.findViewById<ImageView>(R.id.galleryImage)
//        fun bind(galleryItem: GalleryItem, position: Int) {
//            image.setImageDrawable(galleryItem.img)
//        }
//    }
//
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
//        val view = LayoutInflater.from(parent.context).inflate(R.layout.gallery_item, parent, false)
//        return MyViewHolder(view)
//    }
//
//    override fun getItemCount(): Int {
//        return galleryList.size
//    }
//
//    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
//        holder.bind(galleryList[position], position)
//        holder.itemView.setOnClickListener {
//            itemClickListener.onClick(it, position)
//        }
//    }
//
//    interface OnItemClickListener {
//        fun onClick(v: View, position: Int)
//    }
//
//    fun setItemClickListener(onItemClickListener: OnItemClickListener) {
//        this.itemClickListener = onItemClickListener
//    }
//
//}