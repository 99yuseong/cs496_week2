package com.example.cs496_week2

import com.naver.maps.geometry.LatLng
import java.security.cert.CertPath

data class RunningData (
    val path: List<LatLng>,
    val time: Double,
    val dist: Double,
    val avgPace: Double,
    val subDist: List<Double>,
    ){
}