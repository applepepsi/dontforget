package com.example.dontforget

import android.app.*
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.icu.util.Calendar
import android.os.*
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.dontforget.Notification.NotificationData
import com.example.dontforget.Notification.NotificationReceiver
import com.example.dontforget.databinding.ActivityMainBinding
import com.example.dontforget.model.DayCalculation
import com.example.dontforget.model.EnterSchedule
import com.example.dontforget.model.ModifySchedule
import com.example.dontforget.model.RecyclerAdapter
import com.example.dontforget.model.db.*
import com.example.dontforget.util.ItemSpacingController
import com.example.dontforget.util.SpanInfoProcessor
import com.example.dontforget.util.SwipeToDeleteCallback
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*
import androidx.lifecycle.LifecycleOwner


class MainActivity : AppCompatActivity() {

    val binding by lazy{ActivityMainBinding.inflate(layoutInflater)}

    lateinit var scheduleAdapter: RecyclerAdapter
    var scheduleList= mutableListOf<ScheduleModel>()
    var searchList=mutableListOf<ScheduleModel>()

    lateinit var scheduleDao:ScheduleDao
    lateinit var textStyleDao:TextStyleDao
    private var currentSchedule: ScheduleModel? = null
    private lateinit var notifyList:List<ScheduleModel>
    private var searchBarController=false
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

        scheduleAdapter= RecyclerAdapter(scheduleClickListener,textStyleDao)

        refreshAdapter()
        scheduleNotification()


        binding.scheduleViewer.adapter=scheduleAdapter
        binding.scheduleViewer.layoutManager=LinearLayoutManager(this@MainActivity)

        binding.cancelButton.setOnClickListener {
            binding.searchBar.setText(null)
        }

        swipeRefresh()

        binding.showSearchBar.setOnClickListener{
            searchBarController()
        }

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

    private fun refreshAdapter() {
        lifecycleScope.launch(Dispatchers.IO) {
            val newList = scheduleDao.getAll()
            withContext(Dispatchers.Main) {
                scheduleAdapter.submitList(newList)
            }
        }
    }

    private fun searchBarController() {
        if(!searchBarController){
            binding.searchBar.visibility= View.VISIBLE
            binding.cancelButton.visibility= View.VISIBLE
            searchBarController=true
            textWatcher()
        }else{
            binding.searchBar.visibility= View.GONE
            binding.cancelButton.visibility= View.GONE
            searchBarController=false
            binding.searchBar.setText(null)
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

    private fun showSearchBar(){

    }

    private fun search(searchText: String) {
        Log.d("서치 텍스트", searchText)

        if (searchText.isEmpty()) {
            lifecycleScope.launch(Dispatchers.IO) {
                val allSchedules = scheduleDao.getAll()
                withContext(Dispatchers.Main) {
                    scheduleAdapter.submitList(allSchedules)
                    Log.d("호출됨", scheduleList.toString())
                }
            }
        } else {
            lifecycleScope.launch(Dispatchers.IO) {
                val foundSchedules = scheduleDao.findSchedulesByText("%$searchText%")
                withContext(Dispatchers.Main) {
                    scheduleAdapter.submitList(foundSchedules)
                    Log.d("foundSchedules", scheduleList.toString())
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
                            modifyValue.putExtra("dday", schedule.dday)
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
                val dday=data?.getLongExtra("dday",-1)

                Log.d("알림값", setNotification.toString())

                if (scheduleTitle != null) {
                    Log.d("타이틀",scheduleTitle)
                }
                if(scheduleText!=null && scheduleTitle!=null) {
                    lifecycleScope.launch(Dispatchers.IO) {
                        val schedule = ScheduleModel(id = null, scheduleText,
                            scheduleDateMilli!!,
                            textSize!!,
                            scheduleDate!!,
                            lineCount,
                            scheduleTitle,
                            setNotification,
                            dday
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
                val modifyDday=data?.getLongExtra("modifyDday",-1)

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
                                modifySetNotification,
                                modifyDday
                            )
                        scheduleDao.updateSchedule(modifySchedule)

                        withContext(Dispatchers.Main) {
                            refreshAdapter()
                        }
                    }
                }
            }
        }



    private fun scheduleNotification() {

        lifecycleScope.launch(Dispatchers.IO) {

            withContext(Dispatchers.Main) {
                notifyList = scheduleDao.findSwitchOnData(DayCalculation().getCurrentDateMillis())

                val notificationDataList = notifyList.map { scheduleModel ->
                    NotificationData(
                        id = scheduleModel.id,
                        scheduleText = scheduleModel.scheduleText,
                        scheduleTime = scheduleModel.scheduleTime,
                        scheduleDate = scheduleModel.scheduleDate,
                        title = scheduleModel.title
                    )
                }
                val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
                val notificationIntent = Intent(this@MainActivity, NotificationReceiver::class.java).apply {
                    putParcelableArrayListExtra("notifyList", ArrayList(notificationDataList))
                    Log.d("노티파이리스트", notificationDataList.toString())
                }

                var pendingIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    PendingIntent.getBroadcast(
                        this@MainActivity,
                        0,
                        notificationIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )
                } else {
                    PendingIntent.getBroadcast(
                        this@MainActivity,
                        0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT
                    )
                }

                val calendar = Calendar.getInstance().apply {
                    add(Calendar.DAY_OF_MONTH, 1)
                    set(Calendar.HOUR_OF_DAY, 8)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                Log.d("켈린더밀리", calendar.timeInMillis.toString())

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
                } else {
                    alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)

                }
            }
        }
    }

    private fun swipeRefresh() {
        binding.swipeLayout.setOnRefreshListener {
            lifecycleScope.launch(Dispatchers.IO) {
                val allSchedules = scheduleDao.getAll()
                val updatedSchedules = allSchedules.map { schedule ->
                    val currentTime = DayCalculation().getCurrentDateMillis()
                    val dday = DayCalculation().calculationDday(schedule.scheduleTime, currentTime)
                    schedule.copy(dday = dday)

                }
                Log.d("디데이변경", updatedSchedules.toString())
                updatedSchedules.forEach { schedule ->
                    scheduleDao.updateSchedule(schedule)
                }
                withContext(Dispatchers.Main) {

                    scheduleAdapter.submitList(updatedSchedules)
                    binding.swipeLayout.isRefreshing = false
                }
            }
        }
    }


    //todo: 디자인 생각하기
}