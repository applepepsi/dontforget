package com.example.dontforget

import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.os.*
import androidx.appcompat.app.AppCompatActivity
import android.util.Log
import android.util.TypedValue
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.example.dontforget.databinding.ActivityMainBinding
import com.example.dontforget.model.EnterSchedule
import com.example.dontforget.model.ModifySchedule
import com.example.dontforget.model.RecyclerAdapter
import com.example.dontforget.model.db.*
import com.example.dontforget.spanInfo.ColorInfo
import com.example.dontforget.spanInfo.SizeInfo
import com.example.dontforget.util.ItemSpacingController
import com.example.dontforget.util.SwipeToDeleteCallback
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class MainActivity : AppCompatActivity() {

    val binding by lazy{ActivityMainBinding.inflate(layoutInflater)}

    lateinit var scheduleAdapter: RecyclerAdapter
    var scheduleList= mutableListOf<ScheduleModel>()

    lateinit var scheduleDao:ScheduleDao
    lateinit var textStyleDao:TextStyleDao
    private var currentSchedule: ScheduleModel? = null
    private var currentTextStyle:TextStyleModel?=null
    val space=20

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val itemSpacingController = ItemSpacingController(space)
        scheduleDao = ScheduleHelper.getDatabase(this).scheduleDao()
        textStyleDao=ScheduleHelper.getDatabase(this).textStyleDao()

        binding.scheduleViewer.addItemDecoration(itemSpacingController)
        val scheduleClickListener=deleteOrModify()

        scheduleAdapter= RecyclerAdapter(scheduleList,scheduleClickListener,textStyleDao)

        refreshAdapter()


        binding.scheduleViewer.adapter=scheduleAdapter
        binding.scheduleViewer.layoutManager=LinearLayoutManager(this@MainActivity)


        val itemTouchHelper = ItemTouchHelper(SwipeToDeleteCallback(object : SwipeToDeleteCallback.OnSwipeListener {
            override fun onSwipe(position: Int) {

                lifecycleScope.launch(Dispatchers.IO) {
                    val allSchedule=scheduleDao.getAll()
                    val deletedSchedule = allSchedule[position]
                    val scheduleId = deletedSchedule.id

                    scheduleDao.deleteSchedule(deletedSchedule)
                    textStyleDao.deleteTextStylesByScheduleId(scheduleId!!)
                }

                scheduleAdapter.removeItem(position)
            }
        }))
        itemTouchHelper.attachToRecyclerView(binding.scheduleViewer)

        binding.CreateScheduleButton.setOnClickListener{
            val enterScheduleValue=Intent(this@MainActivity,EnterSchedule::class.java)

            enterScheduleActivityResult.launch(enterScheduleValue)
        }
    }

    override fun onResume() {
        super.onResume()
        refreshAdapter()
    }

    //todo: 코루틴 공부하기,삭제 수정 구현
    private fun refreshAdapter() {
        lifecycleScope.launch(Dispatchers.IO) {
            val newList = scheduleDao.getAll()
            Log.d("뉴리스트",newList.toString())
            withContext(Dispatchers.Main) {
                scheduleAdapter.updateList(newList)
            }
        }
    }

//    fun convertSpanInfoToTextStyleModel(spanInfo: List<SpanInfo>, scheduleId: Int): List<TextStyleModel> {
//        return spanInfo.map { spanInfo ->
//            TextStyleModel(
//                scheduleId = scheduleId,
//                startIndex = spanInfo.start,
//                endIndex = spanInfo.end,
//                color = spanInfo.color ?:  0,
//                textSize = spanInfo.size ?:  0f
//            )
//        }
//    }

    private fun deleteOrModify(): RecyclerAdapter.ScheduleClickListener {

        val scheduleClickListener = RecyclerAdapter.ScheduleClickListener { schedule ->
            currentSchedule = schedule

            lifecycleScope.launch {
                withContext(Dispatchers.IO) {
                    val textStyleList= textStyleDao.getTextStyleInfo(schedule.id!!)

                    Log.d("텍스트스타일 리스트 확인", textStyleList.toString())

                    val builder = AlertDialog.Builder(this@MainActivity)
                    builder.setTitle("")
                        .setNeutralButton("수정하기", DialogInterface.OnClickListener { dialog, _ ->
                            val modifyValue = Intent(this@MainActivity, ModifySchedule::class.java)
                            modifyValue.putExtra("scheduleText", schedule.scheduleText)
                            modifyValue.putExtra("scheduleDDay", schedule.scheduleTime)
                            modifyValue.putExtra("scheduleDate", schedule.scheduleDate)
                            modifyValue.putExtra("textSize", schedule.textSize)
                            modifyValue.putParcelableArrayListExtra("textStyleList", ArrayList(textStyleList))

                            modifyActivityResult.launch(modifyValue)
                        })
                        .setNegativeButton("취소", DialogInterface.OnClickListener { dialog, _ ->
                        })
                    withContext(Dispatchers.Main) {
                        builder.show()
                    }
                }
            }
        }
        return scheduleClickListener
    }

    private val enterScheduleActivityResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data

                val scheduleText=data?.getStringExtra("scheduleText")
                val scheduleDateMilli=data?.getLongExtra("scheduleDateMilli",0)
                val scheduleDate=data?.getStringExtra("scheduleDate")
                val textSize=data?.getFloatExtra("textSize",15f)
                val spanInfoList = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    data?.getParcelableArrayListExtra<Parcelable>("spanInfo")
                } else {
                    data?.getParcelableArrayListExtra("spanInfo")
                }
//                val spanInfos: ArrayList<SpanInfo>? = intent.getParcelableArrayListExtra<SpanInfo>("spanInfo")


                Log.d("받은 글자크기 리스트 확인", spanInfoList.toString())

                if(scheduleText!=null) {
                    lifecycleScope.launch(Dispatchers.IO) {
                        val schedule = ScheduleModel(id = null, scheduleText, scheduleDateMilli!!, textSize!!,scheduleDate!!)
                        val scheduleId=scheduleDao.insertSchedule(schedule)
                        Log.d("스케쥴 Id2", scheduleId.toString())
                        if (spanInfoList != null) {
                            for (spanInfo in spanInfoList) {
                                val startIndex: Int
                                val endIndex: Int
                                val color: Int?
                                val size: Float?

                                when (spanInfo) {
                                    is ColorInfo -> {
                                        startIndex = spanInfo.startIndex
                                        endIndex = spanInfo.endIndex
                                        color = spanInfo.color!!
                                        size = null
                                    }
                                    is SizeInfo -> {
                                        startIndex = spanInfo.startIndex
                                        endIndex = spanInfo.endIndex
                                        color = null
                                        size = spanInfo.size!!
                                    }
                                    else -> {
                                        continue
                                    }
                                }
                                val textStyle = TextStyleModel(
                                    id=null,
                                    scheduleId=scheduleId.toInt(),
                                    startIndex=startIndex,
                                    endIndex=endIndex,
                                    color=color,
                                    textSize=size
                                )
                                textStyleDao.insertTextStyle(textStyle)
                            }
                        }
                        withContext(Dispatchers.Main) {
                            refreshAdapter()
                        }
                    }
                }
            }
        }

    private val modifyActivityResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                val modifyText = data?.getStringExtra("modifyText")
                val modifyScheduleMilli = data?.getLongExtra("modifyScheduleMilli", 0)
                val modifyTextSize = data?.getFloatExtra("textSize", 15f)
                val modifyScheduleDate=data?.getStringExtra("scheduleDate")
                val modifySpanInfoList= if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    data?.getParcelableArrayListExtra<Parcelable>("modifySpanInfo")
                } else {
                    data?.getParcelableArrayListExtra("modifySpanInfo")
                }
                Log.d("modifySpanInfo",modifySpanInfoList.toString())

                val modifyScheduleId=currentSchedule!!.id
                if (currentSchedule != null) {
                    lifecycleScope.launch(Dispatchers.IO) {

                        val modifySchedule =
                            ScheduleModel(modifyScheduleId, modifyText!!, modifyScheduleMilli!!, modifyTextSize!!,modifyScheduleDate!!)
                        textStyleDao.deleteTextStylesByScheduleId(modifyScheduleId!!)
                        scheduleDao.updateSchedule(modifySchedule)
                        if (modifySpanInfoList != null) {
                            for (spanInfo in modifySpanInfoList) {
                                val startIndex: Int
                                val endIndex: Int
                                val color: Int?
                                val size: Float?

                                when (spanInfo) {
                                    is ColorInfo -> {
                                        startIndex = spanInfo.startIndex
                                        endIndex = spanInfo.endIndex
                                        color = spanInfo.color!!
                                        size = null
                                    }
                                    is SizeInfo -> {
                                        startIndex = spanInfo.startIndex
                                        endIndex = spanInfo.endIndex
                                        color = null
                                        size = spanInfo.size!!
                                    }
                                    else -> {
                                        continue
                                    }
                                }
                                val textStyle = TextStyleModel(
                                    id=null,
                                    scheduleId=modifyScheduleId!!.toInt(),
                                    startIndex=startIndex,
                                    endIndex=endIndex,
                                    color=color,
                                    textSize=size
                                )
                                Log.d("수정직전 텍스트스타일",textStyle.toString())

                                textStyleDao.insertTextStyle(textStyle)
                            }
                        }
                        withContext(Dispatchers.Main) {
                            refreshAdapter()
                        }
                    }
                }
            }
        }

    //todo: 디자인 생각하기
}