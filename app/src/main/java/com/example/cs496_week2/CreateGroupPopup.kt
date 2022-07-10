package com.example.cs496_week2

import android.app.Dialog
import android.content.Context
import android.util.Log
import android.view.WindowManager
import android.widget.Button
import android.widget.ListView
import android.widget.SearchView
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.cs496_week2.RetrofitInterface.Companion.service
import com.google.android.material.textfield.TextInputEditText
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CreateGroupPopup(context: Context) {
    val context = context
    private val dialog = Dialog(context)

    fun changeDialog(selectedList: ArrayList<UserDT>) {
        dialog.setContentView(R.layout.create_group_popup2)

        val selectedListRv = dialog.findViewById<RecyclerView>(R.id.cg2_selected_list)
        val groupNameTi = dialog.findViewById<TextView>(R.id.cg2_group_name)
        val createbtn = dialog.findViewById<Button>(R.id.cg2_create)

        selectedListRv.layoutManager = LinearLayoutManager(this.context, LinearLayoutManager.HORIZONTAL, false)
        selectedListRv.adapter = SelectedListAdapter(selectedList)

        createbtn.setOnClickListener { view ->
            val groupName = groupNameTi.getText().toString()
            val arrayList = selectedList.map { it._id }.toCollection(ArrayList<String>())
            arrayList.add(MainActivity.user._id)
            val newGroup = GroupDT(
                "",
                groupName,
                arrayList
            )
            service.postCreateGroup(newGroup).enqueue(object : Callback<GroupDT> {
                override fun onResponse(call: Call<GroupDT>, response: Response<GroupDT>) {
                    if(response.isSuccessful) {
                        MainActivity.user.group.add(response.body()!!._id)
                        Log.d("group", MainActivity.user.group.toString())
                    }
                }
                override fun onFailure(call: Call<GroupDT>, t: Throwable) {
                }
            })
            dialog.dismiss()
        }


    }

    fun showDialog() {
        dialog.setContentView(R.layout.create_group_popup1)
        dialog.window!!.setLayout(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT)
        dialog.setCanceledOnTouchOutside(true)
        dialog.setCancelable(true)
        dialog.show()

        val search_name = dialog.findViewById<SearchView>(R.id.cg_search_name)
        val friendListLv = dialog.findViewById<ListView>(R.id.cg_friend_list)
        val selectedListRv = dialog.findViewById<RecyclerView>(R.id.cg_selected_list)
        val nextBtn = dialog.findViewById<Button>(R.id.cg_next)

        lateinit var friendList: ArrayList<UserDT>
        lateinit var filterList: ArrayList<UserDT>

        val selectedList = arrayListOf<UserDT>()
        val selectedListAdapter = SelectedListAdapter(selectedList)

        selectedListRv.layoutManager = LinearLayoutManager(this.context, LinearLayoutManager.HORIZONTAL, false)
        selectedListRv.adapter = selectedListAdapter

        service.getFriends(MainActivity.user._id).enqueue(object : Callback<ArrayList<UserDT>> {
            override fun onResponse(call: Call<ArrayList<UserDT>>, response: Response<ArrayList<UserDT>>) {
                friendList = response?.body()!!
                filterList = friendList
                val friendListAdapter = UserListAdapter(context, filterList)
                friendListLv.adapter = friendListAdapter
            }

            override fun onFailure(call: Call<ArrayList<UserDT>>, t: Throwable) {
            }
        })

        search_name.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextChange(p0: String): Boolean {
                filterList = friendList.filter {
                    it.name.contains(p0)
                } as ArrayList<UserDT>
                val friendListAdapter = UserListAdapter(context, filterList)
                friendListLv.adapter = friendListAdapter
                return true
            }

            override fun onQueryTextSubmit(p0: String?): Boolean {
                return true
            }
        })

        friendListLv.setOnItemClickListener { adapterView, view, position, l ->
            if(selectedList.any { it._id == filterList[position]._id }) {
            } else {
                selectedList.add(filterList[position])
            }
            selectedListAdapter.notifyDataSetChanged()
        }

        nextBtn.setOnClickListener { view ->
            changeDialog(selectedList)
        }


    }
}