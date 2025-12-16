package com.example.dgb.data

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.dgb.MqttService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// 设备数据ViewModel，用于管理设备数据的离线存储和同步
class DeviceViewModel(application: Application) : AndroidViewModel(application) {
    private val context = application.applicationContext
    private val deviceDatabase = DeviceDatabase.getDatabase(context)
    private val syncService = SyncService(context)
    
    // 设备列表LiveData
    private val _devices = MutableLiveData<List<DeviceEntity>>()
    val devices: LiveData<List<DeviceEntity>> get() = _devices
    
    // 加载状态LiveData
    private val _loading = MutableLiveData<Boolean>(false)
    val loading: LiveData<Boolean> get() = _loading
    
    // 错误信息LiveData
    private val _error = MutableLiveData<String?>(null)
    val error: LiveData<String?> get() = _error
    
    // 网络连接状态LiveData
    val isConnected: LiveData<Boolean> get() = syncService.isConnected
    
    // 同步状态LiveData
    val isSyncing: LiveData<Boolean> get() = syncService.isSyncing
    
    init {
        // 初始化时从本地数据库加载设备列表
        loadDevicesFromLocal()
        
        // 监听设备列表变化
        observeDevices()
    }
    
    // 从本地数据库加载设备列表
    private fun loadDevicesFromLocal() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                deviceDatabase.deviceDao().getAllDevices().collect { deviceEntities ->
                    withContext(Dispatchers.Main) {
                        _devices.value = deviceEntities ?: emptyList()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _error.value = "加载设备列表失败：${e.message}"
                }
            }
        }
    }
    
    // 监听设备列表变化
    private fun observeDevices() {
        viewModelScope.launch(Dispatchers.Main) {
            deviceDatabase.deviceDao().getAllDevices().collect {
                _devices.value = it
            }
        }
    }
    
    // 同步设备数据
    fun syncDevices() {
        if (isSyncing.value!!) {
            return // 正在同步时不执行
        }
        
        _loading.value = true
        _error.value = null
        
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // 调用同步服务进行数据同步
                syncService.syncData()
                
                // 同步完成后，从本地数据库重新加载设备列表
                loadDevicesFromLocal()
                
                withContext(Dispatchers.Main) {
                    _loading.value = false
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _error.value = "同步设备数据失败：${e.message}"
                    _loading.value = false
                }
            }
        }
    }
    
    // 将服务器端的设备列表存储到本地
    fun storeDevicesToLocal(devices: List<MqttService.ColdChainDevice>) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                syncService.storeDevicesToLocal(devices)
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _error.value = "存储设备数据失败：${e.message}"
                }
            }
        }
    }
    
    // 根据设备ID获取设备
    suspend fun getDeviceById(deviceId: Int): DeviceEntity? {
        return try {
            deviceDatabase.deviceDao().getDeviceById(deviceId)
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                _error.value = "获取设备信息失败：${e.message}"
            }
            null
        }
    }
    
    // 根据设备状态获取设备列表
    fun getDevicesByStatus(status: com.example.dgb.DeviceStatus): kotlinx.coroutines.flow.Flow<List<DeviceEntity>> {
        return deviceDatabase.deviceDao().getDevicesByStatus(status.ordinal)
    }
    
    // 根据设备类型获取设备列表
    fun getDevicesByType(deviceType: String): kotlinx.coroutines.flow.Flow<List<DeviceEntity>> {
        return deviceDatabase.deviceDao().getDevicesByType(deviceType)
    }
    
    // 清理资源
    override fun onCleared() {
        super.onCleared()
        syncService.cleanup()
    }
}