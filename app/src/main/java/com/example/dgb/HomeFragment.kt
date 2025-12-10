package com.example.dgb

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import com.amap.api.maps.AMap
import com.amap.api.maps.MapView
import com.amap.api.maps.model.LatLng
import com.amap.api.maps.model.MarkerOptions
import com.example.dgb.MqttService.Companion.deviceList
import kotlinx.coroutines.*
import java.text.SimpleDateFormat
import java.util.*

class HomeFragment : Fragment() {

    private lateinit var mapView: MapView
    private var aMap: AMap? = null
    private lateinit var titleText: TextView
    private lateinit var cardsContainer: ViewGroup
    private var updateJob: Job? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        // 初始化视图
        titleText = view.findViewById(R.id.title_text)
        titleText.text = "冷链卫士 - 实时监控"

        // 初始化地图
        mapView = view.findViewById(R.id.map_view)
        mapView.onCreate(savedInstanceState)

        // 获取地图实例
        aMap = mapView.map

        // 设置地图基本配置
        aMap?.let {
            it.uiSettings.isZoomControlsEnabled = true
            it.uiSettings.isCompassEnabled = true
            it.uiSettings.isMyLocationButtonEnabled = true
        }

        // 初始化设备列表容器
        cardsContainer = view.findViewById(R.id.cards_linear_layout)

        // 初始化设备数据
        initializeDeviceData()

        // 添加设备卡片到界面
        addDeviceCards()

        // 启动定时更新
        startPeriodicUpdates()

        return view
    }

    private fun initializeDeviceData() {
        MqttService.deviceList.clear()

        // 模拟数据 - 冷链设备（添加氧气浓度）
        MqttService.deviceList.addAll(listOf(
            MqttService.ColdChainDevice(
                id = 1,
                name = "冷藏车-沪A12345",
                status = MqttService.DeviceStatus.NORMAL,
                temperature = "2.5°C",
                humidity = "65%",
                oxygenLevel = "20.8%", // 正常氧气浓度
                location = "上海市浦东新区张江高科技园区",
                lastUpdate = Date(),
                latLng = LatLng(31.230416, 121.473701),
                speed = "0.0km/h"
            ),
            MqttService.ColdChainDevice(
                id = 2,
                name = "冷库-浦东配送中心",
                status = MqttService.DeviceStatus.WARNING,
                temperature = "4.2°C",
                humidity = "70%",
                oxygenLevel = "19.5%", // 略低氧气浓度
                location = "上海市浦东新区金桥出口加工区",
                lastUpdate = Date(System.currentTimeMillis() - 300000),
                latLng = LatLng(31.263789, 121.593279),
                speed = "0.0km/h"

            ),
            MqttService.ColdChainDevice(
                id = 3,
                name = "冷藏柜-徐汇门店",
                status = MqttService.DeviceStatus.ERROR,
                temperature = "8.7°C",
                humidity = "75%",
                oxygenLevel = "18.2%", // 危险氧气浓度
                location = "上海市徐汇区淮海中路",
                lastUpdate = Date(System.currentTimeMillis() - 600000),
                latLng = LatLng(31.223237, 121.458698),
                speed = "0.0km/h"
            ),
            MqttService.ColdChainDevice(
                id = 4,
                name = "冷藏车-沪B67890",
                status = MqttService.DeviceStatus.NORMAL,
                temperature = "3.1°C",
                humidity = "62%",
                oxygenLevel = "20.9%", // 正常氧气浓度
                location = "上海市虹桥国际机场货运区",
                lastUpdate = Date(),
                latLng = LatLng(31.197358, 121.333083),
                speed = "0.0km/h"
            ),
            MqttService.ColdChainDevice(
                id = 5,
                name = "冷库-松江仓储中心",
                status = MqttService.DeviceStatus.NORMAL,
                temperature = "1.8°C",
                humidity = "58%",
                oxygenLevel = "21.0%", // 正常氧气浓度
                location = "上海市松江区泗泾物流园区",
                lastUpdate = Date(System.currentTimeMillis() - 120000),
                latLng = LatLng(31.128789, 121.268462),
                speed = "0.0km/h"
            ),
            MqttService.ColdChainDevice(
                id = 6,
                name = "疫苗运输车-沪C11223",
                status = MqttService.DeviceStatus.WARNING,
                temperature = "3.5°C",
                humidity = "60%",
                oxygenLevel = "19.8%", // 警戒氧气浓度
                location = "上海市长宁区临空经济园区",
                lastUpdate = Date(System.currentTimeMillis() - 180000),
                latLng = LatLng(31.223850, 121.362350),
                speed = "0.0km/h"
            )
        ))

        // 在地图上添加标记点
        addMarkersToMap()
    }

    private fun addMarkersToMap() {
        MqttService.deviceList.forEach { device ->
            aMap?.addMarker(
                MarkerOptions()
                    .position(device.latLng)
                    .title(device.name)
                    .snippet("温度: ${device.temperature} | 氧气: ${device.oxygenLevel}")
            )
        }
        // 如果设备列表不为空，将地图镜头移动到第一个设备位置
        if (deviceList.isNotEmpty()) {
            aMap?.moveCamera(
                com.amap.api.maps.CameraUpdateFactory.newLatLngZoom(
                    deviceList[0].latLng,
                    10f
                )
            )
        }
    }

    private fun addDeviceCards() {
        // 清空现有卡片
        cardsContainer.removeAllViews()

        // 为每个设备创建卡片
        deviceList.forEach { device ->
            val cardView = LayoutInflater.from(requireContext())
                .inflate(R.layout.item_device_card, cardsContainer, false) as CardView

            // 设置设备名称
            cardView.findViewById<TextView>(R.id.device_name).text = device.name

            // 设置设备状态
            val statusText = cardView.findViewById<TextView>(R.id.device_status)
            statusText.text = device.status.displayName

            // 根据状态设置背景颜色
            when (device.status) {
                MqttService.DeviceStatus.NORMAL ->
                    statusText.setBackgroundResource(R.drawable.status_background_normal)
                MqttService.DeviceStatus.WARNING ->
                    statusText.setBackgroundResource(R.drawable.status_background_warning)
                MqttService.DeviceStatus.ERROR ->
                    statusText.setBackgroundResource(R.drawable.status_background_error)
            }

            // 设置温度信息
            cardView.findViewById<TextView>(R.id.device_temp).text = device.temperature

            // 设置湿度信息
            cardView.findViewById<TextView>(R.id.device_humidity).text = device.humidity

            // 设置氧气浓度信息
            cardView.findViewById<TextView>(R.id.device_oxygen).text = device.oxygenLevel

            // 根据氧气浓度设置颜色（可选：低于19.5%显示警告色）
            val oxygenText = cardView.findViewById<TextView>(R.id.device_oxygen)
            val oxygenValue = device.oxygenLevel.replace("%", "").toDoubleOrNull() ?: 20.9
            when {
                oxygenValue < 19.5 -> oxygenText.setTextColor(resources.getColor(R.color.status_error, null))
                oxygenValue < 20.0 -> oxygenText.setTextColor(resources.getColor(R.color.status_warning, null))
                else -> oxygenText.setTextColor(resources.getColor(R.color.primary_text, null))
            }

            // 设置位置信息
            cardView.findViewById<TextView>(R.id.device_location).text = device.location

            // 设置最后更新时间
            val dateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
            cardView.findViewById<TextView>(R.id.device_last_update).text =
                "最后更新: ${dateFormat.format(device.lastUpdate)}"

            // 为卡片添加点击事件
            cardView.setOnClickListener {
                // 点击卡片时，将地图镜头移动到该设备位置
                aMap?.animateCamera(
                    com.amap.api.maps.CameraUpdateFactory.newLatLngZoom(
                        device.latLng,
                        14f
                    )
                )

                // 显示设备详细信息提示
                showDeviceDetails(device)
            }

            // 将卡片添加到容器
            cardsContainer.addView(cardView)
        }
    }

    private fun showDeviceDetails(device: MqttService.ColdChainDevice) {
        // 这里可以添加显示设备详细信息的逻辑
        // 例如：显示一个对话框或Snackbar
        val message = "${device.name}\n温度: ${device.temperature}\n湿度: ${device.humidity}\n氧气: ${device.oxygenLevel}"

        // 简单示例：可以在Log中显示
        println("设备详情: $message")
    }

    private fun startPeriodicUpdates() {
        // 停止之前的更新任务
        updateJob?.cancel()

        // 启动新的更新任务（每30秒更新一次）
        updateJob = CoroutineScope(Dispatchers.Main).launch {
            while (isActive) {
                delay(5) // 5秒延迟
                updateDeviceData()
                refreshDeviceCards()
            }
        }
    }

    private fun updateDeviceData() {
        // 模拟数据更新
        deviceList.forEachIndexed { index, device ->
//            // 随机更新温度（模拟实时变化）
//            val randomTemp = 2.0 + (index * 0.5) + (Math.random() * 2 - 1)
//            device.temperature = "${String.format("%.1f", randomTemp)}°C"
//
//            // 随机更新湿度
//            val randomHumidity = 60.0 + (index * 2) + (Math.random() * 4 - 2)
//            device.humidity = "${String.format("%.0f", randomHumidity)}%"
//
//            // 随机更新氧气浓度（正常范围18-22%）
//            val randomOxygen = 19.0 + (Math.random() * 3)
//            device.oxygenLevel = "${String.format("%.1f", randomOxygen)}%"
//
//            // 根据氧气浓度自动调整状态
//            val oxygenValue = randomOxygen
            device.status = when {
                (device.oxygenLevel.replace("%", "").toDoubleOrNull() ?: 0.0) < 18.5 -> MqttService.DeviceStatus.ERROR
                (device.temperature.replace("°C", "").toDoubleOrNull() ?: 25.0) >5.0 -> MqttService.DeviceStatus.ERROR
                (device.humidity.replace("%", "").toDoubleOrNull() ?: 25.0) >50.0 -> MqttService.DeviceStatus.ERROR
                (device.oxygenLevel.replace("%", "").toDoubleOrNull() ?: 0.0) < 19.5 -> MqttService.DeviceStatus.WARNING
                (device.temperature.replace("°C", "").toDoubleOrNull() ?: 25.0) >0.0 -> MqttService.DeviceStatus.WARNING
                (device.humidity.replace("%", "").toDoubleOrNull() ?: 25.0) >30.0 -> MqttService.DeviceStatus.WARNING
                else -> MqttService.DeviceStatus.NORMAL
            }

            // 更新最后更新时间
            device.lastUpdate = Date()
        }
    }

    private fun refreshDeviceCards() {
        // 刷新卡片显示
        addDeviceCards()
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
        // 重新启动定时更新
        startPeriodicUpdates()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
        // 停止定时更新
        updateJob?.cancel()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
        // 确保停止所有协程
        updateJob?.cancel()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView.onSaveInstanceState(outState)
    }

    // 数据模型类（添加氧气浓度字段）



}