package com.example.dontforget.model

import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.dontforget.R
import com.example.dontforget.ScheduleDiffCallback
import com.example.dontforget.databinding.FilterItemViewBinding
import com.example.dontforget.databinding.ScheduleItemViewBinding
import com.example.dontforget.model.db.ScheduleModel
import com.example.dontforget.model.db.TextStyleDao
import com.example.dontforget.util.ScheduleFilterData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FilterRecyclerAdapter(
    private val filterData: List<String>,
    private val scheduleFilterListener: FilterClickListener
) : RecyclerView.Adapter<FilterRecyclerAdapter.FilterViewHolder>() {

    var selectedItemIndex: Int? = 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FilterViewHolder {
        val binding = FilterItemViewBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return FilterViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FilterViewHolder, position: Int) {
        holder.bind(filterData[position], scheduleFilterListener,position == selectedItemIndex)
    }

    override fun getItemCount() = filterData.size

    class FilterViewHolder(private val binding: FilterItemViewBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(filterData: String, scheduleClickListener: FilterClickListener,isSelected: Boolean) {

            binding.filterItem.setText(filterData)
            if (isSelected) {
                itemView.background = ContextCompat.getDrawable(itemView.context, R.drawable.filter_on)
                binding.filterItem.setTextColor(Color.parseColor("#ffffff"))
            } else {
                itemView.background = ContextCompat.getDrawable(itemView.context, R.drawable.filter_off)
                binding.filterItem.setTextColor(Color.parseColor("#181818"))
            }
            itemView.setOnClickListener {
                scheduleClickListener.onClick(filterData)
            }
        }


    }

    class FilterClickListener(val scheduleClickListener: (filterCondition: String) -> Unit) {
        fun onClick(filterCondition: String) = scheduleClickListener(filterCondition)
    }

}