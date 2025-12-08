package com.example.dgb

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 启动 MQTT 服务
        startService(Intent(this, MqttService::class.java))

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        val fragmentContainer = findViewById<androidx.fragment.app.FragmentContainerView>(R.id.fragment_container)

        // 设置默认显示主页
        if (savedInstanceState == null) {
            replaceFragment(HomeFragment())
            bottomNavigationView.selectedItemId = R.id.nav_home
        }

        // 底部导航点击监听
        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    replaceFragment(HomeFragment())
                    true
                }
                R.id.nav_device -> {
                    replaceFragment(DeviceFragment())
                    true
                }
                R.id.nav_user -> {
                    replaceFragment(UserFragment())
                    true
                }
                else -> false
            }
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }
}