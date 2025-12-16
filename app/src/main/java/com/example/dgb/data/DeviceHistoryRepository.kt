package com.example.dgb.data

import com.example.dgb.DeviceDataPoint
import com.example.dgb.DeviceStatus
import com.amap.api.maps.model.LatLng
import kotlinx.coroutines.flow.Flow

// 历史数据仓库类，封装数据库操作，提供更高级别的数据访问接口
class DeviceHistoryRepository(private val historyDao: DeviceHistoryDao) {
    // 插入单条历史数据
    suspend fun insertHistoryData(deviceId: Int, status: DeviceStatus, temperature: Double, humidity: Double, oxygenLevel: Double, location: LatLng? = null) {
        val entity = DeviceHistoryEntity(
            deviceId = deviceId,
            timestamp = System.currentTimeMillis(),
            status = status.ordinal,
            temperature = temperature,
            humidity = humidity,
            oxygenLevel = oxygenLevel,
            latitude = location?.latitude,
            longitude = location?.longitude
        )
        historyDao.insertHistory(entity)
    }

    // 插入设备数据点
    suspend fun insertDataPoint(dataPoint: DeviceDataPoint) {
        val entity = DeviceHistoryEntity(
            deviceId = dataPoint.deviceId,
            timestamp = dataPoint.timestamp,
            status = dataPoint.status.ordinal,
            temperature = dataPoint.temperature,
            humidity = dataPoint.humidity,
            oxygenLevel = dataPoint.oxygenLevel,
            latitude = dataPoint.location?.latitude,
            longitude = dataPoint.location?.longitude
        )
        historyDao.insertHistory(entity)
    }

    // 批量插入设备数据点
    suspend fun insertDataPoints(dataPoints: List<DeviceDataPoint>) {
        val entities = dataPoints.map { dataPoint ->
            DeviceHistoryEntity(
                deviceId = dataPoint.deviceId,
                timestamp = dataPoint.timestamp,
                status = dataPoint.status.ordinal,
                temperature = dataPoint.temperature,
                humidity = dataPoint.humidity,
                oxygenLevel = dataPoint.oxygenLevel,
                latitude = dataPoint.location?.latitude,
                longitude = dataPoint.location?.longitude
            )
        }
        historyDao.insertHistories(entities)
    }

    // 获取指定设备的历史数据（Flow）
    fun getDeviceHistories(deviceId: Int): Flow<List<DeviceHistoryEntity>> {
        return historyDao.getHistoriesByDeviceId(deviceId)
    }

    // 获取指定设备在时间范围内的历史数据
    suspend fun getDeviceHistoriesInTimeRange(deviceId: Int, startTime: Long, endTime: Long): List<DeviceHistoryEntity> {
        return historyDao.getHistoriesByDeviceIdAndTimeRange(deviceId, startTime, endTime)
    }

    // 获取最新的N条历史数据
    suspend fun getLatestHistories(deviceId: Int, limit: Int = 50): List<DeviceHistoryEntity> {
        return historyDao.getLatestHistories(deviceId, limit)
    }

    // 删除指定设备的所有历史数据
    suspend fun clearDeviceHistories(deviceId: Int) {
        historyDao.deleteHistoriesByDeviceId(deviceId)
    }

    // 清空所有历史数据
    suspend fun clearAllHistories() {
        historyDao.clearAllHistories()
    }

    // 获取温度统计数据
    suspend fun getTemperatureStatistics(deviceId: Int, startTime: Long, endTime: Long): StatisticsResult {
        val histories = historyDao.getHistoriesByDeviceIdAndTimeRange(deviceId, startTime, endTime)
        return calculateStatistics(histories.map { it.temperature })
    }

    // 获取湿度统计数据
    suspend fun getHumidityStatistics(deviceId: Int, startTime: Long, endTime: Long): StatisticsResult {
        val histories = historyDao.getHistoriesByDeviceIdAndTimeRange(deviceId, startTime, endTime)
        return calculateStatistics(histories.map { it.humidity })
    }

    // 获取氧气浓度统计数据
    suspend fun getOxygenLevelStatistics(deviceId: Int, startTime: Long, endTime: Long): StatisticsResult {
        val histories = historyDao.getHistoriesByDeviceIdAndTimeRange(deviceId, startTime, endTime)
        return calculateStatistics(histories.map { it.oxygenLevel })
    }

    // 获取状态分布统计
    suspend fun getStatusDistribution(deviceId: Int, startTime: Long, endTime: Long): Map<DeviceStatus, Int> {
        val statusCounts = historyDao.getStatusDistribution(deviceId, startTime, endTime)
        return statusCounts.associate { 
            DeviceStatus.values()[it.status] to it.count
        }
    }

    // 计算统计结果
    private fun calculateStatistics(values: List<Double>): StatisticsResult {
        if (values.isEmpty()) {
            return StatisticsResult(0.0, 0.0, 0.0, 0.0, 0)
        }
        val count = values.size
        val sum = values.sum()
        val average = sum / count
        val max = values.maxOrNull() ?: 0.0
        val min = values.minOrNull() ?: 0.0
        return StatisticsResult(average, max, min, sum, count)
    }

    // 统计结果数据类
    data class StatisticsResult(
        val average: Double,
        val max: Double,
        val min: Double,
        val sum: Double,
        val count: Int
    )
}
