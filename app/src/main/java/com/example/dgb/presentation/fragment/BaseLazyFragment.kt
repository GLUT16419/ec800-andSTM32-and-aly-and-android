package com.example.dgb.presentation.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment

abstract class BaseLazyFragment : Fragment() {
    
    // 标记Fragment是否已经初始化
    private var isInitialized = false
    // 标记Fragment是否可见
    private var isVisibleToUser = false
    // 标记View是否已经创建
    private var isViewCreated = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(getLayoutId(), container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        isViewCreated = true
        // 初始化视图
        initView(view)
        // 检查是否需要加载数据
        checkLazyLoad()
    }

    override fun onResume() {
        super.onResume()
        // 检查是否需要加载数据
        if (isVisibleToUser && !isInitialized) {
            lazyLoad()
            isInitialized = true
        }
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        this.isVisibleToUser = isVisibleToUser
        // 检查是否需要加载数据
        checkLazyLoad()
    }

    private fun checkLazyLoad() {
        if (isVisibleToUser && isViewCreated && !isInitialized) {
            lazyLoad()
            isInitialized = true
        }
    }

    // 获取布局ID
    abstract fun getLayoutId(): Int
    
    // 初始化视图
    abstract fun initView(view: View)
    
    // 懒加载数据
    abstract fun lazyLoad()
}
