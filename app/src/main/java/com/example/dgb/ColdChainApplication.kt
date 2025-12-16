package com.example.dgb

import android.app.Application
import android.util.Log
import com.example.dgb.data.*

/**
 * 冷链监控应用程序类
 */
class ColdChainApplication : Application() {
    
    companion object {
        private const val TAG = "ColdChainApplication"
    }
    
    override fun onCreate() {
        super.onCreate()
        
        // 初始化事件日志系统
        initEventLogger()
        
        // 初始化告警服务
        initAlertService()
        
        // 安装全局异常处理器
        installGlobalExceptionHandler()
        
        Log.i(TAG, "应用程序初始化完成")
    }
    
    /**
     * 初始化事件日志系统
     */
    private fun initEventLogger() {
        try {
            val eventLogger = EventLogger.getInstance(this)
            eventLogger.info(
                type = EventType.SYSTEM_EVENT,
                message = "事件日志系统已初始化"
            )
            Log.i(TAG, "事件日志系统初始化完成")
        } catch (e: Exception) {
            Log.e(TAG, "事件日志系统初始化失败", e)
        }
    }
    
    /**
     * 初始化告警服务
     */
    private fun initAlertService() {
        try {
            val alertService = AlertService.getInstance(this)
            Log.i(TAG, "告警服务初始化完成")
        } catch (e: Exception) {
            Log.e(TAG, "告警服务初始化失败", e)
        }
    }
    
    /**
     * 安装全局异常处理器
     */
    private fun installGlobalExceptionHandler() {
        try {
            GlobalExceptionHandler.install(this)
            Log.i(TAG, "全局异常处理器安装完成")
        } catch (e: Exception) {
            Log.e(TAG, "全局异常处理器安装失败", e)
        }
    }
}
