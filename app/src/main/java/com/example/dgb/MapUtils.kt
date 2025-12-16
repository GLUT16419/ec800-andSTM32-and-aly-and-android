package com.example.dgb

import com.amap.api.maps.AMap
import com.amap.api.maps.AMapUtils
import com.amap.api.maps.model.*
import com.example.dgb.DeviceStatus
import java.util.*
import android.graphics.Color

/**
 * 地图工具类，用于处理地图轨迹绘制和设备聚合显示
 */
class MapUtils(private val aMap: AMap) {

    // 轨迹线条集合
    private val trackPolylines = mutableMapOf<Int, Polyline>()
    // 设备标记点集合
    private val deviceMarkers = mutableMapOf<Int, Marker>()
    // 聚合标记点集合
    private val clusterMarkers = mutableListOf<Marker>()

    /**
     * 绘制车辆行驶轨迹
     * @param deviceId 设备ID
     * @param trackPoints 轨迹点列表
     */
    fun drawVehicleTrack(deviceId: Int, trackPoints: List<com.example.dgb.TrackPoint>) {
        if (trackPoints.isEmpty()) return

        // 移除旧轨迹
        trackPolylines[deviceId]?.remove()

        // 转换为LatLng列表
        val latLngs = trackPoints.map { it.latLng }

        // 根据设备状态设置轨迹颜色
        val color = when (trackPoints.lastOrNull()?.status?.displayName) {
            "正常" -> Color.GREEN
            "警告" -> Color.YELLOW
            "异常" -> Color.RED
            else -> Color.BLUE
        }

        // 创建轨迹线条
        val polyline = aMap.addPolyline(
            PolylineOptions()
                .addAll(latLngs)
                .color(color)
                .width(5f)
                .geodesic(true)
        )

        // 保存轨迹线条引用
        trackPolylines[deviceId] = polyline
    }

    /**
     * 更新设备标记点
     * @param device 设备信息
     */
    fun updateDeviceMarker(device: MqttService.ColdChainDevice) {
        device.latLng?.let { latLng ->
            // 复用或创建标记点
            val marker = deviceMarkers.getOrElse(device.id) { 
                // 创建新标记点
                val newMarker = aMap.addMarker(
                    MarkerOptions()
                        .position(latLng)
                        .title(device.name)
                        .snippet("温度: ${device.temperature} | 氧气: ${device.oxygenLevel}")
                )
                deviceMarkers[device.id] = newMarker
                newMarker
            }
            
            // 更新标记点属性
            marker.position = latLng
            marker.title = device.name
            marker.snippet = "温度: ${device.temperature} | 氧气: ${device.oxygenLevel}"
        }
    }

    /**
     * 更新所有设备标记点
     * @param devices 设备列表
     */
    fun updateAllDeviceMarkers(devices: List<MqttService.ColdChainDevice>) {
        // 获取当前设备ID集合
        val currentDeviceIds = devices.map { it.id }.toSet()
        val existingDeviceIds = deviceMarkers.keys.toSet()
        
        // 移除不再存在的设备标记点
        val deviceIdsToRemove = existingDeviceIds - currentDeviceIds
        deviceIdsToRemove.forEach {
            deviceMarkers[it]?.remove()
            deviceMarkers.remove(it)
        }
        
        // 更新或创建设备标记点
        devices.forEach { device ->
            updateDeviceMarker(device)
        }
    }

    /**
     * 显示多设备聚合标记
     * @param devices 设备列表
     */
    fun showDeviceClusters(devices: List<MqttService.ColdChainDevice>) {
        // 清除现有聚合标记
        clearClusterMarkers()
        // 清除轨迹
        clearAllTracks()

        // 如果设备数量为0，直接返回
        if (devices.isEmpty()) return

        // 根据地图缩放级别决定是否显示聚合
        val currentZoom = aMap.cameraPosition.zoom
        
        if (currentZoom < 12.0) {
            // 低缩放级别，显示聚合标记
            val clusters = clusterDevices(devices)
            drawClusterMarkers(clusters)
            
            // 隐藏单个设备标记点
            deviceMarkers.values.forEach { it.isVisible = false }
        } else {
            // 高缩放级别，显示单个设备标记点
            updateAllDeviceMarkers(devices)
            deviceMarkers.values.forEach { it.isVisible = true }
        }
    }
    
    /**
     * 绘制聚合标记点
     */
    private fun drawClusterMarkers(clusters: List<DeviceCluster>) {
        clusters.forEach { cluster ->
            // 创建聚合标记点
            val marker = aMap.addMarker(
                MarkerOptions()
                    .position(cluster.centerLatLng)
                    .title("设备集群")
                    .snippet("设备数量: ${cluster.deviceCount} | 正常: ${cluster.normalCount} | 警告: ${cluster.warningCount} | 异常: ${cluster.errorCount}")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE))
            )
            
            // 保存聚合标记点引用
            clusterMarkers.add(marker)
        }
    }
    
    /**
     * 清除聚合标记点
     */
    private fun clearClusterMarkers() {
        clusterMarkers.forEach { it.remove() }
        clusterMarkers.clear()
    }

    /**
     * 清除指定设备的轨迹
     * @param deviceId 设备ID
     */
    fun clearTrack(deviceId: Int) {
        trackPolylines[deviceId]?.remove()
        trackPolylines.remove(deviceId)
    }

    /**
     * 清除所有轨迹
     */
    fun clearAllTracks() {
        trackPolylines.values.forEach { it.remove() }
        trackPolylines.clear()
    }

    /**
     * 清除所有标记
     */
    fun clearAllMarkers() {
        deviceMarkers.values.forEach { it.remove() }
        deviceMarkers.clear()
        clusterMarkers.forEach { it.remove() }
        clusterMarkers.clear()
    }

    /**
     * 根据设备状态获取标记点图标
     */
    private fun getMarkerIconByStatus(status: com.example.dgb.DeviceStatus): BitmapDescriptor {
        // 使用默认标记点，后续可以添加自定义图标
        return when (status.displayName) {
            "正常" -> BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)
            "警告" -> BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW)
            "异常" -> BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)
            else -> BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)
        }
    }

    /**
     * 设备聚合逻辑
     * 将设备按照地理位置进行聚合
     */
    private fun clusterDevices(devices: List<MqttService.ColdChainDevice>): List<DeviceCluster> {
        val clusters = mutableListOf<DeviceCluster>()
        val clusteredDevices = mutableSetOf<MqttService.ColdChainDevice>()

        // 过滤掉没有位置信息的设备
        val devicesWithLocation = devices.filter { it.latLng != null }

        // 聚合半径（米）
        val clusterRadius = 500.0

        for (device in devicesWithLocation) {
            if (device in clusteredDevices || device.latLng == null) continue

            // 创建新聚合
            val cluster = DeviceCluster(device.latLng!!)
            cluster.addDevice(device)
            clusteredDevices.add(device)

            // 查找附近的设备
            for (otherDevice in devicesWithLocation) {
                if (otherDevice in clusteredDevices || otherDevice.latLng == null) continue

                // 计算距离
                val distance = calculateDistance(device.latLng!!, otherDevice.latLng!!)
                if (distance <= clusterRadius) {
                    cluster.addDevice(otherDevice)
                    clusteredDevices.add(otherDevice)
                }
            }

            clusters.add(cluster)
        }

        return clusters
    }

    /**
     * 计算两个经纬度点之间的距离（米）
     */
    private fun calculateDistance(latLng1: LatLng, latLng2: LatLng): Double {
        return AMapUtils.calculateLineDistance(latLng1, latLng2).toDouble()
    }

    /**
     * 设备聚合类
     */
    private data class DeviceCluster(var centerLatLng: LatLng) {
        var deviceCount = 0
        var normalCount = 0
        var warningCount = 0
        var errorCount = 0
        private val devices = mutableListOf<MqttService.ColdChainDevice>()

        fun addDevice(device: MqttService.ColdChainDevice) {
            devices.add(device)
            deviceCount++

            // 更新计数
            when (device.status.displayName) {
                "正常" -> normalCount++
                "警告" -> warningCount++
                "异常" -> errorCount++
            }

            // 更新中心点
            updateCenterLatLng()
        }

        private fun updateCenterLatLng() {
            if (devices.isEmpty()) return

            var totalLat = 0.0
            var totalLng = 0.0

            for (device in devices) {
                device.latLng?.let {
                    totalLat += it.latitude
                    totalLng += it.longitude
                }
            }

            centerLatLng = LatLng(totalLat / devices.size, totalLng / devices.size)
        }
    }
}

// TrackPoint类已在DataModels.kt中定义
