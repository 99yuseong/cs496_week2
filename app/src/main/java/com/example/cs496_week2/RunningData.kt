package com.example.cs496_week2

import com.naver.maps.geometry.LatLng
import java.security.cert.CertPath

data class RunningData (
    val path: List<LatLng>,
    val runningTime: Int,
    val runningDist: Double,
    val avgPace: Double,
    val subDist: List<Double>,
    val subTime: List<Int>
    ){
}