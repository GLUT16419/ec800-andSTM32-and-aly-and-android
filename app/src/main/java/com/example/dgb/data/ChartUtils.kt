package com.example.dgb.data

import android.content.Context
import android.view.ViewGroup
import android.widget.LinearLayout
import com.example.dgb.DeviceStatus
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.charts.RadarChart
import com.github.mikephil.charting.components.*
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IFillFormatter
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.github.mikephil.charting.interfaces.datasets.IRadarDataSet
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.min

// 图表工具类，用于绘制环境参数趋势图和数据可视化
class ChartUtils(private val context: Context) {
    // 时间格式化器
    private val dateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    private val shortDateFormat = SimpleDateFormat("MM-dd HH:mm", Locale.getDefault())
    
    /**
     * 创建环境参数趋势图
     * @param container 图表容器布局
     * @param histories 设备历史数据
     * @param parameter 参数类型（温度、湿度、氧气浓度）
     * @return 创建的折线图
     */
    fun createParameterTrendChart(
        container: LinearLayout,
        histories: List<DeviceHistoryEntity>,
        parameter: ParameterType
    ): LineChart {
        // 创建折线图实例
        val chart = LineChart(context)
        
        // 设置图表尺寸
        val params = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        chart.layoutParams = params
        
        // 配置图表基本属性
        configureChart(chart)
        
        // 准备图表数据
        val chartData = prepareChartData(histories, parameter)
        chart.data = chartData
        
        // 设置图表标题
        val description = Description()
        description.text = parameter.getTitle()
        chart.description = description
        
        // 更新图表
        chart.invalidate()
        
        // 添加到容器
        container.addView(chart)
        
        return chart
    }
    
    /**
     * 创建多参数对比图
     * @param container 图表容器布局
     * @param histories 设备历史数据
     * @return 创建的折线图
     */
    fun createMultiParameterChart(
        container: LinearLayout,
        histories: List<DeviceHistoryEntity>
    ): LineChart {
        // 创建折线图实例
        val chart = LineChart(context)
        
        // 设置图表尺寸
        val params = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        chart.layoutParams = params
        
        // 配置图表基本属性
        configureChart(chart)
        
        // 准备多参数数据
        val dataSets = mutableListOf<ILineDataSet>()
        
        // 温度数据
        val tempEntries = prepareParameterEntries(histories, ParameterType.TEMPERATURE)
        val tempDataSet = createLineDataSet(tempEntries, "温度(°C)", 0xFFFF5722.toInt()) // 橙色
        dataSets.add(tempDataSet)
        
        // 湿度数据
        val humidityEntries = prepareParameterEntries(histories, ParameterType.HUMIDITY)
        val humidityDataSet = createLineDataSet(humidityEntries, "湿度(%)", 0xFF2196F3.toInt()) // 蓝色
        dataSets.add(humidityDataSet)
        
        // 氧气浓度数据
        val oxygenEntries = prepareParameterEntries(histories, ParameterType.OXYGEN_LEVEL)
        val oxygenDataSet = createLineDataSet(oxygenEntries, "氧气浓度(%)", 0xFF4CAF50.toInt()) // 绿色
        dataSets.add(oxygenDataSet)
        
        // 设置图表数据
        val lineData = LineData(dataSets)
        chart.data = lineData
        
        // 设置图表标题
        val description = Description()
        description.text = "环境参数对比趋势"
        chart.description = description
        
        // 更新图表
        chart.invalidate()
        
        // 添加到容器
        container.addView(chart)
        
        return chart
    }
    
    /**
     * 创建多设备参数对比图表
     * @param container 图表容器布局
     * @param deviceDataMap 设备数据映射，key为设备ID，value为该设备的历史数据
     * @param parameterType 参数类型
     * @return 创建的折线图
     */
    fun createMultiDeviceParameterChart(
        container: LinearLayout,
        deviceDataMap: Map<Int, List<DeviceHistoryEntity>>,
        parameterType: ParameterType
    ): LineChart {
        // 创建折线图实例
        val chart = LineChart(context)
        
        // 设置图表尺寸
        val params = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        chart.layoutParams = params
        
        // 配置图表基本属性
        configureChart(chart)
        
        // 准备数据集
        val dataSets = deviceDataMap.entries.mapIndexed { index, entry ->
            val deviceId = entry.key
            val histories = entry.value
            
            // 按时间排序
            val sortedHistories = histories.sortedBy { it.timestamp }
            
            // 准备数据点
            val entries = sortedHistories.mapIndexed { entryIndex, history ->
                val value = when (parameterType) {
                    ParameterType.TEMPERATURE -> history.temperature
                    ParameterType.HUMIDITY -> history.humidity
                    ParameterType.OXYGEN_LEVEL -> history.oxygenLevel
                }
                Entry(entryIndex.toFloat(), value.toFloat())
            }
            
            // 创建数据集
            val colors = listOf(
                0xFFFF5722.toInt(),  // 橙色
                0xFF2196F3.toInt(),  // 蓝色
                0xFF4CAF50.toInt(),  // 绿色
                0xFF9C27B0.toInt(),  // 紫色
                0xFFFFC107.toInt(),  // 黄色
                0xFFF44336.toInt()   // 红色
            )
            
            val color = colors[index % colors.size]
            val dataSet = createLineDataSet(entries, "设备 $deviceId", color)
            dataSet as ILineDataSet
        }
        
        // 设置图表数据
        val lineData = LineData(dataSets)
        chart.data = lineData
        
        // 设置图表标题
        val description = Description()
        description.text = "多设备${parameterType.getTitle().replace("趋势", "对比")}"
        chart.description = description
        
        // 更新图表
        chart.invalidate()
        
        // 添加到容器
        container.addView(chart)
        
        return chart
    }
    
    /**
     * 配置图表基本属性
     */
    private fun configureChart(chart: LineChart) {
        // 启用触摸手势，但优化性能
        chart.setTouchEnabled(true)
        chart.isDragEnabled = true
        chart.setScaleEnabled(true)
        chart.setDrawGridBackground(false)
        chart.isDoubleTapToZoomEnabled = false // 禁用双击缩放以提高性能
        chart.setPinchZoom(true)
        
        // 优化绘制性能
        chart.animateX(0) // 禁用X轴动画
        chart.animateY(0) // 禁用Y轴动画
        chart.animateXY(0, 0) // 禁用XY轴动画
        
        // 配置X轴
        val xAxis = chart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.setDrawGridLines(false) // 禁用X轴网格线
        xAxis.setDrawAxisLine(true)
        xAxis.textSize = 10f
        xAxis.granularity = 1f
        xAxis.labelRotationAngle = 45f
        xAxis.labelCount = 10 // 限制标签数量
        
        // 配置Y轴
        val leftYAxis = chart.axisLeft
        leftYAxis.setDrawGridLines(true)
        leftYAxis.gridLineWidth = 0.25f // 减小网格线宽度
        leftYAxis.textSize = 10f
        leftYAxis.labelCount = 8 // 限制标签数量
        leftYAxis.setDrawZeroLine(false) // 禁用零线绘制
        
        val rightYAxis = chart.axisRight
        rightYAxis.isEnabled = false
        
        // 配置图例
        val legend = chart.legend
        legend.form = Legend.LegendForm.LINE
        legend.textSize = 12f
        legend.verticalAlignment = Legend.LegendVerticalAlignment.TOP
        legend.horizontalAlignment = Legend.LegendHorizontalAlignment.LEFT
        legend.orientation = Legend.LegendOrientation.HORIZONTAL
        legend.setDrawInside(false)
        
        // 配置描述
        val description = Description()
        description.textSize = 12f
        chart.description = description
        
        // 优化渲染性能
        chart.renderer.paintRender.isAntiAlias = false // 禁用抗锯齿以提高性能
    }
    
    /**
     * 准备图表数据
     */
    private fun prepareChartData(histories: List<DeviceHistoryEntity>, parameter: ParameterType): LineData {
        // 准备数据点
        val entries = prepareParameterEntries(histories, parameter)
        
        // 创建数据集
        val dataSet = createLineDataSet(entries, parameter.getTitle(), parameter.getColor())
        
        // 创建折线图数据
        return LineData(dataSet)
    }
    
    /**
     * 数据采样算法，减少数据点数量同时保持趋势
     */
    private fun sampleDataPoints(histories: List<DeviceHistoryEntity>, maxPoints: Int = 500): List<DeviceHistoryEntity> {
        if (histories.size <= maxPoints) {
            return histories // 如果数据点数量已经较少，直接返回
        }
        
        val sampledData = mutableListOf<DeviceHistoryEntity>()
        val step = histories.size / maxPoints
        
        // 采样算法：保留首末点和关键转折点
        sampledData.add(histories.first())
        
        var lastValue = when {
            histories.first().temperature != 0.0 -> histories.first().temperature
            histories.first().humidity != 0.0 -> histories.first().humidity
            else -> histories.first().oxygenLevel
        }
        
        var lastAddedIndex = 0
        
        for (i in 1 until histories.size - 1 step step) {
            val current = histories[i]
            val next = histories[min(i + step, histories.size - 1)]
            
            // 获取当前值
            val currentValue = when {
                current.temperature != 0.0 -> current.temperature
                current.humidity != 0.0 -> current.humidity
                else -> current.oxygenLevel
            }
            
            val nextValue = when {
                next.temperature != 0.0 -> next.temperature
                next.humidity != 0.0 -> next.humidity
                else -> next.oxygenLevel
            }
            
            // 如果是转折点（斜率变化），保留该点
            val isTurningPoint = (currentValue - lastValue) * (nextValue - currentValue) < 0
            if (isTurningPoint && i - lastAddedIndex > step / 2) {
                sampledData.add(current)
                lastAddedIndex = i
                lastValue = currentValue
            }
            
            // 每step个点强制保留一个
            if (i - lastAddedIndex >= step) {
                sampledData.add(current)
                lastAddedIndex = i
                lastValue = currentValue
            }
        }
        
        // 添加最后一个点
        if (histories.lastIndex - lastAddedIndex > step / 2) {
            sampledData.add(histories.last())
        } else {
            // 如果最后一个点离上一个添加的点太近，替换掉上一个点
            sampledData[sampledData.size - 1] = histories.last()
        }
        
        // 确保最终数据点数量不超过maxPoints
        if (sampledData.size > maxPoints) {
            val finalStep = sampledData.size / maxPoints
            return sampledData.filterIndexed { index, _ -> index % finalStep == 0 }
        }
        
        return sampledData
    }
    
    /**
     * 准备参数数据点
     */
    private fun prepareParameterEntries(histories: List<DeviceHistoryEntity>, parameter: ParameterType): List<Entry> {
        val entries = mutableListOf<Entry>()
        
        // 按时间排序
        val sortedHistories = histories.sortedBy { it.timestamp }
        
        // 数据采样，减少绘制的数据点数量
        val sampledHistories = sampleDataPoints(sortedHistories)
        
        // 创建数据点
        for ((index, history) in sampledHistories.withIndex()) {
            val value = when (parameter) {
                ParameterType.TEMPERATURE -> history.temperature
                ParameterType.HUMIDITY -> history.humidity
                ParameterType.OXYGEN_LEVEL -> history.oxygenLevel
            }
            entries.add(Entry(index.toFloat(), value.toFloat()))
        }
        
        return entries
    }
    
    /**
     * 创建折线图数据集
     */
    private fun createLineDataSet(entries: List<Entry>, label: String, color: Int): LineDataSet {
        val dataSet = LineDataSet(entries, label)
        
        // 设置折线属性，优化性能
        dataSet.color = color
        dataSet.lineWidth = 1.5f // 减小线宽
        dataSet.setDrawCircles(false) // 禁用点的绘制，大幅提高性能
        dataSet.setDrawCircleHole(false)
        dataSet.setDrawValues(false) // 禁用值标签绘制
        
        // 仅在数据点较少时绘制点
        if (entries.size < 500) {
            dataSet.setDrawCircles(true)
            dataSet.circleRadius = 1.5f // 减小点半径
            dataSet.setCircleColor(color)
        }
        
        // 设置填充属性，优化性能
        dataSet.setDrawFilled(true)
        dataSet.fillColor = color
        dataSet.fillAlpha = 30 // 降低填充透明度
        dataSet.fillFormatter = IFillFormatter { _, _ -> dataSet.yMin }
        
        // 设置平滑曲线，但仅在数据点较少时使用
        dataSet.mode = if (entries.size < 1000) {
            LineDataSet.Mode.CUBIC_BEZIER
        } else {
            LineDataSet.Mode.LINEAR // 大数据量时使用直线，提高性能
        }
        
        return dataSet
    }
    
    /**
     * 创建状态分布图表
     * @param container 图表容器布局
     * @param statusDistribution 设备状态分布
     * @return 创建的柱状图
     */
    fun createStatusDistributionChart(
        container: LinearLayout,
        statusDistribution: Map<DeviceStatus, Int>
    ): LineChart {
        // 创建折线图实例（用于显示状态分布）
        val chart = LineChart(context)
        
        // 设置图表尺寸
        val params = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        chart.layoutParams = params
        
        // 配置图表基本属性
        configureChart(chart)
        
        // 准备图表数据
        val entries = mutableListOf<Entry>()
        val labels = mutableListOf<String>()
        
        for ((index, entry) in statusDistribution.entries.withIndex()) {
            entries.add(Entry(index.toFloat(), entry.value.toFloat()))
            labels.add(entry.key.displayName)
        }
        
        // 创建数据集
        val dataSet = LineDataSet(entries, "设备状态分布")
        dataSet.color = 0xFF9C27B0.toInt() // 紫色
        dataSet.lineWidth = 2f
        dataSet.setCircleColor(0xFF9C27B0.toInt())
        dataSet.circleRadius = 4f
        dataSet.setDrawCircleHole(false)
        dataSet.setDrawValues(true)
        
        // 创建图表数据
        val chartData = LineData(dataSet)
        chart.data = chartData
        
        // 设置X轴标签
        val xAxis = chart.xAxis
        xAxis.valueFormatter = IndexAxisValueFormatter(labels)
        xAxis.granularity = 1f
        
        // 设置图表标题
        val description = Description()
        description.text = "设备状态分布"
        chart.description = description
        
        // 更新图表
        chart.invalidate()
        
        // 添加到容器
        container.addView(chart)
        
        return chart
    }
    
    /**
     * 创建环境参数仪表盘图表
     * @param container 图表容器布局
     * @param histories 设备历史数据
     * @param parameter 参数类型（温度、湿度、氧气浓度）
     * @return 创建的雷达图（用于显示仪表盘）
     */
    fun createParameterGaugeChart(
        container: LinearLayout,
        histories: List<DeviceHistoryEntity>,
        parameter: ParameterType
    ): RadarChart {
        // 创建雷达图实例（用于模拟仪表盘效果）
        val chart = RadarChart(context)
        
        // 设置图表尺寸
        val params = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            500 // 设置固定高度以适应仪表盘
        )
        chart.layoutParams = params
        
        // 配置图表基本属性
        configureRadarChart(chart)
        
        // 获取最新数据点作为当前值
        val latestValue = histories.maxByOrNull { it.timestamp }?.let {
            when (parameter) {
                ParameterType.TEMPERATURE -> it.temperature
                ParameterType.HUMIDITY -> it.humidity
                ParameterType.OXYGEN_LEVEL -> it.oxygenLevel
            }
        } ?: 0.0
        
        // 准备图表数据
        val chartData = prepareRadarChartData(latestValue, parameter)
        chart.data = chartData
        
        // 设置图表标题
        val description = Description()
        description.text = "${parameter.getTitle()} - 当前值: ${latestValue}"
        chart.description = description
        
        // 更新图表
        chart.invalidate()
        
        // 添加到容器
        container.addView(chart)
        
        return chart
    }
    
    /**
     * 配置雷达图基本属性
     */
    private fun configureRadarChart(chart: RadarChart) {
        // 启用触摸手势
        chart.setTouchEnabled(true)
        
        // 配置雷达图属性
        chart.webColor = 0xFFE0E0E0.toInt()
        chart.webLineWidth = 1f
        chart.webAlpha = 100
        
        // 配置X轴（参数范围）
        val xAxis = chart.xAxis
        xAxis.textSize = 12f
        xAxis.xOffset = 0f
        xAxis.yOffset = 0f
        
        // 配置Y轴（参数值）
        val yAxis = chart.yAxis
        yAxis.textSize = 12f
        yAxis.axisMinimum = 0f
        yAxis.axisMaximum = 100f
        yAxis.setDrawLabels(true)
        
        // 配置图例
        val legend = chart.legend
        legend.isEnabled = true
        legend.textSize = 12f
        legend.verticalAlignment = Legend.LegendVerticalAlignment.BOTTOM
        legend.horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
        legend.orientation = Legend.LegendOrientation.HORIZONTAL
        legend.setDrawInside(false)
        
        // 配置描述
        val description = Description()
        description.textSize = 12f
        chart.description = description
    }
    
    /**
     * 准备雷达图数据
     */
    private fun prepareRadarChartData(currentValue: Double, parameter: ParameterType): RadarData {
        // 根据参数类型设置不同的范围和区间
        val ranges = when (parameter) {
            ParameterType.TEMPERATURE -> arrayOf("0°C", "10°C", "20°C", "30°C", "40°C", "50°C")
            ParameterType.HUMIDITY -> arrayOf("0%", "20%", "40%", "60%", "80%", "100%")
            ParameterType.OXYGEN_LEVEL -> arrayOf("0%", "20%", "40%", "60%", "80%", "100%")
        }
        
        // 设置X轴标签
        val xAxis = mutableListOf<String>()
        for (i in ranges.indices) {
            xAxis.add(ranges[i])
        }
        
        // 创建数据集
        val entries = mutableListOf<RadarEntry>()
        val maxValue = when (parameter) {
            ParameterType.TEMPERATURE -> 50.0
            ParameterType.HUMIDITY -> 100.0
            ParameterType.OXYGEN_LEVEL -> 100.0
        }
        
        // 根据当前值创建雷达图数据点
        for (i in ranges.indices) {
            val value = if (i == ranges.size - 1) currentValue / maxValue * 100 else (i * 100.0 / ranges.size)
            entries.add(RadarEntry(value.toFloat()))
        }
        
        // 创建数据集
        val dataSet = RadarDataSet(entries, "当前值")
        dataSet.color = parameter.getColor()
        dataSet.lineWidth = 2f
        dataSet.setDrawFilled(true)
        dataSet.fillColor = parameter.getColor()
        dataSet.fillAlpha = 50
        dataSet.setDrawValues(true)
        
        // 创建雷达图数据
        return RadarData(listOf<IRadarDataSet>(dataSet))
    }
    
    /**
     * 清除图表
     */
    fun clearChart(container: LinearLayout) {
        container.removeAllViews()
    }
    
    /**
     * 参数类型枚举
     */
    enum class ParameterType {
        TEMPERATURE, HUMIDITY, OXYGEN_LEVEL;
        
        fun getTitle(): String {
            return when (this) {
                TEMPERATURE -> "温度趋势 (°C)"
                HUMIDITY -> "湿度趋势 (%)"
                OXYGEN_LEVEL -> "氧气浓度趋势 (%)"
            }
        }
        
        fun getColor(): Int {
            return when (this) {
                TEMPERATURE -> 0xFFFF5722.toInt() // 橙色
                HUMIDITY -> 0xFF2196F3.toInt()    // 蓝色
                OXYGEN_LEVEL -> 0xFF4CAF50.toInt() // 绿色
            }
        }
        
        fun getUnit(): String {
            return when (this) {
                TEMPERATURE -> "°C"
                HUMIDITY -> "%"
                OXYGEN_LEVEL -> "%"
            }
        }
    }
    
    /**
     * 创建自定义的时间格式化器
     */
    private class TimeValueFormatter(private val timestamps: List<Long>) : ValueFormatter() {
        private val dateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        
        override fun getFormattedValue(value: Float): String {
            val index = value.toInt()
            return if (index >= 0 && index < timestamps.size) {
                dateFormat.format(Date(timestamps[index]))
            } else {
                ""
            }
        }
    }
}