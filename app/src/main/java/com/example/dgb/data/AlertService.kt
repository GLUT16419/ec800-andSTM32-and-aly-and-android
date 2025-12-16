package com.example.dgb.data

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.RingtoneManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.*
import java.util.*

/**
 * 告警渠道类型
 */
enum class AlertChannel(val value: String) {
    LOCAL_NOTIFICATION("local_notification"),
    EMAIL("email"),
    SMS("sms"),
    PUSH_NOTIFICATION("push_notification")
}

/**
 * 告警配置类
 */
data class AlertConfig(
    var isEnabled: Boolean = true,
    val channels: MutableSet<AlertChannel> = mutableSetOf(AlertChannel.LOCAL_NOTIFICATION),
    val minLevel: EventLevel = EventLevel.WARNING,
    val deviceIds: MutableSet<Int> = mutableSetOf(), // 为空表示所有设备
    val alertInterval: Long = 5 * 60 * 1000L, // 默认5分钟内不重复告警同一事件
    val recipients: MutableList<String> = mutableListOf() // 邮件/短信收件人
)

/**
 * 告警服务
 */
class AlertService private constructor(context: Context) {
    companion object {
        private const val TAG = "AlertService"
        private const val CHANNEL_ID = "cold_chain_alerts"
        private const val CHANNEL_NAME = "冷链监控告警"
        private const val CHANNEL_DESCRIPTION = "冷链设备异常告警通知"
        private var instance: AlertService? = null
        
        // 获取单例实例
        fun getInstance(context: Context): AlertService {
            if (instance == null) {
                synchronized(this) {
                    if (instance == null) {
                        instance = AlertService(context)
                    }
                }
            }
            return instance!!
        }
    }
    
    private val applicationContext = context.applicationContext
    private val coroutineScope = CoroutineScope(Dispatchers.IO)
    private val mainHandler = Handler(Looper.getMainLooper())
    private val eventLogger = EventLogger.getInstance(applicationContext)
    private val notificationManager: NotificationManagerCompat
    
    // 告警配置
    private val _alertConfig = MutableLiveData<AlertConfig>(AlertConfig())
    val alertConfig: LiveData<AlertConfig> get() = _alertConfig
    
    // 最近告警记录，用于去重
    private val recentAlerts = mutableMapOf<String, Long>()
    
    init {
        // 初始化通知渠道
        createNotificationChannel()
        notificationManager = NotificationManagerCompat.from(applicationContext)
        
        // 订阅事件日志
        subscribeToEventLogs()
    }
    
    /**
     * 创建通知渠道
     */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = CHANNEL_DESCRIPTION
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 250, 250, 250)
                setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM), null)
            }
            val notificationManager = applicationContext.getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    /**
     * 订阅事件日志
     */
    private fun subscribeToEventLogs() {
        // 在主线程观察事件日志
        mainHandler.post {
            eventLogger.liveEvents.observeForever { eventLog ->
                // 处理事件日志，发送告警
                handleEventLog(eventLog)
            }
        }
    }
    
    /**
     * 处理事件日志，发送告警
     */
    private fun handleEventLog(eventLog: EventLogEntity) {
        val config = _alertConfig.value ?: return
        
        // 检查告警是否启用
        if (!config.isEnabled) return
        
        // 检查事件级别是否达到告警阈值
        val eventLevel = EventLevel.values().find { it.value == eventLog.level } ?: return
        if (eventLevel.value < config.minLevel.value) return
        
        // 检查是否针对特定设备
        if (config.deviceIds.isNotEmpty() && eventLog.deviceId !in config.deviceIds) return
        
        // 检查是否在告警间隔内（去重）
        val alertKey = "${eventLog.type}-${eventLog.deviceId}-${eventLog.level}"
        val lastAlertTime = recentAlerts[alertKey]
        if (lastAlertTime != null && System.currentTimeMillis() - lastAlertTime < config.alertInterval) {
            Log.d(TAG, "告警去重: $alertKey")
            return
        }
        
        // 更新最近告警时间
        recentAlerts[alertKey] = System.currentTimeMillis()
        
        // 发送告警到各个渠道
        config.channels.forEach { channel ->
            when (channel) {
                AlertChannel.LOCAL_NOTIFICATION -> sendLocalNotification(eventLog)
                AlertChannel.EMAIL -> sendEmailAlert(eventLog)
                AlertChannel.SMS -> sendSmsAlert(eventLog)
                AlertChannel.PUSH_NOTIFICATION -> sendPushNotification(eventLog)
            }
        }
    }
    
    /**
     * 发送本地通知告警
     */
    private fun sendLocalNotification(eventLog: EventLogEntity) {
        mainHandler.post {
            try {
                // 创建通知内容
                val title = when (EventLevel.values().find { it.value == eventLog.level }) {
                    EventLevel.CRITICAL -> "[严重] 冷链设备异常"
                    EventLevel.ERROR -> "[错误] 冷链设备异常"
                    EventLevel.WARNING -> "[警告] 冷链设备异常"
                    else -> "[信息] 冷链设备通知"
                }
                
                val contentText = buildString {
                    append(eventLog.message)
                    eventLog.deviceId?.let { append(" (设备ID: $it)") }
                }
                
                // 创建通知意图
                val intent = Intent(applicationContext, Class.forName("com.example.dgb.MainActivity"))
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                val pendingIntent = PendingIntent.getActivity(
                    applicationContext,
                    eventLog.id.toInt(),
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                
                // 构建通知
                val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
                    .setSmallIcon(android.R.drawable.ic_dialog_alert)
                    .setContentTitle(title)
                    .setContentText(contentText)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true)
                    .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM))
                    .setVibrate(longArrayOf(0, 250, 250, 250))
                    .build()
                
                // 检查通知权限
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                    applicationContext.checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                    Log.w(TAG, "没有通知权限，无法发送通知")
                    return@post
                }
                
                // 发送通知
                notificationManager.notify(eventLog.id.toInt(), notification)
                
                Log.i(TAG, "本地通知已发送: ${eventLog.message}")
            } catch (e: Exception) {
                Log.e(TAG, "发送本地通知失败", e)
                eventLogger.logException(EventType.SYSTEM_EVENT, "发送本地通知失败", e)
            }
        }
    }
    
    /**
     * 发送邮件告警
     */
    private fun sendEmailAlert(eventLog: EventLogEntity) {
        coroutineScope.launch {
            try {
                val config = _alertConfig.value ?: return@launch
                if (config.recipients.isEmpty()) return@launch
                
                // 这里只是模拟邮件发送，实际项目中需要集成邮件发送库
                Log.i(TAG, "邮件告警已发送到: ${config.recipients.joinToString()}, 内容: ${eventLog.message}")
                
                // 记录告警发送日志
                eventLogger.info(EventType.SYSTEM_EVENT, "邮件告警已发送", "收件人: ${config.recipients.joinToString()}")
            } catch (e: Exception) {
                Log.e(TAG, "发送邮件告警失败", e)
                eventLogger.logException(EventType.SYSTEM_EVENT, "发送邮件告警失败", e)
            }
        }
    }
    
    /**
     * 发送短信告警
     */
    private fun sendSmsAlert(eventLog: EventLogEntity) {
        coroutineScope.launch {
            try {
                val config = _alertConfig.value ?: return@launch
                if (config.recipients.isEmpty()) return@launch
                
                // 这里只是模拟短信发送，实际项目中需要集成短信发送SDK或使用系统短信功能
                Log.i(TAG, "短信告警已发送到: ${config.recipients.joinToString()}, 内容: ${eventLog.message}")
                
                // 记录告警发送日志
                eventLogger.info(EventType.SYSTEM_EVENT, "短信告警已发送", "收件人: ${config.recipients.joinToString()}")
            } catch (e: Exception) {
                Log.e(TAG, "发送短信告警失败", e)
                eventLogger.logException(EventType.SYSTEM_EVENT, "发送短信告警失败", e)
            }
        }
    }
    
    /**
     * 发送推送通知
     */
    private fun sendPushNotification(eventLog: EventLogEntity) {
        coroutineScope.launch {
            try {
                // 这里只是模拟推送通知，实际项目中需要集成推送通知服务（如阿里云推送、极光推送等）
                Log.i(TAG, "推送通知已发送: ${eventLog.message}")
                
                // 记录告警发送日志
                eventLogger.info(EventType.SYSTEM_EVENT, "推送通知已发送", "内容: ${eventLog.message}")
            } catch (e: Exception) {
                Log.e(TAG, "发送推送通知失败", e)
                eventLogger.logException(EventType.SYSTEM_EVENT, "发送推送通知失败", e)
            }
        }
    }
    
    /**
     * 更新告警配置
     */
    fun updateConfig(config: AlertConfig) {
        _alertConfig.value = config
        Log.i(TAG, "告警配置已更新")
        eventLogger.info(EventType.SYSTEM_EVENT, "告警配置已更新", config.toString())
    }
    
    /**
     * 添加告警渠道
     */
    fun addAlertChannel(channel: AlertChannel) {
        val config = _alertConfig.value ?: return
        config.channels.add(channel)
        _alertConfig.value = config
        Log.i(TAG, "已添加告警渠道: $channel")
    }
    
    /**
     * 移除告警渠道
     */
    fun removeAlertChannel(channel: AlertChannel) {
        val config = _alertConfig.value ?: return
        config.channels.remove(channel)
        _alertConfig.value = config
        Log.i(TAG, "已移除告警渠道: $channel")
    }
    
    /**
     * 设置告警接收人
     */
    fun setRecipients(recipients: List<String>) {
        val config = _alertConfig.value ?: return
        config.recipients.clear()
        config.recipients.addAll(recipients)
        _alertConfig.value = config
        Log.i(TAG, "已更新告警接收人: ${recipients.joinToString()}")
    }
    
    /**
     * 添加告警设备
     */
    fun addAlertDevice(deviceId: Int) {
        val config = _alertConfig.value ?: return
        config.deviceIds.add(deviceId)
        _alertConfig.value = config
        Log.i(TAG, "已添加告警设备: $deviceId")
    }
    
    /**
     * 移除告警设备
     */
    fun removeAlertDevice(deviceId: Int) {
        val config = _alertConfig.value ?: return
        config.deviceIds.remove(deviceId)
        _alertConfig.value = config
        Log.i(TAG, "已移除告警设备: $deviceId")
    }
}
