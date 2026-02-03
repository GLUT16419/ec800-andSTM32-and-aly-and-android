package com.example.dgb

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.dgb.presentation.fragment.BaseLazyFragment

class UserFragment : BaseLazyFragment() {

    private lateinit var logoutButton: Button
    private lateinit var usernameText: TextView
    private lateinit var userStatusText: TextView

    override fun getLayoutId(): Int {
        return R.layout.fragment_user
    }

    override fun initView(view: View) {
        val titleText = view.findViewById<TextView>(R.id.title_text)
        titleText.text = "个人中心"

        // 初始化视图
        logoutButton = view.findViewById(R.id.logout_button)
        usernameText = view.findViewById(R.id.username_text)
        userStatusText = view.findViewById(R.id.user_status_text)

        // 设置注销按钮点击事件
        logoutButton.setOnClickListener {
            showLogoutConfirmationDialog()
        }

        // 这里可以添加其他个人信息的功能
        setupUserInfoSection(view)
    }

    override fun lazyLoad() {
        // 加载用户信息
        loadUserInfo()
    }

    private fun loadUserInfo() {
        val prefs = requireContext().getSharedPreferences("login_prefs", Context.MODE_PRIVATE)
        val username = prefs.getString("username", "") ?: "未登录"
        val rememberMe = prefs.getBoolean("rememberMe", false)

        // 显示用户名
        usernameText.text = "用户名: $username"

        // 显示登录状态
        userStatusText.text = if (rememberMe && username.isNotEmpty()) {
            "登录状态: 已登录 (记住密码)"
        } else if (username.isNotEmpty()) {
            "登录状态: 已登录"
        } else {
            "登录状态: 未登录"
        }
    }

    private fun setupUserInfoSection(view: View) {
        // 可以在这里添加更多的用户信息显示
        val emailText = view.findViewById<TextView>(R.id.email_text)
        val phoneText = view.findViewById<TextView>(R.id.phone_text)

        // 示例数据，实际应从服务器或数据库获取
        emailText.text = "邮箱: user@example.com"
        phoneText.text = "电话: 138****5678"
    }

    private fun showLogoutConfirmationDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("确认注销")
            .setMessage("您确定要退出登录吗？")
            .setPositiveButton("确定") { dialog, _ ->
                performLogout()
                dialog.dismiss()
            }
            .setNegativeButton("取消") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun performLogout() {
        val context = requireContext()
        val prefs = context.getSharedPreferences("login_prefs", Context.MODE_PRIVATE)

        // 清除保存的登录信息
        prefs.edit().clear().apply()

        // 显示注销成功提示
        Toast.makeText(context, "已成功注销", Toast.LENGTH_SHORT).show()

        // 跳转到登录页面
        val intent = Intent(context, LoginActivity::class.java)

        // 清除返回栈，确保用户不能通过返回键回到个人页面
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

        startActivity(intent)

        // 结束当前Activity（如果Fragment所在的Activity需要结束）
        activity?.finish()
    }

    // 当Fragment重新显示时刷新用户信息
    override fun onResume() {
        super.onResume()
        loadUserInfo()
    }
}