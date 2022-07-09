package com.example.cs496_week2

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
            @Query("name") name: String
        ): Call<ArrayList<UserDT>>

        @GET("/user/add_friend")
        fun getAddFriend(
            @Query("name") name: String,
            @Query("invite") invite: String
        ): String

    }
}