package com.example.dgb

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.amap.api.maps.model.LatLng
import android.widget.CheckBox
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.*
import androidx.lifecycle.lifecycleScope
import com.example.dgb.MqttService
import com.example.dgb.presentation.fragment.BaseLazyFragment
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class DeviceFragment : BaseLazyFragment() {

    private lateinit var titleText: TextView
    private lateinit var totalDevicesText: TextView
    private lateinit var onlineDevicesText: TextView
    private lateinit var errorDevicesText: TextView
    private lateinit var filterButton: MaterialButton
    private lateinit var searchEditText: TextInputEditText
    private lateinit var devicesContainer: ViewGroup
    private lateinit var batchSelectButton: MaterialButton
    private lateinit var batchActionBar: LinearLayout
    private lateinit var selectedCountText: TextView
    private lateinit var selectAllButton: MaterialButton
    private lateinit var deleteBatchButton: MaterialButton
    private lateinit var cancelBatchButton: MaterialButton

    private val deviceList = mutableListOf<com.example.dgb.ColdChainDevice>()
    private val filteredDeviceList = mutableListOf<com.example.dgb.ColdChainDevice>()
    private val selectedDevices = mutableSetOf<Int>() // 存储选中的设备ID
    private var isBatchMode = false // 是否处于批量选择模式

    override fun getLayoutId(): Int {
        return R.layout.fragment_device
    }

    override fun initView(view: View) {
        // 初始化视图
        initViews(view)

        // 设置筛选按钮点击事件
        filterButton.setOnClickListener {
            showFilterDialog()
        }

        // 设置搜索功能
        setupSearch()
    }

    override fun lazyLoad() {
        // 初始化设备数据
        initializeDeviceData()

        // 监听MQTT设备数据更新事件，实时刷新设备列表
        lifecycleScope.launch(Dispatchers.Main) {
            MqttService.Companion.ServiceDataRepository.updateEvent.observe(viewLifecycleOwner) {
                // 重新初始化设备数据以获取最新的MQTT设备数据
                initializeDeviceData()
            }
        }
    }

    private fun initViews(view: View) {
        titleText = view.findViewById(R.id.title_text)
        titleText.text = "已注册设备"

        totalDevicesText = view.findViewById(R.id.total_devices)
        onlineDevicesText = view.findViewById(R.id.online_devices)
        errorDevicesText = view.findViewById(R.id.error_devices)

        filterButton = view.findViewById(R.id.filter_button)
        searchEditText = view.findViewById(R.id.search_edittext)
        devicesContainer = view.findViewById(R.id.devices_container)

        // 初始化批量操作相关视图
        batchSelectButton = view.findViewById<MaterialButton>(R.id.batch_select_button)
        batchActionBar = view.findViewById<LinearLayout>(R.id.batch_action_bar)
        selectedCountText = view.findViewById<TextView>(R.id.selected_count_text)
        selectAllButton = view.findViewById<MaterialButton>(R.id.select_all_button)
        deleteBatchButton = view.findViewById<MaterialButton>(R.id.delete_batch_button)
        cancelBatchButton = view.findViewById<MaterialButton>(R.id.cancel_batch_button)

        // 设置批量操作按钮点击事件
        batchSelectButton.setOnClickListener { 
            toggleBatchMode() 
        }
        selectAllButton.setOnClickListener { 
            selectAllDevices() 
        }
        deleteBatchButton.setOnClickListener { 
            deleteSelectedDevices() 
        }
        cancelBatchButton.setOnClickListener { 
            exitBatchMode() 
        }
    }

    private fun initializeDeviceData() {
        deviceList.clear()

        // 移除模拟数据，从MQTT服务获取真实设备数据
        MqttService.deviceMap.forEach { (deviceName, mqttDevice) ->
            deviceList.add(
                com.example.dgb.ColdChainDevice(
                    id = mqttDevice.id,
                    name = mqttDevice.name,
                    status = mqttDevice.status,
                    temperature = mqttDevice.temperature,
                    humidity = mqttDevice.humidity,
                    oxygenLevel = mqttDevice.oxygenLevel,
                    location = mqttDevice.location,
                    lastUpdate = mqttDevice.lastUpdate,
                    latLng = mqttDevice.latLng
                )
            )
        }

        // 初始化筛选列表
        filteredDeviceList.clear()
        filteredDeviceList.addAll(deviceList)

        // 更新设备统计
        updateDeviceStats()

        // 显示设备列表
        displayDeviceCards()
    }

    private fun updateDeviceStats() {
        val total = deviceList.size
        val online = deviceList.count { it.status == DeviceStatus.NORMAL }
        val error = deviceList.count { it.status == DeviceStatus.ERROR || it.status == DeviceStatus.WARNING }

        totalDevicesText.text = total.toString()
        onlineDevicesText.text = online.toString()
        errorDevicesText.text = error.toString()
    }

    private fun displayDeviceCards() {
        // 清空现有卡片
        devicesContainer.removeAllViews()

        // 为每个设备创建卡片
        filteredDeviceList.forEach { device ->
            val cardView = LayoutInflater.from(requireContext())
                .inflate(R.layout.item_device_card, devicesContainer, false) as CardView

            // 设置设备名称
            cardView.findViewById<TextView>(R.id.device_name).text = device.name

            // 设置设备状态
            val statusText = cardView.findViewById<TextView>(R.id.device_status)
            statusText.text = device.status.displayName

            // 根据状态设置背景颜色
            when (device.status) {
                DeviceStatus.NORMAL ->
                    statusText.setBackgroundResource(R.drawable.status_background_normal)
                DeviceStatus.WARNING ->
                    statusText.setBackgroundResource(R.drawable.status_background_warning)
                DeviceStatus.ERROR ->
                    statusText.setBackgroundResource(R.drawable.status_background_error)
            }
            
            // 设置轨迹回放按钮
            val trackReplayButton = cardView.findViewById<ImageButton>(R.id.track_replay_button)
            trackReplayButton.setOnClickListener {
                navigateToTrackReplay(device.id)
            }

            // 设置温度信息
            cardView.findViewById<TextView>(R.id.device_temp).text = device.temperature

            // 设置湿度信息
            cardView.findViewById<TextView>(R.id.device_humidity).text = device.humidity

            // 设置氧气浓度信息
            cardView.findViewById<TextView>(R.id.device_oxygen).text = device.oxygenLevel

            // 设置位置信息
            cardView.findViewById<TextView>(R.id.device_location).text = device.location

            // 设置最后更新时间
            val dateFormat = SimpleDateFormat("MM/dd HH:mm", Locale.getDefault())
            cardView.findViewById<TextView>(R.id.device_last_update).text =
                "最后更新: ${dateFormat.format(device.lastUpdate)}"

            // 获取复选框
            val checkbox = cardView.findViewById<CheckBox>(R.id.device_checkbox)

            // 根据批量模式设置卡片点击事件
            if (isBatchMode) {
                // 批量模式下点击卡片切换选择状态
                cardView.setOnClickListener {
                    toggleDeviceSelection(device.id, checkbox)
                }

                // 复选框点击事件
                checkbox.setOnCheckedChangeListener { _, isChecked ->
                    if (isChecked) {
                        selectedDevices.add(device.id)
                    } else {
                        selectedDevices.remove(device.id)
                    }
                    updateSelectedCount()
                }
            } else {
                // 普通模式下点击卡片查看历史记录
                cardView.setOnClickListener {
                    navigateToHistoryChart(device.id)
                }

                // 为卡片添加长按事件 - 设备详情
                cardView.setOnLongClickListener {
                    showDeviceDetails(device)
                    true
                }
            }

            // 根据批量模式设置复选框可见性
            checkbox.visibility = if (isBatchMode) View.VISIBLE else View.GONE
            // 设置复选框的选中状态
            checkbox.isChecked = selectedDevices.contains(device.id)

            // 将卡片添加到容器
            devicesContainer.addView(cardView)
        }

        // 如果没有设备，显示空状态
        if (filteredDeviceList.isEmpty()) {
            showEmptyState()
        }
    }

    private fun showEmptyState() {
        val emptyView = LayoutInflater.from(requireContext())
            .inflate(R.layout.layout_empty_state, devicesContainer, false)
        devicesContainer.addView(emptyView)
    }

    private fun setupSearch() {
        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                val query = s.toString().trim()
                filterDevices(query)
            }
        })
    }

    private fun filterDevices(query: String) {
        filteredDeviceList.clear()

        if (query.isEmpty()) {
            filteredDeviceList.addAll(deviceList)
        } else {
            val lowerQuery = query.lowercase(Locale.getDefault())
            filteredDeviceList.addAll(deviceList.filter {
                it.name.lowercase(Locale.getDefault()).contains(lowerQuery) ||
                        it.location.lowercase(Locale.getDefault()).contains(lowerQuery) ||
                        it.status.displayName.lowercase(Locale.getDefault()).contains(lowerQuery)
            })
        }

        displayDeviceCards()
    }

    private fun showFilterDialog() {
        val statuses = arrayOf("全部", "正常", "警告", "异常")
        var selectedStatus = 0

        AlertDialog.Builder(requireContext())
            .setTitle("筛选设备")
            .setSingleChoiceItems(statuses, 0) { _, which ->
                selectedStatus = which
            }
            .setPositiveButton("确定") { dialog, _ ->
                applyFilter(selectedStatus)
                dialog.dismiss()
            }
            .setNegativeButton("取消", null)
            .setNeutralButton("重置") { dialog, _ ->
                resetFilter()
                dialog.dismiss()
            }
            .show()
    }

    private fun applyFilter(statusIndex: Int) {
        filteredDeviceList.clear()
        
        when (statusIndex) {
            0 -> filteredDeviceList.addAll(deviceList) // 全部
            1 -> filteredDeviceList.addAll(deviceList.filter { it.status == DeviceStatus.NORMAL })
            2 -> filteredDeviceList.addAll(deviceList.filter { it.status == DeviceStatus.WARNING })
            3 -> filteredDeviceList.addAll(deviceList.filter { it.status == DeviceStatus.ERROR })
        }
        
        displayDeviceCards()
    }

    /**
     * 切换批量选择模式
     */
    private fun toggleBatchMode() {
        isBatchMode = true
        batchSelectButton?.visibility = android.view.View.GONE
        batchActionBar?.visibility = android.view.View.VISIBLE
        displayDeviceCards()
    }

    /**
     * 退出批量选择模式
     */
    private fun exitBatchMode() {
        isBatchMode = false
        selectedDevices.clear()
        batchSelectButton?.visibility = android.view.View.VISIBLE
        batchActionBar?.visibility = android.view.View.GONE
        displayDeviceCards()
    }

    /**
     * 更新选中设备数量
     */
    private fun updateSelectedCount() {
        selectedCountText.text = "已选择 ${selectedDevices.size} 项"
    }

    /**
     * 切换设备选择状态
     */
    private fun toggleDeviceSelection(deviceId: Int, checkbox: CheckBox) {
        if (selectedDevices.contains(deviceId)) {
            selectedDevices.remove(deviceId)
            checkbox.isChecked = false
        } else {
            selectedDevices.add(deviceId)
            checkbox.isChecked = true
        }
        updateSelectedCount()
    }

    /**
     * 全选/取消全选设备
     */
    private fun selectAllDevices() {
        val isAllSelected = selectedDevices.size == filteredDeviceList.size
        selectedDevices.clear()

        if (!isAllSelected) {
            // 全选
            selectedDevices.addAll(filteredDeviceList.map { it.id })
            selectAllButton.text = "取消全选"
        } else {
            // 取消全选
            selectAllButton.text = "全选"
        }
        updateSelectedCount()
        displayDeviceCards()
    }

    /**
     * 删除选中的设备
     */
    private fun deleteSelectedDevices() {
        if (selectedDevices.isEmpty()) {
            return
        }

        // 显示确认对话框
        AlertDialog.Builder(requireContext())
            .setTitle("确认删除")
            .setMessage("确定要删除选中的 ${selectedDevices.size} 个设备吗？")
            .setPositiveButton("删除") { _, _ ->
                // 从设备列表中移除选中的设备
                deviceList.removeAll { selectedDevices.contains(it.id) }
                filteredDeviceList.removeAll { selectedDevices.contains(it.id) }
                selectedDevices.clear()

                // 更新设备统计和显示
                updateDeviceStats()
                displayDeviceCards()
                updateSelectedCount()
            }
            .setNegativeButton("取消", null)
            .show()
    }
    
    // 导航到历史数据图表Fragment
    private fun navigateToHistoryChart(deviceId: Int) {
        val historyChartFragment = com.example.dgb.ui.charts.HistoryChartFragment.newInstance(deviceId.toLong())
        
        // 使用FragmentManager进行Fragment切换
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, historyChartFragment)
            .addToBackStack(null) // 添加到返回栈，支持返回操作
            .commit()
    }
    
    /**
     * 跳转到轨迹回放页面
     */
    private fun navigateToTrackReplay(deviceId: Int) {
        val intent = Intent(activity, TrackReplayActivity::class.java)
        intent.putExtra("deviceId", deviceId)
        startActivity(intent)
    }

    private fun resetFilter() {
        filteredDeviceList.clear()
        filteredDeviceList.addAll(deviceList)
        searchEditText.text?.clear()
        displayDeviceCards()
    }

    private fun showHistoryDialog(device: ColdChainDevice) {
        val dialog = Dialog(requireContext())
        dialog.setContentView(R.layout.dialog_history)
        dialog.setCancelable(true)

        // 设置对话框大小
        dialog.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        // 设置设备信息
        dialog.findViewById<TextView>(R.id.device_name).text = device.name
        dialog.findViewById<TextView>(R.id.device_location).text = device.location

        // 设置历史记录数据
        val historyRecyclerView = dialog.findViewById<RecyclerView>(R.id.history_recyclerview)
        val historyList = generateHistoryData(device)
        val adapter = HistoryAdapter(historyList)

        historyRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        historyRecyclerView.adapter = adapter

        // 设置时间范围按钮事件
        dialog.findViewById<MaterialButton>(R.id.btn_today).setOnClickListener {
            adapter.updateData(generateHistoryData(device, "today"))
        }

        dialog.findViewById<MaterialButton>(R.id.btn_week).setOnClickListener {
            adapter.updateData(generateHistoryData(device, "week"))
        }

        dialog.findViewById<MaterialButton>(R.id.btn_month).setOnClickListener {
            adapter.updateData(generateHistoryData(device, "month"))
        }

        // 设置导出按钮事件
        dialog.findViewById<MaterialButton>(R.id.btn_export).setOnClickListener {
            showExportDialog(device, historyList)
        }

        // 设置关闭按钮事件
        dialog.findViewById<MaterialButton>(R.id.btn_close).setOnClickListener {
            dialog.dismiss()
        }
        
        // 设置标题栏关闭按钮事件
        dialog.findViewById<ImageButton>(R.id.btn_back).setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun generateHistoryData(device: ColdChainDevice, range: String = "today"): List<HistoryRecord> {
        val historyList = mutableListOf<HistoryRecord>()
        val now = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("MM/dd HH:mm", Locale.getDefault())

        val count = when (range) {
            "today" -> 12
            "week" -> 7
            "month" -> 30
            else -> 12
        }

        for (i in count downTo 1) {
            val time = Calendar.getInstance()
            when (range) {
                "today" -> {
                    time.time = Date(now.timeInMillis - i * 2 * 60 * 60 * 1000L) // 每2小时
                }
                "week" -> {
                    time.add(Calendar.DAY_OF_YEAR, -i)
                    time.set(Calendar.HOUR_OF_DAY, (Math.random() * 24).toInt())
                }
                "month" -> {
                    time.add(Calendar.DAY_OF_YEAR, -i)
                    time.set(Calendar.HOUR_OF_DAY, (Math.random() * 24).toInt())
                }
            }

            // 模拟历史数据变化
            val tempBase = device.temperature.replace("°C", "").toDoubleOrNull() ?: 3.0
            val humidityBase = device.humidity.replace("%", "").toDoubleOrNull() ?: 65.0
            val oxygenBase = device.oxygenLevel.replace("%", "").toDoubleOrNull() ?: 20.5

            val randomTemp = tempBase + (Math.random() * 4 - 2)
            val randomHumidity = humidityBase + (Math.random() * 10 - 5)
            val randomOxygen = oxygenBase + (Math.random() * 2 - 1)

            val status = when {
                randomOxygen < 18.5 || randomTemp > 8 -> DeviceStatus.ERROR
                randomOxygen < 19.5 || randomTemp > 6 -> DeviceStatus.WARNING
                else -> DeviceStatus.NORMAL
            }

            historyList.add(HistoryRecord(
                id = i,
                time = dateFormat.format(time.time),
                status = status,
                temperature = String.format("%.1f°C", randomTemp),
                humidity = String.format("%.0f%%", randomHumidity),
                oxygenLevel = String.format("%.1f%%", randomOxygen),
                location = device.location
            ))
        }

        return historyList
    }

    private fun showExportDialog(device: ColdChainDevice, historyList: List<HistoryRecord>) {
        val formats = arrayOf("CSV", "Excel", "PDF")

        AlertDialog.Builder(requireContext())
            .setTitle("导出数据")
            .setMessage("选择导出格式")
            .setItems(formats) { _, which ->
                val format = formats[which]
                // 这里可以实现实际的导出逻辑
                showToast("正在导出${device.name}的${format}数据...")
            }
            .setNegativeButton("取消", null)
            .show()
    }

    private fun showDeviceDetails(device: ColdChainDevice) {
        val details = """
            设备名称: ${device.name}
            设备状态: ${device.status.displayName}
            当前温度: ${device.temperature}
            当前湿度: ${device.humidity}
            氧气浓度: ${device.oxygenLevel}
            设备位置: ${device.location}
            最后更新: ${SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(device.lastUpdate)}
            """.trimIndent()

        AlertDialog.Builder(requireContext())
            .setTitle("设备详情")
            .setMessage(details)
            .setPositiveButton("确定", null)
            .show()
    }

    private fun showToast(message: String) {
        // 这里可以使用Toast或者Snackbar显示消息
        println("提示: $message")
    }

    // 历史记录适配器
    class HistoryAdapter(private var historyList: List<HistoryRecord>) :
        RecyclerView.Adapter<HistoryAdapter.ViewHolder>() {

        class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val timeText: TextView = view.findViewById(R.id.history_time)
            val statusText: TextView = view.findViewById(R.id.history_status)
            val temperatureText: TextView = view.findViewById(R.id.history_temperature)
            val humidityText: TextView = view.findViewById(R.id.history_humidity)
            val oxygenText: TextView = view.findViewById(R.id.history_oxygen)
            val locationText: TextView = view.findViewById(R.id.history_location)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_history, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val record = historyList[position]

            holder.timeText.text = record.time
            holder.statusText.text = record.status.displayName
            holder.temperatureText.text = record.temperature
            holder.humidityText.text = record.humidity
            holder.oxygenText.text = record.oxygenLevel
            holder.locationText.text = record.location

            // 根据状态设置背景颜色
            when (record.status) {
                DeviceStatus.NORMAL ->
                    holder.statusText.setBackgroundResource(R.drawable.status_background_normal)
                DeviceStatus.WARNING ->
                    holder.statusText.setBackgroundResource(R.drawable.status_background_warning)
                DeviceStatus.ERROR ->
                    holder.statusText.setBackgroundResource(R.drawable.status_background_error)
            }
        }

        override fun getItemCount(): Int = historyList.size

        fun updateData(newList: List<HistoryRecord>) {
            historyList = newList
            notifyDataSetChanged()
        }
    }

    // 历史记录数据类
    data class HistoryRecord(
        val id: Int,
        val time: String,
        val status: com.example.dgb.DeviceStatus,
        val temperature: String,
        val humidity: String,
        val oxygenLevel: String,
        val location: String
    )
}