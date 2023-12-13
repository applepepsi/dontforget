package com.example.dontforget.model.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [ScheduleModel::class], version = 3, exportSchema = false)
abstract class ScheduleHelper : RoomDatabase() {

    abstract fun scheduleDao(): ScheduleDao

    companion object {
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE schedule ADD COLUMN textSize REAL NOT NULL DEFAULT 15.0")
//                database.execSQL("ALTER TABLE schedule ADD COLUMN scheduleDate TEXT NOT NULL DEFAULT ''")
            }
        }
        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE schedule ADD COLUMN scheduleDate TEXT NOT NULL DEFAULT ''")
            }
        }

        @Volatile
        private var INSTANCE: ScheduleHelper? = null

        fun getDatabase(context: Context): ScheduleHelper {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ScheduleHelper::class.java,
                    "schedule_database"
                )
                    .addMigrations(MIGRATION_1_2,MIGRATION_2_3)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}