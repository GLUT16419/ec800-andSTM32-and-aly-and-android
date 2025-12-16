package com.example.dgb.data

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Handler
import android.os.Looper
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.dgb.MqttService
import com.example.dgb.MqttService.ColdChainDevice
import kotlinx.coroutines.*

// 数据同步服务，负责设备数据的离线存储和同步
class SyncService(private val context: Context) {
    // 自定义协程作用域
    private val syncCoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    
    private val deviceDao = DeviceDatabase.getDatabase(context).deviceDao()
    private val deviceHistoryDao = DeviceDatabase.getDatabase(context).deviceHistoryDao()
    private val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    
    // 网络状态LiveData
    private val _isConnected = MutableLiveData<Boolean>()
    val isConnected: LiveData<Boolean> get() = _isConnected
    
    // 同步状态LiveData
    private val _isSyncing = MutableLiveData<Boolean>(false)
    val isSyncing: LiveData<Boolean> get() = _isSyncing
    
    // 同步错误信息LiveData
    private val _syncError = MutableLiveData<String?>(null)
    val syncError: LiveData<String?> get() = _syncError
    
    // 网络回调
    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            super.onAvailable(network)
            _isConnected.postValue(true)
            // 网络可用时，自动同步数据
            syncData()
        }
        
        override fun onLost(network: Network) {
            super.onLost(network)
            _isConnected.postValue(false)
        }
    }
    
    init {
        // 初始化网络状态
        _isConnected.value = isNetworkAvailable()
        
        // 注册网络监听
        val networkRequest = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
            .build()
        connectivityManager.registerNetworkCallback(networkRequest, networkCallback)
    }
    
    // 检查网络是否可用
    private fun isNetworkAvailable(): Boolean {
        val networkCapabilities = connectivityManager.activeNetwork ?: return false
        val actNw = connectivityManager.getNetworkCapabilities(networkCapabilities) ?: return false
        return when {
            actNw.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
            actNw.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
            actNw.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
            else -> false
        }
    }
    
    // 同步数据（将本地设备信息与服务器数据同步）
    fun syncData() {
        if (!_isConnected.value!! || _isSyncing.value!!) {
            return // 网络不可用或正在同步时，不执行同步
        }
        
        _isSyncing.postValue(true)
        _syncError.postValue(null)
        
        // 实际应用中，这里应该从服务器获取最新的设备列表
        // 由于这是演示，我们假设MqttService已经有了最新的设备列表
        // 在实际项目中，应该使用API调用获取设备列表
        
        // 模拟网络请求延迟
        Handler(Looper.getMainLooper()).postDelayed({
            try {
                // 获取当前所有设备（从ConcurrentHashMap转换为List）
                val devices = MqttService.deviceMap.values.toList()
                
                // 将设备信息存储到本地数据库
                storeDevicesToLocal(devices)
                
                // 同步完成
                _isSyncing.postValue(false)
            } catch (e: Exception) {
                _syncError.postValue("同步失败：${e.message}")
                _isSyncing.postValue(false)
            }
        }, 1000)
    }
    
    // 将设备信息存储到本地数据库
    fun storeDevicesToLocal(devices: List<ColdChainDevice>) {
        syncCoroutineScope.launch(Dispatchers.IO) {
            try {
                // 转换为DeviceEntity列表
                val deviceEntities = devices.map { DeviceEntity.fromColdChainDevice(it) }
                
                // 批量插入或更新设备信息
                deviceDao.insertOrUpdateDevices(deviceEntities)
                
                // 同时将设备的最新状态保存到历史记录
                val historyEntities = devices.map { device ->
                    // 从字符串中提取数值（移除单位）
                    val tempValue = device.temperature.replace("°C", "").toDoubleOrNull() ?: 0.0
                    val humidityValue = device.humidity.replace("%", "").toDoubleOrNull() ?: 0.0
                    val oxygenValue = device.oxygenLevel.replace("%", "").toDoubleOrNull() ?: 0.0
                    
                    DeviceHistoryEntity(
                        deviceId = device.id,
                        timestamp = device.lastUpdate.time,
                        status = device.status.ordinal,
                        temperature = tempValue,
                        humidity = humidityValue,
                        oxygenLevel = oxygenValue,
                        latitude = device.latLng.latitude,
                        longitude = device.latLng.longitude
                    )
                }
                deviceHistoryDao.insertHistories(historyEntities)
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _syncError.postValue("存储设备信息失败：${e.message}")
                }
            }
        }
    }
    
    // 从本地数据库获取所有设备
    fun getDevicesFromLocal(): kotlinx.coroutines.flow.Flow<List<DeviceEntity>> {
        return deviceDao.getAllDevices()
    }
    
    // 从本地数据库获取指定设备
    
    suspend fun getDeviceFromLocal(deviceId: Int): DeviceEntity? {
        return deviceDao.getDeviceById(deviceId)
    }
    
    // 清理资源
    fun cleanup() {
        // 取消协程作用域
        syncCoroutineScope.cancel()
        
        // 注销网络监听
        connectivityManager.unregisterNetworkCallback(networkCallback)
    }
}