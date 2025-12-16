package com.example.dgb.ui.charts

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.dgb.DeviceStatus
import com.example.dgb.R
import com.example.dgb.data.DeviceHistoryViewModel
import com.example.dgb.data.DeviceHistoryViewModelFactory
import com.example.dgb.data.StatisticsAnalyzer
import com.github.mikephil.charting.charts.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

// 历史数据图表展示Fragment
class HistoryChartFragment : Fragment() {
    
    // 定义图表类型枚举
    enum class ChartType {
        TEMPERATURE,
        HUMIDITY,
        OXYGEN,
        STATUS_DISTRIBUTION
    }
    
    // 定义时间范围枚举
    enum class TimeRange {
        ONE_HOUR,
        SIX_HOURS,
        TWELVE_HOURS,
        ONE_DAY,
        THREE_DAYS
    }
    
    // 视图组件
    private lateinit var chartTypeSpinner: android.widget.Spinner
    private lateinit var timeRangeSpinner: android.widget.Spinner
    private lateinit var statisticsOverview: ViewGroup
    private lateinit var chartContainer: ViewGroup
    private lateinit var updateButton: android.widget.Button
    
    // 当前选中的设备ID
    private var deviceId: Long = 1L
    
    // 当前选中的图表类型和时间范围
    private var currentChartType: ChartType = ChartType.TEMPERATURE
    private var currentTimeRange: TimeRange = TimeRange.ONE_HOUR
    
    // ViewModel
    private lateinit var viewModel: DeviceHistoryViewModel
    
    // 统计分析器
    private val statisticsAnalyzer = StatisticsAnalyzer()
    
    // 时间格式化器
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_history_charts, container, false)
        
        // 初始化视图组件
        initViews(view)
        
        // 初始化ViewModel
        val factory = DeviceHistoryViewModelFactory(requireActivity().application)
        viewModel = ViewModelProvider(this, factory)[DeviceHistoryViewModel::class.java]
        
        // 初始化设备ID（从参数中获取或使用默认值）
        deviceId = arguments?.getLong("deviceId", 1L) ?: 1L
        
        // 设置事件监听器
        setupListeners()
        
        // 加载并显示数据
        loadAndDisplayData()
        
        return view
    }
    
    // 初始化视图组件
    private fun initViews(view: View) {
        chartTypeSpinner = view.findViewById(R.id.chart_type_spinner)
        timeRangeSpinner = view.findViewById(R.id.time_range_spinner)
        statisticsOverview = view.findViewById(R.id.statistics_overview)
        chartContainer = view.findViewById(R.id.chart_container)
        updateButton = view.findViewById(R.id.update_button)
        
        // 初始化图表类型Spinner
        val chartTypeAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            ChartType.values().map { it.name }
        )
        chartTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        chartTypeSpinner.adapter = chartTypeAdapter
        
        // 初始化时间范围Spinner
        val timeRangeAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            TimeRange.values().map { getTimeRangeDisplayText(it) }
        )
        timeRangeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        timeRangeSpinner.adapter = timeRangeAdapter
    }
    
    // 设置事件监听器
    private fun setupListeners() {
        // 图表类型选择监听
        chartTypeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?,
                position: Int,
                id: Long
            ) {
                currentChartType = ChartType.values()[position]
                loadAndDisplayData()
            }
            
            override fun onNothingSelected(parent: AdapterView<*>) {
                // 不处理
            }
        }
        
        // 时间范围选择监听
        timeRangeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?,
                position: Int,
                id: Long
            ) {
                currentTimeRange = TimeRange.values()[position]
                loadAndDisplayData()
            }
            
            override fun onNothingSelected(parent: AdapterView<*>) {
                // 不处理
            }
        }
        
        // 更新按钮点击监听
        updateButton.setOnClickListener {
            loadAndDisplayData()
        }
    }
    
    // 加载并显示数据
    private fun loadAndDisplayData() {
        // 计算时间范围
        val endTime = System.currentTimeMillis()
        val startTime = calculateStartTime(endTime, currentTimeRange)
        
        // 使用带时间范围的方法获取历史数据
        viewModel.getDeviceHistoriesInTimeRange(deviceId.toInt(), startTime, endTime) { histories ->
            // 更新统计概览
            updateStatisticsOverview(histories)
            
            // 根据选择的图表类型创建并显示图表
            displayChart(histories)
        }
    }
    
    // 更新统计概览
    private fun updateStatisticsOverview(histories: List<com.example.dgb.data.DeviceHistoryEntity>) {
        if (histories.isEmpty()) {
            return
        }
        
        // 计算统计数据
        val temperatures = histories.map { it.temperature }
        val humidities = histories.map { it.humidity }
        val oxygenLevels = histories.map { it.oxygenLevel }
        
        val tempStats = statisticsAnalyzer.calculateBasicStatistics(temperatures)
        val humidityStats = statisticsAnalyzer.calculateBasicStatistics(humidities)
        val oxygenStats = statisticsAnalyzer.calculateBasicStatistics(oxygenLevels)
        
        val statusDistribution = statisticsAnalyzer.calculateStatusDistribution(histories)
        val statusDurations = statisticsAnalyzer.calculateStatusDurations(histories)
        
        val timeRange = statisticsAnalyzer.getTimeRange(histories)
        
        // 在主线程更新UI
        GlobalScope.launch(Dispatchers.Main) {
            // 清空现有内容
            statisticsOverview.removeAllViews()
            
            // 添加统计信息卡片
            addStatisticCard("平均温度", "%.2f°C".format(tempStats.average))
            addStatisticCard("最高温度", "%.2f°C".format(tempStats.max))
            addStatisticCard("最低温度", "%.2f°C".format(tempStats.min))
            addStatisticCard("平均湿度", "%.2f%%".format(humidityStats.average))
            addStatisticCard("平均氧气浓度", "%.2f%%".format(oxygenStats.average))
            addStatisticCard("数据点数量", histories.size.toString())
            addStatisticCard("开始时间", dateFormat.format(Date(timeRange.startTime)))
            addStatisticCard("结束时间", dateFormat.format(Date(timeRange.endTime)))
        }
    }
    
    // 添加统计信息卡片
    private fun addStatisticCard(title: String, value: String) {
        val cardView = LayoutInflater.from(requireContext())
            .inflate(R.layout.item_statistics_card, statisticsOverview, false)
        
        cardView.findViewById<TextView>(R.id.statistic_title).text = title
        cardView.findViewById<TextView>(R.id.statistic_value).text = value
        
        statisticsOverview.addView(cardView)
    }
    
    // 根据选择的图表类型显示图表
    private fun displayChart(histories: List<com.example.dgb.data.DeviceHistoryEntity>) {
        if (histories.isEmpty()) {
            showNoDataMessage()
            return
        }
        
        // 清空现有图表
        chartContainer.removeAllViews()
        
        when (currentChartType) {
            ChartType.TEMPERATURE -> displayTemperatureChart(histories)
            ChartType.HUMIDITY -> displayHumidityChart(histories)
            ChartType.OXYGEN -> displayOxygenChart(histories)
            ChartType.STATUS_DISTRIBUTION -> displayStatusDistributionChart(histories)
        }
    }
    
    // 显示温度图表
    private fun displayTemperatureChart(histories: List<com.example.dgb.data.DeviceHistoryEntity>) {
        // 创建折线图
        val chart = LineChart(requireContext())
        val layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            400 // 高度为400dp
        )
        chart.layoutParams = layoutParams
        
        // 配置图表
        ChartUtils.configureLineChart(chart)
        chart.description.text = "历史温度变化"
        
        // 准备数据
        val timeLabels = histories.map { ChartUtils.formatTime(it.timestamp) }
        val temperatures = histories.map { it.temperature }
        
        // 创建数据
        val lineData = ChartUtils.createTemperatureLineData(timeLabels, temperatures)
        
        // 设置数据
        chart.data = lineData
        
        // 设置X轴标签
        ChartUtils.setXAxisTimeLabels(chart, timeLabels)
        
        // 添加到容器
        chartContainer.addView(chart)
    }
    
    // 显示湿度图表
    private fun displayHumidityChart(histories: List<com.example.dgb.data.DeviceHistoryEntity>) {
        // 创建折线图
        val chart = LineChart(requireContext())
        val layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            400 // 高度为400dp
        )
        chart.layoutParams = layoutParams
        
        // 配置图表
        ChartUtils.configureLineChart(chart)
        chart.description.text = "历史湿度变化"
        
        // 准备数据
        val timeLabels = histories.map { ChartUtils.formatTime(it.timestamp) }
        val humidities = histories.map { it.humidity }
        
        // 创建数据
        val lineData = ChartUtils.createHumidityLineData(timeLabels, humidities)
        
        // 设置数据
        chart.data = lineData
        
        // 设置X轴标签
        ChartUtils.setXAxisTimeLabels(chart, timeLabels)
        
        // 添加到容器
        chartContainer.addView(chart)
    }
    
    // 显示氧气浓度图表
    private fun displayOxygenChart(histories: List<com.example.dgb.data.DeviceHistoryEntity>) {
        // 创建柱状图
        val chart = BarChart(requireContext())
        val layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            400 // 高度为400dp
        )
        chart.layoutParams = layoutParams
        
        // 配置图表
        ChartUtils.configureBarChart(chart)
        chart.description.text = "历史氧气浓度变化"
        
        // 准备数据
        val timeLabels = histories.map { ChartUtils.formatTime(it.timestamp) }
        val oxygenLevels = histories.map { it.oxygenLevel }
        
        // 创建数据
        val barData = ChartUtils.createOxygenBarData(timeLabels, oxygenLevels)
        
        // 设置数据
        chart.data = barData
        
        // 设置X轴标签
        ChartUtils.setXAxisTimeLabels(chart, timeLabels)
        
        // 添加到容器
        chartContainer.addView(chart)
    }
    
    // 显示状态分布图表
    private fun displayStatusDistributionChart(histories: List<com.example.dgb.data.DeviceHistoryEntity>) {
        // 创建饼图
        val chart = PieChart(requireContext())
        val layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            400 // 高度为400dp
        )
        chart.layoutParams = layoutParams
        
        // 配置图表
        ChartUtils.configurePieChart(chart)
        chart.description.text = "设备状态分布"
        
        // 准备数据
        val statusDistribution = statisticsAnalyzer.calculateStatusDistribution(histories)
        val statusNames = statusDistribution.keys.map { it.name }
        val counts = statusDistribution.values.map { it }
        
        // 创建数据
        val pieData = ChartUtils.createStatusPieData(statusNames, counts)
        
        // 设置数据
        chart.data = pieData
        
        // 添加到容器
        chartContainer.addView(chart)
    }
    
    // 显示无数据提示
    private fun showNoDataMessage() {
        // 清空现有图表
        chartContainer.removeAllViews()
        
        val textView = TextView(requireContext())
        textView.text = "没有找到历史数据"
        textView.textSize = 18f
        textView.textAlignment = View.TEXT_ALIGNMENT_CENTER
        textView.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        
        chartContainer.addView(textView)
    }
    
    // 计算开始时间
    private fun calculateStartTime(endTime: Long, timeRange: TimeRange): Long {
        return when (timeRange) {
            TimeRange.ONE_HOUR -> endTime - 60 * 60 * 1000 // 1小时
            TimeRange.SIX_HOURS -> endTime - 6 * 60 * 60 * 1000 // 6小时
            TimeRange.TWELVE_HOURS -> endTime - 12 * 60 * 60 * 1000 // 12小时
            TimeRange.ONE_DAY -> endTime - 24 * 60 * 60 * 1000 // 1天
            TimeRange.THREE_DAYS -> endTime - 3 * 24 * 60 * 60 * 1000 // 3天
        }
    }
    
    // 获取时间范围的显示文本
    private fun getTimeRangeDisplayText(timeRange: TimeRange): String {
        return when (timeRange) {
            TimeRange.ONE_HOUR -> "近1小时"
            TimeRange.SIX_HOURS -> "近6小时"
            TimeRange.TWELVE_HOURS -> "近12小时"
            TimeRange.ONE_DAY -> "近1天"
            TimeRange.THREE_DAYS -> "近3天"
        }
    }
    
    companion object {
        // 创建实例的静态方法
        fun newInstance(deviceId: Long): HistoryChartFragment {
            val fragment = HistoryChartFragment()
            val args = Bundle()
            args.putLong("deviceId", deviceId)
            fragment.arguments = args
            return fragment
        }
    }
}
