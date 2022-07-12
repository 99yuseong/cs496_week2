package com.example.cs496_week2

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.cs496_week2.RetrofitInterface.Companion.service
import com.example.cs496_week2.databinding.FragmentTab4Binding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class Tab4 : Fragment() {
    private var param1: String? = null
    private var param2: String? = null

    private lateinit var binding: FragmentTab4Binding
    private lateinit var mainActivity: MainActivity
    lateinit var root: View
    lateinit var nameTv: TextView
    lateinit var histroyList: ListView
    lateinit var totRun : TextView
    lateinit var totKm : TextView
    lateinit var totPace : TextView
    lateinit var historyListAdapter: historyListAdapter
    lateinit var geocoder : Geocoder
    var serverRunData : MutableList<RunningData> = mutableListOf()

    override fun onAttach(context: Context) {
        super.onAttach(context)

        // 2. Context를 액티비티로 형변환해서 할당
        mainActivity = context as MainActivity
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
//        Log.d("user", MainActivity.user.toString())
        geocoder = Geocoder(mainActivity)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        root = inflater.inflate(R.layout.fragment_tab4, container, false)
        nameTv = root.findViewById(R.id.t4_my_name)
        histroyList = root.findViewById(R.id.history_list)
        totRun = root.findViewById(R.id.total_run)
        totKm = root.findViewById(R.id.total_km)
        totPace = root.findViewById(R.id.total_pace)

        nameTv.text = MainActivity.user.name

        histroyList!!.setOnItemClickListener { parent, view, position, id ->
            val intent = Intent(mainActivity, historyDetailActivity::class.java)
            intent.putExtra("user", serverRunData[position].user)
            intent.putExtra("position", position)
            startActivity(intent)
        }

        return root
    }

    @SuppressLint("SetTextI18n")
    override fun onResume() {
        super.onResume()
        Log.d("user", MainActivity.user.toString())

        service.getRunningData(MainActivity.user._id).enqueue(object : Callback<MutableList<RunningData>> {
            override fun onResponse(call: Call<MutableList<RunningData>>?, response: Response<MutableList<RunningData>>?) {
                if(response!!.isSuccessful) {
                    Log.d("running is back",response.body().toString() )
                    serverRunData = response.body()!!
                    historyListAdapter = historyListAdapter(serverRunData, geocoder)
                    histroyList.adapter = historyListAdapter

                    var tDist = 0.0
                    var tPace = 0.0
                    var tPaceMin = 0
                    var tPaceSec = 0
                    for(i in 0 until serverRunData.size){
                        tDist += serverRunData[i].dist
                        tPace += serverRunData[i].avgPace
                    }
                    tPace = if(serverRunData.size == 0) 0.0 else tPace / serverRunData.size
                    tPaceMin = (tPace / 60).toInt()
                    tPaceSec = (tPace % 60).toInt()
                    totRun.setText(serverRunData.size.toString())
                    totKm.setText("${String.format("%.2f", tDist / 1000.0)} km")
                    totPace.setText("${if(tDist < 1) 0 else tPaceMin}'${if(tPaceSec >= 10) tPaceSec else "0${tPaceSec}"}''")
                }
            }
            override fun onFailure(call: Call<MutableList<RunningData>>?, t: Throwable?) {
            }
        })
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