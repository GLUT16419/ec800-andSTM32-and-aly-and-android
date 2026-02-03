package com.example.dgb.utils

import java.text.SimpleDateFormat
import java.util.*

/**
 * 日期时间工具类，提供统一的日期格式化功能
 */
object DateUtils {
    // 日期时间格式常量
    const val PATTERN_FULL_DATE_TIME = "yyyy-MM-dd HH:mm:ss"
    const val PATTERN_DATE = "yyyy-MM-dd"
    const val PATTERN_TIME = "HH:mm:ss"
    const val PATTERN_SHORT_TIME = "HH:mm"
    const val PATTERN_MONTH_DAY_TIME = "MM-dd HH:mm"
    const val PATTERN_YEAR_MONTH_DAY = "yyyyMMdd"
    const val PATTERN_ISO8601 = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"

    /**
     * 获取当前时间戳（毫秒）
     */
    fun currentTimeMillis(): Long {
        return System.currentTimeMillis()
    }

    /**
     * 获取当前时间（秒）
     */
    fun currentTimeSeconds(): Long {
        return System.currentTimeMillis() / 1000
    }

    /**
     * 格式化日期时间
     * @param timestamp 时间戳（毫秒）
     * @param pattern 日期格式
     * @return 格式化后的日期字符串
     */
    fun format(timestamp: Long, pattern: String = PATTERN_FULL_DATE_TIME): String {
        val sdf = SimpleDateFormat(pattern, Locale.getDefault())
        return sdf.format(Date(timestamp))
    }

    /**
     * 格式化完整日期时间（yyyy-MM-dd HH:mm:ss）
     */
    fun formatFullDateTime(timestamp: Long): String {
        return format(timestamp, PATTERN_FULL_DATE_TIME)
    }

    /**
     * 格式化日期（yyyy-MM-dd）
     */
    fun formatDate(timestamp: Long): String {
        return format(timestamp, PATTERN_DATE)
    }

    /**
     * 格式化时间（HH:mm:ss）
     */
    fun formatTime(timestamp: Long): String {
        return format(timestamp, PATTERN_TIME)
    }

    /**
     * 格式化短时间（HH:mm）
     */
    fun formatShortTime(timestamp: Long): String {
        return format(timestamp, PATTERN_SHORT_TIME)
    }

    /**
     * 格式化月日时间（MM-dd HH:mm）
     */
    fun formatMonthDayTime(timestamp: Long): String {
        return format(timestamp, PATTERN_MONTH_DAY_TIME)
    }

    /**
     * 格式化年份月份日期（yyyyMMdd）
     */
    fun formatYearMonthDay(timestamp: Long): String {
        return format(timestamp, PATTERN_YEAR_MONTH_DAY)
    }

    /**
     * 解析日期字符串为Date对象
     * @param dateStr 日期字符串
     * @param pattern 日期格式
     * @return Date对象，解析失败返回null
     */
    fun parse(dateStr: String, pattern: String = PATTERN_FULL_DATE_TIME): Date? {
        return try {
            val sdf = SimpleDateFormat(pattern, Locale.getDefault())
            sdf.parse(dateStr)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * 解析日期字符串为时间戳
     * @param dateStr 日期字符串
     * @param pattern 日期格式
     * @return 时间戳（毫秒），解析失败返回0
     */
    fun parseToTimestamp(dateStr: String, pattern: String = PATTERN_FULL_DATE_TIME): Long {
        return parse(dateStr, pattern)?.time ?: 0L
    }

    /**
     * 计算两个时间戳之间的差值（毫秒）
     */
    fun getTimeDifference(startTimestamp: Long, endTimestamp: Long): Long {
        return endTimestamp - startTimestamp
    }

    /**
     * 格式化持续时间
     * @param milliseconds 毫秒数
     * @return 格式化后的持续时间字符串，如 "02:30:45" 或 "1d 02:30:45"
     */
    fun formatDuration(milliseconds: Long): String {
        val seconds = (milliseconds / 1000).toInt()
        val hours = seconds / 3600
        val minutes = (seconds % 3600) / 60
        val secs = seconds % 60

        return if (hours >= 24) {
            val days = hours / 24
            val remainingHours = hours % 24
            String.format("%dd %02d:%02d:%02d", days, remainingHours, minutes, secs)
        } else {
            String.format("%02d:%02d:%02d", hours, minutes, secs)
        }
    }

    /**
     * 获取指定时间所在的日期的开始时间（00:00:00）
     */
    fun getStartOfDay(timestamp: Long): Long {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timestamp
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }

    /**
     * 获取指定时间所在的日期的结束时间（23:59:59）
     */
    fun getEndOfDay(timestamp: Long): Long {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timestamp
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        return calendar.timeInMillis
    }

    /**
     * 获取指定时间所在的月份的开始时间（第一天的00:00:00）
     */
    fun getStartOfMonth(timestamp: Long): Long {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timestamp
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }

    /**
     * 获取指定时间所在的月份的结束时间（最后一天的23:59:59）
     */
    fun getEndOfMonth(timestamp: Long): Long {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timestamp
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        return calendar.timeInMillis
    }
}