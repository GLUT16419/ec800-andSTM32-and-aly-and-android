package com.example.dgb

import com.amap.api.maps.model.LatLng
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import java.util.Date
import java.util.concurrent.ConcurrentHashMap

@RunWith(JUnit4::class)
class DeviceTrackTest {

    @Test
    fun `testDeviceTrackMapStorage`() {
        // 创建模拟的设备轨迹映射
        val deviceTrackMap = ConcurrentHashMap<String, MutableList<TrackPoint>>()
        
        // 模拟几个设备
        val devices = listOf(
            ColdChainDevice(
                id = "device1".hashCode(),
                name = "device1",
                status = DeviceStatus.NORMAL,
                temperature = "2°C",
                humidity = "25%",
                oxygenLevel = "20%",
                location = "经度: 116.397428, 纬度: 39.90923, 海拔: 50, 坐标系: GCJ_02",
                lastUpdate = Date(),
                latLng = LatLng(39.90923, 116.397428),
                speed = "50km/h"
            ),
            ColdChainDevice(
                id = "device2".hashCode(),
                name = "device2",
                status = DeviceStatus.WARNING,
                temperature = "4°C",
                humidity = "40%",
                oxygenLevel = "19%",
                location = "经度: 116.410717, 纬度: 39.911147, 海拔: 60, 坐标系: GCJ_02",
                lastUpdate = Date(),
                latLng = LatLng(39.911147, 116.410717),
                speed = "60km/h"
            )
        )
        
        // 模拟存储轨迹数据
        devices.forEach { device ->
            println("存储设备 ${device.name} 的轨迹数据")
            
            // 创建多个轨迹点
            for (i in 1..5) {
                val trackPoint = TrackPoint(
                    latLng = LatLng(
                        device.latLng.latitude + i * 0.001,
                        device.latLng.longitude + i * 0.001
                    ),
                    timestamp = Date().time - (5 - i) * 60000, // 间隔1分钟
                    speed = device.speed.replace("km/h", "").toDoubleOrNull() ?: 0.0,
                    status = device.status
                )
                
                // 获取或创建设备的轨迹列表
                val trackList = deviceTrackMap.computeIfAbsent(device.name) { mutableListOf() }
                
                // 添加新轨迹点
                trackList.add(trackPoint)
                
                println("  - 添加轨迹点: ${trackPoint.latLng}, 时间: ${Date(trackPoint.timestamp)}")
            }
        }
        
        // 验证轨迹数据是否正确存储
        println("\n验证轨迹数据存储:")
        println("deviceTrackMap 包含 ${deviceTrackMap.size} 个设备的轨迹数据")
        
        deviceTrackMap.forEach { (deviceName, trackPoints) ->
            println("设备 $deviceName 有 ${trackPoints.size} 个轨迹点")
            trackPoints.forEachIndexed { index, point ->
                println("  点 ${index + 1}: ${point.latLng}, 时间: ${Date(point.timestamp)}")
            }
        }
        
        // 模拟showDeviceTrack方法的逻辑
        println("\n模拟显示轨迹数据:")
        devices.forEach { device ->
            println("尝试显示设备 ${device.name} 的轨迹")
            
            deviceTrackMap[device.name]?.let { trackPoints ->
                if (trackPoints.isNotEmpty()) {
                    println("显示设备 ${device.name} 的轨迹，包含 ${trackPoints.size} 个点")
                    println("  - 第一个点: ${trackPoints.first().latLng}, 时间: ${Date(trackPoints.first().timestamp)}")
                    println("  - 最后一个点: ${trackPoints.last().latLng}, 时间: ${Date(trackPoints.last().timestamp)}")
                } else {
                    println("设备 ${device.name} 有轨迹列表但为空")
                }
            } ?: run {
                println("设备 ${device.name} 在轨迹映射中不存在")
            }
        }
    }
}