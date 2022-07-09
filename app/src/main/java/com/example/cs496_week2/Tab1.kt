package com.example.cs496_week2

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.PointF
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.NonNull
import androidx.core.app.ActivityCompat
import com.example.cs496_week2.databinding.FragmentTab1Binding
import com.google.android.gms.location.*
import com.google.gson.Gson
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.*
import com.naver.maps.map.overlay.LocationOverlay
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.overlay.OverlayImage
import com.naver.maps.map.overlay.PolylineOverlay
import com.naver.maps.map.util.FusedLocationSource
import io.socket.client.Socket
import io.socket.emitter.Emitter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.IOException
import java.net.MalformedURLException
import java.net.URL
import java.sql.Struct
import java.util.*
import java.util.concurrent.locks.ReentrantLock

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class Tab1 : Fragment(), OnMapReadyCallback {
    private var param1: String? = null
    private var param2: String? = null

    private lateinit var binding: FragmentTab1Binding
    private lateinit var root: View
    private lateinit var mainActivity: MainActivity

    private lateinit var mapView: MapView

    private lateinit var locationSource: FusedLocationSource // 위치를 반환하는 구현체
    private lateinit var naverMap: NaverMap
    private var mFusedLocationProviderClient: FusedLocationProviderClient? = null // 현재 위치를 가져오기 위한 변수
    lateinit var mLastLocation: Location // 위치 값을 가지고 있는 객체
    internal lateinit var mLocationRequest: LocationRequest // 위치 정보 요청의 매개변수를 저장하는
    private val REQUEST_PERMISSION_LOCATION = 10
    private val LOCATION_PERMISSTION_REQUEST_CODE: Int = 1000

    // timer
    private var isRunning = false
    private var timerTask: Timer? = null
    private var date: MutableList<Date> = mutableListOf()
    private var time = 0.0
    private var dist = 0.0
    private var subDist: Double = 0.0
    private var avgPace: Double = 0.0
    lateinit var prvCoord: LatLng
    private lateinit var path: MutableList<LatLng>
    private var subDistList: MutableList<Double> = mutableListOf(0.0)
    val l = ReentrantLock()
    private var totLat : Double = 0.0
    private var totLon : Double = 0.0
    private var runStart: Boolean = true

    //btns
    lateinit var startBtn: ImageView
    lateinit var stopBtn: ImageView

    // running DATA list
    private var firstRunning: Boolean = true
//    lateinit var runningData: MutableList<RunningData>

    // pathline
    var pathLine = PolylineOverlay()

    // interface
    lateinit var timeView : TextView
    lateinit var kmView : TextView
    lateinit var paceView : TextView
    lateinit var infoLayout : LinearLayout
    var min = 0
    var sec = 0
    var km = 0.0
    var paceMin = 0
    var paceSec = 0
//    lateinit var locationOverlay : LocationOverlay
    private val marker = Marker()

    // friends
    lateinit var user: UserDT
    lateinit var locationData : LocationDT
    var runningFriends : MutableList<LocationDT> = mutableListOf()

    // socket
    lateinit var mSocket: Socket

    // data
    lateinit var curRunningData: RunningData

    // Activity 형변환
    override fun onAttach(context: Context) {
        super.onAttach(context)
        mainActivity = context as MainActivity
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
        binding = FragmentTab1Binding.inflate(layoutInflater)
        // 현재 위치 (네이버지도 좌표)
        locationSource = FusedLocationSource(this, LOCATION_PERMISSTION_REQUEST_CODE)
        // 현재 위치 (GPS 좌표)
        mLocationRequest =  LocationRequest.create().apply {
            interval = 1000
            fastestInterval = 1000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            maxWaitTime = 1000
        }
        startLocationUpdates()
        mSocket = SocketApplication.get()
        mSocket.connect()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        root = inflater.inflate(R.layout.fragment_tab1, container, false)

        timeView = root.findViewById(R.id.time)
        kmView = root.findViewById(R.id.kilometer)
        paceView = root.findViewById(R.id.pace)
        infoLayout = root.findViewById(R.id.runningInfo)

        startBtn = root.findViewById(R.id.start)
        stopBtn = root.findViewById(R.id.stop)

        startBtn.setOnClickListener {
            if(!isRunning) {
                start()
                infoLayout.layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT
                )
                mapView.layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    700
                )
            } else {
                pause()
            }
        }
        stopBtn.setOnClickListener {
            reset()
            infoLayout.layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0
            )
            mapView.layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )
        }
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mapView = view.findViewById(R.id.map)
        mapView.onCreate(savedInstanceState)
        mapView.onResume()
        mapView.getMapAsync(this)
    }

    override fun onMapReady(@NonNull naverMap: NaverMap) {
        this.naverMap = naverMap
        val uiSettings = naverMap.uiSettings
        naverMap.locationSource = locationSource
//        locationOverlay = naverMap.locationOverlay
        naverMap.locationTrackingMode = LocationTrackingMode.Follow
        naverMap.mapType = NaverMap.MapType.Navi
//        uiSettings.isZoomControlEnabled = false
//        naverMap.isNightModeEnabled = true

//        var imageUrl = MainActivity.kakaoUser.profileUrl
//        CoroutineScope(Dispatchers.Main).launch {
//            val bitmap = withContext(Dispatchers.IO) {
//                ImageLoader.loadImage(imageUrl!!)
//            }
//            marker.icon = OverlayImage.fromBitmap(bitmap!!)
//            marker.captionText = MainActivity.kakaoUser.nickname!!
//            marker.zIndex = 100
//        }
    }

    override fun onStart() {
        super.onStart()
        mapView.onStart()
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView.onSaveInstanceState(outState)
    }

    override fun onStop() {
        super.onStop()
        mapView.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }

    private fun startLocationUpdates() {
        //FusedLocationProviderClient의 인스턴스를 생성.
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(mainActivity)
        if (ActivityCompat.checkSelfPermission(mainActivity, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(mainActivity,android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return
        }
        // 기기의 위치에 관한 정기 업데이트를 요청하는 메서드 실행
        // 지정한 루퍼 스레드(Looper.myLooper())에서 콜백(mLocationCallback)으로 위치 업데이트를 요청
        mFusedLocationProviderClient!!.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper())
    }

    // 시스템으로 부터 위치 정보를 콜백으로 받음
    private val mLocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            // 시스템에서 받은 location 정보를 onLocationChanged()에 전달
            locationResult.lastLocation
            onLocationChanged(locationResult.lastLocation)
        }
    }

    // 시스템으로 부터 받은 위치정보를 화면에 갱신해주는 메소드
    fun onLocationChanged(location: Location) {
        l.lock()
        try {
            mLastLocation = location
            if(isRunning) {
                val cameraUpdate = CameraUpdate.scrollTo(LatLng(mLastLocation.latitude, mLastLocation.longitude))
                    .pivot(PointF(0.5f, 0.5f)).animate(CameraAnimation.Easing, 500)
                naverMap.moveCamera(cameraUpdate)
//                marker.position = LatLng(mLastLocation.latitude, mLastLocation.longitude)
//                marker.map = naverMap
            }
        } finally {
            l.unlock()
        }
    }

//    // 위치 권한이 있는지 확인하는 메서드
//    private fun checkPermissionForLocation(context: Context): Boolean {
//        // Android 6.0 Marshmallow 이상에서는 위치 권한에 추가 런타임 권한이 필요
//        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            if (context.checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
//                true
//            } else {
//                // 권한이 없으므로 권한 요청 알림 보내기
//                ActivityCompat.requestPermissions(mainActivity, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_PERMISSION_LOCATION)
//                false
//            }
//        } else {
//            true
//        }
//    }

    // 사용자에게 권한 요청 후 결과에 대한 처리 로직
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_PERMISSION_LOCATION) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startLocationUpdates()

            } else {
                Log.d("ttt", "onRequestPermissionsResult() _ 권한 허용 거부")
                Toast.makeText(mainActivity, "권한이 없어 해당 기능을 실행할 수 없습니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun start() {
        isRunning = true
        stopBtn.visibility = View.VISIBLE
        startBtn.setImageResource(R.drawable.ic_round_pause_24)

        if(runStart){
            prvCoord = LatLng(mLastLocation.latitude, mLastLocation.longitude)
            path = mutableListOf(LatLng(mLastLocation.latitude, mLastLocation.longitude))
            totLat = mLastLocation.latitude
            totLon = mLastLocation.longitude
            date.add(Date(System.currentTimeMillis()))
            runStart = false
            locationData = LocationDT(MainActivity.kakaoUser.id!!, prvCoord.latitude, prvCoord.longitude, "imgUrl", "name" )
            mSocket.on("message", Emitter.Listener {
                var jObject = JSONObject(it[0].toString())
                var id = jObject.getString("id")
                var lat = jObject.getString("lat")
                var lon = jObject.getString("lon")
                var name = jObject.getString("name")
                var imgUrl = jObject.getString("imgUrl")
                var inFriendList = false

                if (runningFriends.size == 0 && id.toLong() != MainActivity.kakaoUser.id) {
                    runningFriends.add(LocationDT(id.toLong(), lat.toDouble(), lon.toDouble(), imgUrl, name))
                } else {
                    for (i in 0..runningFriends.size-1) {
                        if(id.toLong() == runningFriends[i].id) {
                            inFriendList = true
                        }
                    }
                    if(!inFriendList && id.toLong() != MainActivity.kakaoUser.id) {
                        runningFriends.add(LocationDT(id.toLong(), lat.toDouble(), lon.toDouble(), imgUrl, name))
                    }
                }
            })
        }

        timerTask = kotlin.concurrent.timer(period = 250) {	// timer() 호출
            mainActivity.runOnUiThread {
                l.lock()
                try {
                    // 경로 좌표
                    if ( (time * 100) % 100 == 0.0 ){
                        startLocationUpdates()
                        path.add(LatLng(mLastLocation.latitude, mLastLocation.longitude))
                        locationData.lat = mLastLocation.latitude
                        locationData.lon = mLastLocation.longitude
                        totLat += mLastLocation.latitude
                        totLon += mLastLocation.longitude
                        // 부분 거리
                        subDist = calDist(
                            prvCoord.latitude,
                            prvCoord.longitude,
                            mLastLocation.latitude,
                            mLastLocation.longitude
                        )
                        // 총 거리
                        dist += subDist
                        // 부분 거리 리스트
                        subDistList.add(subDist)
                        // 부분 거리 리스트
                        // 평균 페이스
                        avgPace = time / (dist / 1000.0)
                        linePath()
//                        Log.d("running Data", "path : ${path.toString()}")
//                        Log.d("subDist", "subDist : ${subDist}")
//                        Log.d("dist", "dist : ${dist}")
//                        Log.d("subDistList", "subDistList : ${subDistList.toString()}")
//                        Log.d("prevCoord", "prevCoord : ${prvCoord}")
//                        Log.d("time", "time: ${time}")
                        prvCoord = LatLng(mLastLocation.latitude, mLastLocation.longitude)
                        min = (time / 60).toInt()
                        sec = (time % 60).toInt()
                        km = dist / 1000
                        paceMin = (avgPace / 60).toInt()
                        paceSec = (avgPace % 60).toInt()
                        timeView.text = "${if(min >= 10) min else "0${min}"}:${if(sec >= 10) sec else "0${sec}"}"
                        kmView.text = "${String.format("%.2f", km)} km"
                        paceView.text = "${if(dist < 1) 0 else paceMin}' ${if(paceSec >= 10) paceSec else "0${paceSec}"}''"

                        // draw friends
                        if(runningFriends.size != 0) {
                            for(i in 0..runningFriends.size-1){
                                friendMarker(i)
                            }
                        }
                            // socket
                        mSocket.emit("curRunning", Gson().toJson(locationData))
                    }
                    // 시간
                    time += 0.25
                } finally {
                    l.unlock()
                }
            }
        }
    }

    private fun pause() {
        isRunning = false
        stopBtn.visibility = View.GONE
        startBtn.setImageResource(R.drawable.ic_round_play_arrow_24)
        timerTask?.cancel();	// 안전한 호출(?.)로 timerTask가 null이 아니면 cancel() 호출

    }

    private fun reset() {
        isRunning = false
        stopBtn.visibility = View.GONE
        startBtn.setImageResource(R.drawable.ic_round_play_arrow_24)
        timerTask?.cancel()	// timerTask가 null이 아니라면 cancel() 호출
        mSocket.disconnect()
        date.add(Date(System.currentTimeMillis()))
        curRunningData = RunningData(date, path, time, dist, avgPace, subDistList)
        // 데이터 저장
        MainActivity.user.running.add(curRunningData)

        // 카메라 이동
        var center = LatLng(totLat / path.size, totLon / path.size)
        val cameraUpdate = CameraUpdate.scrollTo(LatLng(center.latitude, center.longitude))
            .pivot(PointF(0.5f, 0.5f)).animate(CameraAnimation.Fly, 500)
        naverMap.moveCamera(cameraUpdate)

        time = 0.0
        dist = 0.0
        subDist = 0.0
        avgPace = 0.0
        path.clear()
        subDistList.clear()
        totLat = 0.0
        totLon = 0.0
        runStart = true
        Log.d("running Data", "path : ${path.toString()}")
        Log.d("subDist", "subDist : ${subDist}")
        Log.d("dist", "dist : ${dist}")
        Log.d("subDistList", "subDistList : ${subDistList.toString()}")
        Log.d("time", "time: ${time}")
        Log.d("avgpace", "avgpac : ${avgPace}")
    }

    private fun calDist(lat1:Double, lon1:Double, lat2:Double, lon2:Double) : Double {
        val EARTH_R = 6372800.0
        val rad = Math.PI / 180
        val radLat1 = rad * lat1
        val radLat2 = rad * lat2
        val radDist = rad * (lon1 - lon2)

        var distance = Math.sin(radLat1) * Math.sin(radLat2)
        distance = distance + Math.cos(radLat1) * Math.cos(radLat2) * Math.cos(radDist)
        val ret = EARTH_R * Math.acos(distance)

        return ret // 미터 단위
    }

    private fun linePath() {
        pathLine.coords = path
        pathLine.width = 30
//        pathLine.outlineWidth = 2
        pathLine.color = Color.BLUE
        pathLine.map = naverMap
        pathLine.joinType = PolylineOverlay.LineJoin.Round
        pathLine.capType = PolylineOverlay.LineCap.Round
    }

    private fun friendMarker(idx: Int){
        var friendMarker = Marker()
        var imageUrl = runningFriends[idx].imgUrl
        CoroutineScope(Dispatchers.Main).launch {
            val bitmap = withContext(Dispatchers.IO) {
                ImageLoader.loadImage(imageUrl)
            }
            friendMarker.position = LatLng(runningFriends[idx].lat, runningFriends[idx].lon)
            friendMarker.map = naverMap
            friendMarker.icon = OverlayImage.fromBitmap(bitmap!!)
            friendMarker.captionText = runningFriends[idx].name
//            friendMarker.zIndex = 100
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            Tab1().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}

object ImageLoader {
    suspend fun loadImage(imageUrl: String): Bitmap? {
        val bmp: Bitmap? = null
        try {
            val url = URL(imageUrl)
            val stream = url.openStream()

            return BitmapFactory.decodeStream(stream)
        } catch (e: MalformedURLException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return bmp
    }
}
