package com.example.cs496_week2

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import android.widget.TextView
import com.example.cs496_week2.databinding.FragmentTab4Binding

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class Tab4 : Fragment() {
    private var param1: String? = null
    private var param2: String? = null

    private lateinit var binding: FragmentTab4Binding
    lateinit var root: View
    lateinit var histroyList: ListView
    lateinit var totRun : TextView
    lateinit var totKm : TextView
    lateinit var totPace : TextView
    lateinit var historyListAdapter: historyListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
//        Log.d("user", MainActivity.user.toString())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        root = inflater.inflate(R.layout.fragment_tab4, container, false)
        histroyList = root.findViewById(R.id.history_list)
        totRun = root.findViewById(R.id.total_run)
        totKm = root.findViewById(R.id.total_km)
        totPace = root.findViewById(R.id.total_pace)
        return root
    }

    @SuppressLint("SetTextI18n")
    override fun onResume() {
        super.onResume()
        Log.d("user", MainActivity.user.toString())
        historyListAdapter = historyListAdapter(MainActivity.user.running)
        histroyList.adapter = historyListAdapter

        var tDist = 0.0
        var tPace = 0.0
        var tPaceMin = 0
        var tPaceSec = 0
        for(i in 0 until MainActivity.user.running.size){
            tDist += MainActivity.user.running[i].dist
            tPace += MainActivity.user.running[i].avgPace
        }
        tPace = if(MainActivity.user.running.size == 0) 0.0 else tPace / MainActivity.user.running.size
        tPaceMin = (tPace / 60).toInt()
        tPaceSec = (tPace % 60).toInt()
        totRun.setText(MainActivity.user.running.size.toString())
        totKm.setText("${String.format("%.2f", tDist / 1000.0)} km")
        totPace.setText("${if(tDist < 1) 0 else tPaceMin}' ${if(tPaceSec >= 10) tPaceSec else "0${tPaceSec}"}''")
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            Tab4().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}