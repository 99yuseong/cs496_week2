package com.example.cs496_week2

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.*
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.NonNull
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.cs496_week2.databinding.FragmentTab1Binding
import com.google.android.gms.location.*
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.tabs.TabLayout
import com.google.gson.Gson
import com.naver.maps.geometry.LatLng
import com.naver.maps.geometry.LatLngBounds
import com.naver.maps.map.*
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
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okio.BufferedSink
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.net.MalformedURLException
import java.net.URL
import java.util.*
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.schedule
import kotlin.math.abs


private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class Tab1 : Fragment(), OnMapReadyCallback, NaverMap.SnapshotReadyCallback {
    private var param1: String? = null
    private var param2: String? = null

    private lateinit var binding: FragmentTab1Binding
    private lateinit var root: View
    private lateinit var mainActivity: MainActivity

    private lateinit var mapView: MapView

    private lateinit var locationSource: FusedLocationSource // ????????? ???????????? ?????????
    private lateinit var naverMap: NaverMap
    private var mFusedLocationProviderClient: FusedLocationProviderClient? = null // ?????? ????????? ???????????? ?????? ??????
    lateinit var mLastLocation: Location // ?????? ?????? ????????? ?????? ??????
    internal lateinit var mLocationRequest: LocationRequest // ?????? ?????? ????????? ??????????????? ????????????
    private val REQUEST_PERMISSION_LOCATION = 10
    private val LOCATION_PERMISSTION_REQUEST_CODE: Int = 1000
    private val STRORANGE_EXTERNAL_PERMISSION_REQUEST_CODE: Int = 100

    // timer
    private var isRunning = false
    private var timerTask: Timer? = null
    lateinit var startDate: Date
    lateinit var endDate: Date
    private var time = 0.0
    private var dist = 0.0
    private var subDist: Double = 0.0
    private var avgPace: Double = 0.0
    lateinit var prvCoord: LatLng
    private var path: MutableList<LatLng> = mutableListOf()
    private var subDistList: MutableList<Double> = mutableListOf()
    val l = ReentrantLock()
    private var totLat : Double = 0.0
    private var totLon : Double = 0.0
    private var runStart: Boolean = true

    //btns
    lateinit var startBtn: ImageView
    lateinit var stopBtn: ImageView
    lateinit var captureBtn: ImageView

    // running DATA list
//    private var firstRunning: Boolean = true

    // pathline
    var pathLine = PolylineOverlay()

    // interface
    lateinit var timeView : TextView
    lateinit var kmView : TextView
    lateinit var paceView : TextView
    lateinit var infoLayout : LinearLayout
    lateinit var tabs : TabLayout
    lateinit var groupListRv : RecyclerView
    var min = 0
    var sec = 0
    var km = 0.0
    var paceMin = 0
    var paceSec = 0
//    private val marker = Marker()

    // friends
//    lateinit var user: UserDT
    lateinit var locationData : LocationDT
    var currentRoom = " "
    var runningGroupList : MutableList<LocationDT> = mutableListOf()
    var toRemoveFriendsMarker : MutableList<Marker> = mutableListOf()
    var friendsMarker : MutableMap<String, Marker> = mutableMapOf()


    // socket
    lateinit var mSocket: Socket
    val realtimeSocketSender = SocketThread()
    val endTimeSockerSender = EndSocketThread()

    // data
    lateinit var curRunningData: RunningData

    // retrofit service
    val service = RetrofitInterface.service

    // Activity ?????????
    override fun onAttach(context: Context) {
        super.onAttach(context)
        mainActivity = context as MainActivity
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mSocket = SocketApplication.get()

        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
        binding = FragmentTab1Binding.inflate(layoutInflater)
        // ?????? ?????? (??????????????? ??????)
        locationSource = FusedLocationSource(this, LOCATION_PERMISSTION_REQUEST_CODE)
        // ?????? ?????? (GPS ??????)
        mLocationRequest =  LocationRequest.create().apply {
            interval = 1000
            fastestInterval = 1000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            maxWaitTime = 1000
        }
        startLocationUpdates()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        root = inflater.inflate(R.layout.fragment_tab1, container, false)

        timeView = root.findViewById(R.id.time)
        kmView = root.findViewById(R.id.kilometer)
        paceView = root.findViewById(R.id.pace)
        infoLayout = root.findViewById(R.id.runningInfo)
        tabs = mainActivity.findViewById(R.id.tabs)

        startBtn = root.findViewById(R.id.start)
        stopBtn = root.findViewById(R.id.stop)
        captureBtn = root.findViewById(R.id.captureBtn)

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mapView = view.findViewById(R.id.map)
        mapView.onCreate(savedInstanceState)
        mapView.onResume()
        mapView.getMapAsync(this)

        groupListRv = root.findViewById<RecyclerView>(R.id.group_list)
        groupListRv.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            0
        )
    }

    override fun onMapReady(@NonNull naverMap: NaverMap) {
        this.naverMap = naverMap
        naverMap.setLayerGroupEnabled(NaverMap.LAYER_GROUP_BUILDING, false)
        val uiSettings = naverMap.uiSettings
        naverMap.locationSource = locationSource
        naverMap.locationTrackingMode = LocationTrackingMode.Follow
        naverMap.mapType = NaverMap.MapType.Navi
        uiSettings.isZoomControlEnabled = false
        naverMap.isNightModeEnabled = true
    }

    override fun onStart() {
        super.onStart()
        mapView.onStart()
    }

    override fun onResume() {
        super.onResume()

        service.getGroup(MainActivity.kakaoUser.id!!).enqueue(object : Callback<ArrayList<GroupDT>> {
            override fun onResponse(call: Call<ArrayList<GroupDT>>, response: Response<ArrayList<GroupDT>>) {
                if(response.isSuccessful){
                    val groupList = response.body()
                    val groupListAdapter = GroupListAdapter(groupList!!, context!!)
                    groupListRv.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
                    groupListRv.adapter = groupListAdapter
                    if(groupList.size > 0) {
                        groupListRv.layoutParams = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            mainActivity.resources.getDimensionPixelSize(R.dimen.groupToolbar)
                        )
                    }
                    groupListAdapter.setItemClickListener(object: GroupListAdapter.OnItemClickListener {
                        override fun onClick(v: View, position: Int) {
                            if(currentRoom == groupList[position].groupName) {
                                if(currentRoom == " ") {
                                    currentRoom = MainActivity.user._id
                                }
                                mSocket.emit("changeGroup", JSONArray(arrayOf(MainActivity.user._id, groupList[position].groupName, MainActivity.user._id)))
                                currentRoom = MainActivity.user._id
                            } else {
                                if(currentRoom == " ") {
                                    currentRoom = MainActivity.user._id
                                }
                                mSocket.emit("changeGroup", JSONArray(arrayOf(MainActivity.user._id, currentRoom, groupList[position].groupName)))
                                currentRoom = groupList[position].groupName
                                removeAllMarker()
                            }
                        }
                    })
                }
            }
            override fun onFailure(call: Call<ArrayList<GroupDT>>, t: Throwable) {
            }
        })

        mapView.onResume()
        mSocket.connect()
        mSocket.on("message", Emitter.Listener {
            var jObject = JSONObject(it[0].toString())
            var id = jObject.getString("_id")
            var room = jObject.getString("room")
            var lat = jObject.getString("lat")
            var lon = jObject.getString("lon")
            var name = jObject.getString("name")
            var imgUrl = jObject.getString("imgUrl")
            var inFriendList = false

            if(id != MainActivity.user._id) {
                for (i in 0 until runningGroupList.size) {
                    if(id == runningGroupList[i]._id) {
                        inFriendList = true
                        runningGroupList[i].lat = lat.toDouble()
                        runningGroupList[i].lon = lon.toDouble()
                    }
                }
                if(!inFriendList) {
                    runningGroupList.add(LocationDT(id,
                        MainActivity.kakaoUser.id!!, room, lat.toDouble(), lon.toDouble(), imgUrl, name))
                    friendsMarker[id] = Marker()
                }
            }
        })
        mSocket.on("endRun", Emitter.Listener {
            var jObject = JSONObject(it[0].toString())
            var id = jObject.getString("_id")
            if (friendsMarker.size > 0 && friendsMarker[id] != null){
                toRemoveFriendsMarker.add(friendsMarker[id]!!)
                friendsMarker.remove(id)
                for (i in 0 until runningGroupList.size) {
                    if(id == runningGroupList[i]._id) {
                        runningGroupList.removeAt(i)
                    }
                }
            }
        })
        mSocket.on("goOut", Emitter.Listener {
            var id = it[0].toString()
            if (friendsMarker.size > 0 && friendsMarker[id] != null){
                toRemoveFriendsMarker.add(friendsMarker[id]!!)
                friendsMarker.remove(id)
                for (i in 0 until runningGroupList.size) {
                    if(id == runningGroupList[i]._id) {
                        runningGroupList.removeAt(i)
                    }
                }
                mainActivity.runOnUiThread {
                    for (i in 0 until toRemoveFriendsMarker.size) {
                        toRemoveFriendsMarker[i].map = null
                        toRemoveFriendsMarker.removeAt(i)
                    }
                }
            }
        })

        timerTask = kotlin.concurrent.timer(period = 1000) {	// timer() ??????
            mainActivity.runOnUiThread {
                startLocationUpdates()
                // draw friends
                for(i in 0 until runningGroupList.size){
                    friendMarker(i)
                }
                if(isRunning){
                    start()
                }
                for (i in 0 until toRemoveFriendsMarker.size) {
                    toRemoveFriendsMarker[i].map = null
                    toRemoveFriendsMarker.removeAt(i)
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        mSocket.disconnect()
        mapView.onPause()
//        timerTask?.cancel()	// timerTask??? null??? ???????????? cancel() ??????
        mSocket.emit("endRunning", MainActivity.user._id)
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
        //FusedLocationProviderClient??? ??????????????? ??????.
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(mainActivity)
        if (ActivityCompat.checkSelfPermission(mainActivity, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(mainActivity,android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return
        }
        // ????????? ????????? ?????? ?????? ??????????????? ???????????? ????????? ??????
        // ????????? ?????? ?????????(Looper.myLooper())?????? ??????(mLocationCallback)?????? ?????? ??????????????? ??????
        mFusedLocationProviderClient!!.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper())
    }

    // ??????????????? ?????? ?????? ????????? ???????????? ??????
    private val mLocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            // ??????????????? ?????? location ????????? onLocationChanged()??? ??????
            locationResult.lastLocation
            onLocationChanged(locationResult.lastLocation)
        }
    }

    // ??????????????? ?????? ?????? ??????????????? ????????? ??????????????? ?????????
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
            startBtn.setOnClickListener {
                if(!isRunning) {
                    isRunning = true
                    stopBtn.visibility = View.VISIBLE
                    startBtn.setImageResource(R.drawable.ic_round_pause_24)
                    infoLayout.layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.MATCH_PARENT
                    )
                    mapView.layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        700
                    )
                    tabs.layoutParams = AppBarLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        0
                    )
                    captureBtn.visibility = View.GONE
                } else {
                    pause()
                }
            }
            stopBtn.setOnClickListener {
                if (time >= 5 && dist >= 30.0 ) {
                    reset()
                }
                isRunning = false
                stopBtn.visibility = View.GONE
                startBtn.setImageResource(R.drawable.ic_round_play_arrow_24)
                runStart = true
                resetValues()
                infoLayout.layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    0
                )
                mapView.layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT
                )
                tabs.layoutParams = AppBarLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT
                )
            }
            startBtn.visibility = View.VISIBLE
        }
    }

    // ??????????????? ?????? ?????? ??? ????????? ?????? ?????? ??????
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_PERMISSION_LOCATION) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startLocationUpdates()

            } else {
                Log.d("ttt", "onRequestPermissionsResult() _ ?????? ?????? ??????")
                Toast.makeText(mainActivity, "????????? ?????? ?????? ????????? ????????? ??? ????????????.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun start() {
        isRunning = true
        if(runStart){
            prvCoord = LatLng(mLastLocation.latitude, mLastLocation.longitude)
            totLat = mLastLocation.latitude
            totLon = mLastLocation.longitude
            path.add(LatLng(mLastLocation.latitude, mLastLocation.longitude))
            startDate = Date(System.currentTimeMillis())
            runStart = false
            locationData = LocationDT(MainActivity.user._id, MainActivity.kakaoUser.id!!, currentRoom, prvCoord.latitude, prvCoord.longitude, "imgUrl", "name" )
            if(currentRoom == " ") {
                currentRoom = MainActivity.user._id
            }
        }

        l.lock()
        try {
            // ?????? ??????
//                    startLocationUpdates()
            path.add(LatLng(mLastLocation.latitude, mLastLocation.longitude))
            locationData.lat = mLastLocation.latitude
            locationData.lon = mLastLocation.longitude
            locationData.room = currentRoom
            totLat += mLastLocation.latitude
            totLon += mLastLocation.longitude
            // ?????? ??????
            subDist = calDist(
                prvCoord.latitude,
                prvCoord.longitude,
                mLastLocation.latitude,
                mLastLocation.longitude
            )
            // ??????
            time ++
            // ??? ??????
            if(subDist == 0.0) {
                if(subDistList.size > 0) {
                    subDist = subDistList.last()
                } else {
                    subDist = 0.0
                }
            } else {
                dist += subDist
            }
            // ?????? ?????????
            if(dist < 1) {
                avgPace = 1500.0
            } else if(time / (dist / 1000.0) > 1500){
                avgPace = 1500.0
            } else {
                avgPace = time / (dist / 1000.0)
            }
            if(subDistList.size > 2) {
                if((1 / (subDist / 1000.0)) > 1500 || (1 / (subDist / 1000.0)) < 0) {
                    subDistList.add(((1500 / 60.0 * (-1)) + subDistList[subDistList.size -1] + subDistList[subDistList.size -2])/3.0)
                } else {
                    subDistList.add((((1 / (subDist / 1000.0)) / 60.0 * (-1)) + subDistList[subDistList.size -1] + subDistList[subDistList.size -2])/3.0)
                }

            } else {
                if((1 / (subDist / 1000.0)) > 1500 || (1 / (subDist / 1000.0)) < 0) {
                    subDistList.add(1500 / 60.0 * (-1))
                } else {
                    subDistList.add((1 / (subDist / 1000.0)) / 60.0 * (-1))
                }
            }
            // ????????? ?????? ?????????
            linePath()
            prvCoord = LatLng(mLastLocation.latitude, mLastLocation.longitude)
            min = (time / 60).toInt()
            sec = (time % 60).toInt()
            km = dist / 1000
            paceMin = (avgPace / 60).toInt()
            paceSec = (avgPace % 60).toInt()
            timeView.text = "${if(min >= 10) min else "0${min}"}:${if(sec >= 10) sec else "0${sec}"}"
            kmView.text = "${String.format("%.2f", km)} km"
            paceView.text = "${if(dist < 1) 0 else paceMin}' ${if(paceSec >= 10) paceSec else "0${paceSec}"}''"
            Thread(realtimeSocketSender).start()
        } finally {
            l.unlock()
        }
    }

    private fun pause() {
        isRunning = false
        stopBtn.visibility = View.GONE
        startBtn.setImageResource(R.drawable.ic_round_play_arrow_24)
//        timerTask?.cancel();	// ????????? ??????(?.)??? timerTask??? null??? ????????? cancel() ??????
    }

    private fun reset() {
        isRunning = false
        stopBtn.visibility = View.GONE
        startBtn.setImageResource(R.drawable.ic_round_play_arrow_24)
//        timerTask?.cancel()	// timerTask??? null??? ???????????? cancel() ??????

        endDate = Date(System.currentTimeMillis())
        var pathTmp : MutableList<LatLng> = mutableListOf()
        var subDistListTmp : MutableList<Double> = mutableListOf()
        pathTmp.addAll(path)
        subDistListTmp.addAll(subDistList)
        curRunningData = RunningData("empty_id", MainActivity.user._id, startDate, endDate, pathTmp, time, dist, avgPace, subDistListTmp)
//        mSocket.emit("endRunning", MainActivity.user._id)
        Thread(endTimeSockerSender).start()
        service.postCreateRunning(curRunningData).enqueue(object: Callback<ResponseDT> {
            override fun onResponse(call: Call<ResponseDT>, response: Response<ResponseDT>) {
                curRunningData._id = response.body()!!.message
                MainActivity.user.running.add(response.body()!!.message)
                // ????????? ??????
                naverMap.minZoom = 5.0
                naverMap.maxZoom = 18.0
                val latLngBounds = LatLngBounds.Builder()
                for(i in 0..pathTmp.size-1) {
                    latLngBounds.include(pathTmp[i])
                }
                val bounds=latLngBounds.build()
                val updated= CameraUpdate.fitBounds(bounds, 100,300, 100,300).animate(CameraAnimation.Fly, 500)
                naverMap.moveCamera(updated)
                naverMap.addOnCameraIdleListener {
                    Handler(Looper.getMainLooper()).postDelayed({
                        //????????? ??????
                        naverMap.moveCamera(updated)
                    }, 3000)
                }

                naverMap.takeSnapshot(false, this@Tab1)
                captureBtn.visibility = View.VISIBLE
            }
            override fun onFailure(call: Call<ResponseDT>, t: Throwable) {
                Log.d("failed", "fail")
                Log.d("call", call.toString())
                Log.d("throw", t.toString())
            }
        })
        runStart = true
        resetValues()
    }

    private fun resetValues() {
        time = 0.0
        dist = 0.0
        subDist = 0.0
        avgPace = 0.0
        path.clear()
        subDistList.clear()
        totLat = 0.0
        totLon = 0.0
    }

    private fun calDist(lat1:Double, lon1:Double, lat2:Double, lon2:Double) : Double {
        val EARTH_R = 6372800.0
        val rad = Math.PI / 180
        val radLat1 = rad * lat1
        val radLat2 = rad * lat2
        val radDist = rad * abs(lon1 - lon2)

        var distance = Math.sin(radLat1) * Math.sin(radLat2)
        distance = distance + Math.cos(radLat1) * Math.cos(radLat2) * Math.cos(radDist)
        val ret = EARTH_R * Math.acos(distance)
        if (ret < 10000.0) {
            return abs(ret) // ?????? ??????
        } else {
            return 0.0
        }
    }

    private fun linePath() {
        pathLine.coords = path
        pathLine.width = 30
        pathLine.color = Color.YELLOW
        pathLine.map = naverMap
        pathLine.joinType = PolylineOverlay.LineJoin.Round
        pathLine.capType = PolylineOverlay.LineCap.Round
    }

    fun getRoundedCornerBitmap(bitmap: Bitmap): Bitmap? {
        val output = Bitmap.createBitmap(
            bitmap.width,
            bitmap.height, Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(output)
        val color = -0xbdbdbe
        val paint = Paint()
        val rect = Rect(0, 0, bitmap.width, bitmap.height)
        paint.isAntiAlias = true
        canvas.drawARGB(0, 0, 0, 0)
        paint.color = color
        canvas.drawCircle(bitmap.width/2f, bitmap.height/2f, bitmap.width/2f, paint)
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
        canvas.drawBitmap(bitmap, rect, rect, paint)
        return output
    }

    private fun friendMarker(idx: Int){
        var friendMarker = friendsMarker[runningGroupList[idx]._id]
        var imageUrl = runningGroupList[idx].imgUrl
        CoroutineScope(Dispatchers.Main).launch {
            val bitmap = withContext(Dispatchers.IO) {
                ImageLoader.loadImage(imageUrl)
            }
            if (friendMarker != null && runningGroupList.size > 0 && bitmap != null) {
                friendMarker.position = LatLng(runningGroupList[idx].lat, runningGroupList[idx].lon)
                friendMarker.map = naverMap
                friendMarker.icon = getRoundedCornerBitmap(bitmap)?.let { OverlayImage.fromBitmap(it) }!!
                friendMarker.captionText = runningGroupList[idx].name
//            friendMarker.zIndex = 100
            }
        }
    }

    private fun removeAllMarker() {
        if (friendsMarker.size > 0){
            for (i in 0 until runningGroupList.size) {
                friendsMarker[runningGroupList[i]._id]?.map = null
                friendsMarker.clear()
                runningGroupList.clear()
            }
        }
    }

    inner class SocketThread() : Runnable {
        override fun run() {
            mSocket.emit("curRunning", Gson().toJson(locationData))
        }
    }
    inner class EndSocketThread() : Runnable {
        override fun run() {
            mSocket.emit("endRunning", MainActivity.user._id)
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

    override fun onSnapshotReady(bitmap: Bitmap) {
        Log.d("snap", bitmap.toString())
        captureBtn.setOnClickListener {
            saveImageToGallery(bitmap)
        }
        changeBitmapMultipartBody(bitmap, mainActivity)
    }

    fun changePathtpMultipartBody(path: Uri?, context : Context) {
        var proj: Array<String> = arrayOf(MediaStore.Images.Media.DATA)
        var c: Cursor? = context.contentResolver.query(path!!, proj, null, null, null)
        var index = c?.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
        c?.moveToFirst()
        var result = c?.getString(index!!)
        val file = File(result!!)

        val requestFile = RequestBody.create("image/jpeg".toMediaTypeOrNull(), file)
        val body = MultipartBody.Part.createFormData("imageFile", file.name, requestFile)
        sendImage(body)
    }

    fun changeBitmapMultipartBody(bitmap: Bitmap, context : Context) {
        val bitmapRequestBody = bitmap?.let { BitmapRequestBody(it) }
        val bitmapMultipartBody: MultipartBody.Part? =
            if (bitmapRequestBody == null) null
            else MultipartBody.Part.createFormData("imageFile", curRunningData._id + ".jpg", bitmapRequestBody)

//        val body = MultipartBody.Part.createFormData("imageFile", file.name, requestFile)
        sendImage(bitmapMultipartBody!!)
    }

    inner class BitmapRequestBody(private val bitmap: Bitmap) : RequestBody() {
        override fun contentType(): MediaType = "image/jpeg".toMediaType()
        override fun writeTo(sink: BufferedSink) {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 99, sink.outputStream())
        }
    }

    fun sendImage(image : MultipartBody.Part) {
        val requestBody: RequestBody = RequestBody.create("text/plain".toMediaTypeOrNull(), curRunningData._id)
        val call = service.captureSend(requestBody, image) //?????? API ?????? ??????
        call.enqueue(object : Callback<String>{
            override fun onResponse(call: Call<String>, response: Response<String>) {
                if (response?.isSuccessful) {
                    Log.d("????????? ?????? ?????? ",""+response?.body().toString())
                }
                else {
                    Log.d("????????? ?????? ?????? ","????????? ?????? ?????? ")
                }
            }
            override fun onFailure(call: Call<String>, t: Throwable) {
                Log.d("?????? ",t.message.toString())
            }
        })
    }

//    private fun sendToServer(bitmap : Bitmap) {
//        val byteArrayOutputStream = ByteArrayOutputStream()
//        bitmap.compress(Bitmap.CompressFormat.JPEG, 60, byteArrayOutputStream)
//        val byteArray: ByteArray = byteArrayOutputStream.toByteArray()
//        val outStream = ByteArrayOutputStream()
//        val res: Resources = resources
//        val profileImageBase64 = Base64.encodeToString(byteArray, NO_WRAP)
//    }

    private fun saveImageToGallery(bitmap: Bitmap): Boolean{
        //?????? ??????
        if(!checkPermission(mainActivity, android.Manifest.permission.READ_EXTERNAL_STORAGE) ||
            !checkPermission(mainActivity, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            return false
        }
        //?????? ??????
        if(!context?.let { imageExternalSave(mainActivity, bitmap, it.getString(R.string.app_name)) }!!){
            Toast.makeText(context, "?????? ????????? ?????????????????????", Toast.LENGTH_SHORT).show()
            return false
        }
        Toast.makeText(activity, "????????? ???????????? ?????????????????????", Toast.LENGTH_SHORT).show()
        captureBtn.visibility = View.GONE
        return true
    }

    fun checkPermission(activity: Activity, permission: String): Boolean {
        val permissionChecker =
            activity?.let { ContextCompat.checkSelfPermission(it.applicationContext, permission) }
        //????????? ????????? ?????? ??????
        if (permissionChecker == PackageManager.PERMISSION_GRANTED) return true
        ActivityCompat.requestPermissions(activity, arrayOf(permission), STRORANGE_EXTERNAL_PERMISSION_REQUEST_CODE)
        return false
    }

    @SuppressLint("Range")
    private fun imageExternalSave(context: Context, bitmap: Bitmap, path: String): Boolean {
        val state = Environment.getExternalStorageState()
        if (Environment.MEDIA_MOUNTED == state) {
            val rootPath =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                    .toString()
            val dirName = "/" + curRunningData._id
            val fileName = curRunningData._id + ".png"
            val savePath = File(rootPath + dirName)
            savePath.mkdirs()

            val file = File(savePath, fileName)
            if (file.exists()) file.delete()

            try {
                val out = FileOutputStream(file)
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                out.flush()
                out.close()

                //????????? ??????
                context.sendBroadcast(
                    Intent(
                        Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
                        Uri.parse("file://" + Environment.getExternalStorageDirectory())
                    )
                )

//                val cursor = context.contentResolver.query(
//                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
//                    null,
//                    "_data = '" + rootPath + dirName + "/" + fileName+ "'",
//                    null,
//                    null
//                )
//
//                cursor!!.moveToNext()
//                val id = cursor!!.getInt(cursor!!.getColumnIndex("_id"))
//                val uri = ContentUris.withAppendedId(
//                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
//                    id.toLong()
//                )
//                changePathtpMultipartBody(uri, mainActivity)
                return true
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return false
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


