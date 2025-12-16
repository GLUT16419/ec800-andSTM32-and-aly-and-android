package com.example.dgb.ui.charts

import android.content.Context
import android.graphics.Color
import com.github.mikephil.charting.charts.*
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.github.mikephil.charting.utils.ColorTemplate
import java.text.SimpleDateFormat
import java.util.*

// 图表工具类，封装MPAndroidChart库的配置和数据处理方法
object ChartUtils {
    // 时间格式化器
    private val dateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    
    // 配置折线图基础样式
    fun configureLineChart(chart: LineChart) {
        // 启用描述文本
        chart.description.isEnabled = true
        chart.description.textSize = 12f
        chart.description.textColor = Color.GRAY
        
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
        xAxis.textColor = Color.GRAY
        xAxis.granularity = 1f // 最小间隔为1
        xAxis.labelCount = 6 // 显示6个标签
        
        // 配置Y轴
        val leftAxis = chart.axisLeft
        leftAxis.textSize = 10f
        leftAxis.textColor = Color.GRAY
        leftAxis.setDrawGridLines(true)
        leftAxis.gridColor = Color.LTGRAY
        
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
    
    // 配置柱状图基础样式
    fun configureBarChart(chart: BarChart) {
        // 启用描述文本
        chart.description.isEnabled = true
        chart.description.textSize = 12f
        chart.description.textColor = Color.GRAY
        
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
        xAxis.textColor = Color.GRAY
        xAxis.granularity = 1f // 最小间隔为1
        xAxis.labelCount = 6 // 显示6个标签
        
        // 配置Y轴
        val leftAxis = chart.axisLeft
        leftAxis.textSize = 10f
        leftAxis.textColor = Color.GRAY
        leftAxis.setDrawGridLines(true)
        leftAxis.gridColor = Color.LTGRAY
        
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
    
    // 配置饼图基础样式
    fun configurePieChart(chart: PieChart) {
        // 启用描述文本
        chart.description.isEnabled = true
        chart.description.textSize = 12f
        chart.description.textColor = Color.GRAY
        
        // 启用触摸手势
        chart.setTouchEnabled(true)
        
        // 配置中心文本
        chart.setDrawCenterText(true)
        chart.setCenterTextSize(14f)
        chart.setCenterTextColor(Color.BLACK)
        
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
    
    // 创建温度折线图数据
    fun createTemperatureLineData(timeLabels: List<String>, temperatures: List<Double>): LineData {
        val entries = mutableListOf<Entry>()
        
        for (i in temperatures.indices) {
            entries.add(Entry(i.toFloat(), temperatures[i].toFloat()))
        }
        
        val dataSet = LineDataSet(entries, "温度 (°C)")
        dataSet.color = Color.RED
        dataSet.lineWidth = 2f
        dataSet.circleRadius = 4f
        dataSet.circleHoleRadius = 2f
        dataSet.setCircleColor(Color.RED)
        dataSet.setCircleHoleColor(Color.WHITE)
        dataSet.valueTextSize = 9f
        dataSet.valueTextColor = Color.RED
        dataSet.setDrawValues(false) // 不显示每个点的值
        
        return LineData(dataSet)
    }
    
    // 创建湿度折线图数据
    fun createHumidityLineData(timeLabels: List<String>, humidities: List<Double>): LineData {
        val entries = mutableListOf<Entry>()
        
        for (i in humidities.indices) {
            entries.add(Entry(i.toFloat(), humidities[i].toFloat()))
        }
        
        val dataSet = LineDataSet(entries, "湿度 (%)")
        dataSet.color = Color.BLUE
        dataSet.lineWidth = 2f
        dataSet.circleRadius = 4f
        dataSet.circleHoleRadius = 2f
        dataSet.setCircleColor(Color.BLUE)
        dataSet.setCircleHoleColor(Color.WHITE)
        dataSet.valueTextSize = 9f
        dataSet.valueTextColor = Color.BLUE
        dataSet.setDrawValues(false) // 不显示每个点的值
        
        return LineData(dataSet)
    }
    
    // 创建温度和湿度双折线图数据
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
        temperatureSet.color = Color.RED
        temperatureSet.lineWidth = 2f
        temperatureSet.circleRadius = 4f
        temperatureSet.circleHoleRadius = 2f
        temperatureSet.setCircleColor(Color.RED)
        temperatureSet.setCircleHoleColor(Color.WHITE)
        temperatureSet.valueTextSize = 9f
        temperatureSet.valueTextColor = Color.RED
        temperatureSet.setDrawValues(false) // 不显示每个点的值
        
        val humiditySet = LineDataSet(humidityEntries, "湿度 (%)")
        humiditySet.color = Color.BLUE
        humiditySet.lineWidth = 2f
        humiditySet.circleRadius = 4f
        humiditySet.circleHoleRadius = 2f
        humiditySet.setCircleColor(Color.BLUE)
        humiditySet.setCircleHoleColor(Color.WHITE)
        humiditySet.valueTextSize = 9f
        humiditySet.valueTextColor = Color.BLUE
        humiditySet.setDrawValues(false) // 不显示每个点的值
        
        return LineData(temperatureSet, humiditySet)
    }
    
    // 创建氧气浓度柱状图数据
    fun createOxygenBarData(labels: List<String>, oxygenLevels: List<Double>): BarData {
        val entries = mutableListOf<BarEntry>()
        
        for (i in oxygenLevels.indices) {
            entries.add(BarEntry(i.toFloat(), oxygenLevels[i].toFloat()))
        }
        
        val dataSet = BarDataSet(entries, "氧气浓度 (%)")
        dataSet.color = Color.GREEN
        dataSet.valueTextSize = 10f
        dataSet.valueTextColor = Color.GREEN
        
        return BarData(dataSet)
    }
    
    // 创建状态分布饼图数据
    fun createStatusPieData(statusNames: List<String>, counts: List<Int>): PieData {
        val entries = mutableListOf<PieEntry>()
        
        for (i in counts.indices) {
            entries.add(PieEntry(counts[i].toFloat(), statusNames[i]))
        }
        
        val dataSet = PieDataSet(entries, "设备状态分布")
        // 设置饼图颜色
        dataSet.colors = listOf(Color.GREEN, Color.YELLOW, Color.RED)
        // 设置选中的偏移量
        dataSet.selectionShift = 5f
        // 设置标签文本
        dataSet.valueTextSize = 12f
        dataSet.valueTextColor = Color.WHITE
        
        return PieData(dataSet)
    }
    
    // 设置X轴的时间标签
    fun setXAxisTimeLabels(chart: BarLineChartBase<*>, timeLabels: List<String>) {
        val xAxis = chart.xAxis
        xAxis.valueFormatter = IndexAxisValueFormatter(timeLabels)
        // 确保标签数量与数据点数量匹配
        xAxis.labelCount = timeLabels.size
        chart.invalidate() // 刷新图表
    }
    
    // 格式化时间戳为小时:分钟格式
    fun formatTime(timestamp: Long): String {
        return dateFormat.format(Date(timestamp))
    }
}
