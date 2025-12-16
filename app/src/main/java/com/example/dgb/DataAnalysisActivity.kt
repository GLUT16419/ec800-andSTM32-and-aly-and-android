package com.example.dgb

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModelProvider
import com.example.dgb.data.*
import com.github.mikephil.charting.charts.LineChart
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

// 数据报表与分析页面
class DataAnalysisActivity : AppCompatActivity() {
    
    // 视图组件
    private lateinit var deviceSpinner: Spinner
    private lateinit var timeRangeSpinner: Spinner
    private lateinit var parameterSpinner: Spinner
    private lateinit var chartContainer: LinearLayout
    private lateinit var statisticsContainer: LinearLayout
    private lateinit var generateReportBtn: Button
    private lateinit var exportCsvBtn: Button
    private lateinit var multiDeviceContainer: LinearLayout
    private lateinit var deviceListContainer: LinearLayout
    private lateinit var compareDevicesBtn: Button
    
    // 数据和工具
    private lateinit var historyViewModel: DeviceHistoryViewModel
    private lateinit var chartUtils: ChartUtils
    private lateinit var reportGenerator: ReportGenerator
    
    // 当前显示的图表
    private var currentChart: LineChart? = null
    
    // 设备列表
    private var devices: List<DeviceEntity> = emptyList()
    private var selectedDeviceId: Int? = null
    private val selectedDevicesForComparison: MutableList<DeviceEntity> = mutableListOf()
    
    // 时间范围选项
    private val timeRangeOptions = arrayOf(
        "最近1小时",
        "最近6小时",
        "最近12小时",
        "最近24小时",
        "最近7天",
        "自定义范围"
    )
    
    // 参数类型选项
    private val parameterOptions = arrayOf(
        "温度",
        "湿度",
        "氧气浓度",
        "多参数对比",
        "多设备参数对比",
        "温度仪表盘",
        "湿度仪表盘",
        "氧气浓度仪表盘"
    )
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_data_analysis)
        
        // 初始化视图模型和工具
        initViewModelAndTools()
        
        // 初始化视图组件
        initViews()
        
        // 设置视图组件事件监听器
        setListeners()
        
        // 加载设备列表
        loadDevices()
        
        // 检查存储权限
        checkStoragePermission()
    }
    
    /**
     * 初始化视图模型和工具
     */
    private fun initViewModelAndTools() {
        // 初始化视图模型
        val factory = DeviceHistoryViewModelFactory(application)
        historyViewModel = ViewModelProvider(this, factory).get(DeviceHistoryViewModel::class.java)
        
        // 初始化图表工具和报表生成器
        chartUtils = ChartUtils(this)
        reportGenerator = ReportGenerator(this)
    }
    
    /**
     * 初始化视图组件
     */
    private fun initViews() {
        // 初始化Toolbar
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        
        // 设置标题栏
        supportActionBar?.title = "数据报表与分析"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        
        // 获取视图组件
        deviceSpinner = findViewById(R.id.device_spinner)
        timeRangeSpinner = findViewById(R.id.time_range_spinner)
        parameterSpinner = findViewById(R.id.parameter_spinner)
        chartContainer = findViewById(R.id.chart_container)
        statisticsContainer = findViewById(R.id.statistics_container)
        generateReportBtn = findViewById(R.id.generate_report_btn)
        exportCsvBtn = findViewById(R.id.export_csv_btn)
        
        // 设置Spinner适配器
        timeRangeSpinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, timeRangeOptions)
        parameterSpinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, parameterOptions)
        
        // 初始化多设备选择容器
        multiDeviceContainer = LinearLayout(this)
        multiDeviceContainer.orientation = LinearLayout.VERTICAL
        multiDeviceContainer.setPadding(16, 16, 16, 16)
        
        // 创建多设备列表容器
        deviceListContainer = LinearLayout(this)
        deviceListContainer.orientation = LinearLayout.VERTICAL
        deviceListContainer.setPadding(0, 8, 0, 16)
        
        // 创建比较按钮
        compareDevicesBtn = Button(this)
        compareDevicesBtn.text = "对比选中设备"
        compareDevicesBtn.isEnabled = false
        
        // 将组件添加到多设备容器
        val titleView = TextView(this)
        titleView.text = "选择要对比的设备："
        titleView.textSize = 16f
        titleView.setPadding(0, 0, 0, 8)
        
        multiDeviceContainer.addView(titleView)
        multiDeviceContainer.addView(deviceListContainer)
        multiDeviceContainer.addView(compareDevicesBtn)
    }
    
    /**
     * 设置视图组件事件监听器
     */
    private fun setListeners() {
        // 设备选择监听器
        deviceSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                if (position < devices.size) {
                    selectedDeviceId = devices[position].deviceId
                    loadDataAndUpdateUI()
                }
            }
            
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
        
        // 时间范围选择监听器
        timeRangeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                loadDataAndUpdateUI()
            }
            
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
        
        // 参数类型选择监听器
        parameterSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                loadDataAndUpdateUI()
            }
            
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
        
        // 生成报表按钮监听器
        generateReportBtn.setOnClickListener {
            generateReport()
        }
        
        // 导出CSV按钮监听器
        exportCsvBtn.setOnClickListener {
            exportToCsv()
        }
        
        // 比较按钮监听器
        compareDevicesBtn.setOnClickListener {
            loadMultiDeviceDataAndUpdateChart()
        }
    }
    
    /**
     * 加载设备列表
     */
    private fun loadDevices() {
        // 从数据库加载所有设备
        CoroutineScope(Dispatchers.IO).launch {
            val deviceDao = DeviceDatabase.getDatabase(applicationContext).deviceDao()
            val deviceFlow = deviceDao.getAllDevices()
            
            withContext(Dispatchers.Main) {
                deviceFlow.collect { deviceList ->
                    devices = deviceList
                    
                    if (devices.isNotEmpty()) {
                        // 设置设备适配器
                        val deviceNames = devices.map { it.deviceName ?: "未命名设备 (${it.deviceId})" }
                        deviceSpinner.adapter = ArrayAdapter(this@DataAnalysisActivity, android.R.layout.simple_spinner_item, deviceNames)
                        
                        // 默认选择第一个设备
                        selectedDeviceId = devices[0].deviceId
                        loadDataAndUpdateUI()
                        
                        // 更新多设备选择列表
                        updateDeviceSelectionList()
                    } else {
                        Toast.makeText(this@DataAnalysisActivity, "暂无设备数据", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }
    
    /**
     * 加载数据并更新UI
     */
    private fun loadDataAndUpdateUI() {
        val parameterPosition = parameterSpinner.selectedItemPosition
        
        // 如果是多设备对比模式，显示多设备选择界面
        if (parameterPosition == 4) {
            showMultiDeviceSelection()
            return
        } else {
            hideMultiDeviceSelection()
        }
        
        if (selectedDeviceId == null) return
        
        // 获取当前选择的时间范围
        val timeRangePosition = timeRangeSpinner.selectedItemPosition
        
        // 计算时间范围
        val timeRange = calculateTimeRange(timeRangePosition)
        
        // 加载历史数据
        historyViewModel.getDeviceHistoriesInTimeRange(selectedDeviceId!!, timeRange.startTime, timeRange.endTime) { histories ->
            if (histories.isNotEmpty()) {
                // 更新图表
                updateChart(histories, parameterPosition)
                
                // 更新统计信息
                updateStatistics(histories)
            } else {
                // 清空图表和统计信息
                chartUtils.clearChart(chartContainer)
                currentChart = null
                updateStatistics(emptyList())
                Toast.makeText(this, "该时间段内无数据", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    /**
     * 显示多设备选择界面
     */
    private fun showMultiDeviceSelection() {
        // 直接显示多设备选择容器
        multiDeviceContainer.visibility = View.VISIBLE
    }
    
    /**
     * 隐藏多设备选择界面
     */
    private fun hideMultiDeviceSelection() {
        // 目前不实现此功能，因为布局ID不存在
    }
    
    /**
     * 更新设备选择列表
     */
    private fun updateDeviceSelectionList() {
        // 清空设备列表容器
        deviceListContainer.removeAllViews()
        
        // 创建设备复选框列表
        for (device in devices) {
            val checkBox = CheckBox(this)
            checkBox.text = device.deviceName ?: "未命名设备 (${device.deviceId})"
            checkBox.tag = device
            
            // 设置点击事件
            checkBox.setOnCheckedChangeListener { buttonView, isChecked ->
                if (isChecked) {
                    selectedDevicesForComparison.add(device)
                } else {
                    selectedDevicesForComparison.remove(device)
                }
                
                // 更新比较按钮状态
                compareDevicesBtn.isEnabled = selectedDevicesForComparison.size >= 2
            }
            
            // 添加到容器
            deviceListContainer.addView(checkBox)
        }
    }
    
    /**
     * 加载多设备数据并更新图表
     */
    private fun loadMultiDeviceDataAndUpdateChart() {
        if (selectedDevicesForComparison.isEmpty()) {
            Toast.makeText(this, "请选择至少一个设备", Toast.LENGTH_SHORT).show()
            return
        }
        
        // 获取时间范围
        val timeRangePosition = timeRangeSpinner.selectedItemPosition
        val timeRange = calculateTimeRange(timeRangePosition)
        
        // 显示加载提示
        Toast.makeText(this, "正在加载设备数据...", Toast.LENGTH_SHORT).show()
        
        // 加载所有选中设备的数据
        val deviceDataMap = mutableMapOf<DeviceEntity, List<DeviceHistoryEntity>>()
        var devicesLoaded = 0
        
        for (device in selectedDevicesForComparison) {
            historyViewModel.getDeviceHistoriesInTimeRange(device.deviceId, timeRange.startTime, timeRange.endTime) { histories ->
                deviceDataMap[device] = histories
                devicesLoaded++
                
                // 当所有设备数据都加载完成后，更新图表
                if (devicesLoaded == selectedDevicesForComparison.size) {
                    updateMultiDeviceComparisonChart(deviceDataMap)
                }
            }
        }
    }
    
    /**
     * 计算时间范围
     */
    private fun calculateTimeRange(position: Int): TimeRange {
        val calendar = Calendar.getInstance()
        val endTime = calendar.timeInMillis
        
        // 根据选择的时间范围计算开始时间
        val startTime = when (position) {
            0 -> endTime - 3600 * 1000L // 最近1小时
            1 -> endTime - 6 * 3600 * 1000L // 最近6小时
            2 -> endTime - 12 * 3600 * 1000L // 最近12小时
            3 -> endTime - 24 * 3600 * 1000L // 最近24小时
            4 -> endTime - 7 * 24 * 3600 * 1000L // 最近7天
            else -> endTime - 24 * 3600 * 1000L // 默认24小时
        }
        
        return TimeRange(startTime, endTime)
    }
    
    /**
     * 更新图表
     */
    private fun updateChart(histories: List<DeviceHistoryEntity>, parameterPosition: Int) {
        // 清空容器
        chartUtils.clearChart(chartContainer)
        
        // 根据选择的参数类型创建图表
        when (parameterPosition) {
            0 -> chartUtils.createParameterTrendChart(chartContainer, histories, ChartUtils.ParameterType.TEMPERATURE)
            1 -> chartUtils.createParameterTrendChart(chartContainer, histories, ChartUtils.ParameterType.HUMIDITY)
            2 -> chartUtils.createParameterTrendChart(chartContainer, histories, ChartUtils.ParameterType.OXYGEN_LEVEL)
            3 -> chartUtils.createMultiParameterChart(chartContainer, histories)
            4 -> { /* 多设备对比模式，不在这里处理 */ }
            5 -> chartUtils.createParameterGaugeChart(chartContainer, histories, ChartUtils.ParameterType.TEMPERATURE)
            6 -> chartUtils.createParameterGaugeChart(chartContainer, histories, ChartUtils.ParameterType.HUMIDITY)
            7 -> chartUtils.createParameterGaugeChart(chartContainer, histories, ChartUtils.ParameterType.OXYGEN_LEVEL)
            else -> chartUtils.createParameterTrendChart(chartContainer, histories, ChartUtils.ParameterType.TEMPERATURE)
        }
    }
    
    /**
     * 更新多设备对比图表
     */
    private fun updateMultiDeviceComparisonChart(deviceDataMap: Map<DeviceEntity, List<DeviceHistoryEntity>>) {
        // 清空容器
        chartUtils.clearChart(chartContainer)
        
        // 检查是否有设备数据
        if (deviceDataMap.isEmpty()) {
            Toast.makeText(this, "无设备数据可对比", Toast.LENGTH_SHORT).show()
            return
        }
        
        // 获取当前选择的参数类型（温度、湿度、氧气浓度）
        val parameterType = when (timeRangeSpinner.selectedItemPosition % 3) {
            0 -> ChartUtils.ParameterType.TEMPERATURE
            1 -> ChartUtils.ParameterType.HUMIDITY
            2 -> ChartUtils.ParameterType.OXYGEN_LEVEL
            else -> ChartUtils.ParameterType.TEMPERATURE
        }
        
        // 创建多设备对比图表，将DeviceEntity映射转换为id映射
        val deviceIdDataMap = deviceDataMap.mapKeys { it.key.deviceId }
        chartUtils.createMultiDeviceParameterChart(chartContainer, deviceIdDataMap, parameterType)
        
        // 更新统计信息
        updateMultiDeviceStatistics(deviceDataMap)
    }
    
    /**
     * 更新多设备统计信息
     */
    private fun updateMultiDeviceStatistics(deviceDataMap: Map<DeviceEntity, List<DeviceHistoryEntity>>) {
        // 清空统计容器
        statisticsContainer.removeAllViews()
        
        if (deviceDataMap.isEmpty()) return
        
        // 创建统计分析器
        val analyzer = StatisticsAnalyzer()
        
        // 为每个设备创建统计信息
        for ((device, histories) in deviceDataMap) {
            if (histories.isEmpty()) continue
            
            // 基础统计信息
            val tempStats = analyzer.calculateBasicStatistics(histories.map { it.temperature.toDouble() })
            val humidityStats = analyzer.calculateBasicStatistics(histories.map { it.humidity.toDouble() })
            val oxygenStats = analyzer.calculateBasicStatistics(histories.map { it.oxygenLevel.toDouble() })
            
            // 状态分布和异常统计
            val statusDistribution = analyzer.calculateStatusDistribution(histories)
            val abnormalCount = (statusDistribution[DeviceStatus.WARNING] ?: 0) + (statusDistribution[DeviceStatus.ERROR] ?: 0)
            val abnormalRate = if (histories.isNotEmpty()) (abnormalCount.toDouble() / histories.size * 100) else 0.0
            
            // 创建设备统计标题
            val deviceTitle = TextView(this)
            deviceTitle.text = "设备 ${device.deviceName ?: "未命名设备"} 统计信息"
            deviceTitle.textSize = 18f
            deviceTitle.setTextColor(0xFF333333.toInt())
            deviceTitle.setPadding(0, 16, 0, 8)
            deviceTitle.typeface = android.graphics.Typeface.defaultFromStyle(android.graphics.Typeface.BOLD)
            
            // 添加到统计容器
            statisticsContainer.addView(deviceTitle)
            addStatisticView("温度统计", "平均值: ${tempStats.average}°C, 最大值: ${tempStats.max}°C, 最小值: ${tempStats.min}°C")
            addStatisticView("湿度统计", "平均值: ${humidityStats.average}%, 最大值: ${humidityStats.max}%, 最小值: ${humidityStats.min}%")
            addStatisticView("氧气浓度统计", "平均值: ${oxygenStats.average}%, 最大值: ${oxygenStats.max}%, 最小值: ${oxygenStats.min}%")
            addStatisticView("异常率统计", "异常次数: $abnormalCount, 异常率: %.2f%%".format(abnormalRate))
        }
    }
    
    /**
     * 更新统计信息
     */
    private fun updateStatistics(histories: List<DeviceHistoryEntity>) {
        // 清空统计容器
        statisticsContainer.removeAllViews()
        
        if (histories.isEmpty()) return
        
        // 创建统计分析器
        val analyzer = StatisticsAnalyzer()
        
        // 基础统计信息
        val tempStats = analyzer.calculateBasicStatistics(histories.map { it.temperature.toDouble() })
        val humidityStats = analyzer.calculateBasicStatistics(histories.map { it.humidity.toDouble() })
        val oxygenStats = analyzer.calculateBasicStatistics(histories.map { it.oxygenLevel.toDouble() })
        
        // 状态分布和异常统计
        val statusDistribution = analyzer.calculateStatusDistribution(histories)
        val abnormalCount = (statusDistribution[DeviceStatus.WARNING] ?: 0) + (statusDistribution[DeviceStatus.ERROR] ?: 0)
        val abnormalRate = if (histories.isNotEmpty()) (abnormalCount.toDouble() / histories.size * 100) else 0.0
        
        // 创建统计信息视图
        addStatisticView("温度统计", "平均值: ${tempStats.average}°C, 最大值: ${tempStats.max}°C, 最小值: ${tempStats.min}°C")
        addStatisticView("湿度统计", "平均值: ${humidityStats.average}%, 最大值: ${humidityStats.max}%, 最小值: ${humidityStats.min}%")
        addStatisticView("氧气浓度统计", "平均值: ${oxygenStats.average}%, 最大值: ${oxygenStats.max}%, 最小值: ${oxygenStats.min}%")
        addStatisticView("异常率统计", "异常次数: $abnormalCount, 异常率: %.2f%%".format(abnormalRate))
        
        // 设备状态分布图表
        chartUtils.createStatusDistributionChart(statisticsContainer, statusDistribution)
    }
    
    /**
     * 添加统计信息视图
     */
    private fun addStatisticView(title: String, content: String) {
        // 创建统计信息容器
        val statLayout = LinearLayout(this)
        statLayout.orientation = LinearLayout.VERTICAL
        statLayout.setPadding(16, 8, 16, 8)
        
        // 创建标题文本视图
        val titleView = TextView(this)
        titleView.text = title
        titleView.textSize = 16f
        titleView.setPadding(0, 0, 0, 4)
        
        // 创建内容文本视图
        val contentView = TextView(this)
        contentView.text = content
        contentView.textSize = 14f
        contentView.setTextColor(0xFF666666.toInt())
        
        // 添加到容器
        statLayout.addView(titleView)
        statLayout.addView(contentView)
        statisticsContainer.addView(statLayout)
    }
    
    /**
     * 生成报表
     */
    private fun generateReport() {
        if (selectedDeviceId == null) {
            Toast.makeText(this, "请先选择设备", Toast.LENGTH_SHORT).show()
            return
        }
        
        // 获取当前选择的设备
        val selectedDevice = devices.find { it.deviceId == selectedDeviceId }
        if (selectedDevice == null) {
            Toast.makeText(this, "设备不存在", Toast.LENGTH_SHORT).show()
            return
        }
        
        // 显示生成报表的进度
        Toast.makeText(this, "正在生成报表...", Toast.LENGTH_SHORT).show()
        
        // 获取时间范围
        val timeRange = calculateTimeRange(timeRangeSpinner.selectedItemPosition)
        
        // 获取历史数据
        historyViewModel.getDeviceHistoriesInTimeRange(
            selectedDeviceId!!, 
            timeRange.startTime, 
            timeRange.endTime
        ) { histories ->
            if (histories.isNotEmpty()) {
                // 生成设备数据报表
                CoroutineScope(Dispatchers.IO).launch {
                    val reportPath = reportGenerator.generateDeviceReport(
                            deviceId = selectedDeviceId!!,
                            deviceName = selectedDevice.deviceName,
                            histories = histories,
                            startTime = timeRange.startTime,
                            endTime = timeRange.endTime
                        )
                    
                    withContext(Dispatchers.Main) {
                        if (reportPath != null) {
                            Toast.makeText(this@DataAnalysisActivity, "报表已生成: $reportPath", Toast.LENGTH_LONG).show()
                        } else {
                            Toast.makeText(this@DataAnalysisActivity, "报表生成失败", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            } else {
                Toast.makeText(this@DataAnalysisActivity, "该时间段内无数据，无法生成报表", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    /**
     * 导出CSV文件
     */
    private fun exportToCsv() {
        if (selectedDeviceId == null) {
            Toast.makeText(this, "请先选择设备", Toast.LENGTH_SHORT).show()
            return
        }
        
        // 检查存储权限
        if (!checkStoragePermission()) {
            return
        }
        
        // 获取当前选择的设备
        val selectedDevice = devices.find { it.deviceId == selectedDeviceId }
        if (selectedDevice == null) {
            Toast.makeText(this, "设备不存在", Toast.LENGTH_SHORT).show()
            return
        }
        
        // 显示导出进度
        Toast.makeText(this, "正在导出CSV文件...", Toast.LENGTH_SHORT).show()
        
        // 获取时间范围
        val timeRange = calculateTimeRange(timeRangeSpinner.selectedItemPosition)
        
        // 获取历史数据
        historyViewModel.getDeviceHistoriesInTimeRange(
            selectedDeviceId!!, 
            timeRange.startTime, 
            timeRange.endTime
        ) { histories ->
            if (histories.isNotEmpty()) {
                // 生成设备数据报表（CSV格式）
                CoroutineScope(Dispatchers.IO).launch {
                    val reportPath = reportGenerator.generateDeviceReport(
                            deviceId = selectedDeviceId!!,
                            deviceName = selectedDevice.deviceName,
                            histories = histories,
                            startTime = timeRange.startTime,
                            endTime = timeRange.endTime
                        )
                    
                    withContext(Dispatchers.Main) {
                        if (reportPath != null) {
                            Toast.makeText(this@DataAnalysisActivity, "CSV文件已导出: $reportPath", Toast.LENGTH_LONG).show()
                        } else {
                            Toast.makeText(this@DataAnalysisActivity, "CSV导出失败", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            } else {
                Toast.makeText(this@DataAnalysisActivity, "该时间段内无数据，无法导出", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    /**
     * 检查存储权限
     */
    private fun checkStoragePermission(): Boolean {
        // Android 6.0及以上需要动态请求权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                // 请求权限
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 100)
                return false
            }
        }
        return true
    }
    
    /**
     * 处理权限请求结果
     */
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 100) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "存储权限已获取", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "需要存储权限才能导出文件", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    /**
     * 处理返回按钮点击事件
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
    
    /**
     * 时间范围数据类
     */
    data class TimeRange(val startTime: Long, val endTime: Long)
}
