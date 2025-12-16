package com.example.dgb.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.dgb.MqttService

// 设备基本信息实体类，用于离线存储设备信息
@Entity(tableName = "device")
data class DeviceEntity(
    @PrimaryKey val deviceId: Int, // 设备ID作为主键
    val deviceName: String, // 设备名称
    val deviceType: String, // 设备类型
    val deviceStatus: Int, // 设备状态（使用DeviceStatus的ordinal值）
    val latitude: Double? = null, // 纬度
    val longitude: Double? = null, // 经度
    val speed: Double? = null, // 速度
    val lastUpdateTime: Long // 最后更新时间
) {
    // 辅助方法：将DeviceStatus的ordinal值转换为枚举类型
    fun getStatusEnum(): com.example.dgb.DeviceStatus {
        return com.example.dgb.DeviceStatus.values()[deviceStatus]
    }
    
    // 辅助方法：从ColdChainDevice转换为DeviceEntity
    companion object {
        fun fromColdChainDevice(device: MqttService.ColdChainDevice): DeviceEntity {
            // 从字符串中提取速度数值
            val speedValue = device.speed.replace("km/h", "").toDoubleOrNull()
            
            return DeviceEntity(
                deviceId = device.id,
                deviceName = device.name,
                deviceType = "冷链设备", // 默认类型
                deviceStatus = device.status.ordinal,
                latitude = device.latLng.latitude,
                longitude = device.latLng.longitude,
                speed = speedValue,
                lastUpdateTime = device.lastUpdate.time
            )
        }
    }
}