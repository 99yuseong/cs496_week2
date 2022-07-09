package com.example.cs496_week2

import android.annotation.SuppressLint
import android.location.Geocoder
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import java.text.SimpleDateFormat
import java.util.*
import java.util.Date

class historyListAdapter(private val histories: MutableList<RunningData>, private val geocoder: Geocoder) : BaseAdapter(){

    var city : String = ""

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

        val paceMin = (history.avgPace / 60).toInt()
        val paceSec = (history.avgPace % 60).toInt()

        if (location != null) {
            location.setText(getLocaionName(position))
            Log.d("user", MainActivity.user.running.toString())
        }

        if (date != null) {
            date.setText(dateFormatter.format(history.startDate))
        }

        if (time != null) {
            time.setText("${timeFormatter.format(history.startDate)} ~ ${timeFormatter.format(history.endDate)}")
        }

        if (dist != null) {
            dist.setText("${String.format("%.2f", history.dist / 1000.0)} km")
        }

        if (pace != null) {
            pace.setText("${if(history.dist < 1) 0 else paceMin}' ${if(paceSec >= 10) paceSec else "0${paceSec}"}''")
        }

        return convertView
    }

    fun getLocaionName(position: Int) : String{
        Log.d("lat", histories[position].toString())
        Log.d("lat", histories[position].path[0].toString())
        Log.d("lat", histories[position].path[0].latitude.toString())
        var cityList = geocoder.getFromLocation(histories[position].path[0].latitude, histories[position].path[0].longitude,10)
        if(cityList != null) {
            city = cityList.get(0).adminArea
        }
        return city
    }
}