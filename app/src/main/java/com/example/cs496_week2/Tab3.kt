package com.example.cs496_week2

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.cs496_week2.RetrofitInterface.Companion.service
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [Tab3.newInstance] factory method to
 * create an instance of this fragment.
 */
class Tab3 : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    lateinit var root: View

    lateinit var createGroupBtn: View
    lateinit var groupNameTv: TextView
    lateinit var groupListRv: RecyclerView
    lateinit var runsTv: TextView
    lateinit var kmTv: TextView
    lateinit var paceTv: TextView
    lateinit var rankingChart: BarChart

    lateinit var groupList: ArrayList<GroupDT>
    lateinit var memberList: ArrayList<UserDT>
    lateinit var memberStatisticList: ArrayList<RunningStatisticDT>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        root = inflater.inflate(R.layout.fragment_tab3, container, false)
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        createGroupBtn = root.findViewById(R.id.create_group)
        groupNameTv = root.findViewById<TextView>(R.id.t3_group_name)
        groupListRv = root.findViewById<RecyclerView>(R.id.t3_group_list)
        runsTv = root.findViewById<TextView>(R.id.t3_runs)
        kmTv = root.findViewById<TextView>(R.id.t3_km)
        paceTv = root.findViewById<TextView>(R.id.t3_pace)
        rankingChart = root.findViewById(R.id.ranking_chart)

        val groupListAdapter = GroupListAdapter(ArrayList(), this.requireContext())
        groupListRv.layoutManager = LinearLayoutManager(this.context, LinearLayoutManager.HORIZONTAL, false)
        groupListRv.adapter = groupListAdapter

    }

    override fun onResume() {
        super.onResume()
        initGroupList(this.requireContext())
    }

    fun initGroupList(context: Context) {
        service.getGroup(MainActivity.user.id).enqueue(object : Callback<ArrayList<GroupDT>> {
            override fun onResponse(
                call: Call<ArrayList<GroupDT>>,
                response: Response<ArrayList<GroupDT>>
            ) {
                if (response.isSuccessful) {
                    groupList = response.body()!!
                    val groupListAdapter = GroupListAdapter(groupList, context)
                    groupListRv.adapter = groupListAdapter
                    groupListAdapter.setItemClickListener(object :
                        GroupListAdapter.OnItemClickListener {
                        override fun onClick(v: View, position: Int) {
                            setPage(groupList[position])
                        }
                    })
                    this
                    createGroupBtn.setOnClickListener { view ->
                        val popup = CreateGroupPopup(view.context)
                        popup.showDialog(groupList, groupListAdapter)
                    }
                }
            }

            override fun onFailure(call: Call<ArrayList<GroupDT>>, t: Throwable) {
            }
        })
    }

    fun setPage(group: GroupDT) {
        groupNameTv.text = group.groupName
        service.getGroupMember(group._id).enqueue(object : Callback<ArrayList<UserDT>> {
            override fun onResponse(
                call: Call<ArrayList<UserDT>>,
                response: Response<ArrayList<UserDT>>
            ) {
                if (response.isSuccessful) {
                    Log.d("member", response.body().toString())
                    memberList = response.body()!!
                    getMemberRunningStatistic()
                }
            }

            override fun onFailure(call: Call<ArrayList<UserDT>>, t: Throwable) {
            }
        })
    }

    fun getMemberRunningStatistic() {
        memberStatisticList = arrayListOf()
        var numLoaded = 0

        memberList.map {
            service.getFilterData(it._id).enqueue(object : Callback<ArrayList<RunningData>> {
                override fun onResponse(
                    call: Call<ArrayList<RunningData>>,
                    response: Response<ArrayList<RunningData>>
                ) {
                    if (response.isSuccessful) {
                        val runningList = response.body()
                        val runs = runningList?.size
                        val distPace = runningList?.fold(Pair(0.0, 0.0)) { total, running ->
                            Pair(total.first + running.dist, total.second + running.avgPace)
                        }
                        val dist = distPace?.first!! / 1000.0
                        val avgPace = if (runningList.size == 0) {
                            0.0
                        } else {
                            distPace?.second!! / runningList!!.size
                        }

                        memberStatisticList.add(
                            RunningStatisticDT(
                                it._id,
                                it.name,
                                runs!!,
                                dist,
                                avgPace
                            )
                        )
                        numLoaded++
                        if (numLoaded == memberList.size) {
                            val comparator : Comparator<RunningStatisticDT> = compareBy { it.dist }
                            memberStatisticList.sortWith(comparator)
                            memberStatisticList.reverse()
                            makeStatistic()
                            makeRanking()
                        }
                    }
                }

                override fun onFailure(call: Call<ArrayList<RunningData>>, t: Throwable) {
                }
            })
        }
    }

    fun makeStatistic() {
        val groupRunningStatistic = memberStatisticList.fold(Triple(0, 0.0, 0.0)) { total, data ->
            Triple(
                total.first + data.runs,
                total.second + data.dist,
                total.third + data.avgPace
            )
        }

        val distText = String.format("%.2f", groupRunningStatistic.second)
        val avgPace = groupRunningStatistic.third!! / memberStatisticList.size
        val paceMin = (avgPace / 60).toInt()
        val paceSec = (avgPace % 60).toInt()
        val paceText = "${paceMin}'${paceSec}\""
        runsTv.text = groupRunningStatistic.first.toString()
        kmTv.text = distText
        paceTv.text = paceText
    }

    fun makeRanking() {
        val entries = ArrayList<BarEntry>()

        val maxDist = memberStatisticList[0].dist
        val yMax = (((maxDist/10).toInt())*10 + 10).toFloat()
        Log.d("ymax", yMax.toString())

        memberStatisticList.mapIndexed { index, data ->
            entries.add(BarEntry((index+1).toFloat(), data.dist.toFloat()))
        }

        rankingChart.run {
            description.isEnabled = false

            setMaxVisibleValueCount(7) // ?????? ????????? ????????? ????????? 7?????? ??????
            setPinchZoom(false) // ?????????(?????????????????? ?????? ??? ???????????????) ??????
            setDrawBarShadow(false) //???????????? ?????????
            setDrawGridBackground(false)//???????????? ????????????
            axisLeft.run { //?????? ???. ??? Y?????? ?????? ?????????.
                axisMaximum = yMax //100 ????????? ?????? ????????? ?????? 101f??? ???????????? ??????
                axisMinimum = 0f // ????????? 0
                granularity = 10f // 50 ???????????? ?????? ???????????? ??????.
                setDrawLabels(true) // ??? ????????? ?????? (0, 50, 100)
                setDrawGridLines(true) //?????? ?????? ??????
                setDrawAxisLine(false) // ??? ????????? ??????
                axisLineColor = ContextCompat.getColor(context, R.color.white) // ??? ?????? ??????
                gridColor = ContextCompat.getColor(context, R.color.white) // ??? ?????? ?????? ?????? ??????
                textColor = ContextCompat.getColor(context, R.color.white) // ?????? ????????? ?????? ??????
                textSize = 15f //?????? ????????? ??????
            }
            xAxis.run {
                position = XAxis.XAxisPosition.BOTTOM //X?????? ??????????????? ??????.
                granularity = 1f // 1 ???????????? ?????? ??????
                setDrawAxisLine(true) // ??? ??????
                setDrawGridLines(false) // ??????
                textColor = ContextCompat.getColor(context, R.color.white) //?????? ??????
                textSize = 12f // ????????? ??????
                valueFormatter = MyXAxisFormatter() // X??? ?????????(?????? ???????????? ??????) ???????????? ?????? ??????
            }
            axisRight.isEnabled = false // ????????? Y?????? ???????????? ??????.
            setTouchEnabled(false) // ????????? ???????????? ?????? ???????????? ??????
            animateY(1000) // ??????????????? ???????????? ??????????????? ??????
            legend.isEnabled = false //?????? ?????? ??????
        }

        var set = BarDataSet(entries,"DataSet") // ???????????? ?????????
        set.color = ContextCompat.getColor(requireView().context, R.color.`object`) // ??? ????????? ??? ??????

        val dataSet :ArrayList<IBarDataSet> = ArrayList()
        dataSet.add(set)
        val data = BarData(dataSet)
        data.barWidth = 0.5f //?????? ?????? ??????
        rankingChart.run {
            this.data = data //????????? ???????????? data??? ????????????.
            setFitBars(true)
            invalidate()
        }

    }

    inner class MyXAxisFormatter : ValueFormatter() {
        private val names = memberStatisticList.map { it.name }
        override fun getAxisLabel(value: Float, axis: AxisBase?): String {
            return names.getOrNull(value.toInt()-1) ?: value.toString()
        }
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment Tab3.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            Tab3().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}