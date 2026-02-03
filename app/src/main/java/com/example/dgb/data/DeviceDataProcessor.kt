package com.example.dgb.data

import android.content.Context
import com.example.dgb.MqttService
import com.example.dgb.MqttService.ColdChainDevice
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * 设备数据处理器，负责设备数据的转换、存储和查询
 */
class DeviceDataProcessor(private val context: Context) {
    private val deviceDatabase = DeviceDatabase.getDatabase(context)
    private val deviceDao = deviceDatabase.deviceDao()
    private val deviceHistoryDao = deviceDatabase.deviceHistoryDao()

    /**
     * 将ColdChainDevice转换为DeviceEntity
     */
    fun convertToDeviceEntity(device: ColdChainDevice): DeviceEntity {
        return DeviceEntity.fromColdChainDevice(device)
    }

    /**
     * 将ColdChainDevice列表转换为DeviceEntity列表
     */
    fun convertToDeviceEntityList(devices: List<ColdChainDevice>): List<DeviceEntity> {
        return devices.map { convertToDeviceEntity(it) }
    }

    /**
     * 将ColdChainDevice转换为DeviceHistoryEntity
     */
    fun convertToDeviceHistoryEntity(device: ColdChainDevice): DeviceHistoryEntity {
        // 从字符串中提取数值（移除单位）
        val tempValue = device.temperature.replace("°C", "").toDoubleOrNull() ?: 0.0
        val humidityValue = device.humidity.replace("%", "").toDoubleOrNull() ?: 0.0
        val oxygenValue = device.oxygenLevel.replace("%", "").toDoubleOrNull() ?: 0.0

        return DeviceHistoryEntity(
            deviceId = device.id,
            timestamp = device.lastUpdate.time,
            status = device.status.ordinal,
            temperature = tempValue,
            humidity = humidityValue,
            oxygenLevel = oxygenValue,
            latitude = device.latLng.latitude,
            longitude = device.latLng.longitude
        )
    }

    /**
     * 将ColdChainDevice列表转换为DeviceHistoryEntity列表
     */
    fun convertToDeviceHistoryEntityList(devices: List<ColdChainDevice>): List<DeviceHistoryEntity> {
        return devices.map { convertToDeviceHistoryEntity(it) }
    }

    /**
     * 将设备信息存储到本地数据库
     */
    suspend fun storeDevicesToLocal(devices: List<ColdChainDevice>) {
        withContext(Dispatchers.IO) {
            try {
                // 转换为DeviceEntity列表
                val deviceEntities = convertToDeviceEntityList(devices)
                
                // 批量插入或更新设备信息
                deviceDao.insertOrUpdateDevices(deviceEntities)
                
                // 同时将设备的最新状态保存到历史记录
                val historyEntities = convertToDeviceHistoryEntityList(devices)
                deviceHistoryDao.insertHistories(historyEntities)
            } catch (e: Exception) {
                // 记录数据库操作异常
                android.util.Log.e("DeviceDataProcessor", "存储设备信息失败: ${e.message}", e)
                throw e // 重新抛出异常，让调用者处理
            }
        }
    }

    /**
     * 从本地数据库获取所有设备
     */
    fun getAllDevices() = deviceDao.getAllDevices()

    /**
     * 从本地数据库获取指定设备
     */
    suspend fun getDeviceById(deviceId: Int): DeviceEntity? {
        return withContext(Dispatchers.IO) {
            deviceDao.getDeviceById(deviceId)
        }
    }

    /**
     * 根据设备状态获取设备列表
     */
    fun getDevicesByStatus(status: com.example.dgb.DeviceStatus) = 
        deviceDao.getDevicesByStatus(status.ordinal)

    /**
     * 根据设备类型获取设备列表
     */
    fun getDevicesByType(deviceType: String) = 
        deviceDao.getDevicesByType(deviceType)

    /**
     * 从本地数据库获取设备历史记录
     */
    suspend fun getDeviceHistory(deviceId: Int, startTime: Long, endTime: Long): List<DeviceHistoryEntity> {
        return withContext(Dispatchers.IO) {
            deviceHistoryDao.getHistoriesByDeviceIdAndTimeRange(deviceId, startTime, endTime)
        }
    }

    /**
     * 插入设备历史记录
     */
    suspend fun insertDeviceHistory(history: DeviceHistoryEntity) {
        withContext(Dispatchers.IO) {
            deviceHistoryDao.insertHistory(history)
        }
    }

    /**
     * 批量插入设备历史记录
     */
    suspend fun insertDeviceHistories(histories: List<DeviceHistoryEntity>) {
        withContext(Dispatchers.IO) {
            deviceHistoryDao.insertHistories(histories)
        }
    }

    /**
     * 删除指定设备的历史记录
     */
    suspend fun deleteDeviceHistory(deviceId: Int) {
        withContext(Dispatchers.IO) {
            deviceHistoryDao.deleteHistoriesByDeviceId(deviceId)
        }
    }

    /**
     * 删除指定时间范围内的历史记录
     */
    suspend fun deleteHistoryByTimeRange(startTime: Long, endTime: Long) {
        withContext(Dispatchers.IO) {
            deviceHistoryDao.deleteHistoriesByTimeRange(startTime, endTime)
        }
    }

    /**
     * 获取设备最新的N条历史记录
     */
    suspend fun getLatestDeviceHistory(deviceId: Int, limit: Int): List<DeviceHistoryEntity> {
        return withContext(Dispatchers.IO) {
            deviceHistoryDao.getLatestHistories(deviceId, limit)
        }
    }
}