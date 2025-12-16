package com.example.dgb.data

import com.example.dgb.DeviceStatus
import java.text.SimpleDateFormat
import java.util.*

// 统计分析工具类，提供多维度的数据统计计算功能
class StatisticsAnalyzer {
    // 时间格式化器
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    
    // 计算基础统计信息（平均值、最大值、最小值、总和、数据点数量）
    fun calculateBasicStatistics(values: List<Double>): BasicStatistics {
        if (values.isEmpty()) {
            return BasicStatistics(0.0, 0.0, 0.0, 0.0, 0)
        }
        
        val count = values.size
        val sum = values.sum()
        val average = sum / count
        val max = values.maxOrNull() ?: 0.0
        val min = values.minOrNull() ?: 0.0
        
        return BasicStatistics(average, max, min, sum, count)
    }
    
    // 按时间段聚合数据
    fun aggregateByTime(histories: List<DeviceHistoryEntity>, intervalMillis: Long): Map<Long, List<DeviceHistoryEntity>> {
        val aggregatedMap = mutableMapOf<Long, MutableList<DeviceHistoryEntity>>()
        
        for (history in histories) {
            // 计算所属的时间间隔键
            val intervalKey = history.timestamp - (history.timestamp % intervalMillis)
            // 将数据添加到对应时间间隔的列表中
            aggregatedMap.computeIfAbsent(intervalKey) { mutableListOf() }.add(history)
        }
        
        return aggregatedMap
    }
    
    // 计算每个时间间隔的平均温度
    fun calculateAverageTemperatureByTime(aggregatedData: Map<Long, List<DeviceHistoryEntity>>): Map<String, Double> {
        val result = mutableMapOf<String, Double>()
        
        for ((timestamp, histories) in aggregatedData) {
            val temperatures = histories.map { it.temperature }
            val statistics = calculateBasicStatistics(temperatures)
            val timeKey = dateFormat.format(Date(timestamp))
            result[timeKey] = statistics.average
        }
        
        return result
    }
    
    // 计算每个时间间隔的平均湿度
    fun calculateAverageHumidityByTime(aggregatedData: Map<Long, List<DeviceHistoryEntity>>): Map<String, Double> {
        val result = mutableMapOf<String, Double>()
        
        for ((timestamp, histories) in aggregatedData) {
            val humidities = histories.map { it.humidity }
            val statistics = calculateBasicStatistics(humidities)
            val timeKey = dateFormat.format(Date(timestamp))
            result[timeKey] = statistics.average
        }
        
        return result
    }
    
    // 计算每个时间间隔的平均氧气浓度
    fun calculateAverageOxygenLevelByTime(aggregatedData: Map<Long, List<DeviceHistoryEntity>>): Map<String, Double> {
        val result = mutableMapOf<String, Double>()
        
        for ((timestamp, histories) in aggregatedData) {
            val oxygenLevels = histories.map { it.oxygenLevel }
            val statistics = calculateBasicStatistics(oxygenLevels)
            val timeKey = dateFormat.format(Date(timestamp))
            result[timeKey] = statistics.average
        }
        
        return result
    }
    
    // 统计状态分布
    fun calculateStatusDistribution(histories: List<DeviceHistoryEntity>): Map<DeviceStatus, Int> {
        val distribution = mutableMapOf<DeviceStatus, Int>()
        
        for (history in histories) {
            val status = DeviceStatus.values()[history.status]
            distribution[status] = distribution.getOrDefault(status, 0) + 1
        }
        
        return distribution
    }
    
    // 计算异常状态的持续时间
    fun calculateStatusDurations(histories: List<DeviceHistoryEntity>): Map<DeviceStatus, Long> {
        val durations = mutableMapOf<DeviceStatus, Long>()
        
        if (histories.isEmpty()) {
            return durations
        }
        
        // 按时间排序
        val sortedHistories = histories.sortedBy { it.timestamp }
        
        for (i in 0 until sortedHistories.size - 1) {
            val current = sortedHistories[i]
            val next = sortedHistories[i + 1]
            val status = DeviceStatus.values()[current.status]
            val duration = next.timestamp - current.timestamp
            
            durations[status] = durations.getOrDefault(status, 0) + duration
        }
        
        return durations
    }
    
    // 计算温度异常（超过阈值）的次数
    fun countTemperatureExceptions(histories: List<DeviceHistoryEntity>, maxThreshold: Double, minThreshold: Double): Int {
        return histories.count { 
            it.temperature > maxThreshold || it.temperature < minThreshold
        }
    }
    
    // 计算湿度异常（超过阈值）的次数
    fun countHumidityExceptions(histories: List<DeviceHistoryEntity>, maxThreshold: Double, minThreshold: Double): Int {
        return histories.count { 
            it.humidity > maxThreshold || it.humidity < minThreshold
        }
    }
    
    // 计算氧气浓度异常（低于阈值）的次数
    fun countOxygenExceptions(histories: List<DeviceHistoryEntity>, minThreshold: Double): Int {
        return histories.count { 
            it.oxygenLevel < minThreshold
        }
    }
    
    // 获取数据的时间范围
    fun getTimeRange(histories: List<DeviceHistoryEntity>): TimeRange {
        if (histories.isEmpty()) {
            return TimeRange(0, 0)
        }
        
        val sortedHistories = histories.sortedBy { it.timestamp }
        val startTime = sortedHistories.first().timestamp
        val endTime = sortedHistories.last().timestamp
        
        return TimeRange(startTime, endTime)
    }
    
    // 格式化时间戳为字符串
    fun formatTimestamp(timestamp: Long): String {
        return dateFormat.format(Date(timestamp))
    }
    
    // 格式化持续时间（毫秒）为可读字符串
    fun formatDuration(milliseconds: Long): String {
        val seconds = milliseconds / 1000
        val minutes = seconds / 60
        val hours = minutes / 60
        val days = hours / 24
        
        return when {
            days > 0 -> "${days}天 ${hours % 24}小时"
            hours > 0 -> "${hours}小时 ${minutes % 60}分钟"
            minutes > 0 -> "${minutes}分钟 ${seconds % 60}秒"
            else -> "${seconds}秒"
        }
    }
    
    // 基础统计数据类
    data class BasicStatistics(
        val average: Double,
        val max: Double,
        val min: Double,
        val sum: Double,
        val count: Int
    )
    
    // 时间范围数据类
    data class TimeRange(
        val startTime: Long,
        val endTime: Long
    )
}
