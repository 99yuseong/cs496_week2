package com.example.cs496_week2

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
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
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*


class historyDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHistoryDetailBinding
    private lateinit var runningData: RunningData
    private lateinit var chart: LineChart

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHistoryDetailBinding.inflate(layoutInflater)
        setContentView(R.layout.activity_history_detail)

        val startDateView: TextView = findViewById(R.id.history_act_date)
        val distView: TextView = findViewById(R.id.history_act_distance)
        val paceView: TextView = findViewById(R.id.history_act_pace)
        val timeView: TextView = findViewById(R.id.history_act_time)
        chart = findViewById(R.id.history_act_chart)

        Log.d("intent", "sdfsdfsf")
        val userid = intent.getStringExtra("user")
        val position = intent.getIntExtra("position", 0)

        // 에러 아닙니다 "u"가 맞습니다
        val dateFormatter = SimpleDateFormat("u", Locale.getDefault())
        Log.d("server", "sdfsdfsf")
        service.getEachRunningData(userid!!, position).enqueue(object: Callback<RunningData> {
            @SuppressLint("SetTextI18n")
            override fun onResponse(call: Call<RunningData>, response: Response<RunningData>) {
                runningData = response.body()!!
                Log.d("rundfnsnfusd", runningData.toString())
                val time = runningData.time
                val min = (time / 60).toInt()
                val sec = (time % 60).toInt()
                val tDist = runningData.dist
                val tPace = runningData.avgPace
                val tPaceMin = (tPace / 60).toInt()
                val tPaceSec = (tPace % 60).toInt()
                startDateView.setText("${getDay(dateFormatter.format(runningData.startDate).toInt())} Running")
                distView.setText("${String.format("%.2f", tDist / 1000.0)} km")
                paceView.setText("${if(tDist < 1) 0 else tPaceMin}' ${if(tPaceSec >= 10) tPaceSec else "0${tPaceSec}"}''")
                timeView.setText("${if(min >= 10) min else "0${min}"}:${if(sec >= 10) sec else "0${sec}"}")
                val thread = ThreadClass()
                thread.start()
            }

            override fun onFailure(call: Call<RunningData>, t: Throwable) {

            }
        })
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
            val entries: ArrayList<Entry> = ArrayList()
            entries.add(Entry(0F, 0F))
            val dataset: LineDataSet = LineDataSet(entries, "input")
            val data: LineData = LineData(dataset)
            dataset.lineWidth = 5f
//            dataset.circleRadius = 6f
            dataset.setDrawValues(false)
//            dataset.setDrawCircleHole(true)
//            dataset.setDrawCircles(true)
            dataset.setDrawHorizontalHighlightIndicator(false)
            dataset.setDrawHighlightIndicators(false)
            dataset.color = Color.rgb(255, 155, 155)
            dataset.setCircleColor(Color.rgb(255, 155, 155))

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
            xAxis.spaceMin = 0.1f // Chart 맨 왼쪽 간격 띄우기
            xAxis.spaceMax = 0.1f // Chart 맨 오른쪽 간격 띄우기

            // YAxis(Right) (왼쪽) - 선 유무, 데이터 최솟값/최댓값, 색상
            val yAxisLeft = chart.axisLeft
            yAxisLeft.setDrawLabels(false) // label 삭제
            yAxisLeft.textColor = Color.rgb(163, 163, 163)
            yAxisLeft.setDrawAxisLine(false)
//            yAxisLeft.axisLineWidth = 2f
            yAxisLeft.axisMinimum = -180f // 최솟값
            yAxisLeft.axisMaximum = -1200f

            // YAxis(Left) (오른쪽) - 선 유무, 데이터 최솟값/최댓값, 색상
            val yAxis = chart.axisRight
            yAxis.setDrawLabels(false) // label 삭제
            yAxis.textColor = Color.rgb(163, 163, 163)
            yAxis.setDrawAxisLine(false)
//            yAxis.axisLineWidth = 180f
            yAxis.axisMinimum = -180f // 최솟값
            yAxis.axisMaximum = -1200f

            runOnUiThread {
                // 그래프 생성
                chart.animateXY(1, 1)
            }

            for (i in 0 until input.size){
                SystemClock.sleep(10)
                data.addEntry(Entry(i.toFloat(), input[i].toFloat()), 0)
                data.notifyDataChanged()
                chart.notifyDataSetChanged()
                chart.invalidate()
            }
        }
    }

//    private fun configureChartAppearance(lineChart: LineChart, range: Int) {
//        lineChart.extraBottomOffset = 15f // 간격
//        lineChart.description.isEnabled = false // chart 밑에 description 표시 유무
//
//        // Legend는 차트의 범례
//        val legend = lineChart.legend
//        legend.verticalAlignment = Legend.LegendVerticalAlignment.BOTTOM
//        legend.horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
//        legend.form = Legend.LegendForm.CIRCLE
//        legend.formSize = 10f
//        legend.textSize = 13f
//        legend.textColor = Color.parseColor("#A3A3A3")
//        legend.orientation = Legend.LegendOrientation.VERTICAL
//        legend.setDrawInside(false)
//        legend.yEntrySpace = 5f
//        legend.isWordWrapEnabled = true
//        legend.xOffset = 80f
//        legend.yOffset = 20f
//        legend.calculatedLineSizes
//
//        // XAxis (아래쪽) - 선 유무, 사이즈, 색상, 축 위치 설정
//        val xAxis = lineChart.xAxis
//        xAxis.setDrawAxisLine(false)
//        xAxis.setDrawGridLines(false)
//        xAxis.position = XAxis.XAxisPosition.BOTTOM // x축 데이터 표시 위치
//        xAxis.granularity = 1f
//        xAxis.textSize = 14f
//        xAxis.textColor = Color.rgb(118, 118, 118)
//        xAxis.spaceMin = 0.1f // Chart 맨 왼쪽 간격 띄우기
//        xAxis.spaceMax = 0.1f // Chart 맨 오른쪽 간격 띄우기
//
//        // YAxis(Right) (왼쪽) - 선 유무, 데이터 최솟값/최댓값, 색상
//        val yAxisLeft = lineChart.axisLeft
//        yAxisLeft.textSize = 14f
//        yAxisLeft.textColor = Color.rgb(163, 163, 163)
//        yAxisLeft.setDrawAxisLine(false)
//        yAxisLeft.axisLineWidth = 2f
//        yAxisLeft.axisMinimum = 0f // 최솟값
//        yAxisLeft.axisMaximum = RANGE.get(0).get(range) // 최댓값
//        yAxisLeft.granularity = RANGE.get(1).get(range)
//
//        // YAxis(Left) (오른쪽) - 선 유무, 데이터 최솟값/최댓값, 색상
//        val yAxis = lineChart.axisRight
//        yAxis.setDrawLabels(false) // label 삭제
//        yAxis.textColor = Color.rgb(163, 163, 163)
//        yAxis.setDrawAxisLine(false)
//        yAxis.axisLineWidth = 2f
//        yAxis.axisMinimum = 0f // 최솟값
//        yAxis.axisMaximum = RANGE.get(0).get(range) // 최댓값
//        yAxis.granularity = RANGE.get(1).get(range)
//
//        // XAxis에 원하는 String 설정하기 (날짜)
//        xAxis.valueFormatter = object : ValueFormatter() {
//            override fun getFormattedValue(value: Float): String {
//                return LABEL.get(range).get(value.toInt())
//            }
//        }
//    }
//
//    private fun createChartData(range: Int): LineData? {
//        val entry1: ArrayList<Entry> = ArrayList() // 앱1
//        val chartData = LineData()
//
//        // 랜덤 데이터 추출
//        for (i in 0..3) {
//            val val1 = (Math.random() * RANGE.get(0).get(range)) as Float // 앱1 값
//            entry1.add(Entry(i.toFloat(), val1))
//            entry2.add(Entry(i.toFloat(), val2))
//            entry3.add(Entry(i.toFloat(), val3))
//            entry4.add(Entry(i.toFloat(), val4))
//        }
//
//        // 4개 앱의 DataSet 추가 및 선 커스텀
//
//        // 앱1
//        val lineDataSet1 = LineDataSet(entry1, APPS.get(0))
//        chartData.addDataSet(lineDataSet1)
//        lineDataSet1.lineWidth = 3f
//        lineDataSet1.circleRadius = 6f
//        lineDataSet1.setDrawValues(false)
//        lineDataSet1.setDrawCircleHole(true)
//        lineDataSet1.setDrawCircles(true)
//        lineDataSet1.setDrawHorizontalHighlightIndicator(false)
//        lineDataSet1.setDrawHighlightIndicators(false)
//        lineDataSet1.color = Color.rgb(255, 155, 155)
//        lineDataSet1.setCircleColor(Color.rgb(255, 155, 155))
//
//        return chartData
//    }
//
//    private fun prepareChartData(data: LineData, lineChart: LineChart) {
//        lineChart.data = data // LineData 전달
//        lineChart.invalidate() // LineChart 갱신해 데이터 표시
//    }
}


