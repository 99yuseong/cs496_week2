package com.example.cs496_week2

import android.app.Dialog
import android.content.Context
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.*
import android.widget.AdapterView.OnItemClickListener
import com.example.cs496_week2.RetrofitInterface.Companion.service
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class AddFriendPopup(context: Context) {
    val context = context
    private val dialog = Dialog(context)

    fun showDialog(friendList: ArrayList<UserDT>, friendListAdapter: FriendListAdapter) {
        dialog.setContentView(R.layout.add_friend_popup)
        dialog.window!!.setLayout(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT)
        dialog.setCanceledOnTouchOutside(true)
        dialog.setCancelable(true)
        dialog.show()

        val search_name = dialog.findViewById<SearchView>(R.id.search_name)
        val listview = dialog.findViewById<ListView>(R.id.friend_list)

        val searchResult = arrayListOf<UserDT>()

        val listAdapter = UserListAdapter(context, searchResult)
        listview.adapter = listAdapter

        listview.setOnItemClickListener { adapterView, view, position, l ->
            service.getAddFriend(MainActivity.user._id, searchResult[position]._id).enqueue(object : Callback<ResponseDT> {
                override fun onResponse(call: Call<ResponseDT>?, response: Response<ResponseDT>?) {
                    if(response!!.isSuccessful) {
                        MainActivity.user.friends.add(response.body()!!.message)
                        friendList.add(searchResult[position])
                        friendListAdapter.notifyDataSetChanged()
                        Toast.makeText(context,"New friend added", Toast.LENGTH_SHORT)
                    }
                }

                override fun onFailure(call: Call<ResponseDT>?, t: Throwable?) {
                    Log.e("retrofit", t.toString())
                }
            })
        }

        search_name.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(p0: String): Boolean {
                if(p0 == "") return true
                searchResult.clear()
                service.getSearchName(MainActivity.user._id, p0).enqueue(object : Callback<ArrayList<UserDT>> {
                    override fun onResponse(call: Call<ArrayList<UserDT>>?, response: Response<ArrayList<UserDT>>?) {
                        if(response!!.isSuccessful) {
                            Log.d("retrofit", response?.body().toString())
                            response?.body()?.map {
                                searchResult.add(it)
                            }
                        }
                        listAdapter.notifyDataSetChanged()

                    }
                    override fun onFailure(call: Call<ArrayList<UserDT>>?, t: Throwable?) {
                        Log.e("retrofit", t.toString())
                    }
                })
                listAdapter.notifyDataSetChanged()
                return true
            }

            override fun onQueryTextChange(p0: String): Boolean {
                return true
            }
        })
    }
}