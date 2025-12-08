package com.example.dgb

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment

class DeviceFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_device, container, false)

        val titleText = view.findViewById<TextView>(R.id.title_text)
        titleText.text = "已注册设备"

        // 这里可以添加设备列表的逻辑

        return view
    }
}