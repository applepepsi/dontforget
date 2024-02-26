package com.example.dontforget

import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
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
import com.example.dontforget.model.db.ScheduleDao
import com.example.dontforget.model.db.ScheduleHelper
import com.example.dontforget.model.db.ScheduleModel
import com.example.dontforget.model.db.TextStyleModel
import com.example.dontforget.util.ItemSpacingController
import com.example.dontforget.util.SwipeToDeleteCallback
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class MainActivity : AppCompatActivity() {

    val binding by lazy{ActivityMainBinding.inflate(layoutInflater)}
    lateinit var helper: ScheduleHelper
    lateinit var scheduleAdapter: RecyclerAdapter
    var scheduleList= mutableListOf<ScheduleModel>()
    lateinit var scheduleDao:ScheduleDao
    private var currentSchedule: ScheduleModel? = null
    val space=20

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val itemSpacingController = ItemSpacingController(space)
        scheduleDao = ScheduleHelper.getDatabase(this).scheduleDao()

        binding.scheduleViewer.addItemDecoration(itemSpacingController)
        val scheduleClickListener=deleteOrModify()

        scheduleAdapter= RecyclerAdapter(scheduleList,scheduleClickListener)

        refreshAdapter()


        binding.scheduleViewer.adapter=scheduleAdapter
        binding.scheduleViewer.layoutManager=LinearLayoutManager(this@MainActivity)


        val itemTouchHelper = ItemTouchHelper(SwipeToDeleteCallback(object : SwipeToDeleteCallback.OnSwipeListener {
            override fun onSwipe(position: Int) {

                lifecycleScope.launch(Dispatchers.IO) {
                    val allSchedule=scheduleDao.getAll()
                    val deletedSchedule = allSchedule[position]

                    scheduleDao.deleteSchedule(deletedSchedule)
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
            withContext(Dispatchers.Main) {
                scheduleAdapter.updateList(newList)
            }
        }
    }

    fun convertSpanInfoToTextStyleModel(spanInfo: List<SpanInfo>, scheduleId: Int): List<TextStyleModel> {
        return spanInfo.map { spanInfo ->
            TextStyleModel(
                scheduleId = scheduleId,
                startIndex = spanInfo.start,
                endIndex = spanInfo.end,
                color = spanInfo.color ?:  0,
                textSize = spanInfo.size ?:  0f
            )
        }
    }

    private fun deleteOrModify(): RecyclerAdapter.ScheduleClickListener {

        val scheduleClickListener = RecyclerAdapter.ScheduleClickListener { schedule ->
            currentSchedule = schedule

            lifecycleScope.launch {
                withContext(Dispatchers.IO) {
                    val builder = AlertDialog.Builder(this@MainActivity)

                    builder.setTitle("")
                        .setNeutralButton("수정하기", DialogInterface.OnClickListener { dialog, _ ->
                            val modifyValue = Intent(this@MainActivity, ModifySchedule::class.java)
                            modifyValue.putExtra("scheduleText", schedule.scheduleText)
                            modifyValue.putExtra("scheduleDDay", schedule.scheduleTime)
                            modifyValue.putExtra("scheduleDate", schedule.scheduleDate)
                            modifyValue.putExtra("textSize", schedule.textSize)

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
                val spanInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    data?.getParcelableArrayListExtra<TextSizeData>("spanInfo")
                } else {
                    data?.getParcelableArrayListExtra("spanInfo")
                }
//                val spanInfos: ArrayList<SpanInfo>? = intent.getParcelableArrayListExtra<SpanInfo>("spanInfo")


                Log.d("받은 글자크기 리스트 확인", spanInfo.toString())

                if(scheduleText!=null) {
                    lifecycleScope.launch(Dispatchers.IO) {
                        val schedule = ScheduleModel(id = null, scheduleText, scheduleDateMilli!!, textSize!!,scheduleDate!!)
                        scheduleDao.insertSchedule(schedule)
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

                if (currentSchedule != null) {
                    lifecycleScope.launch(Dispatchers.IO) {
                        val modifySchedule =
                            ScheduleModel(currentSchedule!!.id, modifyText!!, modifyScheduleMilli!!, modifyTextSize!!,modifyScheduleDate!!)
                        scheduleDao.updateSchedule(modifySchedule)
                        withContext(Dispatchers.Main) {
                            refreshAdapter()
                        }
                    }
                }
            }
        }

    //todo: 디자인 생각하기
}