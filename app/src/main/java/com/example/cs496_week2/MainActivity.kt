package com.example.cs496_week2

import android.app.Application
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.example.cs496_week2.RetrofitInterface.Companion.service
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

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val toolbar: Toolbar = findViewById(R.id.my_toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        binding.viewpager.apply {
            adapter = MyPagerAdapter(context as FragmentActivity)
        }

        TabLayoutMediator(binding.tabs, binding.viewpager) { tab, position ->
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
                        toolbar.title = "Running"
                        binding.viewpager.isUserInputEnabled = false;
                    }
                    1-> {
                        toolbar.title = "Friends"
                        binding.viewpager.isUserInputEnabled = true;
                    }
                    2-> {
                        toolbar.title = "Groups"
                        binding.viewpager.isUserInputEnabled = true;
                    }
                    3-> {
                        toolbar.title = "Records"
                        binding.viewpager.isUserInputEnabled = true;
                    }
                }

            }
        })

        service.postFirstAccess(kakaoAccount).enqueue(object : Callback<UserDT> {
            override fun onResponse(call: Call<UserDT>?, response: Response<UserDT>?) {
                if(response != null && response!!.isSuccessful) {
                    user = response?.body()!!
                    Log.d("UserDT", MainActivity.user.toString())
                }
            }
            override fun onFailure(call: Call<UserDT>?, t: Throwable?) {
                Log.e("retrofit", t.toString())
                Log.e("retrofit", call.toString())
            }
        })
    }

    inner class MyPagerAdapter(fa: FragmentActivity): FragmentStateAdapter(fa) {
        private val NUM_PAGES = 4

        override fun getItemCount(): Int = NUM_PAGES

        override fun createFragment(position: Int): Fragment {
            return when (position) {
                0 -> { Tab1.newInstance("Page 1","")}
                1 -> { Tab2.newInstance("Page 2","")}
                2 -> { Tab3.newInstance("Page 3","")}
                else -> { Tab4.newInstance("Page 4","")}
            }
        }
    }

    companion object {
        lateinit var kakaoUser : KakaoAccount
        lateinit var user : UserDT
    }
}