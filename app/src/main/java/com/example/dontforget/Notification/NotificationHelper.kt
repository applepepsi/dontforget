package com.example.dontforget.Notification

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.icu.util.Calendar
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat.getSystemService
import com.example.dontforget.R
import com.example.dontforget.model.DayCalculation
import kotlinx.coroutines.withContext

class NotificationHelper(private val context: Context) {
    private val channelId = "channelId"


    fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "알림 제목"
            val descriptionText = "알림 내용"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(channelId, name, importance).apply {
                description = descriptionText
            }

            val notificationManager: NotificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun sendNotification(notificationDataList:ArrayList<NotificationData>?) {
        createNotificationChannel()

        Log.d("샌드노티파이",notificationDataList.toString())
        if (notificationDataList != null) {
            var message:String
            for (data in notificationDataList) {
                val Dday= DayCalculation().calculationDday(data.scheduleTime, DayCalculation().getCurrentDateMillis())

                if (Dday == null || Dday < 0L) {
                    continue
                }else{
                    message = if (Dday ==0L) {
                        "${data.title} 스케쥴의 D - Day 입니다."
                    }else{
                        "${data.title} 스케쥴이 ${Dday}일 남았습니다."
                    }
                }

                Log.d("데이터", data.toString())

                val builder = NotificationCompat.Builder(context, channelId)
                    .setSmallIcon(R.drawable.ic_stat_name)
                    .setContentTitle("D - Day 알림")
                    .setContentText(message)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)

                val notificationId = data.id

                with(NotificationManagerCompat.from(context)) {
                    if (notificationId != null) {
                        notify(notificationId, builder.build())
                    }
                }
            }
        }
//        val builder = NotificationCompat.Builder(context, channelId)
//            .setSmallIcon(R.drawable.ic_baseline_arrow_back_ios_24)
//            .setContentTitle("타이틀")
//            .setContentText("설명")
//            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
//
//        with(NotificationManagerCompat.from(context)) {
//            notify(notificationId, builder.build())
//        }
    }
}