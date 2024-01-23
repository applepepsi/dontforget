package com.example.dontforget.model



import android.content.Context
import android.graphics.Color
import android.text.Editable
import android.util.Log
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

                scheduleInfo.text = schedule.scheduleText

                scheduleInfo.textSize=schedule.textSize

                //만약 사용자가 설정한 시간이 1이상으로 설정됐다면
                if(schedule.scheduleTime>=1L){
                    var currentTime=DayCalculation().getCurrentDateMillis()
                    val Dday=DayCalculation().calculationDday(schedule.scheduleTime,currentTime)

                    //Dday를 계산했을때의 if문
                   if(Dday!!>0L){
                        ddayCounter.text = "D - ${Dday.toString()}"
                    }
                    else if(Dday==0L){
                        ddayCounter.text = "D - Day"
                    }
                    else{
                        ddayCounter.text="만료"
                    }
                }

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