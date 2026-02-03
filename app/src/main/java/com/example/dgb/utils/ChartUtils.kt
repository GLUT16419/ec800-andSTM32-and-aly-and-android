package com.example.dgb.utils

import android.content.Context
import android.view.ViewGroup
import android.widget.LinearLayout
import com.example.dgb.DeviceStatus
import com.example.dgb.data.DeviceHistoryEntity
import com.github.mikephil.charting.charts.*
import com.github.mikephil.charting.components.*
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IFillFormatter
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.github.mikephil.charting.interfaces.datasets.IRadarDataSet
import com.github.mikephil.charting.utils.ColorTemplate
import java.text.SimpleDateFormat
import java.util.*

/**
 * 统一的图表工具类，整合所有图表相关功能
 */
object ChartUtils {
    // 时间格式化器
    private val dateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    private val shortDateFormat = SimpleDateFormat("MM-dd HH:mm", Locale.getDefault())
    private val fullDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

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

    // region 图表配置方法

    /**
     * 配置折线图基础样式
     */
    fun configureLineChart(chart: LineChart) {
        // 启用触摸手势
        chart.setTouchEnabled(true)
        chart.isDragEnabled = true
        chart.setScaleEnabled(true)
        chart.setDrawGridBackground(false)
        chart.isDoubleTapToZoomEnabled = true
        chart.setPinchZoom(true)

        // 启用描述文本
        chart.description.isEnabled = true
        chart.description.textSize = 12f
        chart.description.textColor = 0xFF666666.toInt()

        // 配置X轴
        val xAxis = chart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.setDrawGridLines(false)
        xAxis.setDrawAxisLine(true)
        xAxis.textSize = 10f
        xAxis.textColor = 0xFF666666.toInt()
        xAxis.granularity = 1f
        xAxis.labelRotationAngle = 45f

        // 配置Y轴
        val leftAxis = chart.axisLeft
        leftAxis.textSize = 10f
        leftAxis.textColor = 0xFF666666.toInt()
        leftAxis.setDrawGridLines(true)
        leftAxis.gridColor = 0xFFE0E0E0.toInt()
        leftAxis.gridLineWidth = 0.5f

        val rightAxis = chart.axisRight
        rightAxis.isEnabled = false

        // 配置图例
        val legend = chart.legend
        legend.form = Legend.LegendForm.LINE
        legend.textSize = 11f
        legend.verticalAlignment = Legend.LegendVerticalAlignment.TOP
        legend.horizontalAlignment = Legend.LegendHorizontalAlignment.RIGHT
        legend.orientation = Legend.LegendOrientation.VERTICAL
        legend.setDrawInside(false)

        // 配置动画
        chart.animateXY(1000, 1000)
    }

    /**
     * 配置柱状图基础样式
     */
    fun configureBarChart(chart: BarChart) {
        // 启用描述文本
        chart.description.isEnabled = true
        chart.description.textSize = 12f
        chart.description.textColor = 0xFF666666.toInt()

        // 启用触摸手势
        chart.setTouchEnabled(true)
        // 启用缩放
        chart.setScaleEnabled(true)
        // 启用拖拽
        chart.setDragEnabled(true)
        // 启用双击缩放
        chart.setDoubleTapToZoomEnabled(true)

        // 配置X轴
        val xAxis = chart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.textSize = 10f
        xAxis.textColor = 0xFF666666.toInt()
        xAxis.granularity = 1f // 最小间隔为1
        xAxis.labelCount = 6 // 显示6个标签

        // 配置Y轴
        val leftAxis = chart.axisLeft
        leftAxis.textSize = 10f
        leftAxis.textColor = 0xFF666666.toInt()
        leftAxis.setDrawGridLines(true)
        leftAxis.gridColor = 0xFFE0E0E0.toInt()

        val rightAxis = chart.axisRight
        rightAxis.isEnabled = false

        // 配置图例
        val legend = chart.legend
        legend.form = Legend.LegendForm.SQUARE
        legend.textSize = 11f
        legend.verticalAlignment = Legend.LegendVerticalAlignment.TOP
        legend.horizontalAlignment = Legend.LegendHorizontalAlignment.RIGHT
        legend.orientation = Legend.LegendOrientation.VERTICAL
        legend.setDrawInside(false)

        // 配置动画
        chart.animateY(1000)
    }

    /**
     * 配置饼图基础样式
     */
    fun configurePieChart(chart: PieChart) {
        // 启用描述文本
        chart.description.isEnabled = true
        chart.description.textSize = 12f
        chart.description.textColor = 0xFF666666.toInt()

        // 启用触摸手势
        chart.setTouchEnabled(true)

        // 配置中心文本
        chart.setDrawCenterText(true)
        chart.setCenterTextSize(14f)
        chart.setCenterTextColor(0xFF000000.toInt())

        // 配置图例
        val legend = chart.legend
        legend.form = Legend.LegendForm.CIRCLE
        legend.textSize = 11f
        legend.verticalAlignment = Legend.LegendVerticalAlignment.BOTTOM
        legend.horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
        legend.orientation = Legend.LegendOrientation.HORIZONTAL
        legend.setDrawInside(false)
        legend.formSize = 12f
        legend.formToTextSpace = 4f
        legend.xEntrySpace = 10f

        // 配置动画
        chart.animateY(1000)
    }

    /**
     * 配置雷达图基本属性
     */
    fun configureRadarChart(chart: RadarChart) {
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

    // endregion

    // region 图表数据创建方法

    /**
     * 创建环境参数趋势图
     * @param context 上下文
     * @param container 图表容器布局
     * @param histories 设备历史数据
     * @param parameter 参数类型（温度、湿度、氧气浓度）
     * @return 创建的折线图
     */
    fun createParameterTrendChart(
        context: Context,
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
        configureLineChart(chart)

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
     * @param context 上下文
     * @param container 图表容器布局
     * @param histories 设备历史数据
     * @return 创建的折线图
     */
    fun createMultiParameterChart(
        context: Context,
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
        configureLineChart(chart)

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
     * @param context 上下文
     * @param container 图表容器布局
     * @param deviceDataMap 设备数据映射，key为设备ID，value为该设备的历史数据
     * @param parameterType 参数类型
     * @return 创建的折线图
     */
    fun createMultiDeviceParameterChart(
        context: Context,
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
        configureLineChart(chart)

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
     * 创建环境参数仪表盘图表
     * @param context 上下文
     * @param container 图表容器布局
     * @param histories 设备历史数据
     * @param parameter 参数类型（温度、湿度、氧气浓度）
     * @return 创建的雷达图（用于显示仪表盘）
     */
    fun createParameterGaugeChart(
        context: Context,
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
     * 创建温度折线图数据
     */
    fun createTemperatureLineData(timeLabels: List<String>, temperatures: List<Double>): LineData {
        val entries = mutableListOf<Entry>()

        for (i in temperatures.indices) {
            entries.add(Entry(i.toFloat(), temperatures[i].toFloat()))
        }

        val dataSet = LineDataSet(entries, "温度 (°C)")
        dataSet.color = 0xFFFF5722.toInt()
        dataSet.lineWidth = 2f
        dataSet.circleRadius = 4f
        dataSet.circleHoleRadius = 2f
        dataSet.setCircleColor(0xFFFF5722.toInt())
        dataSet.setCircleHoleColor(0xFFFFFFFF.toInt())
        dataSet.valueTextSize = 9f
        dataSet.valueTextColor = 0xFFFF5722.toInt()
        dataSet.setDrawValues(false) // 不显示每个点的值

        return LineData(dataSet)
    }

    /**
     * 创建湿度折线图数据
     */
    fun createHumidityLineData(timeLabels: List<String>, humidities: List<Double>): LineData {
        val entries = mutableListOf<Entry>()

        for (i in humidities.indices) {
            entries.add(Entry(i.toFloat(), humidities[i].toFloat()))
        }

        val dataSet = LineDataSet(entries, "湿度 (%)")
        dataSet.color = 0xFF2196F3.toInt()
        dataSet.lineWidth = 2f
        dataSet.circleRadius = 4f
        dataSet.circleHoleRadius = 2f
        dataSet.setCircleColor(0xFF2196F3.toInt())
        dataSet.setCircleHoleColor(0xFFFFFFFF.toInt())
        dataSet.valueTextSize = 9f
        dataSet.valueTextColor = 0xFF2196F3.toInt()
        dataSet.setDrawValues(false) // 不显示每个点的值

        return LineData(dataSet)
    }

    /**
     * 创建温度和湿度双折线图数据
     */
    fun createTemperatureHumidityLineData(
        timeLabels: List<String>,
        temperatures: List<Double>,
        humidities: List<Double>
    ): LineData {
        val temperatureEntries = mutableListOf<Entry>()
        val humidityEntries = mutableListOf<Entry>()

        for (i in temperatures.indices) {
            temperatureEntries.add(Entry(i.toFloat(), temperatures[i].toFloat()))
            if (i < humidities.size) {
                humidityEntries.add(Entry(i.toFloat(), humidities[i].toFloat()))
            }
        }

        val temperatureSet = LineDataSet(temperatureEntries, "温度 (°C)")
        temperatureSet.color = 0xFFFF5722.toInt()
        temperatureSet.lineWidth = 2f
        temperatureSet.circleRadius = 4f
        temperatureSet.circleHoleRadius = 2f
        temperatureSet.setCircleColor(0xFFFF5722.toInt())
        temperatureSet.setCircleHoleColor(0xFFFFFFFF.toInt())
        temperatureSet.valueTextSize = 9f
        temperatureSet.valueTextColor = 0xFFFF5722.toInt()
        temperatureSet.setDrawValues(false) // 不显示每个点的值

        val humiditySet = LineDataSet(humidityEntries, "湿度 (%)")
        humiditySet.color = 0xFF2196F3.toInt()
        humiditySet.lineWidth = 2f
        humiditySet.circleRadius = 4f
        humiditySet.circleHoleRadius = 2f
        humiditySet.setCircleColor(0xFF2196F3.toInt())
        humiditySet.setCircleHoleColor(0xFFFFFFFF.toInt())
        humiditySet.valueTextSize = 9f
        humiditySet.valueTextColor = 0xFF2196F3.toInt()
        humiditySet.setDrawValues(false) // 不显示每个点的值

        return LineData(temperatureSet, humiditySet)
    }

    /**
     * 创建氧气浓度柱状图数据
     */
    fun createOxygenBarData(labels: List<String>, oxygenLevels: List<Double>): BarData {
        val entries = mutableListOf<BarEntry>()

        for (i in oxygenLevels.indices) {
            entries.add(BarEntry(i.toFloat(), oxygenLevels[i].toFloat()))
        }

        val dataSet = BarDataSet(entries, "氧气浓度 (%)")
        dataSet.color = 0xFF4CAF50.toInt()
        dataSet.valueTextSize = 10f
        dataSet.valueTextColor = 0xFF4CAF50.toInt()

        return BarData(dataSet)
    }

    /**
     * 创建状态分布饼图数据
     */
    fun createStatusPieData(statusNames: List<String>, counts: List<Int>): PieData {
        val entries = mutableListOf<PieEntry>()

        for (i in counts.indices) {
            entries.add(PieEntry(counts[i].toFloat(), statusNames[i]))
        }

        val dataSet = PieDataSet(entries, "设备状态分布")
        // 设置饼图颜色
        dataSet.colors = listOf(0xFF4CAF50.toInt(), 0xFFFFC107.toInt(), 0xFFF44336.toInt())
        // 设置选中的偏移量
        dataSet.selectionShift = 5f
        // 设置标签文本
        dataSet.valueTextSize = 12f
        dataSet.valueTextColor = 0xFFFFFFFF.toInt()

        return PieData(dataSet)
    }

    /**
     * 创建状态分布图表
     * @param context 上下文
     * @param container 图表容器布局
     * @param statusDistribution 设备状态分布
     * @return 创建的柱状图
     */
    fun createStatusDistributionChart(
        context: Context,
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
        configureLineChart(chart)

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

    // endregion

    // region 辅助方法

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
     * 准备参数数据点
     */
    private fun prepareParameterEntries(histories: List<DeviceHistoryEntity>, parameter: ParameterType): List<Entry> {
        val entries = mutableListOf<Entry>()

        // 按时间排序
        val sortedHistories = histories.sortedBy { it.timestamp }

        // 创建数据点
        for ((index, history) in sortedHistories.withIndex()) {
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

        // 设置折线属性
        dataSet.color = color
        dataSet.lineWidth = 2f
        dataSet.setCircleColor(color)
        dataSet.circleRadius = 3f
        dataSet.setDrawCircleHole(false)
        dataSet.setDrawValues(false)

        // 设置填充属性
        dataSet.setDrawFilled(true)
        dataSet.fillColor = color
        dataSet.fillAlpha = 50
        dataSet.fillFormatter = IFillFormatter { _, _ -> dataSet.yMin }

        // 设置平滑曲线
        dataSet.mode = LineDataSet.Mode.CUBIC_BEZIER

        return dataSet
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
     * 设置X轴的时间标签
     */
    fun setXAxisTimeLabels(chart: BarLineChartBase<*>, timeLabels: List<String>) {
        val xAxis = chart.xAxis
        xAxis.valueFormatter = IndexAxisValueFormatter(timeLabels)
        // 确保标签数量与数据点数量匹配
        xAxis.labelCount = timeLabels.size
        chart.invalidate() // 刷新图表
    }

    /**
     * 清除图表
     */
    fun clearChart(container: LinearLayout) {
        container.removeAllViews()
    }

    /**
     * 格式化时间
     */
    fun formatTime(timestamp: Long): String {
        return dateFormat.format(Date(timestamp))
    }

    /**
     * 格式化日期
     */
    fun formatDate(timestamp: Long): String {
        return shortDateFormat.format(Date(timestamp))
    }

    /**
     * 格式化完整日期时间
     */
    fun formatFullDateTime(timestamp: Long): String {
        return fullDateFormat.format(Date(timestamp))
    }

    /**
     * 创建自定义的时间格式化器
     */
    class TimeValueFormatter(private val timestamps: List<Long>) : ValueFormatter() {
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

    // endregion
}