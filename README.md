# 冷链监控系统 Android 客户端

## 项目简介

冷链监控系统 Android 客户端是一个基于物联网技术的应用程序，用于实时监控冷藏运输车辆的环境参数，包括温度、湿度、氧气浓度等，并提供地理位置追踪、历史数据查询、异常告警等功能。

## 功能特性

- **实时监控**：实时获取并显示冷链设备的环境参数
- **地理位置追踪**：显示设备的实时位置和历史轨迹
- **历史数据查询**：查看设备的历史数据和趋势图表
- **异常告警**：当环境参数异常时，发送告警通知
- **多设备管理**：支持同时管理多个冷链设备

## 技术栈

- **开发语言**：Kotlin
- **UI框架**：Jetpack Compose
- **网络通信**：MQTT、阿里云IoT LinkKit
- **地图服务**：高德地图SDK
- **数据库**：Room
- **协程**：Kotlin Coroutines

## 系统架构

- **客户端**：Android 应用，负责数据展示和用户交互
- **服务端**：阿里云 IoT 平台，负责设备连接和数据转发
- **设备端**：STM32 控制器，负责采集环境参数并上传

## 安装说明

1. **克隆仓库**
   ```bash
   git clone https://github.com/GLUT16419/ec800-andSTM32-and-aly-and-android.git
   ```

2. **打开项目**
   使用 Android Studio 打开项目目录

3. **配置依赖**
   项目使用 Gradle 管理依赖，同步项目后会自动下载所需依赖

4. **构建项目**
   点击 Android Studio 中的 "Build" -> "Build Project" 构建项目

5. **运行应用**
   连接 Android 设备或使用模拟器运行应用

## 配置说明

### MQTT 连接配置

在 `MqttService.kt` 文件中配置 MQTT 连接参数：

```kotlin
private val PRODUCTKEY = "your_product_key"
private var DEVICENAME = "your_device_name"
private val DEVICESECRET = "your_device_secret"
private val PORT = "1883"
var hosturl = "your_mqtt_host:" + PORT
val InstanceID = "your_instance_id"
```

### 高德地图 API 配置

在 `AndroidManifest.xml` 文件中配置高德地图 API 密钥：

```xml
<meta-data
    android:name="com.amap.api.v2.apikey"
    android:value="your_amap_api_key" />
```

## 项目结构

```
app/
├── src/
│   ├── main/
│   │   ├── java/com/example/dgb/
│   │   │   ├── core/            # 核心功能模块
│   │   │   ├── data/            # 数据层
│   │   │   ├── feature/         # 功能模块
│   │   │   ├── presentation/    # 表示层
│   │   │   └── utils/           # 工具类
│   │   ├── res/                 # 资源文件
│   │   └── AndroidManifest.xml  # 应用配置文件
│   └── test/                    # 测试代码
└── build.gradle.kts             # 模块构建配置
```

## 开发环境

- **Android Studio**：2023.1.1 或更高版本
- **Kotlin**：1.9.0 或更高版本
- **Android SDK**：API Level 36
- **构建工具**：Gradle 8.0 或更高版本

## 贡献指南

1. **Fork 仓库**
2. **创建分支**
   ```bash
   git checkout -b feature/your-feature
   ```
3. **提交更改**
   ```bash
   git commit -m "Add your feature"
   ```
4. **推送到分支**
   ```bash
   git push origin feature/your-feature
   ```
5. **创建 Pull Request**

## 许可证

本项目采用 MIT 许可证，详见 [LICENSE](LICENSE) 文件。

## 联系方式

如有问题或建议，请通过以下方式联系：

- **GitHub Issues**：在仓库中创建 Issues
- **Email**：your-email@example.com

---

**注意**：本项目为示例代码，实际使用时需要替换为真实的配置参数。