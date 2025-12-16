package com.example.dgb

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import com.amap.api.maps.AMap
import com.amap.api.maps.CameraUpdateFactory
import com.amap.api.maps.MapView
import com.amap.api.maps.model.LatLng
import com.amap.api.maps.model.MarkerOptions
import com.example.dgb.MqttService.Companion.deviceMap
import com.example.dgb.DeviceStatus
import com.example.dgb.TrackPoint
import kotlinx.coroutines.*
import java.text.SimpleDateFormat
import java.util.*

class HomeFragment : Fragment() {

    private lateinit var mapView: MapView
    private var aMap: AMap? = null
    private lateinit var titleText: TextView
    private lateinit var cardsContainer: ViewGroup
    private var updateJob: Job? = null
    private lateinit var mapUtils: MapUtils
    // 示例轨迹数据
    private val exampleTracks = mutableMapOf<Int, List<TrackPoint>>()
    // 设备ID到卡片视图的映射，用于视图复用
    private val deviceCardMap = mutableMapOf<String, CardView>()

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
            
            // 初始化MapUtils
            mapUtils = MapUtils(it)
            
            // 添加地图缩放级别监听，实现多设备聚合显示
            it.setOnCameraChangeListener(object : AMap.OnCameraChangeListener {
                override fun onCameraChange(p0: com.amap.api.maps.model.CameraPosition?) {
                    // 相机位置变化时触发
                }
                
                override fun onCameraChangeFinish(cameraPosition: com.amap.api.maps.model.CameraPosition?) {
                    cameraPosition?.let {
                        // 相机位置变化结束后，显示聚合标记
                        mapUtils.showDeviceClusters(deviceMap.values.toList())
                    }
                }
            })
        }

        // 初始化设备列表容器
        cardsContainer = view.findViewById(R.id.cards_linear_layout)

        // 初始化设备数据
        initializeDeviceData()
        MqttService.Companion.ServiceDataRepository.updateEvent.observe(viewLifecycleOwner) {
            // 执行 UI 更新
            refreshDeviceCards()
        }
        // 添加设备卡片到界面
        addDeviceCards()
        return view
    }

    private fun initializeDeviceData() {
        exampleTracks.clear()

        // 移除模拟数据，仅使用真实的MQTT设备数据
        
        // 生成示例轨迹数据（如果需要）
        generateExampleTracks()

        // 在地图上添加标记点（会自动使用真实设备数据）
        addMarkersToMap()
    }
    
    /**
     * 生成示例轨迹数据
     */
    private fun generateExampleTracks() {
        // 为冷藏车1生成示例轨迹
        val truck1Track = listOf(
            TrackPoint(LatLng(31.230416, 121.473701), (System.currentTimeMillis() - 3600000), 30.0, DeviceStatus.NORMAL),
            TrackPoint(LatLng(31.235416, 121.478701), (System.currentTimeMillis() - 3500000), 35.0, DeviceStatus.NORMAL),
            TrackPoint(LatLng(31.240416, 121.483701), (System.currentTimeMillis() - 3400000), 40.0, DeviceStatus.NORMAL),
            TrackPoint(LatLng(31.245416, 121.488701), (System.currentTimeMillis() - 3300000), 45.0, DeviceStatus.NORMAL),
            TrackPoint(LatLng(31.250416, 121.493701), (System.currentTimeMillis() - 3200000), 50.0, DeviceStatus.NORMAL)
        )
        exampleTracks[1] = truck1Track
        
        // 为疫苗运输车6生成示例轨迹
        val vaccineTruckTrack = listOf(
            TrackPoint(LatLng(31.223850, 121.362350), (System.currentTimeMillis() - 3600000), 25.0, DeviceStatus.WARNING),
            TrackPoint(LatLng(31.218850, 121.357350), (System.currentTimeMillis() - 3500000), 30.0, DeviceStatus.WARNING),
            TrackPoint(LatLng(31.213850, 121.352350), (System.currentTimeMillis() - 3400000), 35.0, DeviceStatus.WARNING),
            TrackPoint(LatLng(31.208850, 121.347350), (System.currentTimeMillis() - 3300000), 40.0, DeviceStatus.WARNING),
            TrackPoint(LatLng(31.203850, 121.342350), (System.currentTimeMillis() - 3200000), 45.0, DeviceStatus.WARNING)
        )
        exampleTracks[6] = vaccineTruckTrack
    }

    private fun addMarkersToMap() {
        // 使用MapUtils更新所有设备标记点
        aMap?.let { 
            val devices = deviceMap.values.toList()
            mapUtils.updateAllDeviceMarkers(devices)
            
            // 如果设备列表不为空，将地图镜头移动到第一个设备位置
            if (devices.isNotEmpty()) {
                it.moveCamera(
                    CameraUpdateFactory.newLatLngZoom(
                        devices[0].latLng,
                        10f
                    )
                )
            }
        }
    }

    private fun addDeviceCards() {
        // 设备名称集合，用于确定需要保留哪些卡片
        val currentDeviceNames = deviceMap.keys.toSet()
        val existingDeviceNames = deviceCardMap.keys.toSet()
        
        // 移除不再存在的设备卡片
        val devicesToRemove = existingDeviceNames - currentDeviceNames
        devicesToRemove.forEach {
            val cardView = deviceCardMap.remove(it)
            cardView?.let { view ->
                cardsContainer.removeView(view)
            }
        }
        
        // 为每个设备创建或更新卡片
        deviceMap.values.toList().forEach { device ->
            val cardView = deviceCardMap.getOrPut(device.name) {
                // 新建卡片
                val newCard = LayoutInflater.from(requireContext())
                    .inflate(R.layout.item_device_card, cardsContainer, false) as CardView
                    
                // 设置点击事件
                newCard.setOnClickListener {
                    // 点击卡片时，将地图镜头移动到该设备位置
                    aMap?.animateCamera(
                        CameraUpdateFactory.newLatLngZoom(
                            device.latLng,
                            14f
                        )
                    )

                    // 显示设备详细信息提示
                    showDeviceDetails(device)
                    
                    // 显示设备行驶轨迹
                    showDeviceTrack(device.id, device.name)
                }
                
                cardsContainer.addView(newCard)
                newCard
            }
            
            // 更新卡片内容
            updateDeviceCard(cardView, device)
        }
    }
    
    /**
     * 更新单个设备卡片的内容
     */
    private fun updateDeviceCard(cardView: CardView, device: MqttService.ColdChainDevice) {
        // 设置设备名称
        cardView.findViewById<TextView>(R.id.device_name).text = device.name

        // 设置设备状态
        val statusText = cardView.findViewById<TextView>(R.id.device_status)
        statusText.text = device.status.displayName

        // 根据状态设置背景颜色
        when (device.status) {
            com.example.dgb.DeviceStatus.NORMAL ->
                statusText.setBackgroundResource(R.drawable.status_background_normal)
            com.example.dgb.DeviceStatus.WARNING ->
                statusText.setBackgroundResource(R.drawable.status_background_warning)
            com.example.dgb.DeviceStatus.ERROR ->
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
    }

    private fun showDeviceDetails(device: MqttService.ColdChainDevice) {
        // 显示设备详细信息
        val message = "${device.name}\n温度: ${device.temperature}\n湿度: ${device.humidity}\n氧气: ${device.oxygenLevel}"
        println("设备详情: $message")
    }
    
    /**
     * 显示设备行驶轨迹
     */
    private fun showDeviceTrack(deviceId: Int, deviceName: String) {
        // 直接使用设备名称从MqttService获取轨迹数据
        Log.d("HomeFragment", "尝试显示设备 $deviceName 的轨迹，deviceId: $deviceId")
        
        // 检查deviceTrackMap的整体状态
        Log.d("HomeFragment", "deviceTrackMap 包含 ${MqttService.deviceTrackMap.size} 个设备的轨迹数据")
        
        // 查看所有有轨迹数据的设备名称
        MqttService.deviceTrackMap.forEach { (name, points) ->
            Log.d("HomeFragment", "  - 设备 $name 有 ${points.size} 个轨迹点")
        }
        
        MqttService.deviceTrackMap[deviceName]?.let { 
            if (it.isNotEmpty()) {
                Log.d("HomeFragment", "显示设备 $deviceName 的轨迹，包含 ${it.size} 个点")
                Log.d("HomeFragment", "  - 第一个点: ${it.first().latLng}, 时间: ${Date(it.first().timestamp)}")
                Log.d("HomeFragment", "  - 最后一个点: ${it.last().latLng}, 时间: ${Date(it.last().timestamp)}")
                mapUtils.drawVehicleTrack(deviceId, it)
            } else {
                Log.d("HomeFragment", "设备 $deviceName 有轨迹列表但为空")
                println("设备 $deviceName 没有轨迹数据")
            }
        } ?: run {
            Log.d("HomeFragment", "设备 $deviceName 在轨迹映射中不存在")
            println("设备 $deviceName 没有轨迹数据")
        }
    }



    private fun refreshDeviceCards() {
        // 刷新卡片显示
        addDeviceCards()
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
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