package com.example.dontforget.model.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [ScheduleModel::class], version = 1, exportSchema = false)
abstract class ScheduleHelper: RoomDatabase() {

    abstract fun scheduleDao(): ScheduleDao

//    companion object {
//        private var instance: ScheduleHelper? = null
//
//        @Synchronized
//        fun getDatabase(context: Context): ScheduleHelper? {
//            if (instance == null) {
//                instance = Room.databaseBuilder(
//                    context.applicationContext,
//                    ScheduleHelper::class.java,
//                    "database-schedule"
//                )
//                    .allowMainThreadQueries()
//                    .build()
//            }
//            return instance
//        }
//    }
}