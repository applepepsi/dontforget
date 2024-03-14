package com.example.dontforget

import androidx.recyclerview.widget.DiffUtil
import com.example.dontforget.model.db.ScheduleModel

class ScheduleDiffCallback : DiffUtil.ItemCallback<ScheduleModel>() {
    override fun areItemsTheSame(oldItem: ScheduleModel, newItem: ScheduleModel): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: ScheduleModel, newItem: ScheduleModel): Boolean {
        return oldItem == newItem
    }
}