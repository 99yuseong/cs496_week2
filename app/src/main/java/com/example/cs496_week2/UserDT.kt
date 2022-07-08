package com.example.cs496_week2

import com.naver.maps.geometry.LatLng
import java.util.*
import kotlin.collections.ArrayList

data class UserDT (
    val id: Long,
    val name: String,
    val imgUrl: String,
    val friend: ArrayList<UserDT>,
    val group: ArrayList<ArrayList<UserDT>>,
    val running: ArrayList<RunningDT>
)

data class RunningDT (
    val date: ArrayList<Date>,
    val coordinate: ArrayList<LatLng>
)