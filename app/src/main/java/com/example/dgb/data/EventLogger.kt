package com.example.dgb.data

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.coroutines.*
import java.text.SimpleDateFormat
import java.util.*

/**
 * 事件日志级别
 */
enum class EventLevel(val value: Int) {
    INFO(1),
    WARNING(2),
    ERROR(3),
    CRITICAL(4)
}

/**
 * 事件类型
 */
enum class EventType(val value: String) {
    APP_CRASH("app_crash"),
    NETWORK_ERROR("network_error"),
    DEVICE_OFFLINE("device_offline"),
    SENSOR_ALARM("sensor_alarm"),
    USER_ACTION("user_action"),
    SYSTEM_EVENT("system_event"),
    DATA_SYNC("data_sync")
}

/**
 * 事件日志实体类
 */
@Entity(tableName = "event_logs")
data class EventLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long,
    val level: Int,
    val type: String,
    val message: String,
    val details: String? = null,
    val deviceId: Int? = null,
    val userId: Int? = null
)

/**
 * 事件日志仓库
 */
class EventLogRepository(private val eventLogDao: EventLogDao) {
    // 插入事件日志
    suspend fun logEvent(
        level: EventLevel,
        type: EventType,
        message: String,
        details: String? = null,
        deviceId: Int? = null,
        userId: Int? = null
    ): Long {
        val eventLog = EventLogEntity(
            timestamp = System.currentTimeMillis(),
            level = level.value,
            type = type.value,
            message = message,
            details = details,
            deviceId = deviceId,
            userId = userId
        )
        eventLogDao.insert(eventLog)
        return 0 // 简化实现，返回0作为日志ID
    }
    
    // 获取最新的N条事件日志
    suspend fun getLatestEventLogs(limit: Int = 100): List<EventLogEntity> {
        return eventLogDao.getLatestEventLogs(limit)
    }
    
    // 根据级别获取事件日志
    suspend fun getEventLogsByLevel(level: EventLevel, limit: Int = 100): List<EventLogEntity> {
        val logs = eventLogDao.getEventLogsByLevel(level.value)
        return if (logs.size > limit) logs.subList(0, limit) else logs
    }
    
    // 根据类型获取事件日志
    suspend fun getEventLogsByType(type: EventType, limit: Int = 100): List<EventLogEntity> {
        val logs = eventLogDao.getEventLogsByType(type.value)
        return if (logs.size > limit) logs.subList(0, limit) else logs
    }
    
    // 获取特定设备的事件日志
    suspend fun getDeviceEventLogs(deviceId: Int, limit: Int = 100): List<EventLogEntity> {
        return eventLogDao.getDeviceEventLogs(deviceId, limit)
    }
    
    // 清除指定时间之前的事件日志
    suspend fun clearOldEventLogs(beforeTimestamp: Long) {
        eventLogDao.clearOldEventLogs(beforeTimestamp)
    }
    
    // 获取事件总数
    suspend fun getEventCount(): Int {
        return eventLogDao.getEventCount()
    }
}

/**
 * 事件日志服务
 */
class EventLogger private constructor(context: Context) {
    companion object {
        private const val TAG = "EventLogger"
        private const val MAX_LOGS_TO_KEEP = 1000
        private var instance: EventLogger? = null
        
        // 获取单例实例
        fun getInstance(context: Context): EventLogger {
            if (instance == null) {
                synchronized(this) {
                    if (instance == null) {
                        instance = EventLogger(context)
                    }
                }
            }
            return instance!!
        }
    }
    
    private val applicationContext = context.applicationContext
    private val repository: EventLogRepository
    private val coroutineScope = CoroutineScope(Dispatchers.IO)
    private val mainHandler = Handler(Looper.getMainLooper())
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    
    // 实时事件通知
    private val _liveEvents = MutableLiveData<EventLogEntity>()
    val liveEvents: LiveData<EventLogEntity> get() = _liveEvents
    
    init {
        // 初始化数据库连接
        val db = DeviceDatabase.getDatabase(applicationContext)
        repository = EventLogRepository(db.eventLogDao())
        
        // 清理旧日志
        coroutineScope.launch {
            val oneMonthAgo = System.currentTimeMillis() - (30 * 24 * 60 * 60 * 1000)
            repository.clearOldEventLogs(oneMonthAgo)
        }
    }
    
    /**
     * 记录INFO级别的事件
     */
    fun info(type: EventType, message: String, details: String? = null, deviceId: Int? = null, userId: Int? = null) {
        log(EventLevel.INFO, type, message, details, deviceId, userId)
    }
    
    /**
     * 记录WARNING级别的事件
     */
    fun warning(type: EventType, message: String, details: String? = null, deviceId: Int? = null, userId: Int? = null) {
        log(EventLevel.WARNING, type, message, details, deviceId, userId)
    }
    
    /**
     * 记录ERROR级别的事件
     */
    fun error(type: EventType, message: String, details: String? = null, deviceId: Int? = null, userId: Int? = null) {
        log(EventLevel.ERROR, type, message, details, deviceId, userId)
    }
    
    /**
     * 记录CRITICAL级别的事件
     */
    fun critical(type: EventType, message: String, details: String? = null, deviceId: Int? = null, userId: Int? = null) {
        log(EventLevel.CRITICAL, type, message, details, deviceId, userId)
    }
    
    /**
     * 记录异常事件
     */
    fun logException(type: EventType, message: String, exception: Throwable, deviceId: Int? = null, userId: Int? = null) {
        val exceptionDetails = buildString {
            appendLine("异常类型: ${exception.javaClass.name}")
            appendLine("异常信息: ${exception.message}")
            appendLine("堆栈跟踪:")
            exception.stackTrace.forEach { stackTraceElement ->
                appendLine("    at $stackTraceElement")
            }
        }
        log(EventLevel.ERROR, type, message, exceptionDetails, deviceId, userId)
    }
    
    /**
     * 核心日志记录方法
     */
    private fun log(
        level: EventLevel,
        type: EventType,
        message: String,
        details: String? = null,
        deviceId: Int? = null,
        userId: Int? = null
    ) {
        // 在控制台输出日志
        val formattedMessage = "[${level.name}] ${type.value}: $message"
        when (level) {
            EventLevel.INFO -> Log.i(TAG, formattedMessage)
            EventLevel.WARNING -> Log.w(TAG, formattedMessage)
            EventLevel.ERROR, EventLevel.CRITICAL -> Log.e(TAG, formattedMessage)
        }
        
        // 持久化到数据库
        coroutineScope.launch {
            val logId = repository.logEvent(level, type, message, details, deviceId, userId)
            
            // 如果是重要事件，发送实时通知
            if (level.value >= EventLevel.WARNING.value) {
                val eventLog = EventLogEntity(
                    id = logId,
                    timestamp = System.currentTimeMillis(),
                    level = level.value,
                    type = type.value,
                    message = message,
                    details = details,
                    deviceId = deviceId,
                    userId = userId
                )
                mainHandler.post {
                    _liveEvents.value = eventLog
                }
            }
        }
    }
    
    /**
     * 获取最新的事件日志
     */
    suspend fun getLatestEventLogs(limit: Int = 100): List<EventLogEntity> {
        return repository.getLatestEventLogs(limit)
    }
    
    /**
     * 获取特定级别的事件日志
     */
    suspend fun getEventLogsByLevel(level: EventLevel, limit: Int = 100): List<EventLogEntity> {
        return repository.getEventLogsByLevel(level, limit)
    }
    
    /**
     * 格式化时间戳
     */
    fun formatTimestamp(timestamp: Long): String {
        return dateFormat.format(Date(timestamp))
    }
}
