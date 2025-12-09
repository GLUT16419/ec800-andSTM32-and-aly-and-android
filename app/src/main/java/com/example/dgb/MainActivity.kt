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
        // +++ 新增：设置高德SDK隐私合规接口，必须在所有SDK调用前执行 +++
        // 告知SDK：你的App隐私政策中已包含高德隐私政策内容，并已弹窗告知用户
        com.amap.api.maps.MapsInitializer.updatePrivacyShow(this, true, true)
        // 告知SDK：用户已同意隐私政策
        com.amap.api.maps.MapsInitializer.updatePrivacyAgree(this, true)
        // 启动 MQTT 服务
        startService(Intent(this, MqttService::class.java))

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)

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