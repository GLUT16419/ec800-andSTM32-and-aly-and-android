package com.example.dgb

import android.os.Debug
import android.util.Log
import java.text.SimpleDateFormat
import java.util.*

/**
 * 性能监控工具类，用于收集和分析应用性能数据
 */
object PerformanceMonitor {
    private const val TAG = "PerformanceMonitor"
    private val dateFormat = SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault())
    
    // 性能指标集合
    private val performanceMetrics = mutableMapOf<String, MutableList<Long>>()
    // 内存使用记录
    private val memoryRecords = mutableListOf<MemoryRecord>()
    // 启动时间
    private var startTime = 0L
    
    /**
     * 启动性能监控
     */
    fun startMonitoring() {
        startTime = System.currentTimeMillis()
        Log.d(TAG, "性能监控已启动")
    }
    
    /**
     * 停止性能监控并输出报告
     */
    fun stopMonitoring() {
        val totalTime = System.currentTimeMillis() - startTime
        Log.d(TAG, "性能监控已停止，总时长: ${totalTime}ms")
        
        // 输出所有性能指标
        Log.d(TAG, "=== 性能指标报告 ===")
        performanceMetrics.forEach { (operation, times) ->
            if (times.isNotEmpty()) {
                val avgTime = times.average()
                val maxTime = times.maxOrNull() ?: 0L
                val minTime = times.minOrNull() ?: 0L
                Log.d(TAG, "$operation: 平均=${String.format("%.2f", avgTime)}ms, 最大=${maxTime}ms, 最小=${minTime}ms, 次数=${times.size}")
            }
        }
        
        // 输出内存使用报告
        if (memoryRecords.isNotEmpty()) {
            Log.d(TAG, "=== 内存使用报告 ===")
            val firstRecord = memoryRecords.first()
            val lastRecord = memoryRecords.last()
            Log.d(TAG, "初始内存: ${formatMemory(firstRecord.totalMemory)}, 初始可用: ${formatMemory(firstRecord.freeMemory)}")
            Log.d(TAG, "结束内存: ${formatMemory(lastRecord.totalMemory)}, 结束可用: ${formatMemory(lastRecord.freeMemory)}")
            
            val maxUsedMemory = memoryRecords.maxByOrNull { it.usedMemory }?.usedMemory
            val avgUsedMemory = memoryRecords.map { it.usedMemory }.average()
            Log.d(TAG, "最大使用内存: ${formatMemory(maxUsedMemory ?: 0L)}")
            Log.d(TAG, "平均使用内存: ${formatMemory(avgUsedMemory.toLong())}")
        }
    }
    
    /**
     * 记录操作执行时间
     */
    fun <T> measureExecutionTime(operationName: String, block: () -> T): T {
        val startTime = System.nanoTime()
        val result = block()
        val endTime = System.nanoTime()
        val duration = (endTime - startTime) / 1_000_000 // 转换为毫秒
        
        performanceMetrics.getOrPut(operationName) { mutableListOf() }.add(duration)
        Log.d(TAG, "$operationName 执行时间: ${duration}ms")
        
        return result
    }
    
    /**
     * 记录挂起函数执行时间
     */
    suspend fun <T> measureExecutionTimeSuspend(operationName: String, block: suspend () -> T): T {
        val startTime = System.nanoTime()
        val result = block()
        val endTime = System.nanoTime()
        val duration = (endTime - startTime) / 1_000_000 // 转换为毫秒
        
        performanceMetrics.getOrPut(operationName) { mutableListOf() }.add(duration)
        Log.d(TAG, "$operationName 执行时间: ${duration}ms")
        
        return result
    }
    
    /**
     * 记录内存使用情况
     */
    fun recordMemoryUsage() {
        val runtime = Runtime.getRuntime()
        val totalMemory = runtime.totalMemory()
        val freeMemory = runtime.freeMemory()
        val usedMemory = totalMemory - freeMemory
        val maxMemory = runtime.maxMemory()
        
        val record = MemoryRecord(System.currentTimeMillis(), totalMemory, freeMemory, usedMemory, maxMemory)
        memoryRecords.add(record)
        
        Log.d(TAG, "内存使用记录: 总=${formatMemory(totalMemory)}, 可用=${formatMemory(freeMemory)}, 使用=${formatMemory(usedMemory)}")
    }
    
    /**
     * 记录堆内存使用情况（需要添加android.permission.DUMP权限）
     */
    fun recordHeapMemory() {
        val heapSize = Debug.getNativeHeapSize()
        val heapAllocated = Debug.getNativeHeapAllocatedSize()
        val heapFree = Debug.getNativeHeapFreeSize()
        
        Log.d(TAG, "堆内存使用: 大小=${formatMemory(heapSize)}, 已分配=${formatMemory(heapAllocated)}, 可用=${formatMemory(heapFree)}")
    }
    
    /**
     * 格式化内存大小为易读格式
     */
    private fun formatMemory(bytes: Long): String {
        return when {
            bytes < 1024 -> "${bytes}B"
            bytes < 1024 * 1024 -> "${String.format("%.2f", bytes / 1024.0)}KB"
            bytes < 1024 * 1024 * 1024 -> "${String.format("%.2f", bytes / (1024.0 * 1024.0))}MB"
            else -> "${String.format("%.2f", bytes / (1024.0 * 1024.0 * 1024.0))}GB"
        }
    }
    
    /**
     * 内存使用记录数据类
     */
    data class MemoryRecord(
        val timestamp: Long,
        val totalMemory: Long,
        val freeMemory: Long,
        val usedMemory: Long,
        val maxMemory: Long
    )
}
