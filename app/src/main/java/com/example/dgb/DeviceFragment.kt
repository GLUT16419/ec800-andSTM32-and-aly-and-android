package com.example.dgb

import android.app.Dialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.amap.api.maps.model.LatLng
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class DeviceFragment : Fragment() {

    private lateinit var titleText: TextView
    private lateinit var totalDevicesText: TextView
    private lateinit var onlineDevicesText: TextView
    private lateinit var errorDevicesText: TextView
    private lateinit var filterButton: MaterialButton
    private lateinit var searchEditText: TextInputEditText
    private lateinit var devicesContainer: ViewGroup

    private val deviceList = mutableListOf<ColdChainDevice>()
    private val filteredDeviceList = mutableListOf<ColdChainDevice>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_device, container, false)

        // 初始化视图
        initViews(view)

        // 初始化设备数据
        initializeDeviceData()

        // 设置筛选按钮点击事件
        filterButton.setOnClickListener {
            showFilterDialog()
        }

        // 设置搜索功能
        setupSearch()

        return view
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
    }

    private fun initializeDeviceData() {
        deviceList.clear()

        // 添加设备数据（与HomeFragment一致）
        deviceList.addAll(listOf(
            ColdChainDevice(
                id = 1,
                name = "冷藏车-沪A12345",
                status = DeviceStatus.NORMAL,
                temperature = "2.5°C",
                humidity = "65%",
                oxygenLevel = "20.8%",
                location = "上海市浦东新区张江高科技园区",
                lastUpdate = Date(),
                latLng = null
            ),
            ColdChainDevice(
                id = 2,
                name = "冷库-浦东配送中心",
                status = DeviceStatus.WARNING,
                temperature = "4.2°C",
                humidity = "70%",
                oxygenLevel = "19.5%",
                location = "上海市浦东新区金桥出口加工区",
                lastUpdate = Date(System.currentTimeMillis() - 300000),
                latLng = null
            ),
            ColdChainDevice(
                id = 3,
                name = "冷藏柜-徐汇门店",
                status = DeviceStatus.ERROR,
                temperature = "8.7°C",
                humidity = "75%",
                oxygenLevel = "18.2%",
                location = "上海市徐汇区淮海中路",
                lastUpdate = Date(System.currentTimeMillis() - 600000),
                latLng = null
            ),
            ColdChainDevice(
                id = 4,
                name = "冷藏车-沪B67890",
                status = DeviceStatus.NORMAL,
                temperature = "3.1°C",
                humidity = "62%",
                oxygenLevel = "20.9%",
                location = "上海市虹桥国际机场货运区",
                lastUpdate = Date(),
                latLng = null
            ),
            ColdChainDevice(
                id = 5,
                name = "冷库-松江仓储中心",
                status = DeviceStatus.NORMAL,
                temperature = "1.8°C",
                humidity = "58%",
                oxygenLevel = "21.0%",
                location = "上海市松江区泗泾物流园区",
                lastUpdate = Date(System.currentTimeMillis() - 120000),
                latLng = null
            ),
            ColdChainDevice(
                id = 6,
                name = "疫苗运输车-沪C11223",
                status = DeviceStatus.WARNING,
                temperature = "3.5°C",
                humidity = "60%",
                oxygenLevel = "19.8%",
                location = "上海市长宁区临空经济园区",
                lastUpdate = Date(System.currentTimeMillis() - 180000),
                latLng = null
            ),
            ColdChainDevice(
                id = 7,
                name = "冷藏集装箱-洋山港",
                status = DeviceStatus.NORMAL,
                temperature = "2.8°C",
                humidity = "63%",
                oxygenLevel = "20.7%",
                location = "上海市洋山深水港",
                lastUpdate = Date(System.currentTimeMillis() - 240000),
                latLng = null
            ),
            ColdChainDevice(
                id = 8,
                name = "医药冷库-嘉定园区",
                status = DeviceStatus.NORMAL,
                temperature = "1.5°C",
                humidity = "55%",
                oxygenLevel = "21.1%",
                location = "上海市嘉定区工业区",
                lastUpdate = Date(System.currentTimeMillis() - 360000),
                latLng = null
            )
        ))

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

            // 为卡片添加点击事件 - 查看历史记录
            cardView.setOnClickListener {
                showHistoryDialog(device)
            }

            // 为卡片添加长按事件 - 设备详情
            cardView.setOnLongClickListener {
                showDeviceDetails(device)
                true
            }

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
        val status: DeviceStatus,
        val temperature: String,
        val humidity: String,
        val oxygenLevel: String,
        val location: String
    )

    // 数据模型类（与HomeFragment共享）
    data class ColdChainDevice(
        val id: Int,
        val name: String,
        val status: DeviceStatus,
        val temperature: String,
        val humidity: String,
        val oxygenLevel: String,
        val location: String,
        val lastUpdate: Date,
        val latLng: LatLng?
    )

    // 设备状态枚举（与HomeFragment共享）
    enum class DeviceStatus(val displayName: String) {
        NORMAL("正常"),
        WARNING("警告"),
        ERROR("异常")
    }
}