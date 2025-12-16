package com.example.dgb.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * 事件日志数据访问对象
 */
@Dao
interface EventLogDao {
    /**
     * 插入单条事件日志
     */
    @Insert
    suspend fun insert(eventLog: EventLogEntity)
    
    /**
     * 批量插入事件日志
     */
    @Insert
    suspend fun insertAll(eventLogs: List<EventLogEntity>)
    
    /**
     * 获取所有事件日志
     */
    @Query("SELECT * FROM event_logs ORDER BY timestamp DESC")
    suspend fun getAllEventLogs(): List<EventLogEntity>
    
    /**
     * 获取最新的事件日志
     */
    @Query("SELECT * FROM event_logs ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getLatestEventLogs(limit: Int): List<EventLogEntity>
    
    /**
     * 根据级别获取事件日志
     */
    @Query("SELECT * FROM event_logs WHERE level = :level ORDER BY timestamp DESC")
    suspend fun getEventLogsByLevel(level: Int): List<EventLogEntity>
    
    /**
     * 根据类型获取事件日志
     */
    @Query("SELECT * FROM event_logs WHERE type = :type ORDER BY timestamp DESC")
    suspend fun getEventLogsByType(type: String): List<EventLogEntity>
    
    /**
     * 获取指定设备的事件日志
     */
    @Query("SELECT * FROM event_logs WHERE deviceId = :deviceId ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getDeviceEventLogs(deviceId: Int, limit: Int): List<EventLogEntity>
    
    /**
     * 清除指定时间戳之前的事件日志
     */
    @Query("DELETE FROM event_logs WHERE timestamp < :beforeTimestamp")
    suspend fun clearOldEventLogs(beforeTimestamp: Long)
    
    /**
     * 获取事件日志数量
     */
    @Query("SELECT COUNT(*) FROM event_logs")
    suspend fun getEventCount(): Int
    
    /**
     * 实时观察事件日志变化
     */
    @Query("SELECT * FROM event_logs ORDER BY timestamp DESC LIMIT 1")
    fun observeLatestEvent(): Flow<EventLogEntity?>
}