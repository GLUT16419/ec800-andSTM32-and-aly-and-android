package com.example.dgb

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.amap.api.maps.AMap
import com.amap.api.maps.MapView
import com.amap.api.maps.model.LatLng
import com.example.dgb.data.DeviceHistoryEntity
import com.example.dgb.data.DeviceHistoryViewModel
import com.example.dgb.data.DeviceHistoryViewModelFactory
import androidx.lifecycle.ViewModelProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

/**
 * 设备轨迹回放Activity
 */
class TrackReplayActivity : AppCompatActivity() {
    
    // 地图相关
    private lateinit var mapView: MapView
    private lateinit var aMap: AMap
    private lateinit var mapUtils: MapUtils
    
    // UI组件
    private lateinit var toolbar: Toolbar
    private lateinit var progressBar: SeekBar
    private lateinit var currentTimeText: TextView
    private lateinit var totalTimeText: TextView
    private lateinit var btnPlayPause: ImageButton
    private lateinit var btnForward: ImageButton
    private lateinit var btnBackward: ImageButton
    private lateinit var speedSpinner: Spinner
    private lateinit var infoWindow: LinearLayout
    private lateinit var deviceNameText: TextView
    private lateinit var infoText: TextView
    
    // 数据和工具
    private lateinit var historyViewModel: DeviceHistoryViewModel
    private var trackPoints: List<DeviceTrack> = emptyList()
    private var currentTrack: DeviceTrack? = null
    private var selectedDeviceId: Int? = null
    
    // 回放控制
    private var isPlaying = false
    private var currentProgress = 0
    private var totalProgress = 0
    private var replaySpeed = 1.0f
    private val replayHandler = Handler(Looper.getMainLooper())
    private val replayRunnable = Runnable { updateReplayProgress() }
    
    // 时间格式化
    private val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_track_replay)
        
        // 获取传入的设备ID
        selectedDeviceId = intent.getIntExtra("deviceId", -1)
        
        // 初始化视图组件
        initViews()
        
        // 初始化地图
        initMap(savedInstanceState)
        
        // 初始化视图模型
        initViewModel()
        
        // 设置事件监听器
        setListeners()
        
        // 加载设备轨迹数据
        loadTrackData()
    }
    
    /**
     * 初始化视图组件
     */
    private fun initViews() {
        // 初始化Toolbar
        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = "设备轨迹回放"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        
        // 初始化地图视图
        mapView = findViewById(R.id.map_view)
        
        // 初始化回放控制组件
        progressBar = findViewById(R.id.progress_bar)
        currentTimeText = findViewById(R.id.current_time)
        totalTimeText = findViewById(R.id.total_time)
        btnPlayPause = findViewById(R.id.btn_play_pause)
        btnForward = findViewById(R.id.btn_forward)
        btnBackward = findViewById(R.id.btn_backward)
        speedSpinner = findViewById(R.id.speed_spinner)
        
        // 初始化信息窗口
        infoWindow = findViewById(R.id.info_window)
        deviceNameText = findViewById(R.id.device_name)
        infoText = findViewById(R.id.info_text)
    }
    
    /**
     * 初始化地图
     */
    private fun initMap(savedInstanceState: Bundle?) {
        // 初始化地图
        mapView.onCreate(savedInstanceState)
        aMap = mapView.map
        
        // 初始化地图工具
        mapUtils = MapUtils(aMap)
        
        // 配置地图
        val uiSettings = aMap.uiSettings
        uiSettings.isZoomControlsEnabled = true
        uiSettings.isMyLocationButtonEnabled = true
    }
    
    /**
     * 初始化视图模型
     */
    private fun initViewModel() {
        val factory = DeviceHistoryViewModelFactory(application)
        historyViewModel = ViewModelProvider(this, factory).get(DeviceHistoryViewModel::class.java)
    }
    
    /**
     * 设置事件监听器
     */
    private fun setListeners() {
        // 播放/暂停按钮
        btnPlayPause.setOnClickListener { togglePlayPause() }
        
        // 前进按钮
        btnForward.setOnClickListener { skipForward() }
        
        // 后退按钮
        btnBackward.setOnClickListener { skipBackward() }
        
        // 进度条
        progressBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    currentProgress = progress
                    updateReplayPosition(progress)
                }
            }
            
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
        
        // 速度选择器
        speedSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val speedText = parent?.getItemAtPosition(position).toString()
                replaySpeed = speedText.substring(0, speedText.length - 1).toFloat()
            }
            
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }
    
    /**
     * 加载设备轨迹数据
     */
    private fun loadTrackData() {
        if (selectedDeviceId == null || selectedDeviceId == -1) {
            Toast.makeText(this, "设备ID无效", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        
        // 加载设备历史数据
        historyViewModel.getLatestHistories(selectedDeviceId!!) { histories ->
            if (histories.isNotEmpty()) {
                // 转换为轨迹数据
                val deviceTrack = convertToDeviceTrack(histories)
                currentTrack = deviceTrack
                
                // 更新UI
                runOnUiThread {
                    setupTrackForReplay(deviceTrack)
                }
            } else {
                runOnUiThread {
                    Toast.makeText(this@TrackReplayActivity, "暂无轨迹数据", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    
    /**
     * 将历史数据转换为轨迹数据
     */
    private fun convertToDeviceTrack(histories: List<DeviceHistoryEntity>): DeviceTrack {
        // 过滤出有位置信息的数据点
        val locationHistories = histories.filter { it.latitude != null && it.longitude != null }
        
        if (locationHistories.isEmpty()) {
            return DeviceTrack(
                deviceId = selectedDeviceId!!,
                deviceName = "设备${selectedDeviceId}",
                trackPoints = emptyList(),
                totalDistance = 0.0,
                avgSpeed = 0.0,
                startTime = 0L,
                endTime = 0L
            )
        }
        
        // 转换为TrackPoint列表
        val trackPoints = locationHistories.map { history ->
            TrackPoint(
                latLng = LatLng(history.latitude!!, history.longitude!!),
                timestamp = history.timestamp,
                speed = 0.0, // 模拟速度数据
                status = history.getStatusEnum()
            )
        }
        
        // 计算总距离和平均速度
        var totalDistance = 0.0
        for (i in 1 until trackPoints.size) {
            totalDistance += com.amap.api.maps.AMapUtils.calculateLineDistance(
                trackPoints[i-1].latLng,
                trackPoints[i].latLng
            )
        }
        
        val startTime = trackPoints.firstOrNull()?.timestamp ?: 0L
        val endTime = trackPoints.lastOrNull()?.timestamp ?: 0L
        val duration = if (endTime > startTime) (endTime - startTime).toDouble() / 1000 / 60 / 60 else 1.0
        val avgSpeed = if (duration > 0) (totalDistance / 1000) / duration else 0.0
        
        return DeviceTrack(
            deviceId = selectedDeviceId!!,
            deviceName = "设备${selectedDeviceId}",
            trackPoints = trackPoints,
            totalDistance = totalDistance,
            avgSpeed = avgSpeed,
            startTime = startTime,
            endTime = endTime
        )
    }
    
    /**
     * 设置轨迹用于回放
     */
    private fun setupTrackForReplay(track: DeviceTrack) {
        if (track.trackPoints.isEmpty()) return
        
        // 设置设备名称
        deviceNameText.text = track.deviceName
        
        // 绘制轨迹
        mapUtils.drawVehicleTrack(track.deviceId, track.trackPoints)
        
        // 设置进度条
        totalProgress = track.trackPoints.size - 1
        progressBar.max = totalProgress
        progressBar.progress = 0
        
        // 设置时间显示
        updateTimeDisplay(0)
        
        // 显示信息窗口
        infoWindow.visibility = View.VISIBLE
        updateInfoWindow(0)
    }
    
    /**
     * 切换播放/暂停状态
     */
    private fun togglePlayPause() {
        if (currentTrack == null || currentTrack?.trackPoints?.isEmpty() == true) {
            Toast.makeText(this, "暂无轨迹数据", Toast.LENGTH_SHORT).show()
            return
        }
        
        isPlaying = !isPlaying
        
        if (isPlaying) {
            // 开始播放
            btnPlayPause.setImageResource(android.R.drawable.ic_media_pause)
            replayHandler.postDelayed(replayRunnable, (1000 / replaySpeed).toLong())
        } else {
            // 暂停播放
            btnPlayPause.setImageResource(android.R.drawable.ic_media_play)
            replayHandler.removeCallbacks(replayRunnable)
        }
    }
    
    /**
     * 前进
     */
    private fun skipForward() {
        currentProgress += 5
        if (currentProgress > totalProgress) {
            currentProgress = totalProgress
            stopReplay()
        }
        updateReplayPosition(currentProgress)
    }
    
    /**
     * 后退
     */
    private fun skipBackward() {
        currentProgress -= 5
        if (currentProgress < 0) currentProgress = 0
        updateReplayPosition(currentProgress)
    }
    
    /**
     * 更新回放进度
     */
    private fun updateReplayProgress() {
        if (!isPlaying) return
        
        currentProgress++
        if (currentProgress > totalProgress) {
            stopReplay()
            return
        }
        
        updateReplayPosition(currentProgress)
        replayHandler.postDelayed(replayRunnable, (1000 / replaySpeed).toLong())
    }
    
    /**
     * 更新回放位置
     */
    private fun updateReplayPosition(progress: Int) {
        currentProgress = progress
        progressBar.progress = progress
        updateTimeDisplay(progress)
        updateInfoWindow(progress)
        
        // 更新地图上的设备位置
        currentTrack?.let {
            if (progress < it.trackPoints.size) {
                val currentPoint = it.trackPoints[progress]
                // 可以在这里更新设备标记点
            }
        }
    }
    
    /**
     * 更新时间显示
     */
    private fun updateTimeDisplay(progress: Int) {
        currentTrack?.let {
            if (it.trackPoints.isNotEmpty() && progress < it.trackPoints.size) {
                val currentTime = it.trackPoints[progress].timestamp
                currentTimeText.text = timeFormat.format(Date(currentTime))
                
                val totalTime = it.trackPoints.last().timestamp - it.trackPoints.first().timestamp
                totalTimeText.text = timeFormat.format(Date(totalTime))
            }
        }
    }
    
    /**
     * 更新信息窗口
     */
    private fun updateInfoWindow(progress: Int) {
        currentTrack?.let {
            if (it.trackPoints.isNotEmpty() && progress < it.trackPoints.size) {
                val currentPoint = it.trackPoints[progress]
                val latLng = currentPoint.latLng
                
                infoText.text = "经度: ${latLng.longitude.format(6)}\n" +
                                "纬度: ${latLng.latitude.format(6)}\n" +
                                "状态: ${currentPoint.status.displayName}\n" +
                                "时间: ${dateFormat.format(Date(currentPoint.timestamp))}"
            }
        }
    }
    
    /**
     * 停止回放
     */
    private fun stopReplay() {
        isPlaying = false
        btnPlayPause.setImageResource(android.R.drawable.ic_media_play)
        replayHandler.removeCallbacks(replayRunnable)
    }
    
    /**
     * 格式化double数字
     */
    private fun Double.format(digits: Int) = java.lang.String.format("%.${digits}f", this)
    
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
     * 生命周期方法
     */
    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }
    
    override fun onPause() {
        super.onPause()
        mapView.onPause()
        stopReplay()
    }
    
    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
        replayHandler.removeCallbacks(replayRunnable)
    }
    
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView.onSaveInstanceState(outState)
    }
    
    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }
}