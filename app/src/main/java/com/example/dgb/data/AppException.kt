package com.example.dgb.data

/**
 * 应用程序统一异常类，用于规范异常处理
 * @param message 异常信息
 * @param code 异常编码
 * @param cause 原始异常
 */
open class AppException(
    message: String? = null,
    val code: Int = DEFAULT_ERROR_CODE,
    cause: Throwable? = null
) : Exception(message, cause) {

    companion object {
        // 默认错误编码
        const val DEFAULT_ERROR_CODE = 1000
        
        // 网络错误编码
        const val NETWORK_ERROR_CODE = 2000
        
        // 数据库错误编码
        const val DATABASE_ERROR_CODE = 3000
        
        // MQTT错误编码
        const val MQTT_ERROR_CODE = 4000
        
        // 权限错误编码
        const val PERMISSION_ERROR_CODE = 5000
        
        // 其他错误编码
        const val OTHER_ERROR_CODE = 6000
    }

    /**
     * 网络异常子类
     */
    class NetworkException(
        message: String? = "网络连接失败",
        code: Int = NETWORK_ERROR_CODE,
        cause: Throwable? = null
    ) : AppException(message, code, cause)

    /**
     * 数据库异常子类
     */
    class DatabaseException(
        message: String? = "数据库操作失败",
        code: Int = DATABASE_ERROR_CODE,
        cause: Throwable? = null
    ) : AppException(message, code, cause)

    /**
     * MQTT异常子类
     */
    class MqttException(
        message: String? = "MQTT通信失败",
        code: Int = MQTT_ERROR_CODE,
        cause: Throwable? = null
    ) : AppException(message, code, cause)

    /**
     * 权限异常子类
     */
    class PermissionException(
        message: String? = "权限不足",
        code: Int = PERMISSION_ERROR_CODE,
        cause: Throwable? = null
    ) : AppException(message, code, cause)

    /**
     * 其他异常子类
     */
    class OtherException(
        message: String? = "操作失败",
        code: Int = OTHER_ERROR_CODE,
        cause: Throwable? = null
    ) : AppException(message, code, cause)
}
