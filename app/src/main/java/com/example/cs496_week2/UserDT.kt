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
    val running: ArrayList<RunningData>
)

data class LocationDT (
    var id: Long,
    var lat: Double,
    var lon: Double,
    var imgUrl: String,
    var name: String
)

data class RunningData (
    val date: Date,
    var path: MutableList<LatLng>,
    var time: Double,
    var dist: Double,
    var avgPace: Double,
    var subDist: MutableList<Double>,
){

}