package com.example.dgb

import android.app.Service
import android.content.ContentValues.TAG
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.aliyun.alink.dm.api.DeviceInfo
import com.aliyun.alink.dm.api.IoTApiClientConfig
import com.aliyun.alink.linkkit.api.ILinkKitConnectListener
import com.aliyun.alink.linkkit.api.IoTDMConfig
import com.aliyun.alink.linkkit.api.IoTMqttClientConfig
import com.aliyun.alink.linkkit.api.LinkKit
import com.aliyun.alink.linkkit.api.LinkKitInitParams
import com.aliyun.alink.linksdk.channel.core.persistent.PersistentNet
import com.aliyun.alink.linksdk.cmp.connect.channel.MqttSubscribeRequest
import com.aliyun.alink.linksdk.cmp.core.base.AMessage
import com.aliyun.alink.linksdk.cmp.core.base.ARequest
import com.aliyun.alink.linksdk.cmp.core.base.ConnectState
import com.aliyun.alink.linksdk.cmp.core.listener.IConnectNotifyListener
import com.aliyun.alink.linksdk.cmp.core.listener.IConnectSubscribeListener
import com.aliyun.alink.linksdk.tmp.device.payload.ValueWrapper
import com.aliyun.alink.linksdk.tools.AError
import com.aliyun.alink.linksdk.tools.ALog
import org.json.JSONObject

class MqttService : Service() {

    private val PRODUCTKEY = "k21inrJttUu"
    private var DEVICENAME = "android"
    private val DEVICESECRET = "26b9547a2fa381c978868c7b578c9271"
    private val PRODUCTSECRET = ""
    private val PORT="1883"
    val params: LinkKitInitParams = LinkKitInitParams()
    var hosturl="iot-06z00ccayws04qj.mqtt.iothub.aliyuncs.com:"+PORT
    val InstanceID="iot-06z00ccayws04qj"

    public

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
    private fun parseAndHandleMessage(jsonString: String) {
        try {
            val rootObject = JSONObject(jsonString)
            val deviceId = rootObject.optString("deviceId")
            val dataObject = rootObject.optJSONObject("data")
            if (dataObject != null) {
                val temperature = dataObject.optDouble("temperature")
                val humidity = dataObject.optInt("humidity")
                Log.d(TAG, "解析结果 -> 设备: $deviceId, 温度: $temperature, 湿度: $humidity")
                // TODO: 在这里处理解析出的数据，例如更新UI、保存到数据库或触发其他逻辑
                // 例如：sendBroadcastToUpdateUI(temperature, humidity)
            }
        } catch (e: Exception) {
            Log.e(TAG, "解析JSON失败", e)
        }
    }
    fun deviceConnect()
    {
        //获取MQTT连接信息
        var deviceinfo= DeviceInfo()
        deviceinfo.productKey=PRODUCTKEY
        deviceinfo.deviceName=DEVICENAME
        deviceinfo.deviceSecret=DEVICESECRET
        deviceinfo.productSecret=PRODUCTSECRET
        params.deviceInfo = deviceinfo;


        //Step2: 全局默认域名
        val userData = IoTApiClientConfig()
        params.connectConfig = userData


        //Step3: 物模型缓存
        val propertyValues: MutableMap<String?, ValueWrapper<*>?> =
            HashMap<String?, ValueWrapper<*>?>()

        /**
         * 物模型的数据会缓存到该字段中，不可删除或者设置为空，否则功能会异常
         * 用户调用物模型上报接口之后，物模型会有相关数据缓存。
         */
        params.propertyValues = propertyValues


        //Step4: mqtt设置
        /**
         * Mqtt 相关参数设置，包括接入点等信息，具体参见deviceinfo文件说明
         * 域名、产品密钥、认证安全模式等；
         */
        val clientConfig = IoTMqttClientConfig()
        clientConfig.receiveOfflineMsg = true //cleanSession=1 不接受离线消息

        //mqtt接入点信息
        clientConfig.channelHost = hosturl
        params.mqttClientConfig = clientConfig
        //如果灭屏情况下经常出现设备离线，请参考下述的"设置自定义心跳和解决灭屏情况下的心跳不准问题"一节

        //Step5: 高阶功能配置，除物模型外，其余默认均为关闭状态
        val ioTDMConfig = IoTDMConfig()

        // 默认开启物模型功能，物模型初始化（包含请求云端物模型）完成后才返回onInitDone
        ioTDMConfig.enableThingModel = true

        // 默认不开启网关功能，开启之后，初始化的时候会初始化网关模块，获取云端网关子设备列表
        ioTDMConfig.enableGateway = false

        // 默认不开启，是否开启日志推送功能
        ioTDMConfig.enableLogPush = false
        params.ioTDMConfig = ioTDMConfig

        val client=LinkKit.getInstance()
        //Step6: 下行消息处理回调设置
        client.registerOnPushListener(object : IConnectNotifyListener {
            override fun onNotify(s: String?, s1: String?, aMessage: AMessage?) {
                //TODO: 处理下行消息的回调,请参考文档
                // 1. 安全判断：确认消息不为空
                if (aMessage == null) {
                    Log.w(TAG, "收到空消息")
                    return
                }

                // 2. 提取消息负载（payload）：核心步骤
                // aMessage.data 通常是 ByteArray 类型
                val payloadBytes = aMessage.data as? ByteArray
                if (payloadBytes == null) {
                    Log.e(TAG, "消息负载格式不正确")
                    return
                }

                // 3. 将字节数组转换为可读的字符串（假设是UTF-8编码）
                try {
                    val messageContent = String(payloadBytes, Charsets.UTF_8)
                    Log.d(TAG, "收到消息 -> Topic相关标识: $s, 内容: $messageContent")

                    // TODO: 4. 在这里根据你的业务逻辑处理 messageContent
                    // 例如，如果消息是JSON格式，可以在这里解析出具体的属性值
                    parseAndHandleMessage(messageContent)

                } catch (e: Exception) {
                    Log.e(TAG, "解析消息内容时出错", e)
                }}

            override fun shouldHandle(s: String?, s1: String?): Boolean {
                return true //TODO 根据实际情况设置,请参考文档

            }

            override fun onConnectStateChange(connectId: String?, connectState: ConnectState?) {
                // 对应连接类型的连接状态变化回调，具体连接状态参考SDK ConnectState
                Log.d(
                    TAG,
                    "onConnectStateChange() called with: connectId = [" + connectId + "], connectState = [" + connectState + "]"
                )


                //首次连云可能失败。对于首次连云失败，SDK会报出ConnectState.CONNECTFAIL这种状态。对于这种场景，用户可以尝试若干次后退出，也可以一直重试直到连云成功
                //TODO: 以下是首次建连时用户主动重试的一个参考实现，用户可以打开下面注释使能下述代码
                if(connectState == ConnectState.CONNECTFAIL){
                    try{
                        Thread.sleep(5000);
                        PersistentNet.getInstance().reconnect();
                    }catch (e: InterruptedException){
                        Log.d(TAG, "exception is " + e);
                    };
                    Log.d(TAG, "onConnectStateChange() try to reconnect when connect failed");
                }

                //SDK连云成功后，后续如果网络波动导致连接断开时，SDK会抛出ConnectState.DISCONNECTED这种状态。在这种情况下，SDK会自动尝试重连，重试的间隔是1s、2s、4s、8s...128s...128s，到了最大间隔128s后，会一直以128s为间隔重连直到连云成功。
            }
        })

        /**
         * 设备初始化建联
         * onError 初始化建联失败，如果因网络问题导致初始化失败，需要用户重试初始化
         * onInitDone 初始化成功
         */
        client.init(getApplicationContext(), params, object : ILinkKitConnectListener {
            override fun onError(p0: AError) {
                ALog.d(TAG, "onError() called with: error = [" + p0 + "]")
            }

            override fun onInitDone(data: Any?) {
                ALog.d(TAG, "onInitDone() called with: data = [" + data + "]")
            }
        })


    }
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        deviceConnect()
        return START_STICKY
    }
}