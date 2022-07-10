package com.example.cs496_week2

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import androidx.fragment.app.Fragment
import com.example.cs496_week2.RetrofitInterface.Companion.service
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [Tab2.newInstance] factory method to
 * create an instance of this fragment.
 */
class Tab2 : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    lateinit var root :View

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
        root = inflater.inflate(R.layout.fragment_tab2, container, false)
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val friendListLv = root.findViewById<ListView>(R.id.friend_list)
        val addFriendBtn : View = root.findViewById(R.id.add_friend)

        var friendList = arrayListOf<UserDT>()
        var friendListAdapter = FriendListAdapter(view.context, friendList)

        service.getFriends(MainActivity.user._id).enqueue(object : Callback<ArrayList<UserDT>> {
            override fun onResponse(call: Call<ArrayList<UserDT>>, response: Response<ArrayList<UserDT>>) {
                if(response!!.isSuccessful) {
                    Log.d("Friends", response.body()!!.toString())
                    response.body()!!.map {
                        friendList.add(it)
                    }
                    friendListAdapter.notifyDataSetChanged()
                    friendListLv.adapter = friendListAdapter
                }
            }

            override fun onFailure(call: Call<ArrayList<UserDT>>, t: Throwable) {
                TODO("Not yet implemented")
            }
        })

        addFriendBtn.setOnClickListener { view ->
            val popup = AddFriendPopup(view.context)
            popup.showDialog(friendList, friendListAdapter)
        }
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment Tab2.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            Tab2().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }



}