package com.example.cs496_week2

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
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




        val groupListAdapter = GroupListAdapter(ArrayList())
        groupListRv.layoutManager = LinearLayoutManager(this.context, LinearLayoutManager.HORIZONTAL, false)
        groupListRv.adapter = groupListAdapter

    }

    override fun onResume() {
        super.onResume()
        initGroupList()
    }

    fun initGroupList() {
        service.getGroup(MainActivity.user.id).enqueue(object : Callback<ArrayList<GroupDT>> {
            override fun onResponse(
                call: Call<ArrayList<GroupDT>>,
                response: Response<ArrayList<GroupDT>>
            ) {
                if (response.isSuccessful) {
                    groupList = response.body()!!
                    val groupListAdapter = GroupListAdapter(groupList)
                    groupListRv.adapter = groupListAdapter
                    groupListAdapter.setItemClickListener(object :
                        GroupListAdapter.OnItemClickListener {
                        override fun onClick(v: View, position: Int) {
                            setPage(groupList[position])
                        }
                    })

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

            setMaxVisibleValueCount(7) // 최대 보이는 그래프 개수를 7개로 지정
            setPinchZoom(false) // 핀치줌(두손가락으로 줌인 줌 아웃하는것) 설정
            setDrawBarShadow(false) //그래프의 그림자
            setDrawGridBackground(false)//격자구조 넣을건지
            axisLeft.run { //왼쪽 축. 즉 Y방향 축을 뜻한다.
                axisMaximum = yMax //100 위치에 선을 그리기 위해 101f로 맥시멈값 설정
                axisMinimum = 0f // 최소값 0
                granularity = 10f // 50 단위마다 선을 그리려고 설정.
                setDrawLabels(true) // 값 적는거 허용 (0, 50, 100)
                setDrawGridLines(true) //격자 라인 활용
                setDrawAxisLine(false) // 축 그리기 설정
                axisLineColor = ContextCompat.getColor(context, R.color.white) // 축 색깔 설정
                gridColor = ContextCompat.getColor(context, R.color.white) // 축 아닌 격자 색깔 설정
                textColor = ContextCompat.getColor(context, R.color.white) // 라벨 텍스트 컬러 설정
                textSize = 15f //라벨 텍스트 크기
            }
            xAxis.run {
                position = XAxis.XAxisPosition.BOTTOM //X축을 아래에다가 둔다.
                granularity = 1f // 1 단위만큼 간격 두기
                setDrawAxisLine(true) // 축 그림
                setDrawGridLines(false) // 격자
                textColor = ContextCompat.getColor(context, R.color.white) //라벨 색상
                textSize = 12f // 텍스트 크기
                valueFormatter = MyXAxisFormatter() // X축 라벨값(밑에 표시되는 글자) 바꿔주기 위해 설정
            }
            axisRight.isEnabled = false // 오른쪽 Y축을 안보이게 해줌.
            setTouchEnabled(false) // 그래프 터치해도 아무 변화없게 막음
            animateY(1000) // 밑에서부터 올라오는 애니매이션 적용
            legend.isEnabled = false //차트 범례 설정
        }

        var set = BarDataSet(entries,"DataSet") // 데이터셋 초기화
        set.color = ContextCompat.getColor(requireView().context, R.color.white) // 바 그래프 색 설정

        val dataSet :ArrayList<IBarDataSet> = ArrayList()
        dataSet.add(set)
        val data = BarData(dataSet)
        data.barWidth = 0.5f //막대 너비 설정
        rankingChart.run {
            this.data = data //차트의 데이터를 data로 설정해줌.
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