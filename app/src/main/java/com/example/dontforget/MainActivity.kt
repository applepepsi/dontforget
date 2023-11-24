package com.example.dontforget

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.room.Room
import com.example.dontforget.databinding.ActivityMainBinding
import com.example.dontforget.model.EnterSchedule
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

        scheduleAdapter= RecyclerAdapter(scheduleList)

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
    fun refreshAdapter(){
        scheduleList.clear()
        scheduleList.addAll(scheduleDao.getAll())
        scheduleAdapter.notifyDataSetChanged()
    }

}