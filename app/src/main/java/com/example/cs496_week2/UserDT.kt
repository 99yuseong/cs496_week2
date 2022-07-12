package com.example.cs496_week2

import android.location.Location
import com.naver.maps.geometry.LatLng
import java.util.*
import kotlin.collections.ArrayList

data class UserDT (
    val _id: String,
    val id: Long,
    val name: String,
    val imgUrl: String,
    val friends: ArrayList<String>,
    val group: ArrayList<String>,
    val running: ArrayList<String>
)

data class LocationDT (
    var _id: String,
    var id: Long,
    var room: String,
    var lat: Double,
    var lon: Double,
    var imgUrl: String,
    var name: String
)

data class RunningData (
    var _id : String,
    val user: String,
    var startDate: Date,
    var endDate: Date,
    var path: MutableList<LatLng>,
    var time: Double,
    var dist: Double,
    var avgPace: Double,
    var subDist: MutableList<Double>,
)

data class ResponseDT (
    val message: String
)

data class GroupDT (
    val _id: String,
    val groupName: String,
    val member: ArrayList<String>
)

data class RunningStatisticDT (
    val id: String,
    val name: String,
    val runs: Int,
    val dist: Double,
    val avgPace: Double
)