package com.example.dgb.data

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dgb.DeviceDataPoint
import com.example.dgb.DeviceStatus
import com.amap.api.maps.model.LatLng
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

// 历史数据ViewModel，作为UI层和数据层之间的桥梁
class DeviceHistoryViewModel(private val repository: DeviceHistoryRepository) : ViewModel() {
    // 保存设备实时数据到数据库
    fun saveDeviceData(deviceId: Int, status: DeviceStatus, temperature: Double, humidity: Double, oxygenLevel: Double, location: LatLng? = null) {
        viewModelScope.launch {
            repository.insertHistoryData(deviceId, status, temperature, humidity, oxygenLevel, location)
        }
    }

    // 保存设备数据点到数据库
    fun saveDeviceDataPoint(dataPoint: DeviceDataPoint) {
        viewModelScope.launch {
            repository.insertDataPoint(dataPoint)
        }
    }

    // 批量保存设备数据点到数据库
    fun saveDeviceDataPoints(dataPoints: List<DeviceDataPoint>) {
        viewModelScope.launch {
            repository.insertDataPoints(dataPoints)
        }
    }

    // 获取指定设备的历史数据
    fun getDeviceHistories(deviceId: Int): Flow<List<DeviceHistoryEntity>> {
        return repository.getDeviceHistories(deviceId)
    }

    // 获取指定设备在时间范围内的历史数据
    fun getDeviceHistoriesInTimeRange(deviceId: Int, startTime: Long, endTime: Long, callback: (List<DeviceHistoryEntity>) -> Unit) {
        viewModelScope.launch {
            val histories = repository.getDeviceHistoriesInTimeRange(deviceId, startTime, endTime)
            callback(histories)
        }
    }

    // 获取最新的N条历史数据
    fun getLatestHistories(deviceId: Int, limit: Int = 50, callback: (List<DeviceHistoryEntity>) -> Unit) {
        viewModelScope.launch {
            val histories = repository.getLatestHistories(deviceId, limit)
            callback(histories)
        }
    }

    // 清除指定设备的所有历史数据
    fun clearDeviceHistories(deviceId: Int, callback: () -> Unit = {}) {
        viewModelScope.launch {
            repository.clearDeviceHistories(deviceId)
            callback()
        }
    }

    // 清空所有历史数据
    fun clearAllHistories(callback: () -> Unit = {}) {
        viewModelScope.launch {
            repository.clearAllHistories()
            callback()
        }
    }

    // 获取温度统计数据
    fun getTemperatureStatistics(deviceId: Int, startTime: Long, endTime: Long, callback: (DeviceHistoryRepository.StatisticsResult) -> Unit) {
        viewModelScope.launch {
            val statistics = repository.getTemperatureStatistics(deviceId, startTime, endTime)
            callback(statistics)
        }
    }

    // 获取湿度统计数据
    fun getHumidityStatistics(deviceId: Int, startTime: Long, endTime: Long, callback: (DeviceHistoryRepository.StatisticsResult) -> Unit) {
        viewModelScope.launch {
            val statistics = repository.getHumidityStatistics(deviceId, startTime, endTime)
            callback(statistics)
        }
    }

    // 获取氧气浓度统计数据
    fun getOxygenLevelStatistics(deviceId: Int, startTime: Long, endTime: Long, callback: (DeviceHistoryRepository.StatisticsResult) -> Unit) {
        viewModelScope.launch {
            val statistics = repository.getOxygenLevelStatistics(deviceId, startTime, endTime)
            callback(statistics)
        }
    }

    // 获取状态分布统计
    fun getStatusDistribution(deviceId: Int, startTime: Long, endTime: Long, callback: (Map<DeviceStatus, Int>) -> Unit) {
        viewModelScope.launch {
            val distribution = repository.getStatusDistribution(deviceId, startTime, endTime)
            callback(distribution)
        }
    }
}
