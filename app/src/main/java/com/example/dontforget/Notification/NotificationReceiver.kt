package com.example.dontforget.Notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {

        Log.d("컨텍스트",context.toString())
        val notificationHelper = NotificationHelper(context)
        val notificationDataList = intent.getParcelableArrayListExtra<NotificationData>("notifyList")
        Log.d("보낸노티파이",notificationDataList.toString())
        notificationHelper.sendNotification(notificationDataList)
    }
}