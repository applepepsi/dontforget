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

        fun bind(schedule: ScheduleModel, scheduleClickListener: ScheduleClickListener) {
            with(binding) {


                scheduleInfo.text = schedule.scheduleText

                scheduleInfo.textSize=schedule.textSize

                if(schedule.title!=null){
                    scheduleTitle.text=schedule.title

                }
                else{
                    scheduleTitle.text="미설정"
                }


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


//                val drawableDown = ContextCompat.getDrawable(binding.root.context, R.drawable.ic_baseline_arrow_drop_down_24)
//                val drawableUp = ContextCompat.getDrawable(binding.root.context, R.drawable.ic_baseline_arrow_drop_up_24)
//                var index = false
//                Log.d("라인카운트", schedule.lineCount.toString())


//                extensionButton.setImageDrawable(drawableDown)
//                scheduleInfo.setPadding(10, 5, 10, 20)
//                scheduleInfo.maxLines = 1
//                scheduleInfo.ellipsize = TextUtils.TruncateAt.END
//
//                Log.d("인덱스id", scheduleInfo.id.toString())
//                Log.d("인덱스값", index.toString())
//
//                extensionButton.setOnClickListener {
//                    if (index) {
//                        extensionButton.setImageDrawable(drawableDown)
//                        scheduleInfo.maxLines = 1
//                        scheduleInfo.ellipsize = TextUtils.TruncateAt.END
//
//                        index = false
//                        Log.d("인덱스값", index.toString())
//                    } else if (!index) {
//                        extensionButton.setImageDrawable(drawableUp)
//                        scheduleInfo.maxLines = Integer.MAX_VALUE
//                        index = true
//                        Log.d("인덱스값", index.toString())
//                    }
//                }


//                scheduleInfo.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
//                    override fun onGlobalLayout() {
//                        val layout = scheduleInfo.layout
//                        val line=layout.lineCount
//
//                        scheduleInfo.viewTreeObserver.removeOnGlobalLayoutListener(this)
//                    }
//                })

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