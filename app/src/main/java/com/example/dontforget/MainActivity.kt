package com.example.dontforget

import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.room.Room
import com.example.dontforget.databinding.ActivityMainBinding
import com.example.dontforget.model.EnterSchedule
import com.example.dontforget.model.ModifySchedule
import com.example.dontforget.model.RecyclerAdapter
import com.example.dontforget.model.db.ScheduleDao
import com.example.dontforget.model.db.ScheduleHelper
import com.example.dontforget.model.db.ScheduleModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class MainActivity : AppCompatActivity() {

    val binding by lazy{ActivityMainBinding.inflate(layoutInflater)}
    lateinit var helper: ScheduleHelper
    lateinit var scheduleAdapter: RecyclerAdapter
    val scheduleList= mutableListOf<ScheduleModel>()
    lateinit var scheduleDao:ScheduleDao
    private var currentSchedule: ScheduleModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        scheduleDao = ScheduleHelper.getDatabase(this).scheduleDao()


        val scheduleClickListener=deleteOrModify()

        scheduleAdapter= RecyclerAdapter(scheduleList,scheduleClickListener)

        refreshAdapter()


        binding.scheduleViewer.adapter=scheduleAdapter
        binding.scheduleViewer.layoutManager=LinearLayoutManager(this@MainActivity)

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
    private fun refreshAdapter(){

        lifecycleScope.launch(Dispatchers.IO) {
            val newList = scheduleDao.getAll()
            withContext(Dispatchers.Main) {
                scheduleList.clear()
                scheduleList.addAll(newList)
                scheduleAdapter.notifyDataSetChanged()
            }
        }
    }

    private fun deleteOrModify(): RecyclerAdapter.ScheduleClickListener {
        val options = mutableListOf("삭제하기", "수정하기", "취소")

        val scheduleClickListener = RecyclerAdapter.ScheduleClickListener { schedule ->
            currentSchedule = schedule

            lifecycleScope.launch {
                withContext(Dispatchers.IO) {
                    val builder = AlertDialog.Builder(this@MainActivity)

                    builder.setTitle("")
                        .setPositiveButton("삭제하기", DialogInterface.OnClickListener { dialog, _ ->
                            lifecycleScope.launch {
                                scheduleDao.deleteSchedule(schedule)
                                withContext(Dispatchers.Main) {
                                    refreshAdapter()
                                }
                            }
                        })
                        .setNeutralButton("수정하기", DialogInterface.OnClickListener { dialog, _ ->
                            val modifyValue = Intent(this@MainActivity, ModifySchedule::class.java)
                            modifyValue.putExtra("scheduleText", schedule.scheduleText)
                            modifyValue.putExtra("scheduleTime", schedule.scheduleTime)
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
                val scheduleTime=data?.getLongExtra("scheduleTime",0)
                val textSize=data?.getFloatExtra("textSize",15f)

                if(scheduleText!=null) {
                    lifecycleScope.launch(Dispatchers.IO) {
                        val schedule = ScheduleModel(id = null, scheduleText, scheduleTime!!, textSize!!)
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
                val modifyTime = data?.getLongExtra("scheduleTime", 0)
                val modifyTextSize = data?.getFloatExtra("textSize", 15f)
                Log.d("받은 스케쥴타임", modifyTime.toString())
                Log.d("받은 텍스트", modifyText!!)
                if (currentSchedule != null) {
                    lifecycleScope.launch(Dispatchers.IO) {
                        val modifySchedule =
                            ScheduleModel(currentSchedule!!.id, modifyText, modifyTime!!, modifyTextSize!!)
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