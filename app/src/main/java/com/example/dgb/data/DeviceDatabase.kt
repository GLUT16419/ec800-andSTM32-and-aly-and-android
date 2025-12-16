package com.example.dgb.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

// 数据库类，定义数据库版本和包含的实体
@Database(
    entities = [DeviceHistoryEntity::class, DeviceEntity::class, EventLogEntity::class],
    version = 3, // 增加版本号以支持事件日志表
    exportSchema = false
)
@TypeConverters() // 如果需要类型转换器，可以在这里添加
abstract class DeviceDatabase : RoomDatabase() {
    // 获取历史数据DAO
    abstract fun deviceHistoryDao(): DeviceHistoryDao
    
    // 获取设备基本信息DAO
    abstract fun deviceDao(): DeviceDao
    
    // 获取事件日志DAO
    abstract fun eventLogDao(): EventLogDao

    companion object {
        // 单例模式，确保只有一个数据库实例
        @Volatile
        private var INSTANCE: DeviceDatabase? = null

        // 获取数据库实例
        fun getDatabase(context: Context): DeviceDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    DeviceDatabase::class.java,
                    "device_history_database"
                )
                    // 允许在主线程执行查询（仅用于开发，生产环境应该禁用）
                    // .allowMainThreadQueries()
                    // 允许破坏性迁移，用于开发环境快速测试
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}