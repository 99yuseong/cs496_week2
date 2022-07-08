package com.example.cs496_week2

import android.app.Application
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
import io.socket.client.IO
import io.socket.client.Socket.EVENT_CONNECT
import io.socket.emitter.Emitter
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.net.Socket
import java.net.URISyntaxException

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    lateinit var mSocket: Socket

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val keyHash = Utility.getKeyHash(this)//onCreate 안에 입력해주자
        Log.d("Hash", keyHash)

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
    }

    inner class MyPagerAdapter(fa: FragmentActivity): FragmentStateAdapter(fa) {
        private val NUM_PAGES = 3

        override fun getItemCount(): Int = NUM_PAGES

        override fun createFragment(position: Int): Fragment {
            return when (position) {
                0 -> { Tab1.newInstance("Page 1","")}
                1 -> { Tab2.newInstance("Page 2","")}
                else -> { Tab3.newInstance("Page 3","")}
            }
        }

    }
}