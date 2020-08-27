package com.kakao.iron.ui.base

import android.app.Activity
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.kakao.iron.ui.SplashActivity
import kotlin.system.exitProcess

class ExceptionHandler(
    private val activity: Activity
) : Thread.UncaughtExceptionHandler {

    override fun uncaughtException(t: Thread, e: Throwable) {
        try {
            kotlin.run {
                val intent = Intent(activity, SplashActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
                val pendingIntent = PendingIntent.getActivity(
                    activity.applicationContext , 0, intent, intent.flags
                )
                val alarmManager: AlarmManager = activity.applicationContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                alarmManager.set(AlarmManager.RTC, System.currentTimeMillis() + RESTART_TIME, pendingIntent)
            }
            activity.finish()
            exitProcess(2)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    companion object {
        const val RESTART_TIME = 1000L
    }
}