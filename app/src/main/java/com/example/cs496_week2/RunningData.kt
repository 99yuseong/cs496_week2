package com.example.cs496_week2

import com.naver.maps.geometry.LatLng
import java.security.cert.CertPath

data class RunningData (
    public var path: MutableList<LatLng>,
    public var time: Double,
    public var dist: Double,
    public var avgPace: Double,
    public var subDist: MutableList<Double>,
    ){

}

