package com.example.dgb.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

// 设备基本信息DAO接口，定义数据库操作方法
@Dao
interface DeviceDao {
    // 插入或更新设备信息（主键冲突时替换）
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateDevice(device: DeviceEntity): Long
    
    // 批量插入或更新设备信息（主键冲突时替换）
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateDevices(devices: List<DeviceEntity>)
    
    // 更新设备信息
    @Update
    suspend fun updateDevice(device: DeviceEntity)
    
    // 根据设备ID删除设备
    @Query("DELETE FROM device WHERE deviceId = :deviceId")
    suspend fun deleteDeviceById(deviceId: Int)
    
    // 批量删除设备
    @Query("DELETE FROM device WHERE deviceId IN (:deviceIds)")
    suspend fun deleteDevicesByIds(deviceIds: List<Int>)
    
    // 清空所有设备信息
    @Query("DELETE FROM device")
    suspend fun clearAllDevices()
    
    // 查询所有设备信息
    @Query("SELECT * FROM device ORDER BY deviceName")
    fun getAllDevices(): Flow<List<DeviceEntity>>
    
    // 根据设备ID查询设备信息
    @Query("SELECT * FROM device WHERE deviceId = :deviceId")
    suspend fun getDeviceById(deviceId: Int): DeviceEntity?
    
    // 根据设备状态查询设备信息
    @Query("SELECT * FROM device WHERE deviceStatus = :status ORDER BY deviceName")
    fun getDevicesByStatus(status: Int): Flow<List<DeviceEntity>>
    
    // 根据设备类型查询设备信息
    @Query("SELECT * FROM device WHERE deviceType = :deviceType ORDER BY deviceName")
    fun getDevicesByType(deviceType: String): Flow<List<DeviceEntity>>
    
    // 查询设备总数
    @Query("SELECT COUNT(*) FROM device")
    suspend fun getDeviceCount(): Int
}
