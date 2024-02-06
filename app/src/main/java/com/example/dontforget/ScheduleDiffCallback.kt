package com.example.dontforget

import androidx.recyclerview.widget.DiffUtil
import com.example.dontforget.model.db.ScheduleModel

class ScheduleDiffCallback(
    private val oldList: List<ScheduleModel>,
    private val newList: List<ScheduleModel>
) : DiffUtil.Callback() {

    override fun getOldListSize() = oldList.size

    override fun getNewListSize() = newList.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition].id == newList[newItemPosition].id
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition] == newList[newItemPosition]
    }
}