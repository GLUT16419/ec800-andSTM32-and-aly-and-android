package com.example.dgb

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import com.example.dgb.ui.theme.DgbTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        startService(Intent(this, MqttService::class.java))
        enableEdgeToEdge()
        setContent {
            DgbTheme {
                DgbApp()
            }
        }
    }
}

@PreviewScreenSizes
@Composable
fun DgbApp() {
    var currentDestination by rememberSaveable { mutableStateOf(AppDestinations.HOME) }

    NavigationSuiteScaffold(
        navigationSuiteItems = {
            AppDestinations.entries.forEach {
                item(
                    icon = {
                        Icon(
                            it.icon,
                            contentDescription = it.label
                        )
                    },
                    label = { Text(it.label) },
                    selected = it == currentDestination,
                    onClick = { currentDestination = it }
                )
            }
        }
    ) {
        when (currentDestination) {
            AppDestinations.HOME -> HomeScreen()
            AppDestinations.DEVICE -> DeviceScreen()
            AppDestinations.USER -> UserScreen()
        }
    }
}

enum class AppDestinations(
    val label: String,
    val icon: ImageVector,
) {
    HOME("主页", Icons.Default.Home),
    DEVICE("已注册设备", Icons.Default.Menu),
    USER("个人", Icons.Default.AccountBox),
}

@Composable
fun HomeScreen() {
    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        Column(modifier =
            Modifier.padding(innerPadding).fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally)
        {
            Text("欢迎访问冷链卫士", style = MaterialTheme.typography.headlineMedium)
            // 这里可以添加更多主页内容
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = { /* TODO */ }) {
                Text("示例按钮")
            }
        }
    }
}

@Composable
fun DeviceScreen() {
    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        Column(modifier =
            Modifier.padding(innerPadding).fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally)
        {
            Text("已注册设备", style = MaterialTheme.typography.headlineMedium)
            // 这里可以添加设备列表等内容
        }
    }
}
@Composable
fun UserScreen() {
    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        Column(modifier =
            Modifier.padding(innerPadding).fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally)
        {
            Text("个人", style = MaterialTheme.typography.headlineMedium)
            // 这里可以添加个人信息等内容
        }
    }
}
