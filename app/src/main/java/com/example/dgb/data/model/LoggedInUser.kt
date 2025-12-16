package com.example.dgb.data.model

/**
 * 用户角色枚举
 */
enum class UserRole {
    ADMIN,      // 管理员：拥有所有权限
    OPERATOR,   // 操作员：拥有设备操作权限
    VIEWER      // 查看员：仅拥有查看权限
}

/**
 * Data class that captures user information for logged in users retrieved from LoginRepository
 */
data class LoggedInUser(
    val userId: String,
    val displayName: String,
    val role: UserRole = UserRole.VIEWER, // 用户角色，默认为查看员
    val email: String? = null,            // 电子邮件
    val phone: String? = null             // 电话号码
)