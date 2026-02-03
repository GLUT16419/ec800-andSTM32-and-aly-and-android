// LoginScreenWithRemember.kt
package com.example.dgb

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.tooling.preview.Preview

// 预设的账户密码
private const val PRESET_USERNAME = "user"
private const val PRESET_PASSWORD = "password123"
private const val PRESET_ADMIN_USERNAME = "admin"
private const val PRESET_ADMIN_PASSWORD = "admin123"

class LoginActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 检查是否已经记住密码并登录过
        val prefs = getSharedPreferences("login_prefs", Context.MODE_PRIVATE)
        val username = prefs.getString("username", "")
        val password = prefs.getString("password", "")
        val rememberMe = prefs.getBoolean("rememberMe", false)

        // 如果记住了密码且保存的用户名密码与预设的一致，则直接跳转到 MainActivity
        if (rememberMe && (
                (username == PRESET_USERNAME && password == PRESET_PASSWORD) ||
                (username == PRESET_ADMIN_USERNAME && password == PRESET_ADMIN_PASSWORD)
            )
        ) {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish() // 关闭当前登录页面
            return // 直接返回，不显示登录界面
        }

        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    LoginScreenWithRemember(
                        onLoginSuccess = {
                            // 登录成功后跳转到 MainActivity
                            val intent = Intent(this@LoginActivity, MainActivity::class.java)
                            startActivity(intent)
                            finish() // 关闭当前登录页面
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun LoginScreenWithRemember(onLoginSuccess: () -> Unit) {
    val context = LocalContext.current

    // 从SharedPreferences读取保存的账户信息
    val prefs = remember {
        context.getSharedPreferences("login_prefs", Context.MODE_PRIVATE)
    }

    var username by remember {
        mutableStateOf(prefs.getString("username", "") ?: "")
    }
    var password by remember {
        mutableStateOf(prefs.getString("password", "") ?: "")
    }
    var passwordVisible by remember { mutableStateOf(false) }
    var rememberMe by remember {
        mutableStateOf(prefs.getBoolean("rememberMe", false))
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // 标题
        Text(
            text = "用户登录",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 48.dp)
        )

        // 用户名输入框
        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("用户名") },
            leadingIcon = { Icon(Icons.Default.Email, "用户名") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            placeholder = { Text("输入 $PRESET_USERNAME") }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 密码输入框
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("密码") },
            leadingIcon = { Icon(Icons.Default.Lock, "密码") },
            trailingIcon = {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        if (passwordVisible) Icons.Default.Visibility
                        else Icons.Default.VisibilityOff,
                        "切换密码可见性"
                    )
                }
            },
            visualTransformation = if (passwordVisible) {
                VisualTransformation.None
            } else {
                PasswordVisualTransformation()
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            placeholder = { Text("输入 $PRESET_PASSWORD") }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 记住密码选项
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = rememberMe,
                onCheckedChange = { rememberMe = it }
            )
            Text(
                text = "记住密码",
                fontSize = 14.sp
            )

            Spacer(modifier = Modifier.weight(1f))

            // 清除记住的密码按钮
            TextButton(onClick = {
                prefs.edit().clear().apply()
                username = ""
                password = ""
                rememberMe = false
                Toast.makeText(context, "已清除保存的登录信息", Toast.LENGTH_SHORT).show()
            }) {
                Text("清除密码")
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // 登录按钮
        Button(
            onClick = {
                // 验证账户密码
                when {
                    username.isEmpty() || password.isEmpty() -> {
                        Toast.makeText(context, "请输入用户名和密码", Toast.LENGTH_SHORT).show()
                    }
                    username == PRESET_USERNAME && password == PRESET_PASSWORD -> {
                        // 保存账户信息
                        if (rememberMe) {
                            prefs.edit()
                                .putString("username", username)
                                .putString("password", password)
                                .putBoolean("rememberMe", true)
                                .apply()
                        } else {
                            prefs.edit().clear().apply()
                        }

                        Toast.makeText(context, "登录成功！", Toast.LENGTH_SHORT).show()

                        // 触发登录成功回调，跳转到MainActivity
                        onLoginSuccess()
                    }
                    else -> {
                        Toast.makeText(context, "用户名或密码错误！", Toast.LENGTH_SHORT).show()
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("登录", fontSize = 18.sp)
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 快速登录提示
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "预设账户信息",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Text("普通用户")
                Text("用户名: $PRESET_USERNAME")
                Text("密码: $PRESET_PASSWORD")
                Spacer(modifier = Modifier.height(8.dp))
                Text("管理员")
                Text("用户名: $PRESET_ADMIN_USERNAME")
                Text("密码: $PRESET_ADMIN_PASSWORD")
            }
        }
    }
}

// 预览函数
@Preview
@Composable
fun LoginScreenPreview() {
    MaterialTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            LoginScreenWithRemember(onLoginSuccess = {})
        }
    }
}