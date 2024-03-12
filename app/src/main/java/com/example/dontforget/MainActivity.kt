package com.example.dontforget

import android.app.*
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.icu.util.Calendar
import android.os.*
import android.text.Editable
import android.text.TextWatcher
import androidx.appcompat.app.AppCompatActivity
import android.util.Log
import android.util.TypedValue
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import androidx.room.Transaction
import com.example.dontforget.Notification.NotificationHelper
import com.example.dontforget.Notification.NotificationReceiver
import com.example.dontforget.databinding.ActivityMainBinding
import com.example.dontforget.model.EnterSchedule
import com.example.dontforget.model.ModifySchedule
import com.example.dontforget.model.RecyclerAdapter
import com.example.dontforget.model.db.*
import com.example.dontforget.spanInfo.ColorInfo
import com.example.dontforget.spanInfo.SizeInfo
import com.example.dontforget.util.ItemSpacingController
import com.example.dontforget.util.SpanInfoProcessor
import com.example.dontforget.util.SwipeToDeleteCallback
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class MainActivity : AppCompatActivity() {

    val binding by lazy{ActivityMainBinding.inflate(layoutInflater)}

    lateinit var scheduleAdapter: RecyclerAdapter
    var scheduleList= mutableListOf<ScheduleModel>()
    var searchList=mutableListOf<ScheduleModel>()
    lateinit var scheduleDao:ScheduleDao
    lateinit var textStyleDao:TextStyleDao
    private var currentSchedule: ScheduleModel? = null
    private lateinit var spanInfoProcessor: SpanInfoProcessor
//    private var textList=mutableListOf<String>()

    val space=20

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val itemSpacingController = ItemSpacingController(space)
        scheduleDao = ScheduleHelper.getDatabase(this).scheduleDao()
        textStyleDao=ScheduleHelper.getDatabase(this).textStyleDao()
        spanInfoProcessor = SpanInfoProcessor(textStyleDao)
        binding.scheduleViewer.addItemDecoration(itemSpacingController)
        val scheduleClickListener=deleteOrModify()

        scheduleAdapter= RecyclerAdapter(scheduleList,scheduleClickListener,textStyleDao)

        refreshAdapter()



        binding.scheduleViewer.adapter=scheduleAdapter
        binding.scheduleViewer.layoutManager=LinearLayoutManager(this@MainActivity)
        binding.cancelButton.setOnClickListener {
            binding.searchBar.setText(null)

        }
        textWatcher()

        val itemTouchHelper = ItemTouchHelper(SwipeToDeleteCallback(object : SwipeToDeleteCallback.OnSwipeListener {
            override fun onSwipe(position: Int) {

                lifecycleScope.launch(Dispatchers.IO) {
                    val allSchedule=scheduleDao.getAll()
                    val deletedSchedule = allSchedule[position]
                    val scheduleId = deletedSchedule.id

                    scheduleDao.deleteSchedule(deletedSchedule)
//                    textStyleDao.deleteTextStylesByScheduleId(scheduleId!!)
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


    private fun textWatcher() {
        binding.searchBar.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }
            override fun afterTextChanged(s: Editable?) {
                search(s.toString())
            }

        })

    }

    private fun search(searchText:String){
        Log.d("서치 텍스트",searchText)

        scheduleList.clear()

        if(searchText.isEmpty()){
            lifecycleScope.launch(Dispatchers.IO) {
                val allSchedules = scheduleDao.getAll()
                withContext(Dispatchers.Main) {
                    scheduleList.addAll(allSchedules)
                    scheduleAdapter.updateList(scheduleList)
                    Log.d("호출됨", scheduleList.toString())
                }
            }
        }
        else {
            lifecycleScope.launch(Dispatchers.IO) {

                val foundSchedules = scheduleDao.findSchedulesByText("%$searchText%")

                withContext(Dispatchers.Main) {
                    scheduleList.addAll(foundSchedules)
                    Log.d("foundSchedules", scheduleList.toString())
                    scheduleAdapter.updateList(scheduleList)
                }
            }
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
                            modifyValue.putExtra("scheduleTitle", schedule.title)
                            modifyValue.putExtra("setNotification", schedule.setNotification)

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
                val lineCount=data?.getIntExtra("lineCount",0)
                val scheduleTitle=data?.getStringExtra("scheduleTitle")
                val setNotification=data?.getIntExtra("setNotification",0)
                Log.d("알림값", setNotification.toString())

                if (scheduleTitle != null) {
                    Log.d("타이틀",scheduleTitle)
                }
                if(scheduleText!=null && scheduleTitle!=null) {
                    lifecycleScope.launch(Dispatchers.IO) {
                        val schedule = ScheduleModel(id = null, scheduleText, scheduleDateMilli!!, textSize!!,scheduleDate!!,lineCount,
                            scheduleTitle,setNotification
                        )
                        val scheduleId=scheduleDao.insertSchedule(schedule)

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
                Log.d("모디파이 텍스트 사이즈",modifyTextSize.toString())
                val modifyScheduleDate=data?.getStringExtra("scheduleDate")

                val modifyScheduleId=currentSchedule!!.id
                val lineCount=data?.getIntExtra("lineCount",0)
                val modifyTitle=data?.getStringExtra("modifyTitle")
                val modifySetNotification=data?.getIntExtra("modifySetNotification",0)

                if (currentSchedule != null) {
                    lifecycleScope.launch(Dispatchers.IO) {


                        val modifySchedule =
                            ScheduleModel(modifyScheduleId,
                                modifyText!!,
                                modifyScheduleMilli!!,
                                modifyTextSize!!,
                                modifyScheduleDate!!,
                                lineCount,
                                modifyTitle,
                                modifySetNotification
                            )
                        scheduleDao.updateSchedule(modifySchedule)

                        withContext(Dispatchers.Main) {
                            refreshAdapter()
                        }
                    }
                }
            }
        }



//    fun scheduleNotification() {
//
//
//        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
//        val intent = Intent(this, NotificationReceiver::class.java).apply {
//            putExtra("memo", memo)
//        }
//        val pendingIntent = PendingIntent.getBroadcast(this, 0, intent, 0)
//
//        // 현재 시간을 기준으로 다음 날 8시까지의 시간 계산
//        val calendar = Calendar.getInstance().apply {
//            time = targetDate
//            set(Calendar.HOUR_OF_DAY, 8)
//            set(Calendar.MINUTE, 0)
//            set(Calendar.SECOND, 0)
//        }
//
//        // 알림을 보내는 작업을 8시에 예약
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
//        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
//            alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
//        } else {
//            alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
//        }
//    }




    //todo: 디자인 생각하기
}