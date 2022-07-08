package com.example.cs496_week2

import android.location.Location
import com.naver.maps.geometry.LatLng
import java.util.*
import kotlin.collections.ArrayList

data class UserDT (
    val id: Long,
    val name: String,
    val imgUrl: String,
    val friends: ArrayList<UserDT>,
    val group: ArrayList<ArrayList<UserDT>>,
    val running: ArrayList<RunningDT>
)

data class RunningDT (
    val date: ArrayList<Date>,
    val coordinate: ArrayList<LatLng>
)

data class LocationDT (
    var id: Long,
    var latLng: LatLng,
)