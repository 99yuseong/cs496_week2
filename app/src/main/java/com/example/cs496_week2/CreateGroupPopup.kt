package com.example.cs496_week2

import android.app.Activity
import android.app.Dialog
import android.app.PendingIntent.getActivity
import android.content.Context
import android.content.Intent
import android.graphics.ImageDecoder
import android.os.Build
import android.provider.MediaStore
import android.view.WindowManager
import android.widget.*
import androidx.core.app.ActivityCompat.startActivityForResult
import androidx.core.graphics.drawable.toDrawable
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.cs496_week2.RetrofitInterface.Companion.service
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CreateGroupPopup(context: Context) {
    val context = context
    private val dialog = Dialog(context)
    private val requestStorage = 300

    fun showDialog(groupList: ArrayList<GroupDT>, groupListAdapter: GroupListAdapter) {
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
            if(selectedList.size >= 3) changeDialog(selectedList, groupList, groupListAdapter)

        }

    }

    fun changeDialog(selectedList: ArrayList<UserDT>, groupList: ArrayList<GroupDT>, groupListAdapter: GroupListAdapter) {
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
                        val newGroup = response.body()
                        MainActivity.user.group.add(newGroup!!._id)
                        groupList.add(newGroup)
                        groupListAdapter.notifyDataSetChanged()
                    }
                }
                override fun onFailure(call: Call<GroupDT>, t: Throwable) {
                }
            })
            Toast.makeText(context,"New group created", Toast.LENGTH_LONG)
            dialog.dismiss()
        }
    }

//    fun openGallery() {
//        val intent = Intent(Intent.ACTION_PICK)
//        intent.type = "image/*"
//        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
//        intent.action = Intent.ACTION_GET_CONTENT
//        frag.startActivityForResult(intent, requestStorage)
//    }




//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
//
//        if(resultCode != Activity.RESULT_OK) return
//
//        when(requestCode) {
//            requestStorage -> {
//                data?.data?.let {
//                    val source = ImageDecoder.createSource(context.contentResolver, it)
//                    val bitmap = ImageDecoder.decodeBitmap(source)
//                }
//            }
//        }
//
//        if (resultCode != Activity.RESULT_OK && requestCode == requestStorage) {
//            if(data?.clipData != null) {
//                val count = data.clipData!!.itemCount
//                for (i in 0 until count) {
//                    val imageUri = data.clipData!!.getItemAt(i).uri
//
//                    imageUri?.let {
//                        if(Build.VERSION.SDK_INT < 28) {
//                            val bitmap = MediaStore.Images.Media.getBitmap(
//                                mcontext.contentResolver,
//                                imageUri
//                            )
//                        } else {
//                            val source = ImageDecoder.createSource(mcontext.contentResolver, imageUri)
//                            val bitmap = ImageDecoder.decodeBitmap(source)
//                            totlist?.add(
//                                GalleryItem(bitmap.toDrawable(resources)),
//                            )
//                        }
//                    }
//                }
//            }
//            else {
//                data?.data?.let {
//                    if(Build.VERSION.SDK_INT < 28) {
//                        val bitmap = MediaStore.Images.Media.getBitmap(
//                            context.contentResolver,
//                            it
//                        )
//                    } else {
//                        val source = ImageDecoder.createSource(context.contentResolver, it)
//                        val bitmap = ImageDecoder.decodeBitmap(source)
//                        totlist?.add(
//                            GalleryItem(bitmap.toDrawable(resources)),
//                        )
//                    }
//                }
//            }
//            galleryAdapter.notifyDataSetChanged()
//        }
//    }


}