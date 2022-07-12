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
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.overlay.OverlayImage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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

        val startDateCorrection = Calendar.getInstance()
        val endDateCorrection = Calendar.getInstance()
        startDateCorrection.time = history.startDate
        endDateCorrection.time = history.endDate
        startDateCorrection.add(Calendar.HOUR, -9)
        endDateCorrection.add(Calendar.HOUR, -9)

        val dateFormatter = SimpleDateFormat("yyyy.MM.dd.", Locale.getDefault())
        val timeFormatter = SimpleDateFormat("HH:mm", Locale.getDefault())

        val paceMin = (history.avgPace / 60).toInt()
        val paceSec = (history.avgPace % 60).toInt()

        CoroutineScope(Dispatchers.Main).launch {
            val bitmap = withContext(Dispatchers.IO) {
                ImageLoader.loadImage("http://192.249.19.179:80/" + history._id + ".jpg" )
            }
            if (img != null) {
                img.setImageBitmap(bitmap)
            }
        }

        if (location != null) {
            location.setText(getLocationName(position))
        }

        if (date != null) {
            Log.d("date", history.startDate.toString())
            date.setText(dateFormatter.format(startDateCorrection.time))
        }

        if (time != null) {
            time.setText("${timeFormatter.format(startDateCorrection.time)} ~ ${timeFormatter.format(endDateCorrection.time)}")
        }

        if (dist != null) {
            dist.setText("${String.format("%.2f", history.dist / 1000.0)} km")
        }

        if (pace != null) {
            pace.setText("${if(history.dist < 1) 0 else paceMin}' ${if(paceSec >= 10) paceSec else "0${paceSec}"}''")
        }

        return convertView
    }

    fun getLocationName(position: Int) : String {
        Log.d("lat", histories[position].toString())
        Log.d("lat", histories[position].path[0].toString())
        Log.d("lat", histories[position].path[0].latitude.toString())
//        var cityList: String
        if (geocoder.getFromLocation(
                histories[position].path[0].latitude,
                histories[position].path[0].longitude,
                10
            ) != null
        ) {
            return geocoder.getFromLocation(
                histories[position].path[0].latitude,
                histories[position].path[0].longitude,
                10
            ).get(0).adminArea.toString()
        }
        return ""
    }
}