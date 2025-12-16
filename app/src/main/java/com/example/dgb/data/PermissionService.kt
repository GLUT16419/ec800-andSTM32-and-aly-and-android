package com.example.dgb.data

import com.example.dgb.data.model.LoggedInUser
import com.example.dgb.data.model.UserRole

/**
 * 权限服务类，用于检查用户是否具有执行特定操作的权限
 */
class PermissionService(private val loginRepository: LoginRepository) {
    // 操作权限枚举
    enum class PermissionAction {
        VIEW_DEVICES,        // 查看设备列表
        EDIT_DEVICE,         // 编辑设备信息
        DELETE_DEVICE,       // 删除设备
        BATCH_OPERATION,     // 批量操作
        VIEW_HISTORY,        // 查看历史数据
        EXPORT_DATA,         // 导出数据
        MANAGE_USERS,        // 管理用户
        CONFIGURE_SYSTEM     // 配置系统
    }
    
    // 检查用户是否具有执行特定操作的权限
    fun hasPermission(action: PermissionAction): Boolean {
        val currentUser = loginRepository.user ?: return false
        
        return when (action) {
            // 所有角色都可以查看设备列表和历史数据
            PermissionAction.VIEW_DEVICES, PermissionAction.VIEW_HISTORY -> true
            
            // 操作员和管理员可以编辑设备信息和导出数据
            PermissionAction.EDIT_DEVICE, PermissionAction.EXPORT_DATA -> 
                currentUser.role in listOf(UserRole.OPERATOR, UserRole.ADMIN)
            
            // 只有管理员可以删除设备、批量操作、管理用户和配置系统
            PermissionAction.DELETE_DEVICE, PermissionAction.BATCH_OPERATION, 
            PermissionAction.MANAGE_USERS, PermissionAction.CONFIGURE_SYSTEM -> 
                currentUser.role == UserRole.ADMIN
        }
    }
    
    // 获取当前用户的角色
    fun getCurrentUserRole(): UserRole? {
        return loginRepository.user?.role
    }
    
    // 获取用户可以执行的所有操作列表
    fun getUserPermissions(): List<PermissionAction> {
        return PermissionAction.values().filter { hasPermission(it) }
    }
    
    // 检查用户是否是管理员
    fun isAdmin(): Boolean {
        return getCurrentUserRole() == UserRole.ADMIN
    }
    
    // 检查用户是否是操作员
    fun isOperator(): Boolean {
        val role = getCurrentUserRole()
        return role == UserRole.OPERATOR || role == UserRole.ADMIN
    }
    
    // 检查用户是否只是查看员
    fun isViewer(): Boolean {
        return getCurrentUserRole() == UserRole.VIEWER
    }
}
