package com.example.cs496_week2

import android.icu.text.Transliterator
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*

class RetrofitInterface {

    companion object {
        val serverUrl = "http://192.249.19.179:80"

        val retrofit = Retrofit.Builder()
            .baseUrl(serverUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val service: RetrofitService = retrofit.create(RetrofitService::class.java)
    }

    interface RetrofitService{
        //post1
        // 매개변수를 미리 정해두는 방식
        @GET("/")
        fun getRequest(
        ): Call<UserDT>

        //post2
        // 호출하는 곳에서 매개변수를 HashMap 형태로 보내는 방식
        @POST("/user")
        fun postFirstAccess(
            @Body parameters: KakaoAccount
        ): Call<UserDT>


        @GET("/user/search_name")
        fun getSearchName(
            @Query("myId") myId: String,
            @Query("searchName") searchName: String
        ): Call<ArrayList<UserDT>>

//        @GET("/user/search_friend_name")
//        fun getSearchFriendName(
//            @Query("myId") myId: String,
//            @Query("searchName") searchName: String
//        ): Call<ArrayList<UserDT>>


        @GET("/user/add_friend")
        fun getAddFriend(
            @Query("myId") myId: String,
            @Query("inviteId") inviteId: String
        ): Call<ResponseDT>

        @GET("/user/friends")
        fun getFriends(
            @Query("id") id : String
        ) : Call<ArrayList<UserDT>>

        @GET("/running/filter_data")
        fun getFilterData(
            @Query("id") id : String
        ) : Call<ArrayList<RunningData>>

        @POST("/running/create_running")
        fun postCreateRunning(
            @Body runningData: RunningData
        ): Call<ResponseDT>

        @GET("/running/get_data")
        fun getRunningData(
            @Query("id") id : String
        ) : Call<MutableList<RunningData>>

        @GET("/running/get_each_data")
        fun getEachRunningData(
            @Query("id") id: String,
            @Query("position") position: Int
        ) : Call<RunningData>

        @POST("/group/create_group")
        fun postCreateGroup(
            @Body group: GroupDT
        ) : Call<GroupDT>

        @GET("/group")
        fun getGroup(
            @Query("id") id: Long
        ) : Call<ArrayList<GroupDT>>

    }
}