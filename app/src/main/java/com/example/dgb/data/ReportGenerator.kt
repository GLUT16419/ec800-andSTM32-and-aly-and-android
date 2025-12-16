package com.example.dgb.data

import android.content.Context
import android.os.Environment
import android.util.Log
import com.example.dgb.DeviceStatus
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*
import java.io.IOException

// 数据报表生成工具类，支持生成和导出Excel格式的报表
class ReportGenerator(private val context: Context) {
    // 日志标签
    private val TAG = "ReportGenerator"
    
    // 时间格式化器
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    private val shortDateFormat = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
    
    // 统计分析器
    private val statisticsAnalyzer = StatisticsAnalyzer()
    
    /**
     * 生成设备数据报表
     * @param deviceId 设备ID
     * @param deviceName 设备名称
     * @param histories 设备历史数据
     * @param startTime 报表开始时间
     * @param endTime 报表结束时间
     * @return 生成的报表文件路径
     */
    fun generateDeviceReport(
        deviceId: Int,
        deviceName: String,
        histories: List<DeviceHistoryEntity>,
        startTime: Long,
        endTime: Long
    ): String? {
        try {
            // 计算统计数据
            val tempStats = statisticsAnalyzer.calculateBasicStatistics(histories.map { it.temperature })
            val humidityStats = statisticsAnalyzer.calculateBasicStatistics(histories.map { it.humidity })
            val oxygenStats = statisticsAnalyzer.calculateBasicStatistics(histories.map { it.oxygenLevel })
            val statusDistribution = statisticsAnalyzer.calculateStatusDistribution(histories)
            val statusDurations = statisticsAnalyzer.calculateStatusDurations(histories)
            
            // 创建报表内容
            val reportContent = buildReportContent(
                deviceId, deviceName, histories, startTime, endTime,
                tempStats, humidityStats, oxygenStats, statusDistribution, statusDurations
            )
            
            // 导出报表文件
            return exportReportToFile(reportContent, deviceName)
            
        } catch (e: Exception) {
            Log.e(TAG, "生成报表失败: ${e.message}")
            return null
        }
    }
    
    /**
     * 构建报表内容
     */
    private fun buildReportContent(
        deviceId: Int,
        deviceName: String,
        histories: List<DeviceHistoryEntity>,
        startTime: Long,
        endTime: Long,
        tempStats: StatisticsAnalyzer.BasicStatistics,
        humidityStats: StatisticsAnalyzer.BasicStatistics,
        oxygenStats: StatisticsAnalyzer.BasicStatistics,
        statusDistribution: Map<DeviceStatus, Int>,
        statusDurations: Map<DeviceStatus, Long>
    ): String {
        val sb = StringBuilder()
        
        // 报表标题
        sb.appendLine("# 冷藏运输车环境监测系统 - 设备数据报表")
        sb.appendLine()
        
        // 报表基本信息
        sb.appendLine("## 报表基本信息")
        sb.appendLine("设备ID: $deviceId")
        sb.appendLine("设备名称: $deviceName")
        sb.appendLine("报表周期: ${dateFormat.format(Date(startTime))} 至 ${dateFormat.format(Date(endTime))}")
        sb.appendLine("数据记录数量: ${histories.size}")
        sb.appendLine()
        
        // 环境参数统计
        sb.appendLine("## 环境参数统计")
        
        // 温度统计
        sb.appendLine("### 温度统计")
        sb.appendLine("平均温度: %.2f°C".format(tempStats.average))
        sb.appendLine("最高温度: %.2f°C".format(tempStats.max))
        sb.appendLine("最低温度: %.2f°C".format(tempStats.min))
        sb.appendLine()
        
        // 湿度统计
        sb.appendLine("### 湿度统计")
        sb.appendLine("平均湿度: %.2f%%".format(humidityStats.average))
        sb.appendLine("最高湿度: %.2f%%".format(humidityStats.max))
        sb.appendLine("最低湿度: %.2f%%".format(humidityStats.min))
        sb.appendLine()
        
        // 氧气浓度统计
        sb.appendLine("### 氧气浓度统计")
        sb.appendLine("平均氧气浓度: %.2f%%".format(oxygenStats.average))
        sb.appendLine("最高氧气浓度: %.2f%%".format(oxygenStats.max))
        sb.appendLine("最低氧气浓度: %.2f%%".format(oxygenStats.min))
        sb.appendLine()
        
        // 设备状态统计
        sb.appendLine("## 设备状态统计")
        
        // 状态分布
        sb.appendLine("### 状态分布")
        for ((status, count) in statusDistribution) {
            val percentage = if (histories.isNotEmpty()) (count.toDouble() / histories.size * 100) else 0.0
            sb.appendLine("${status.displayName}: ${count}次 (${String.format("%.1f%%", percentage)})")
        }
        sb.appendLine()
        
        // 状态持续时间
        sb.appendLine("### 状态持续时间")
        for ((status, duration) in statusDurations) {
            sb.appendLine("${status.displayName}: ${statisticsAnalyzer.formatDuration(duration)}")
        }
        sb.appendLine()
        
        // 设备异常率计算
        val totalStatusCount = histories.size
        val errorCount = statusDistribution.getOrDefault(DeviceStatus.ERROR, 0)
        val warningCount = statusDistribution.getOrDefault(DeviceStatus.WARNING, 0)
        val errorRate = if (totalStatusCount > 0) (errorCount.toDouble() / totalStatusCount * 100) else 0.0
        val warningRate = if (totalStatusCount > 0) (warningCount.toDouble() / totalStatusCount * 100) else 0.0
        val abnormalRate = errorRate + warningRate
        
        sb.appendLine("### 设备异常率")
        sb.appendLine("异常率: ${String.format("%.1f%%", abnormalRate)}")
        sb.appendLine("错误率: ${String.format("%.1f%%", errorRate)}")
        sb.appendLine("警告率: ${String.format("%.1f%%", warningRate)}")
        sb.appendLine("正常率: ${String.format("%.1f%%", 100 - abnormalRate)}")
        sb.appendLine()
        
        // 详细数据记录
        sb.appendLine("## 详细数据记录")
        sb.appendLine("时间,温度(°C),湿度(%),氧气浓度(%),设备状态")
        
        for (history in histories) {
            val timeStr = dateFormat.format(Date(history.timestamp))
            val status = DeviceStatus.values()[history.status]
            sb.appendLine("$timeStr,${history.temperature},${history.humidity},${history.oxygenLevel},${status.displayName}")
        }
        
        return sb.toString()
    }
    
    /**
     * 导出报表到文件
     */
    private fun exportReportToFile(content: String, deviceName: String): String? {
        try {
            // 创建报表文件目录
            val reportDir = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "reports")
            if (!reportDir.exists()) {
                if (!reportDir.mkdirs()) {
                    Log.e(TAG, "无法创建报表目录")
                    return null
                }
            }
            
            // 创建报表文件
            val fileName = "device_${deviceName}_report_${shortDateFormat.format(Date())}.csv"
            val reportFile = File(reportDir, fileName)
            
            // 写入文件内容
            FileOutputStream(reportFile).use { fos ->
                fos.write(content.toByteArray(Charsets.UTF_8))
            }
            
            Log.d(TAG, "报表已生成: ${reportFile.absolutePath}")
            return reportFile.absolutePath
            
        } catch (e: IOException) {
            Log.e(TAG, "导出报表失败: ${e.message}")
            e.printStackTrace()
            return null
        }
    }
    
    /**
     * 生成系统汇总报表
     * @param deviceReports 所有设备的报表数据
     * @return 生成的汇总报表文件路径
     */
    fun generateSystemReport(deviceReports: List<DeviceReportData>): String? {
        try {
            // 创建报表内容
            val sb = StringBuilder()
            
            // 报表标题
            sb.appendLine("# 冷藏运输车环境监测系统 - 系统汇总报表")
            sb.appendLine()
            
            // 报表生成时间
            sb.appendLine("报表生成时间: ${dateFormat.format(Date())}")
            sb.appendLine("设备总数: ${deviceReports.size}")
            sb.appendLine()
            
            // 设备汇总统计
            sb.appendLine("## 设备汇总统计")
            sb.appendLine("设备名称,记录数量,平均温度(°C),平均湿度(%),平均氧气浓度(%),异常率(%)")
            
            for (reportData in deviceReports) {
                val abnormalRate = reportData.errorRate + reportData.warningRate
                sb.appendLine("${reportData.deviceName},${reportData.recordCount},${String.format("%.2f", reportData.averageTemperature)},${String.format("%.2f", reportData.averageHumidity)},${String.format("%.2f", reportData.averageOxygenLevel)},${String.format("%.1f", abnormalRate)}")
            }
            
            // 导出报表文件
            return exportSystemReportToFile(sb.toString())
            
        } catch (e: Exception) {
            Log.e(TAG, "生成系统汇总报表失败: ${e.message}")
            return null
        }
    }
    
    /**
     * 导出系统汇总报表到文件
     */
    private fun exportSystemReportToFile(content: String): String? {
        try {
            // 创建报表文件目录
            val reportDir = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "reports")
            if (!reportDir.exists()) {
                reportDir.mkdirs()
            }
            
            // 创建报表文件
            val fileName = "system_report_${shortDateFormat.format(Date())}.csv"
            val reportFile = File(reportDir, fileName)
            
            // 写入文件内容
            FileOutputStream(reportFile).use { fos ->
                fos.write(content.toByteArray(Charsets.UTF_8))
            }
            
            Log.d(TAG, "系统汇总报表已生成: ${reportFile.absolutePath}")
            return reportFile.absolutePath
            
        } catch (e: IOException) {
            Log.e(TAG, "导出系统汇总报表失败: ${e.message}")
            return null
        }
    }
    
    /**
     * 设备报表数据类
     */
    data class DeviceReportData(
        val deviceName: String,
        val recordCount: Int,
        val averageTemperature: Double,
        val averageHumidity: Double,
        val averageOxygenLevel: Double,
        val errorRate: Double,
        val warningRate: Double
    )
}