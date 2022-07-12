package com.example.cs496_week2

import android.content.Context
import android.graphics.Canvas
import android.widget.TextView
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.highlight.Highlight

class paceMarkerView : MarkerView{
    private lateinit var tvContent: TextView

    // marker
    constructor(context: Context?, layoutResource: Int) : super(context, layoutResource) {

        tvContent = findViewById(R.id.pace_marker_view)
    }

    // draw override를 사용해 marker의 위치 조정 (bar의 상단 중앙)
    override fun draw(canvas: Canvas?) {
        canvas!!.translate(-(width / 2).toFloat(), 0.0F)

        super.draw(canvas)
    }

    // entry를 content의 텍스트에 지정
    override fun refreshContent(e: Entry?, highlight: Highlight?) {
        var pace = e?.y?.times((-60))
        var tPaceMin = 0
        var tPaceSec = 0
        if (pace != null) {
            tPaceMin = (pace / 60).toInt()
            tPaceSec = (pace % 60).toInt()
        }
        tvContent.setText("${if(tPaceMin > 20 || tPaceMin < 0) 20 else tPaceMin}' ${if(tPaceSec >= 10) tPaceSec else "0${tPaceSec}"}''")
        super.refreshContent(e, highlight)
    }
}