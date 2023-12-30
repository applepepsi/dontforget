package com.example.dontforget.model



import android.content.Context
import android.text.Editable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.dontforget.R
import com.example.dontforget.databinding.ActivityMainBinding
import com.example.dontforget.databinding.ScheduleItemViewBinding

import com.example.dontforget.model.db.ScheduleModel
import java.nio.file.Files.size
import java.text.FieldPosition
import java.text.SimpleDateFormat

class RecyclerAdapter(private val scheduleList: List<ScheduleModel>,
                        private val scheduleClickListener: ScheduleClickListener
                        ): RecyclerView.Adapter<RecyclerAdapter.Holder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val binding=ScheduleItemViewBinding.inflate(
            LayoutInflater.from(parent.context),parent,false)
        return Holder((binding)
        )
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.bind(scheduleList[position], scheduleClickListener)


    }

    override fun getItemCount()=scheduleList.size

    class Holder(private val binding: ScheduleItemViewBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(schedule: ScheduleModel, scheduleClickListener: ScheduleClickListener) {
            with(binding) {
//                scheduleNo.text = "${schedule.id}"
                scheduleInfo.text = schedule.scheduleText

                scheduleInfo.textSize=schedule.textSize
//                val date = SimpleDateFormat("yyyy/MM/dd hh:mm")
//                scheduleDate.text = date.format(schedule.scheduleTime)
                if(schedule.scheduleTime>=1L){
                    var currentTime=DayCalculation().getCurrentDateMillis()
                    val Dday=DayCalculation().calculationDday(schedule.scheduleTime,currentTime)
                    if(Dday!!>=1L){
                        ddayCounter.text = "D - ${Dday.toString()}"
                    }
                    else{
                        ddayCounter.text = "D - Day"
                    }
                }
//                else if(schedule.scheduleTime<=1L){
//                    ddayCounter.text=""
//                }


                scheduleDate.text=schedule.scheduleDate

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