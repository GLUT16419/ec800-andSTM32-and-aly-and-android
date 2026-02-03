package com.example.dgb.data

import android.content.Context
import android.util.Log
import android.widget.Toast
import java.lang.Thread.UncaughtExceptionHandler

/**
 * 全局异常处理器
 */
class GlobalExceptionHandler(
    private val context: Context,
    private val defaultHandler: UncaughtExceptionHandler?
) : UncaughtExceptionHandler {
    
    companion object {
        private const val TAG = "GlobalExceptionHandler"
        
        /**
         * 安装全局异常处理器
         */
        fun install(context: Context) {
            val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
            Thread.setDefaultUncaughtExceptionHandler(GlobalExceptionHandler(context, defaultHandler))
            Log.i(TAG, "全局异常处理器已安装")
        }
    }
    
    override fun uncaughtException(t: Thread, e: Throwable) {
        try {
            // 记录异常到事件日志系统
            val eventLogger = EventLogger.getInstance(context)
            eventLogger.logException(
                type = EventType.APP_CRASH,
                message = "应用程序崩溃",
                exception = e
            )
            
            Log.e(TAG, "捕获到未处理异常:", e)
            
            // 显示友好的错误提示
            showErrorToast(e)
        } catch (ex: Exception) {
            // 防止异常处理器本身抛出异常
            Log.e(TAG, "异常处理器处理异常时出错:", ex)
        } finally {
            // 调用默认的异常处理器，确保应用程序正常终止
            defaultHandler?.uncaughtException(t, e)
        }
    }
    
    /**
     * 显示友好的错误提示
     */
    private fun showErrorToast(e: Throwable) {
        // 只在主线程显示Toast
        if (Thread.currentThread().name == "main") {
            Toast.makeText(
                context,
                "应用程序出现异常，即将重启：${e.message}",
                Toast.LENGTH_LONG
            ).show()
        } else {
            // 在非主线程，需要切换到主线程显示Toast
            android.os.Handler(android.os.Looper.getMainLooper()).post {
                Toast.makeText(
                    context,
                    "应用程序出现异常，即将重启：${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
}
