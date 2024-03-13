package com.example.dontforget.model



import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context.NOTIFICATION_SERVICE
import android.graphics.Color
import android.graphics.Typeface
import android.os.Build
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.dontforget.R
import com.example.dontforget.ScheduleDiffCallback
import com.example.dontforget.databinding.ScheduleItemViewBinding
import com.example.dontforget.model.db.ScheduleModel
import com.example.dontforget.model.db.TextStyleDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RecyclerAdapter(private var scheduleList: List<ScheduleModel>,
                      private val scheduleClickListener: ScheduleClickListener,
                      private val textStyleDao: TextStyleDao
                        ): RecyclerView.Adapter<RecyclerAdapter.Holder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val binding=ScheduleItemViewBinding.inflate(
            LayoutInflater.from(parent.context),parent,false)
        return Holder((binding)
        )
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {

        CoroutineScope(Dispatchers.Main).launch {
            holder.bind(scheduleList[position], scheduleClickListener)
        }
    }

    override fun getItemCount()=scheduleList.size

    fun updateList(newList: List<ScheduleModel>) {
        val diffCallback = ScheduleDiffCallback(scheduleList, newList)
        val diffResult = DiffUtil.calculateDiff(diffCallback)

        scheduleList = newList.toMutableList()
        diffResult.dispatchUpdatesTo(this)
    }

    fun removeItem(position: Int){
        Log.d("리스트 크기", "Before removeItem: ${scheduleList.size}")
        val newList = scheduleList.toMutableList()
        newList.removeAt(position)
        updateList(newList)

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
                        val currentTime = withContext(Dispatchers.Default) {
                            DayCalculation().getCurrentDateMillis()
                        }
                        val Dday = withContext(Dispatchers.Default) {
                            Log.d("타임", currentTime.toString())
                            Log.d("타임스케쥴", schedule.scheduleTime.toString())
                            DayCalculation().calculationDday(schedule.scheduleTime, currentTime)
                        }
                        if (Dday != null) {
                            if (Dday > 0L) {
                                ddayCounter.text = "D - ${Dday.toString()}"
                            } else if (Dday == 0L) {
                                ddayCounter.text = "D - Day"
                            } else {
                                ddayCounter.text = "만료"
                            }
                        }
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


    class ScheduleClickListener(val clickListener: (schedule: ScheduleModel)->Unit){
        fun onClick(schedule: ScheduleModel)=clickListener(schedule)
    }
}


//Todo:내용을 입력하지 않고 날짜만 고르고 메모를 작성하면 출력문제생김