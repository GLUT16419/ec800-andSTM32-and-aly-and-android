package com.example.dgb

import com.amap.api.maps.model.LatLng
import org.junit.Test
import java.util.Date
import java.util.concurrent.CountDownLatch
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class ThreadSafetyTest {

    @Test
    fun testDeviceMapThreadSafety() {
        // 创建测试线程池
        val executorService: ExecutorService = Executors.newFixedThreadPool(10)
        val latch = CountDownLatch(100) // 100个并发操作
        
        // 清除现有设备数据
        MqttService.deviceMap.clear()
        
        // 执行100个并发设备更新操作
        for (i in 0 until 100) {
            executorService.submit {
                try {
                    // 创建或更新设备
                    val device = MqttService.ColdChainDevice(
                        id = (i % 10) + 1,
                        name = "TestDevice-${i % 10}",
                        status = MqttService.DeviceStatus.NORMAL,
                        temperature = "${Math.random() * 10 + 2}°C",
                        humidity = "${Math.random() * 40 + 50}%",
                        oxygenLevel = "${Math.random() * 2 + 19}%",
                        location = "测试位置",
                        lastUpdate = Date(),
                        latLng = LatLng(39.9042, 116.4074),
                        speed = "40.0"
                    )
                    
                    // 直接操作deviceMap
                    MqttService.deviceMap[device.name] = device
                    
                    // 验证更新结果
                    val updatedDevice = MqttService.deviceMap["TestDevice-${i % 10}"]
                    assert(updatedDevice != null)
                    assert(updatedDevice?.name == "TestDevice-${i % 10}")
                    
                } catch (e: Exception) {
                    e.printStackTrace()
                    assert(false) { "线程安全测试失败: ${e.message}" }
                } finally {
                    latch.countDown()
                }
            }
        }
        
        // 等待所有操作完成
        latch.await(10, TimeUnit.SECONDS)
        
        // 验证最终设备数量
        assert(MqttService.deviceMap.size == 10) { "最终设备数量应为10，实际为${MqttService.deviceMap.size}" }
        
        // 关闭线程池
        executorService.shutdown()
        
        println("线程安全测试通过！")
    }
    
    @Test
    fun testDeviceMapPerformance() {
        // 清除现有设备数据
        MqttService.deviceMap.clear()
        
        val startTime = System.currentTimeMillis()
        val iterations = 10000
        
        // 执行大量设备更新操作
        for (i in 0 until iterations) {
            val device = MqttService.ColdChainDevice(
                id = (i % 1000) + 1,
                name = "PerfDevice-${i % 1000}",
                status = MqttService.DeviceStatus.NORMAL,
                temperature = "${Math.random() * 10 + 2}°C",
                humidity = "${Math.random() * 40 + 50}%",
                oxygenLevel = "${Math.random() * 2 + 19}%",
                location = "测试位置",
                lastUpdate = Date(),
                latLng = LatLng(39.9042, 116.4074),
                speed = "40.0"
            )
            
            // 直接操作deviceMap
            MqttService.deviceMap[device.name] = device
        }
        
        val endTime = System.currentTimeMillis()
        val duration = endTime - startTime
        
        println("性能测试：${iterations}次设备更新操作耗时${duration}ms")
        println("平均每次操作耗时：${duration.toDouble() / iterations}ms")
        
        // 验证最终设备数量
        assert(MqttService.deviceMap.size == 1000) { "最终设备数量应为1000，实际为${MqttService.deviceMap.size}" }
        
        println("性能测试通过！")
    }
}