package com.example.dgb.ui

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.dgb.data.EventLogger
import com.example.dgb.data.EventLevel
import com.example.dgb.data.EventLogEntity
import com.example.dgb.databinding.FragmentEventLogBinding
import kotlinx.coroutines.launch

/**
 * 事件日志查看Fragment
 */
class EventLogFragment : Fragment() {
    
    private var _binding: FragmentEventLogBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var eventLogger: EventLogger
    private lateinit var eventLogAdapter: EventLogAdapter
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentEventLogBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // 初始化事件日志记录器
        eventLogger = EventLogger.getInstance(requireContext())
        
        // 初始化RecyclerView
        initRecyclerView()
        
        // 加载事件日志
        loadEventLogs()
        
        // 设置刷新按钮点击事件
        binding.refreshButton.setOnClickListener {
            loadEventLogs()
        }
    }
    
    /**
     * 初始化RecyclerView
     */
    private fun initRecyclerView() {
        eventLogAdapter = EventLogAdapter()
        binding.eventLogRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = eventLogAdapter
        }
    }
    
    /**
     * 加载事件日志
     */
    private fun loadEventLogs() {
        lifecycleScope.launch {
            try {
                // 显示加载状态
                binding.loadingIndicator.visibility = View.VISIBLE
                
                // 获取最新的100条事件日志
                val eventLogs = eventLogger.getLatestEventLogs(100)
                
                // 更新RecyclerView
                eventLogAdapter.submitList(eventLogs)
                
                // 隐藏加载状态
                binding.loadingIndicator.visibility = View.GONE
                
                // 如果没有日志，显示提示信息
                if (eventLogs.isEmpty()) {
                    binding.emptyStateText.visibility = View.VISIBLE
                } else {
                    binding.emptyStateText.visibility = View.GONE
                }
            } catch (e: Exception) {
                Log.e(TAG, "加载事件日志失败", e)
                binding.loadingIndicator.visibility = View.GONE
                binding.emptyStateText.text = "加载失败，请重试"
                binding.emptyStateText.visibility = View.VISIBLE
            }
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    
    companion object {
        private const val TAG = "EventLogFragment"
        
        /**
         * 创建新实例
         */
        fun newInstance(): EventLogFragment {
            return EventLogFragment()
        }
    }
}

/**
 * 事件日志适配器
 */
class EventLogAdapter : androidx.recyclerview.widget.ListAdapter<EventLogEntity, EventLogAdapter.EventLogViewHolder>(
    object : androidx.recyclerview.widget.DiffUtil.ItemCallback<EventLogEntity>() {
        override fun areItemsTheSame(oldItem: EventLogEntity, newItem: EventLogEntity): Boolean {
            return oldItem.id == newItem.id
        }
        
        override fun areContentsTheSame(oldItem: EventLogEntity, newItem: EventLogEntity): Boolean {
            return oldItem == newItem
        }
    }
) {
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventLogViewHolder {
        val view = android.view.LayoutInflater.from(parent.context)
            .inflate(android.R.layout.simple_list_item_2, parent, false)
        return EventLogViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: EventLogViewHolder, position: Int) {
        val eventLog = getItem(position)
        holder.bind(eventLog)
    }
    
    /**
     * 事件日志视图持有者
     */
    class EventLogViewHolder(itemView: android.view.View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView) {
        
        private val titleTextView: android.widget.TextView = itemView.findViewById(android.R.id.text1)
        private val subtitleTextView: android.widget.TextView = itemView.findViewById(android.R.id.text2)
        private val dateFormat = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault())
        
        fun bind(eventLog: EventLogEntity) {
            // 设置标题
            val level = EventLevel.values().find { it.value == eventLog.level }?.name ?: "UNKNOWN"
            val timestamp = dateFormat.format(java.util.Date(eventLog.timestamp))
            titleTextView.text = "[$level] ${eventLog.message}"
            
            // 设置副标题
            val subtitle = buildString {
                append(timestamp)
                append(" | 类型: ${eventLog.type}")
                eventLog.deviceId?.let { append(" | 设备ID: $it") }
            }
            subtitleTextView.text = subtitle
            
            // 设置文本颜色
            when (EventLevel.values().find { it.value == eventLog.level }) {
                EventLevel.CRITICAL, EventLevel.ERROR -> {
                    titleTextView.setTextColor(itemView.context.getColor(android.R.color.holo_red_dark))
                }
                EventLevel.WARNING -> {
                    titleTextView.setTextColor(itemView.context.getColor(android.R.color.holo_orange_dark))
                }
                else -> {
                    titleTextView.setTextColor(itemView.context.getColor(android.R.color.primary_text_light))
                }
            }
        }
    }
}
