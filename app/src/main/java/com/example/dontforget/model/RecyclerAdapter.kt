package com.example.dontforget.model



import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.dontforget.ScheduleDiffCallback
import com.example.dontforget.databinding.ScheduleItemViewBinding
import com.example.dontforget.model.db.ScheduleModel
import com.example.dontforget.model.db.TextStyleDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RecyclerAdapter(
    private val scheduleClickListener: ScheduleClickListener,
    private val textStyleDao: TextStyleDao
) : ListAdapter<ScheduleModel, RecyclerAdapter.Holder>(ScheduleDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val binding = ScheduleItemViewBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return Holder(binding)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.bind(getItem(position), scheduleClickListener)
    }

    fun removeItem(position: Int) {
        val currentList = currentList.toMutableList()
        currentList.removeAt(position)
        submitList(currentList)
    }

    class Holder(private val binding: ScheduleItemViewBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(schedule: ScheduleModel, scheduleClickListener: ScheduleClickListener) {
            with(binding) {
                scheduleInfo.text = schedule.scheduleText
                scheduleInfo.textSize = schedule.textSize

                if (schedule.title != null) {
                    scheduleTitle.text = schedule.title
                } else {
                    scheduleTitle.text = "미설정"
                }

                if (schedule.scheduleTime >= 1L) {
                    CoroutineScope(Dispatchers.Main).launch {
                        ddayCounter.text=DayCalculation().replaceDday(schedule.scheduleTime)

                    if (schedule.setNotification == 1) {
                        notificationMessage.text = "알림 On"
                    } else if (schedule.setNotification == 0) {
                        notificationMessage.text = "알림 Off"
                    }
                        scheduleDate.text = schedule.scheduleDate
                    }
                } else {
                    scheduleDate.text = "미설정"
                    ddayCounter.text = ""
                    notificationMessage.text = ""
                }

                itemView.setOnClickListener {
                    scheduleClickListener.onClick(schedule)
                }
            }
        }
    }

    class ScheduleClickListener(val clickListener: (schedule: ScheduleModel) -> Unit) {
        fun onClick(schedule: ScheduleModel) = clickListener(schedule)
    }
}


//Todo:내용을 입력하지 않고 날짜만 고르고 메모를 작성하면 출력문제생김