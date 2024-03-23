package com.example.dontforget

import android.app.*
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
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
import com.example.dontforget.model.*
import com.example.dontforget.model.db.*
import com.example.dontforget.setting.SettingsActivity
import com.example.dontforget.util.ItemSpacingController
import com.example.dontforget.util.ScheduleFilterData
import com.example.dontforget.util.SpanInfoProcessor
import com.example.dontforget.util.SwipeToDeleteCallback
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*
import kotlin.collections.ArrayList


class MainActivity : AppCompatActivity() {

    val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }

    lateinit var scheduleAdapter: RecyclerAdapter
    lateinit var filterRecyclerAdapter: FilterRecyclerAdapter
    var scheduleList = mutableListOf<ScheduleModel>()
    var searchList = mutableListOf<ScheduleModel>()

    lateinit var scheduleDao: ScheduleDao
    lateinit var textStyleDao: TextStyleDao

    private val filterList: List<String> =
        listOf("모든 항목", "디데이 있음", "디데이 없음", "만료된 스케쥴", "임박한 스케쥴", "알림 On")
    private var currentSchedule: ScheduleModel? = null

    private var searchBarController = false
    private lateinit var spanInfoProcessor: SpanInfoProcessor
//    private var textList=mutableListOf<String>()

    val space = 8

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val itemSpacingController = ItemSpacingController(space)
        scheduleDao = ScheduleHelper.getDatabase(this).scheduleDao()
        textStyleDao = ScheduleHelper.getDatabase(this).textStyleDao()
        spanInfoProcessor = SpanInfoProcessor(textStyleDao)

        binding.filterView.addItemDecoration(itemSpacingController)
        val scheduleClickListener = deleteOrModify()
        val filterClickListener = scheduleFilter()
        Log.d("클릭리스너", filterClickListener.toString())
        scheduleAdapter = RecyclerAdapter(scheduleClickListener, textStyleDao)

        filterRecyclerAdapter = FilterRecyclerAdapter(filterList, filterClickListener)




        binding.scheduleViewer.adapter = scheduleAdapter
        binding.scheduleViewer.layoutManager = LinearLayoutManager(this@MainActivity)

        //어뎁터
        binding.filterView.adapter = filterRecyclerAdapter
        binding.filterView.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)


        binding.cancelButton.setOnClickListener {
            binding.searchBar.setText(null)
        }

        swipeRefresh()

        binding.showSearchBar.setOnClickListener {
            searchBarController()
            filterViewController()
        }

        val itemTouchHelper =
            ItemTouchHelper(SwipeToDeleteCallback(object : SwipeToDeleteCallback.OnSwipeListener {
                override fun onSwipe(position: Int) {

                    lifecycleScope.launch(Dispatchers.IO) {
                        val allSchedule = scheduleDao.getAll()
                        val deletedSchedule = allSchedule[position]
                        val scheduleId = deletedSchedule.id

                        scheduleDao.deleteSchedule(deletedSchedule)
//                    textStyleDao.deleteTextStylesByScheduleId(scheduleId!!)
                    }

                    scheduleAdapter.removeItem(position)
                }
            }))
        itemTouchHelper.attachToRecyclerView(binding.scheduleViewer)

        binding.CreateScheduleButton.setOnClickListener {
            val enterScheduleValue = Intent(this@MainActivity, EnterSchedule::class.java)
            enterScheduleActivityResult.launch(enterScheduleValue)
            incrementMemoCount(this)
        }

        binding.settingButton.setOnClickListener {

            lifecycleScope.launch(Dispatchers.IO)
            {
                withContext(Dispatchers.IO)

                {
                    val settingsActivityValue =
                        Intent(this@MainActivity, SettingsActivity::class.java)

                    startActivity(settingsActivityValue)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        refreshAdapter()
    }

    private fun refreshAdapter() {
        lifecycleScope.launch(Dispatchers.IO) {
//            notificationFilter()
            scheduleNotification()

            val newList = scheduleDao.getAll()
            withContext(Dispatchers.Main) {
                scheduleAdapter.submitList(newList)
            }
        }
    }

    private fun searchBarController() {
        if (!searchBarController) {
            binding.searchBar.visibility = View.VISIBLE
            binding.cancelButton.visibility = View.VISIBLE
            searchBarController = true
            textWatcher()
        } else {
            binding.searchBar.visibility = View.GONE
            binding.cancelButton.visibility = View.GONE
            searchBarController = false
            binding.searchBar.setText(null)
        }
    }

    private fun filterViewController() {
        if (!searchBarController) {
            binding.filterView.visibility = View.VISIBLE
        } else {
            binding.filterView.visibility = View.GONE
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

    private fun scheduleFilter(): FilterRecyclerAdapter.FilterClickListener {

        val scheduleClickListener = FilterRecyclerAdapter.FilterClickListener { filter ->
            lifecycleScope.launch(Dispatchers.IO) {
                val filterSchedule: List<ScheduleModel> = when (filter) {
                    filterList[0] -> {
                        scheduleDao.getAll()
                    }
                    filterList[1] -> scheduleDao.findHaveDday()
                    filterList[2] -> scheduleDao.findWithOutDday()
                    filterList[3] -> scheduleDao.findHExpiredDday()
                    filterList[4] -> scheduleDao.findImminentDday()
                    filterList[5] -> scheduleDao.findSetNotification()
                    else -> {
                        scheduleDao.getAll()
                    }
                }
                withContext(Dispatchers.Main) {
                    scheduleAdapter.submitList(filterSchedule)
                    val previousSelectedIndex = filterRecyclerAdapter.selectedItemIndex
                    filterRecyclerAdapter.selectedItemIndex = filterList.indexOf(filter)
                    Log.d("인덱스", previousSelectedIndex.toString())

                    filterRecyclerAdapter.notifyItemChanged(previousSelectedIndex!!)

                    filterRecyclerAdapter.notifyItemChanged(filterList.indexOf(filter))
                }
            }
        }
        return scheduleClickListener
    }


    private val enterScheduleActivityResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                val scheduleText = data?.getStringExtra("scheduleText")
                val scheduleDateMilli = data?.getLongExtra("scheduleDateMilli", 0)
                val scheduleDate = data?.getStringExtra("scheduleDate")
                val textSize = data?.getFloatExtra("textSize", 20f)
                val lineCount = data?.getIntExtra("lineCount", 0)
                val scheduleTitle = data?.getStringExtra("scheduleTitle")
                val setNotification = data?.getIntExtra("setNotification", 0)
                var dday = data?.getLongExtra("dday", Long.MIN_VALUE)



                if (dday == Long.MIN_VALUE) {
                    dday = null
                }
                if (scheduleText != null && scheduleTitle != null) {
                    lifecycleScope.launch(Dispatchers.IO) {
                        val schedule = ScheduleModel(
                            id = null, scheduleText,
                            scheduleDateMilli!!,
                            textSize!!,
                            scheduleDate!!,
                            lineCount,
                            scheduleTitle,
                            setNotification,
                            dday
                        )

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
                val modifyTextSize = data?.getFloatExtra("textSize", 20f)
                Log.d("모디파이 텍스트 사이즈", modifyTextSize.toString())
                val modifyScheduleDate = data?.getStringExtra("scheduleDate")

                val modifyScheduleId = currentSchedule!!.id
                val lineCount = data?.getIntExtra("lineCount", 0)
                val modifyTitle = data?.getStringExtra("modifyTitle")
                val modifySetNotification = data?.getIntExtra("modifySetNotification", 0)
                var modifyDday = data?.getLongExtra("modifyDday", Long.MIN_VALUE)

                if (modifyDday == Long.MIN_VALUE) {

                    modifyDday = null
                }
                if (currentSchedule != null) {
                    lifecycleScope.launch(Dispatchers.IO) {
                        val modifySchedule =
                            ScheduleModel(
                                modifyScheduleId,
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

                val notificationId=0

                val currentCurrentDateMilli = DayCalculation().getCurrentDateMillis()
                val notifyList = scheduleDao.findSwitchOnData(currentCurrentDateMilli)
                val notificationDataList = notifyList.map { scheduleModel ->
                    NotificationData(
                        id = scheduleModel.id,
                        scheduleText = scheduleModel.scheduleText,
                        scheduleTime = scheduleModel.scheduleTime,
                        title = scheduleModel.title,
                        dday=scheduleModel.dday
                    )
                }

                Log.d("리스트", notificationDataList.toString())
                val alarmManager: AlarmManager =
                    getSystemService(Context.ALARM_SERVICE) as AlarmManager


                val notificationIntent =
                    Intent(this@MainActivity, NotificationReceiver::class.java)

                notificationIntent.putParcelableArrayListExtra("notificationDataList", ArrayList(notificationDataList))
                notificationIntent.putExtra("notificationId", notificationId)


                val pendingIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    PendingIntent.getBroadcast(
                        this@MainActivity,
                        notificationId,
                        notificationIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )
                } else {
                    PendingIntent.getBroadcast(
                        this@MainActivity,
                        notificationId,
                        notificationIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )
                }
                Log.d("팬딩", pendingIntent.toString())
                val calendar = Calendar.getInstance().apply {
                    timeInMillis = System.currentTimeMillis()
                    set(Calendar.HOUR_OF_DAY, 8)
//                    add(Calendar.DAY_OF_YEAR, 1)
                }

                if(calendar.before(Calendar.getInstance()))
                    calendar.add(Calendar.DAY_OF_YEAR,1)

                Log.d("calendar", calendar.timeInMillis.toString())
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        calendar.timeInMillis,
                        pendingIntent
                    )
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    alarmManager.setExact(
                        AlarmManager.RTC_WAKEUP,
                        calendar.timeInMillis,
                        pendingIntent
                    )
                } else {
                    alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
                }

            }
        }
    }

    private fun notificationCheck() {
        lifecycleScope.launch(Dispatchers.IO) {
            withContext(Dispatchers.Main) {
                val currentCurrentDateMilli = DayCalculation().getCurrentDateMillis()
                val notifyList = scheduleDao.findSwitchOnData(currentCurrentDateMilli)
                val notificationDataList = notifyList.map { scheduleModel ->
                    NotificationData(
                        id = scheduleModel.id,
                        scheduleText = scheduleModel.scheduleText,
                        scheduleTime = scheduleModel.scheduleTime,
                        title = scheduleModel.title,
                        dday=scheduleModel.dday
                    )
                }
            }
        }
    }

//    fun scheduleNotification() {
//
//        lifecycleScope.launch(Dispatchers.IO) {
//            withContext(Dispatchers.Main) {
//
//                val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
//                val notificationIntent = Intent(this@MainActivity, NotificationReceiver::class.java).apply {
//                    putExtra("notificationDataList", 2000)
//                }
//
//                val pendingIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//                    PendingIntent.getBroadcast(
//                        this@MainActivity,
//                        0,
//                        notificationIntent,
//                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
//                    )
//                } else {
//                    PendingIntent.getBroadcast(
//                        this@MainActivity,
//                        0,
//                        notificationIntent,
//                        PendingIntent.FLAG_UPDATE_CURRENT
//                    )
//                }
//
//                val calendar = Calendar.getInstance().apply {
//                    timeInMillis = System.currentTimeMillis()
//                    set(Calendar.HOUR_OF_DAY, 8)
//                    add(Calendar.DAY_OF_YEAR, 1)
//                }
//
//                Log.d("calendar", calendar.timeInMillis.toString())
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
//                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
//                    alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
//                } else {
//                    alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
//                }
//            }
//        }
//    }

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
//                    scheduleNotification()
//                    scheduleAdapter.submitList(updatedSchedules)
                    refreshAdapter()
                    binding.swipeLayout.isRefreshing = false
                }
            }
        }
    }

    fun incrementMemoCount(context: Context) {
        val sharedPreferences = context.getSharedPreferences("MemoCount", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        val currentCount = sharedPreferences.getInt("count", 0)
        val newCount = currentCount + 1
        editor.putInt("count", newCount)
        editor.apply()
    }

}