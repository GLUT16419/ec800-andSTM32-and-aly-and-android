package com.example.dgb.data

import com.example.dgb.data.model.LoggedInUser
import com.example.dgb.data.model.UserRole
import java.io.IOException

/**
 * Class that handles authentication w/ login credentials and retrieves user information.
 */
class LoginDataSource {

    fun login(username: String, password: String): Result<LoggedInUser> {
        try {
            // 根据用户名和密码返回不同角色的用户
            // 预设的账户密码验证
            when {
                username == "admin" && password == "admin123" -> {
                    // 管理员账户
                    val adminUser = LoggedInUser(
                        userId = "admin-001",
                        displayName = "管理员",
                        role = UserRole.ADMIN,
                        email = "admin@example.com",
                        phone = "13800138000"
                    )
                    return Result.Success(adminUser)
                }
                username == "user" && password == "password123" -> {
                    // 普通操作员账户
                    val operatorUser = LoggedInUser(
                        userId = "user-001",
                        displayName = "普通用户",
                        role = UserRole.OPERATOR,
                        email = "user@example.com",
                        phone = "13900139000"
                    )
                    return Result.Success(operatorUser)
                }
                else -> {
                    // 无效的用户名或密码
                    return Result.Error(IOException("用户名或密码错误"))
                }
            }
        } catch (e: Throwable) {
            return Result.Error(IOException("登录失败: ${e.message}", e))
        }
    }

    fun logout() {
        // TODO: revoke authentication
    }
}