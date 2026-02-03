package com.example.dgb.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.dgb.DeviceStatus
import kotlinx.coroutines.flow.Flow

// 历史数据DAO接口，定义数据库操作方法
@Dao
interface DeviceHistoryDao {
    // 插入单条历史数据
    @Insert
    suspend fun insertHistory(history: DeviceHistoryEntity): Long

    // 批量插入历史数据
    @Insert
    suspend fun insertHistories(histories: List<DeviceHistoryEntity>)

    // 更新历史数据
    @Update
    suspend fun updateHistory(history: DeviceHistoryEntity)

    // 删除指定ID的历史数据
    @Query("DELETE FROM device_history WHERE id = :id")
    suspend fun deleteHistoryById(id: Long)

    // 删除指定设备的所有历史数据
    @Query("DELETE FROM device_history WHERE deviceId = :deviceId")
    suspend fun deleteHistoriesByDeviceId(deviceId: Int)

    // 删除指定时间范围内的历史数据
    @Query("DELETE FROM device_history WHERE timestamp BETWEEN :startTime AND :endTime")
    suspend fun deleteHistoriesByTimeRange(startTime: Long, endTime: Long)

    // 清空所有历史数据
    @Query("DELETE FROM device_history")
    suspend fun clearAllHistories()

    // 查询指定设备的所有历史数据，按时间降序排列
    @Query("SELECT * FROM device_history WHERE deviceId = :deviceId ORDER BY timestamp DESC")
    fun getHistoriesByDeviceId(deviceId: Int): Flow<List<DeviceHistoryEntity>>

    // 查询指定设备在时间范围内的历史数据，按时间升序排列
    @Query("SELECT * FROM device_history WHERE deviceId = :deviceId AND timestamp BETWEEN :startTime AND :endTime ORDER BY timestamp ASC")
    suspend fun getHistoriesByDeviceIdAndTimeRange(
        deviceId: Int,
        startTime: Long,
        endTime: Long
    ): List<DeviceHistoryEntity>

    // 查询最新的N条历史数据
    @Query("SELECT * FROM device_history WHERE deviceId = :deviceId ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getLatestHistories(deviceId: Int, limit: Int): List<DeviceHistoryEntity>

    // 统计指定设备在时间范围内的平均温度
    @Query("SELECT AVG(temperature) FROM device_history WHERE deviceId = :deviceId AND timestamp BETWEEN :startTime AND :endTime")
    suspend fun getAverageTemperature(deviceId: Int, startTime: Long, endTime: Long): Double?

    // 统计指定设备在时间范围内的平均湿度
    @Query("SELECT AVG(humidity) FROM device_history WHERE deviceId = :deviceId AND timestamp BETWEEN :startTime AND :endTime")
    suspend fun getAverageHumidity(deviceId: Int, startTime: Long, endTime: Long): Double?

    // 统计指定设备在时间范围内的平均氧气浓度
    @Query("SELECT AVG(oxygenLevel) FROM device_history WHERE deviceId = :deviceId AND timestamp BETWEEN :startTime AND :endTime")
    suspend fun getAverageOxygenLevel(deviceId: Int, startTime: Long, endTime: Long): Double?

    // 统计指定设备在时间范围内的状态分布
    @Query("SELECT status, COUNT(*) as count FROM device_history WHERE deviceId = :deviceId AND timestamp BETWEEN :startTime AND :endTime GROUP BY status")
    suspend fun getStatusDistribution(deviceId: Int, startTime: Long, endTime: Long): List<StatusCount>

    // 统计指定设备在时间范围内的最大温度
    @Query("SELECT MAX(temperature) FROM device_history WHERE deviceId = :deviceId AND timestamp BETWEEN :startTime AND :endTime")
    suspend fun getMaxTemperature(deviceId: Int, startTime: Long, endTime: Long): Double?

    // 统计指定设备在时间范围内的最小温度
    @Query("SELECT MIN(temperature) FROM device_history WHERE deviceId = :deviceId AND timestamp BETWEEN :startTime AND :endTime")
    suspend fun getMinTemperature(deviceId: Int, startTime: Long, endTime: Long): Double?

    // 分页查询指定设备的历史数据，按时间降序排列
    @Query("SELECT * FROM device_history WHERE deviceId = :deviceId ORDER BY timestamp DESC LIMIT :limit OFFSET :offset")
    suspend fun getHistoriesByDeviceIdPaged(deviceId: Int, limit: Int, offset: Int): List<DeviceHistoryEntity>

    // 分页查询指定设备在时间范围内的历史数据，按时间升序排列
    @Query("SELECT * FROM device_history WHERE deviceId = :deviceId AND timestamp BETWEEN :startTime AND :endTime ORDER BY timestamp ASC LIMIT :limit OFFSET :offset")
    suspend fun getHistoriesByDeviceIdAndTimeRangePaged(
        deviceId: Int,
        startTime: Long,
        endTime: Long,
        limit: Int,
        offset: Int
    ): List<DeviceHistoryEntity>

    // 获取指定设备的历史数据总数
    @Query("SELECT COUNT(*) FROM device_history WHERE deviceId = :deviceId")
    suspend fun getHistoriesCountByDeviceId(deviceId: Int): Int

    // 获取指定设备在时间范围内的历史数据总数
    @Query("SELECT COUNT(*) FROM device_history WHERE deviceId = :deviceId AND timestamp BETWEEN :startTime AND :endTime")
    suspend fun getHistoriesCountByDeviceIdAndTimeRange(deviceId: Int, startTime: Long, endTime: Long): Int

    // 内部数据类：状态统计结果
    data class StatusCount(
        val status: Int,
        val count: Int
    )
}