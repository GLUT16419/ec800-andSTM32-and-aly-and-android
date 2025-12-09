package com.example.dgb

import com.amap.api.maps.model.LatLng
import java.util.*

// 共享的数据模型
data class ColdChainDevice(
    val id: Int,
    val name: String,
    var status: DeviceStatus,
    var temperature: String,
    var humidity: String,
    var oxygenLevel: String,
    val location: String,
    var lastUpdate: Date,
    val latLng: LatLng?
)

// 设备状态枚举
enum class DeviceStatus(val displayName: String, val colorResId: Int) {
    NORMAL("正常", R.color.status_normal),
    WARNING("警告", R.color.status_warning),
    ERROR("异常", R.color.status_error)
}

// 原始数据点（每5秒一条）
data class DeviceDataPoint(
    val deviceId: Int,
    val timestamp: Long,
    val status: DeviceStatus,
    val temperature: Double,
    val humidity: Double,
    val oxygenLevel: Double,
    val location: LatLng?
)

// 状态时间段
data class StatusPeriod(
    val deviceId: Int,
    val status: DeviceStatus,
    val startTime: Long,
    val endTime: Long,
    val duration: String, // 格式化的持续时间
    val averageTemperature: Double,
    val averageHumidity: Double,
    val averageOxygen: Double,
    val locationChanges: List<LocationPoint> // 该时间段内的位置变化
)

// 位置点
data class LocationPoint(
    val latLng: LatLng,
    val timestamp: Long,
    val status: DeviceStatus
)

// 轨迹数据
data class DeviceTrack(
    val deviceId: Int,
    val deviceName: String,
    val trackPoints: List<TrackPoint>,
    val totalDistance: Double, // 总距离（米）
    val avgSpeed: Double,      // 平均速度（km/h）
    val startTime: Long,
    val endTime: Long
)

// 轨迹点
data class TrackPoint(
    val latLng: LatLng,
    val timestamp: Long,
    val speed: Double, // km/h
    val status: DeviceStatus
)