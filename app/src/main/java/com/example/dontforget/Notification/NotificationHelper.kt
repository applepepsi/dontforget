package com.example.dontforget.Notification

import android.app.*
import android.app.NotificationManager.IMPORTANCE_DEFAULT
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.Color
import android.icu.util.Calendar
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat.getSystemService
import com.example.dontforget.R
import com.example.dontforget.model.DayCalculation
import kotlinx.coroutines.withContext
import java.util.ArrayList

class NotificationHelper(base:Context?):ContextWrapper(base) {
    private val channelId = "channelId"
    private val channelNm = "channelNm"

    init{
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createChannel()
        }
    }

    private fun createChannel() {
        val channel = NotificationChannel(channelId, channelNm, NotificationManager.IMPORTANCE_DEFAULT)

        channel.enableLights(true)
        channel.enableVibration(false)
        channel.lightColor= Color.GREEN
        channel.lockscreenVisibility= Notification.VISIBILITY_PRIVATE

        getManager().createNotificationChannel(channel)
    }

    fun getManager():NotificationManager{
        return getSystemService(NOTIFICATION_SERVICE) as NotificationManager
    }

    fun getChannelNotification(title:String,message:String):NotificationCompat.Builder{
        return NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(R.drawable.ic_stat_name)
            .setContentTitle(title)
            .setContentText(message)


//            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        //        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
//        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
//            alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
//        } else {
//            alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
//        }
    }

//    fun sendNotification(notificationDataList: ArrayList<NotificationData>?) {
//
//        Log.d("샌드노티파이",notificationDataList.toString())
//        if (notificationDataList != null) {
//            var message:String
//            for (data in notificationDataList) {
//                val Dday= DayCalculation().calculationDday(data.scheduleTime, DayCalculation().getCurrentDateMillis())
//
//                if (Dday == null || Dday < 0L) {
//                    continue
//                }else{
//                    message = if (Dday ==0L) {
//                        "${data.title} 스케쥴의 D - Day 입니다."
//                    }else{
//                        "${data.title} 스케쥴이 ${Dday}일 남았습니다."
//                    }
//                }
//
//                Log.d("데이터", data.toString())
//
//                val builder = NotificationCompat.Builder(context, channelId)
//                    .setSmallIcon(R.drawable.ic_stat_name)
//                    .setContentTitle("D - Day 알림")
//                    .setContentText(message)
//                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
//
//                val notificationId = data.id
//
//                with(NotificationManagerCompat.from(context)) {
//                    if (notificationId != null) {
//                        notify(notificationId, builder.build())
//                    }
//                }
//            }
//        }
//        val calendar = Calendar.getInstance().apply {
//            timeInMillis = System.currentTimeMillis()
//            set(Calendar.HOUR_OF_DAY, 8)
//            set(Calendar.MINUTE, 0)
//            set(Calendar.SECOND, 0)
//            add(Calendar.DAY_OF_YEAR, 1)
//        }
//
//    }
}