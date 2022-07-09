package com.example.cs496_week2

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import java.text.SimpleDateFormat
import java.util.*

class historyListAdapter(private val histories: MutableList<RunningData>) : BaseAdapter(){

    override fun getCount(): Int = histories.size

    override fun getItem(position: Int):  RunningData = histories[position]

    override fun getItemId(position: Int): Long = position.toLong()

    @SuppressLint("SetTextI18n")
    override fun getView(position: Int, view: View?, parent: ViewGroup?): View? {
        var convertView = view
        if (convertView == null) convertView = LayoutInflater.from(parent?.context).inflate(R.layout.history_item, parent, false)

        var history = histories[position]

        var img : ImageView? = convertView?.findViewById(R.id.history_image)
        var location : TextView? = convertView?.findViewById(R.id.history_location)
        var date : TextView? = convertView?.findViewById(R.id.history_date)
        var time : TextView? = convertView?.findViewById(R.id.history_time)
        var dist : TextView? = convertView?.findViewById(R.id.history_distance)
        var pace : TextView? = convertView?.findViewById(R.id.history_pace)

        val dateFormatter = SimpleDateFormat("yyyy / MM / dd", Locale.getDefault())
        val timeFormatter = SimpleDateFormat("HH:mm", Locale.getDefault())

        if (date != null) {
            date.setText(dateFormatter.format(history.date))
        }

        if (time != null) {
            time.setText("${timeFormatter.format(history.date[0])} ~ ${timeFormatter.format(history.date[1])}")
        }

        return convertView
    }
}