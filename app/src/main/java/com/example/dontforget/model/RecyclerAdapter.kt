package com.example.dontforget.model



import android.content.Context
import android.graphics.Color
import android.text.Editable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.dontforget.R
import com.example.dontforget.ScheduleDiffCallback
import com.example.dontforget.databinding.ActivityMainBinding
import com.example.dontforget.databinding.ScheduleItemViewBinding

import com.example.dontforget.model.db.ScheduleModel
import com.example.dontforget.model.db.TextStyleDao
import com.example.dontforget.model.db.TextStyleModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.nio.file.Files.size
import java.text.FieldPosition
import java.text.SimpleDateFormat

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
            holder.bind(scheduleList[position], scheduleClickListener, textStyleDao)
        }
    }

    override fun getItemCount()=scheduleList.size

    fun updateList(newList:List<ScheduleModel>){
        val diffCallback= ScheduleDiffCallback(scheduleList,newList)
        val diffResult=DiffUtil.calculateDiff(diffCallback)

        scheduleList = newList.toMutableList()
        Log.d("리스트값 확인", scheduleList.toString())
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

        suspend fun bind(schedule: ScheduleModel, scheduleClickListener: ScheduleClickListener, textStyleDao: TextStyleDao) {
            with(binding) {

                scheduleInfo.text = schedule.scheduleText

                scheduleInfo.textSize=schedule.textSize

                val textStyle= textStyleDao.getTextStylesByScheduleId(schedule.id!!)
                Log.d("들어온 값확인", textStyle.toString())
                //만약 사용자가 설정한 시간이 1이상으로 설정됐다면
                if(schedule.scheduleTime>=1L){
                    var currentTime=DayCalculation().getCurrentDateMillis()
                    val Dday=DayCalculation().calculationDday(schedule.scheduleTime,currentTime)
                    scheduleDate.text=schedule.scheduleDate
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
                else{
                    scheduleDate.text="미설정"
                    ddayCounter.text = ""
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