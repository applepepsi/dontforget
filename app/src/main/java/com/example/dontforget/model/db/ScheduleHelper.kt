package com.example.dontforget.model.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [ScheduleModel::class,TextStyleModel::class], version = 8, exportSchema = false)
abstract class ScheduleHelper : RoomDatabase() {

    abstract fun scheduleDao(): ScheduleDao
    abstract fun textStyleDao(): TextStyleDao

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
        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("CREATE TABLE IF NOT EXISTS text_style " +
                        "(id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "scheduleId INTEGER NOT NULL," +
                        "startIndex INTEGER NOT NULL," +
                        "endIndex INTEGER NOT NULL," +
                        "color INTEGER," +
                        "textSize REAL," +
                        "FOREIGN KEY(scheduleId) REFERENCES schedule(id) ON DELETE CASCADE)")
            }
        }
        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE schedule ADD COLUMN lineCount INTEGER")
            }
        }
        private val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE schedule ADD COLUMN title TEXT")
            }
        }
        private val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE schedule ADD COLUMN setNotification INTEGER")
            }
        }
        private val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE schedule ADD COLUMN dday INTEGER")
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
                    .addMigrations(MIGRATION_1_2,MIGRATION_2_3, MIGRATION_3_4,MIGRATION_4_5,MIGRATION_5_6,MIGRATION_6_7,MIGRATION_7_8)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}