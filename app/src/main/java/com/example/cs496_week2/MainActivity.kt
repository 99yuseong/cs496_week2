package com.example.cs496_week2

import android.app.Application
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.example.cs496_week2.databinding.ActivityMainBinding
import com.google.android.material.tabs.TabLayoutMediator
import com.kakao.sdk.common.KakaoSdk
import com.kakao.sdk.common.util.Utility
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import io.socket.client.IO
import io.socket.client.Socket.EVENT_CONNECT
import io.socket.emitter.Emitter
import java.net.Socket

class MainActivity : AppCompatActivity() {
    lateinit var kakaoAccount: KakaoAccount
    val serverUrl = "http://172.10.5.172:80"

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val intent = intent
        kakaoAccount = intent.getSerializableExtra("kakaoAccount") as KakaoAccount
        Log.i("KAKAO", kakaoAccount.toString())
        kakaoUser = kakaoAccount

//        val keyHash = Utility.getKeyHash(this) //onCreate 안에 입력해주자
//        Log.d("Hash", keyHash)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.viewpager.apply {
            adapter = MyPagerAdapter(context as FragmentActivity)
        }

        TabLayoutMediator(binding.tabs, binding.viewpager) { tab, position ->
            tab.text = "Title $position"
            when (position) {
                0 -> tab.setIcon(R.drawable.ic_launcher_background)
                1 -> tab.setIcon(R.drawable.ic_launcher_background)
                2 -> tab.setIcon(R.drawable.ic_launcher_background)
                3 -> tab.setIcon(R.drawable.ic_launcher_background)
            }
        }.attach()

        binding.viewpager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                when (position) {
                    0-> {
                        binding.viewpager.isUserInputEnabled = false;
                    }
                    1-> {
                        binding.viewpager.isUserInputEnabled = true;
                    }
                    2-> {
                        binding.viewpager.isUserInputEnabled = true;
                    }
                }

            }
        })

        val service = RetrofitInterface.service

        service.postFirstAccess(kakaoAccount).enqueue(object : Callback<UserDT> {
            override fun onResponse(call: Call<UserDT>?, response: Response<UserDT>?) {
                if(response != null && response!!.isSuccessful) {
                    Log.d("retrofit", response?.body().toString())
//                    user = response?.body()!!
                }
            }
            override fun onFailure(call: Call<UserDT>?, t: Throwable?) {
                Log.e("retrofit", t.toString())
            }
        })
    }

    inner class MyPagerAdapter(fa: FragmentActivity): FragmentStateAdapter(fa) {
        private val NUM_PAGES = 3

        override fun getItemCount(): Int = NUM_PAGES

        override fun createFragment(position: Int): Fragment {
            return when (position) {
                0 -> { Tab1.newInstance("Page 1","")}
                1 -> { Tab2.newInstance("Page 2","")}
                2 -> { Tab3.newInstance("Page 3","")}
                else -> { Tab3.newInstance("Page 3","")}
            }
        }
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
        fun postRequest(
            @Body parameters: KakaoAccount
        ): Call<UserDT>

    }

    companion object {
        lateinit var kakaoUser : KakaoAccount
        lateinit var user : UserDT
    }
}