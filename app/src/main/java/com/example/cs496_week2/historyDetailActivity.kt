package com.example.cs496_week2

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.cs496_week2.RetrofitInterface.Companion.service
import com.example.cs496_week2.databinding.ActivityHistoryDetailBinding
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.naver.maps.geometry.LatLng
import com.naver.maps.geometry.LatLngBounds
import com.naver.maps.map.*
import com.naver.maps.map.CameraUpdate.fitBounds
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.overlay.MultipartPathOverlay
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*


class historyDetailActivity : AppCompatActivity(),OnMapReadyCallback {

    private lateinit var binding: ActivityHistoryDetailBinding
    private lateinit var runningData: RunningData
    private lateinit var chart: LineChart
    private var eachPathArray : MutableList<List<LatLng>> = mutableListOf()

    private lateinit var mapView: MapView
    private lateinit var naverMap: NaverMap
    private var totLat = 0.0
    private var totLon = 0.0
    val multipartPath = MultipartPathOverlay()
    val pathColor :MutableList<MultipartPathOverlay.ColorPart> = mutableListOf()
    val marker = Marker()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHistoryDetailBinding.inflate(layoutInflater)
        setContentView(R.layout.activity_history_detail)
        mapView = findViewById(R.id.history_act_map)
        mapView.onCreate(savedInstanceState)
        mapView.onResume()
        mapView.getMapAsync(this)

        val startDateView: TextView = findViewById(R.id.history_act_date)
        val distView: TextView = findViewById(R.id.history_act_distance)
        val paceView: TextView = findViewById(R.id.history_act_pace)
        val timeView: TextView = findViewById(R.id.history_act_time)
        chart = findViewById(R.id.history_act_chart)

        val userid = intent.getStringExtra("user")
        val position = intent.getIntExtra("position", 0)

        // 에러 아닙니다 "u"가 맞습니다
        val dateFormatter = SimpleDateFormat("u", Locale.getDefault())
        service.getEachRunningData(userid!!, position).enqueue(object: Callback<RunningData> {
            @SuppressLint("SetTextI18n")
            override fun onResponse(call: Call<RunningData>, response: Response<RunningData>) {
                runningData = response.body()!!
                val time = runningData.time
                val min = (time / 60).toInt()
                val sec = (time % 60).toInt()
                val tDist = runningData.dist
                val tPace = runningData.avgPace
                val tPaceMin = (tPace / 60).toInt()
                val tPaceSec = (tPace % 60).toInt()
                val cal = Calendar.getInstance()
                cal.time = runningData.startDate
                cal.add(Calendar.HOUR, -9)
                startDateView.setText("${getDay(dateFormatter.format(cal.time).toInt())} Running")
                distView.setText("${String.format("%.2f", tDist / 1000.0)} km")
                paceView.setText("${if(tDist < 1) 0 else tPaceMin}' ${if(tPaceSec >= 10) tPaceSec else "0${tPaceSec}"}''")
                timeView.setText("${if(min >= 10) min else "0${min}"}:${if(sec >= 10) sec else "0${sec}"}")
                val thread = ThreadClass()
                thread.start()
                val marker = paceMarkerView(this@historyDetailActivity, layoutResource = R.layout.pace_marker_view)
                chart.marker = marker

                // map
                for(i in 0 until runningData.path.size-1) {
                    totLat += runningData.path[i].latitude
                    totLon += runningData.path[i].longitude
                    if(runningData.path.size % 2 == 0) {
                        if(i < runningData.path.size-2){
                            eachPathArray.add(listOf(runningData.path[i], runningData.path[i+1]))
                            pickColor((runningData.subDist[i] + runningData.subDist[i+1] )/2.0)
                        }
                    } else {
                        if (i == runningData.path.size-1) {
                            eachPathArray.add(listOf(runningData.path[i-1], runningData.path[i]))
                            pickColor((runningData.subDist[i]))
                        } else if( i < runningData.path.size - 2) {
                            eachPathArray.add(listOf(runningData.path[i], runningData.path[i+1]))
                            pickColor((runningData.subDist[i] + runningData.subDist[i+1] )/2.0)
                        }
                    }
                }
                this@historyDetailActivity.runOnUiThread {
                    naverMap.minZoom = 5.0
                    naverMap.maxZoom = 18.0
                    naverMap.setLayerGroupEnabled(NaverMap.LAYER_GROUP_BUILDING, false)
                    naverMap.mapType = NaverMap.MapType.Navi
                    naverMap.isNightModeEnabled = true
                    val latLngBounds = LatLngBounds.Builder()
                    for(i in 0..runningData.path.size-1) {
                        latLngBounds.include(runningData.path[i])
                    }
                    val bounds=latLngBounds.build()
                    val padding= 100
                    val updated= fitBounds(bounds,padding).animate(CameraAnimation.Fly, 500)
                    naverMap.moveCamera(updated)
                    linePath()
                    naverMap.addOnCameraIdleListener {
                        Handler(Looper.getMainLooper()).postDelayed({
                            //실행할 코드
                            naverMap.moveCamera(updated)
                        }, 3000)
                    }
                }
            }

            override fun onFailure(call: Call<RunningData>, t: Throwable) {

            }
        })
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

    override fun onMapReady(naverMap: NaverMap) {
        this.naverMap = naverMap
    }

    private fun linePath() {
        multipartPath.width = 30
        multipartPath.outlineWidth = 5
        multipartPath.coordParts = eachPathArray
        multipartPath.colorParts = pathColor
        Log.d("color",pathColor.toString())
        Log.d("path", eachPathArray.toString())
        multipartPath.map = naverMap
    }

    @SuppressLint("ResourceAsColor")
    private fun pickColor(pace : Double) {
        when (pace * (-1)) {
            in 15.0..100.0 -> pathColor.add(MultipartPathOverlay.ColorPart(resources.getColor(R.color.pace_over_15),resources.getColor(R.color.pace_over_15),R.color.pace_over_15,R.color.pace_over_15))
            in 13.0..15.0 -> pathColor.add(MultipartPathOverlay.ColorPart(resources.getColor(R.color.pace_15_to_13),resources.getColor(R.color.pace_over_15),R.color.pace_15_to_13,R.color.pace_15_to_13))
            in 11.0..13.0 -> pathColor.add(MultipartPathOverlay.ColorPart(resources.getColor(R.color.pace_13_to_11),resources.getColor(R.color.pace_15_to_13),R.color.pace_13_to_11,R.color.pace_13_to_11))
            in 9.0..11.0 -> pathColor.add(MultipartPathOverlay.ColorPart(resources.getColor(R.color.pace_11_to_9),resources.getColor(R.color.pace_13_to_11),R.color.pace_11_to_9,R.color.pace_11_to_9))
            in 8.0..9.0 -> pathColor.add(MultipartPathOverlay.ColorPart(resources.getColor(R.color.pace_9_to_8),resources.getColor(R.color.pace_11_to_9),R.color.pace_9_to_8,R.color.pace_9_to_8))
            in 7.5..8.0 -> pathColor.add(MultipartPathOverlay.ColorPart(resources.getColor(R.color.pace_8_to_7_5),resources.getColor(R.color.pace_9_to_8),R.color.pace_8_to_7_5,R.color.pace_8_to_7_5))
            in 7.0..7.5 -> pathColor.add(MultipartPathOverlay.ColorPart(resources.getColor(R.color.pace_7_5_to_7_0),resources.getColor(R.color.pace_8_to_7_5),R.color.pace_7_5_to_7_0,R.color.pace_7_5_to_7_0))
            in 6.5..7.0 -> pathColor.add(MultipartPathOverlay.ColorPart(resources.getColor(R.color.pace_7_0_to_6_5),resources.getColor(R.color.pace_7_5_to_7_0),R.color.pace_7_0_to_6_5,R.color.pace_7_0_to_6_5))
            in 6.0..6.5 -> pathColor.add(MultipartPathOverlay.ColorPart(resources.getColor(R.color.pace_6_5_to_6_0),resources.getColor(R.color.pace_7_0_to_6_5),R.color.pace_6_5_to_6_0,R.color.pace_6_5_to_6_0))
            in 5.5..6.0 -> pathColor.add(MultipartPathOverlay.ColorPart(resources.getColor(R.color.pace_6_0_to_5_5),resources.getColor(R.color.pace_6_5_to_6_0),R.color.pace_6_0_to_5_5,R.color.pace_6_0_to_5_5))
            in 5.0..5.5 -> pathColor.add(MultipartPathOverlay.ColorPart(resources.getColor(R.color.pace_5_5_to_5_0),resources.getColor(R.color.pace_6_0_to_5_5),R.color.pace_5_5_to_5_0,R.color.pace_5_5_to_5_0))
            in 4.5..5.0 -> pathColor.add(MultipartPathOverlay.ColorPart(resources.getColor(R.color.pace_5_0_to_4_5),resources.getColor(R.color.pace_5_5_to_5_0),R.color.pace_5_0_to_4_5,R.color.pace_5_0_to_4_5))
            in 4.0..4.5 -> pathColor.add(MultipartPathOverlay.ColorPart(resources.getColor(R.color.pace_4_5_to_4_0),resources.getColor(R.color.pace_5_0_to_4_5),R.color.pace_4_5_to_4_0,R.color.pace_4_5_to_4_0))
            in 3.5..4.0 -> pathColor.add(MultipartPathOverlay.ColorPart(resources.getColor(R.color.pace_4_0_to_3_5),resources.getColor(R.color.pace_4_5_to_4_0),R.color.pace_4_0_to_3_5,R.color.pace_4_0_to_3_5))
            in 3.0..3.5 -> pathColor.add(MultipartPathOverlay.ColorPart(resources.getColor(R.color.pace_3_5_to_3_0),resources.getColor(R.color.pace_4_0_to_3_5),R.color.pace_3_5_to_3_0,R.color.pace_3_5_to_3_0))
            else -> pathColor.add(MultipartPathOverlay.ColorPart(resources.getColor(R.color.pace_under_3_0),resources.getColor(R.color.pace_3_5_to_3_0),R.color.pace_under_3_0,R.color.pace_under_3_0))
        }
    }

    private fun getDay(int: Int) : String{
        return when (int) {
            1 -> "MON"
            2 -> "TUE"
            3 -> "WED"
            4 -> "THU"
            5 -> "FRI"
            6 -> "SAT"
            else -> "SUN"
        }
    }

    inner class ThreadClass : Thread() {
        override fun run() {
            val input = runningData.subDist
            Log.d("subdist", runningData.subDist.toString())
            val entries: ArrayList<Entry> = ArrayList()
            entries.add(Entry(0F, 0F))
            val dataset: LineDataSet = LineDataSet(entries, "Pace")
            val data: LineData = LineData(dataset)
            dataset.lineWidth = 5f
//            dataset.circleRadius = 6f
            dataset.setDrawValues(false)
            dataset.setDrawCircleHole(false)
//            dataset.setDrawCircles(true)
            dataset.setDrawHorizontalHighlightIndicator(false)
            dataset.setDrawVerticalHighlightIndicator(true)
            dataset.color = Color.rgb(254, 201, 45)
            dataset.setCircleColor(Color.rgb(254, 201, 45))
            dataset.highLightColor = Color.BLACK
            dataset.highlightLineWidth = 1f

            chart.data = data
            val legend = chart.legend
            legend.verticalAlignment = Legend.LegendVerticalAlignment.BOTTOM
            legend.horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
            legend.form = Legend.LegendForm.CIRCLE
            legend.formSize = 10f
            legend.textSize = 13f
            legend.textColor = Color.parseColor("#A3A3A3")
            legend.orientation = Legend.LegendOrientation.VERTICAL
            legend.setDrawInside(false)
            legend.yEntrySpace = 5f
            legend.isWordWrapEnabled = true
            legend.xOffset = 80f
            legend.yOffset = 20f
            legend.calculatedLineSizes

            val xAxis = chart.xAxis
            xAxis.setDrawAxisLine(false)
            xAxis.setDrawGridLines(false)
            xAxis.setDrawLabels(false) // label 삭제
            xAxis.position = XAxis.XAxisPosition.BOTTOM // x축 데이터 표시 위치
            xAxis.granularity = 1f
            xAxis.textSize = 14f
            xAxis.textColor = Color.rgb(118, 118, 118)
            xAxis.spaceMin = 5f // Chart 맨 왼쪽 간격 띄우기
            xAxis.spaceMax = 5f // Chart 맨 오른쪽 간격 띄우기

            // YAxis(Right) (왼쪽) - 선 유무, 데이터 최솟값/최댓값, 색상
            val yAxisLeft = chart.axisLeft
            yAxisLeft.setDrawLabels(false) // label 삭제
            yAxisLeft.textColor = Color.rgb(163, 163, 163)
            yAxisLeft.setDrawAxisLine(false)
//            yAxisLeft.axisLineWidth = 2f
            yAxisLeft.axisMinimum = -40f // 최솟값
            yAxisLeft.axisMaximum = -3f

            // YAxis(Left) (오른쪽) - 선 유무, 데이터 최솟값/최댓값, 색상
            val yAxis = chart.axisRight
            yAxis.setDrawLabels(false) // label 삭제
            yAxis.textColor = Color.rgb(163, 163, 163)
            yAxis.setDrawAxisLine(false)
//            yAxis.axisLineWidth = 180f
            yAxis.axisMinimum = -40f // 최솟값
            yAxis.axisMaximum = -3f

            runOnUiThread {
                // 그래프 생성
                chart.animateXY(1, 1)
            }

            for (i in 0 until input.size){
                SystemClock.sleep(1)
                data.addEntry(Entry(i.toFloat(), input[i].toFloat()), 0)
                data.notifyDataChanged()
                chart.notifyDataSetChanged()
                chart.invalidate()
            }

            chart.description.isEnabled = false // chart 밑에 description 표시 유무
            chart.setOnChartValueSelectedListener(object: OnChartValueSelectedListener{
                override fun onValueSelected(e: Entry?, h: Highlight?) {
                    if (e != null) {
                        marker.position = LatLng(runningData.path[e.x.toInt()].latitude, runningData.path[e.x.toInt()].longitude)
                        marker.map =naverMap
                        Log.d("clcickcckickcci", e.y.toString())
                        Log.d("clcickcckickcci", e.x.toString())
                    }
                }
                override fun onNothingSelected() {
                }
            })

        }
    }
}


