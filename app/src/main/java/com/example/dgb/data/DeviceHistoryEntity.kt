package com.example.dgb.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.dgb.DeviceStatus

// 设备历史数据实体类，对应数据库表
@Entity(tableName = "device_history")
data class DeviceHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val deviceId: Int,
    val timestamp: Long,
    val status: Int, // DeviceStatus的ordinal值
    val temperature: Double,
    val humidity: Double,
    val oxygenLevel: Double,
    val latitude: Double? = null,
    val longitude: Double? = null
) {
    // 辅助方法：将status转换为DeviceStatus枚举
    fun getStatusEnum(): DeviceStatus {
        return DeviceStatus.values()[status]
    }
}