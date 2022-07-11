package com.example.cs496_week2

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.graphics.drawable.toDrawable
import androidx.recyclerview.widget.RecyclerView
import com.example.cs496_week2.RetrofitInterface.Companion.service
import com.kakao.sdk.user.model.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

class GroupListAdapter(var groupList: ArrayList<GroupDT>, val context: Context): RecyclerView.Adapter<GroupListAdapter.MyViewHolder>() {
    private lateinit var itemClickListener : OnItemClickListener

    inner class MyViewHolder(itemView: View?): RecyclerView.ViewHolder(itemView!!) {
        val groupNameTv = itemView!!.findViewById<TextView>(R.id.gi_group_name)
        val groupProfileIv = itemView!!.findViewById<ImageView>(R.id.gi_group_profile)

        fun bind(groupItem: GroupDT, position: Int) {
            groupNameTv.text = groupItem.groupName
            val pos = adapterPosition
            if(pos!= RecyclerView.NO_POSITION) {
                itemView.setOnClickListener {
                    itemClickListener?.onClick(itemView,pos)
                }
            }

            var count = 0
            var memberList = ArrayList<UserDT>()
            if(groupItem.member.size >= 4) {
                service.getGroupMember(groupItem._id).enqueue(object : Callback<ArrayList<UserDT>> {
                    override fun onResponse(
                        call: Call<ArrayList<UserDT>>,
                        response: Response<ArrayList<UserDT>>
                    ) {
                        memberList = response.body()!!
                        CoroutineScope(Dispatchers.Main).launch {
                            val bitmap1 = withContext(Dispatchers.IO) {
                                ImageLoader.loadImage(memberList[0].imgUrl)
                            }
                            val bitmap2 = withContext(Dispatchers.IO) {
                                ImageLoader.loadImage(memberList[1].imgUrl)
                            }
                            val bitmap3 = withContext(Dispatchers.IO) {
                                ImageLoader.loadImage(memberList[2].imgUrl)
                            }
                            val bitmap4 = withContext(Dispatchers.IO) {
                                ImageLoader.loadImage(memberList[3].imgUrl)
                            }
                            val bitmap = combineImage(bitmap1!!, bitmap2!!, bitmap3!!, bitmap4!!)
                            groupProfileIv.setImageDrawable(bitmap.toDrawable(context.resources))
                        }
                    }

                    override fun onFailure(call: Call<ArrayList<UserDT>>, t: Throwable) {
                    }
                })

            }

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        var view = LayoutInflater.from(parent.context).inflate(R.layout.group_item, parent, false)
        return MyViewHolder(view)
    }

    override fun getItemCount(): Int {
        return groupList.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.bind(groupList[position], position)
    }

    interface OnItemClickListener {
        fun onClick(v: View, position: Int)
    }

    fun setItemClickListener(onItemClickListener: OnItemClickListener) {
        this.itemClickListener = onItemClickListener
    }

    fun combineImage(first: Bitmap, second: Bitmap, third: Bitmap, fourth: Bitmap): Bitmap {
        val option = BitmapFactory.Options();
        option.inDither = true;
        option.inPurgeable = true;

        var resultBitmap = Bitmap.createScaledBitmap(first, first.getWidth()*2, first.getHeight()*2, true);

        val p = Paint()
        p.setDither(true)
        p.setFlags(Paint.ANTI_ALIAS_FLAG)

        val canvas = Canvas(resultBitmap)
        canvas.drawBitmap(first, 0.0f, 0.0f, p)
        canvas.drawBitmap(second, 0.0f, first.getHeight().toFloat(), p)
        canvas.drawBitmap(third, first.getHeight().toFloat(), 0.0f, p)
        canvas.drawBitmap(fourth, first.getHeight().toFloat(), first.getHeight().toFloat(), p)

        first.recycle()
        second.recycle()
        third.recycle()
        fourth.recycle()
        return resultBitmap;
    }

}