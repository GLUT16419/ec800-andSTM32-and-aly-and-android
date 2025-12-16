package com.example.dgb.data

import android.app.Application

// DeviceHistoryViewModel工厂类，用于创建ViewModel实例
class DeviceHistoryViewModelFactory(private val application: Application) : androidx.lifecycle.ViewModelProvider.Factory {
    @Suppress("unchecked_cast")
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DeviceHistoryViewModel::class.java)) {
            // 创建数据库实例
            val database = DeviceDatabase.getDatabase(application)
            // 创建DAO实例
            val historyDao = database.deviceHistoryDao()
            // 创建仓库实例
            val repository = DeviceHistoryRepository(historyDao)
            // 创建并返回ViewModel实例
            return DeviceHistoryViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
