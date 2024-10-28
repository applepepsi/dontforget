package com.example.dontforget.Notification

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Context.ALARM_SERVICE
import android.content.Intent
import android.icu.util.Calendar
import android.os.Build
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.content.getSystemService
import com.example.dontforget.R
import com.example.dontforget.model.DayCalculation

class NotificationReceiver : BroadcastReceiver() {

    var id=0

    override fun onReceive(context: Context, notificationIntent: Intent) {

        var notificationDataList=notificationIntent.getParcelableArrayListExtra<NotificationData>("notificationDataList")
        id=notificationIntent.getIntExtra("notificationId",0)

        var notificationHelper:NotificationHelper=NotificationHelper(context)

        Log.d("notificationDataList", notificationDataList.toString())
        for (data in notificationDataList!!) {
            val Dday= DayCalculation().calculationDday(data.scheduleTime, DayCalculation().getCurrentDateMillis())
            if (Dday == null || Dday < 0L) {
                continue
            }else{
                var message = if (Dday ==0L) {
                    "${data.title} 스케쥴의 D - Day 입니다."
                }else{
                    "${data.title} 스케쥴이 ${Dday}일 남았습니다."
                }
                val nb:NotificationCompat.Builder=notificationHelper.getChannelNotification(data.title!!,message)
                notificationHelper.getManager().notify(data.id!!, nb.build())
            }
        }
        createNewAlarm(context, notificationIntent)

        // TODO: 알림 매일 오게는 설정했으나   adb shell dumpsys alarm | findstr com.example.dontforget 명령어를 쳤을때
        //알람이 올때마다 wakeups이 계속 증가해 휴대폰에 부하를 준다. 또한 메인엑티비티에서 스케쥴을 추가, 수정했을 때 새로운 알림을 설정하는
        //기능도 추가해야함(지금은 처음 앱을 실행했을때만 알람을 세팅함
    }
    private fun createNewAlarm(context: Context, notificationIntent: Intent){
        val calendar = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
            set(Calendar.HOUR_OF_DAY, 8)
            add(Calendar.DAY_OF_YEAR, 1)
        }

        val alarmManager: AlarmManager =
            context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val pendingInte

        nt = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.getBroadcast(
                context,
                id,
                notificationIntent,
                PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        } else {
            PendingIntent.getBroadcast(
                context,
                id,
                notificationIntent,
                PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
        } else {
            alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
        }
    }



//    private fun createNewAlarm(context: Context?, NotificationIntent: Intent){
//        val calendar = Calendar.getInstance().apply {
//            timeInMillis = System.currentTimeMillis()
//            set(Calendar.HOUR_OF_DAY, 8)
//            add(Calendar.DAY_OF_YEAR, 1)
//        }
//
//
//        val alarmManager:AlarmManager= context?.getSystemService(ALARM_SERVICE) as AlarmManager
//
//
//        val pendingIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            PendingIntent.getBroadcast(
//                context,
//                id,
//                NotificationIntent,
//                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
//            )
//        } else {
//            PendingIntent.getBroadcast(
//                context,
//                id,
//                NotificationIntent,
//                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
//            )
//        }
//        Log.d("팬딩", pendingIntent.toString())
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
//        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
//            alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
//        } else {
//            alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
//        }
//
//    }

//    private fun cancelAlarm(context: Context?, NotificationIntent: Intent){
//
//        val alarmManager:AlarmManager= context?.getSystemService(ALARM_SERVICE) as AlarmManager
//
//
//        val pendingIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            PendingIntent.getBroadcast(
//                context,
//                id,
//                NotificationIntent,
//                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
//            )
//        } else {
//            PendingIntent.getBroadcast(
//                context,
//                id,
//                NotificationIntent,
//                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
//            )
//        }
//
//        alarmManager.cancel(pendingIntent)
//
//    }

}



//
//    fun createNewNotification(){
//
//    }

//    fun notificationFilter(){
//        lifecycleScope.launch(Dispatchers.IO) {
//            withContext(Dispatchers.Main) {
//                val currentCurrentDateMilli = DayCalculation().getCurrentDateMillis()
//                val notifyList = scheduleDao.findSwitchOnData(currentCurrentDateMilli)
//                val notificationDataList = notifyList.map { scheduleModel ->
//                    NotificationData(
//                        id = scheduleModel.id,
//                        scheduleText = scheduleModel.scheduleText,
//                        scheduleTime = scheduleModel.scheduleTime,
//                        title = scheduleModel.title,
//                        dday=scheduleModel.dday
//                    )
//                }
//                scheduleNotification(notificationDataList)
//            }
//        }
//    }
