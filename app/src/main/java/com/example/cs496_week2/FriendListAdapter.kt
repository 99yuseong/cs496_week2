package com.example.cs496_week2

import android.content.Context
import android.content.res.Resources
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import androidx.core.graphics.drawable.toDrawable
import com.example.cs496_week2.RetrofitInterface.Companion.service
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.overlay.OverlayImage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.w3c.dom.Text
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.math.round

class FriendListAdapter(val context: Context, val friendList: ArrayList<UserDT>) : BaseAdapter() {
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view: View = LayoutInflater.from(context).inflate(R.layout.friend_item, null)

        val profileIv = view.findViewById<ImageView>(R.id.fi_profile_image)
        val nameTv = view.findViewById<TextView>(R.id.fi_name)
        val distTv = view.findViewById<TextView>(R.id.fi_dist)
        val paceTv = view.findViewById<TextView>(R.id.fi_avg_pace)

        val friend = friendList[position]

        service.getFilterData(friend._id).enqueue(object : Callback<ArrayList<RunningData>> {
            override fun onResponse(call: Call<ArrayList<RunningData>>, response: Response<ArrayList<RunningData>>) {
                if(response!!.isSuccessful) {
                    val runningList = response.body()
                    val distPace =  runningList?.fold(Pair(0.0, 0.0)) { total, running ->
                        Pair(total.first + running.dist, total.second + running.avgPace)
                    }
                    val avgPace = distPace?.second!!/runningList!!.size
                    val paceMin = (avgPace / 60).toInt()
                    val paceSec = (avgPace % 60).toInt()
                    val distText = String.format("%.2f", distPace?.first/1000.0) + " km"
                    val paceText = "${paceMin}'${paceSec}\""
                    distTv.text = distText
                    paceTv.text = paceText
                }
            }

            override fun onFailure(call: Call<ArrayList<RunningData>>, t: Throwable) {
                TODO("Not yet implemented")
            }
        })

        CoroutineScope(Dispatchers.Main).launch {
            val bitmap = withContext(Dispatchers.IO) {
                ImageLoader.loadImage(friend.imgUrl)
            }
            profileIv.setImageDrawable(bitmap?.toDrawable(context.resources))
        }

        nameTv.text = friend.name

        return view
    }

    override fun getItem(position: Int): Any {
        return friendList[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getCount(): Int {
        return friendList.size
    }
}