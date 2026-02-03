package com.example.dgb.core.constants

/**
 * 应用程序常量定义
 */
object Constants {
    
    /**
     * 应用程序信息
     */
    object App {
        const val APP_NAME = "冷链监控"
        const val APP_VERSION = "1.0"
    }
    
    /**
     * 网络相关常量
     */
    object Network {
        const val CONNECT_TIMEOUT = 30L // 连接超时时间（秒）
        const val READ_TIMEOUT = 30L // 读取超时时间（秒）
        const val WRITE_TIMEOUT = 30L // 写入超时时间（秒）
        const val RETRY_COUNT = 3 // 重试次数
    }
    
    /**
     * MQTT相关常量
     */
    object Mqtt {
        const val ALGORITHM = "HmacSHA256"
        const val VERSION = "paho-android-1.0.0"
        const val SECURE_MODE = 2
        const val SIGN_METHOD = "hmacsha256"
    }
    
    /**
     * 数据库相关常量
     */
    object Database {
        const val DATABASE_NAME = "cold_chain.db"
        const val DATABASE_VERSION = 1
    }
    
    /**
     * 告警相关常量
     */
    object Alert {
        const val CHANNEL_ID = "cold_chain_alerts"
        const val CHANNEL_NAME = "冷链监控告警"
        const val CHANNEL_DESCRIPTION = "冷链设备异常告警通知"
        const val DEFAULT_ALERT_INTERVAL = 5 * 60 * 1000L // 默认5分钟内不重复告警同一事件
    }
    
    /**
     * 设备状态相关常量
     */
    object Device {
        const val STATUS_ONLINE = 0
        const val STATUS_OFFLINE = 1
        const val STATUS_ALARM = 2
    }
    
    /**
     * 事件日志相关常量
     */
    object EventLog {
        const val MAX_LOG_COUNT = 1000 // 最大日志记录数
    }
    
    /**
     * 图表相关常量
     */
    object Chart {
        const val DEFAULT_CHART_POINT_COUNT = 50 // 默认图表数据点数量
        const val MAX_CHART_POINT_COUNT = 1000 // 最大图表数据点数量
    }
    
    /**
     * 地图相关常量
     */
    object Map {
        const val DEFAULT_ZOOM_LEVEL = 15f // 默认地图缩放级别
        const val MAX_ZOOM_LEVEL = 20f // 最大地图缩放级别
        const val MIN_ZOOM_LEVEL = 5f // 最小地图缩放级别
    }
    
    /**
     * 权限相关常量
     */
    object Permission {
        const val PERMISSION_REQUEST_CODE = 1001 // 权限请求码
    }
}
