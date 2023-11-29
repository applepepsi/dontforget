package com.example.dontforget

import android.content.DialogInterface
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.room.Room
import com.example.dontforget.databinding.ActivityMainBinding
import com.example.dontforget.model.EnterSchedule
import com.example.dontforget.model.ModifySchedule
import com.example.dontforget.model.RecyclerAdapter
import com.example.dontforget.model.db.ScheduleDao
import com.example.dontforget.model.db.ScheduleHelper
import com.example.dontforget.model.db.ScheduleModel


class MainActivity : AppCompatActivity() {

    val binding by lazy{ActivityMainBinding.inflate(layoutInflater)}
    lateinit var helper: ScheduleHelper
    lateinit var scheduleAdapter: RecyclerAdapter
    val scheduleList= mutableListOf<ScheduleModel>()
    lateinit var scheduleDao:ScheduleDao


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        helper= Room.databaseBuilder(this,ScheduleHelper::class.java,"schedule_db")
            .allowMainThreadQueries()//공부할때만 써라
            .build()

        scheduleDao=helper.scheduleDao()

//        val scheduleClickListener = RecyclerAdapter.ScheduleClickListener { schedule ->
//
//            Toast.makeText(this@MainActivity, "Clicked: ${schedule.scheduleText}", Toast.LENGTH_SHORT)
//                .show()
//        }
        val scheduleClickListener=deleteOrModify()

        scheduleAdapter= RecyclerAdapter(scheduleList,scheduleClickListener)

        refreshAdapter()

        with(binding){
            scheduleViewer.adapter=scheduleAdapter
            scheduleViewer.layoutManager=LinearLayoutManager(this@MainActivity)

            CreateScheduleButton.setOnClickListener{
                startActivity(Intent(this@MainActivity,EnterSchedule::class.java))
            }
            val scheduleText=intent.getStringExtra("scheduleText")
            val scheduleTime=intent.getLongExtra("scheduleTime",0)

            if(scheduleText!=null){
                val schedule=ScheduleModel(id = null,scheduleText,scheduleTime)

                scheduleDao.insertSchedule(schedule)
                refreshAdapter()
            }

        }
    }
    //todo: 코루틴 공부하기,삭제 수정 구현
    private fun refreshAdapter(){
        scheduleList.clear()
        scheduleList.addAll(scheduleDao.getAll())
        scheduleAdapter.notifyDataSetChanged()
    }

    private fun deleteOrModify(): RecyclerAdapter.ScheduleClickListener {

        val options = mutableListOf("삭제하기", "수정하기", "취소")

        val scheduleClickListener = RecyclerAdapter.ScheduleClickListener { schedule ->

            val builder = AlertDialog.Builder(this)

            builder.setTitle("")
                .setPositiveButton("삭제하기", DialogInterface.OnClickListener { dialog, _ ->
                    scheduleDao.deleteSchedule(schedule)
                    refreshAdapter()
                })
                .setNeutralButton("수정하기", DialogInterface.OnClickListener { dialog, _ ->
                    val modifyValue = Intent(this@MainActivity, ModifySchedule::class.java)
                    modifyValue.putExtra("scheduleText", schedule.scheduleText)
                    startActivity(modifyValue)
                    val modifyText = intent.getStringExtra("modifyText")

                    if (modifyText != null) {
                        val modifySchedule =
                            ScheduleModel(schedule.id, modifyText, schedule.scheduleTime)
                        scheduleDao.updateSchedule(modifySchedule)

                    }
                })
                .setNegativeButton("취소", DialogInterface.OnClickListener { dialog, _ ->
                })
            builder.show()
        }
        return scheduleClickListener
    }
}