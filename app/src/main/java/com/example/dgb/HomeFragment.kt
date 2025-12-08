package com.example.dgb

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment

class HomeFragment : Fragment() {


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // 加载布局
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        // 初始化视图
        val titleText = view.findViewById<TextView>(R.id.title_text)
        titleText.text = "欢迎访问冷链卫士"

        // 初始化地图（如果需要）
        // mapView = view.findViewById(R.id.map_view)

        return view
    }

    override fun onResume() {
        super.onResume()
        // mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        // mapView.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        // mapView.onDestroy()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        // mapView.onSaveInstanceState(outState)
    }
}