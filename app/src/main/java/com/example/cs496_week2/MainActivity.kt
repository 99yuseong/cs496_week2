package com.example.cs496_week2

import android.app.Application
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.cs496_week2.databinding.ActivityMainBinding
import com.google.android.material.tabs.TabLayoutMediator
import com.kakao.sdk.common.KakaoSdk
import com.kakao.sdk.common.util.Utility

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
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
    }

    inner class MyPagerAdapter(fa: FragmentActivity): FragmentStateAdapter(fa) {
        private val NUM_PAGES = 3

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
}