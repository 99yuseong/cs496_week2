package com.example.cs496_week2

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import androidx.core.graphics.drawable.toDrawable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class UserListAdapter(val context: Context, val userList: ArrayList<UserDT>) : BaseAdapter() {
    @SuppressLint("ViewHolder")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view: View = LayoutInflater.from(context).inflate(R.layout.search_user_item, null)

        val name = view.findViewById<TextView>(R.id.nameTv)
        val profileIv = view.findViewById<ImageView>(R.id.profileIv)

        val info = userList[position]

        CoroutineScope(Dispatchers.Main).launch {
            val bitmap = withContext(Dispatchers.IO) {
                ImageLoader.loadImage(info.imgUrl)
            }
            profileIv.setImageDrawable(bitmap?.toDrawable(context.resources))
        }

        name.text = info.name
        return view
    }

    override fun getItem(position: Int): Any {
        return userList[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getCount(): Int {
        return userList.size
    }

}