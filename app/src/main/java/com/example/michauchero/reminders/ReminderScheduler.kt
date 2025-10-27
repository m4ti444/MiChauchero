package com.example.michauchero.reminders

import android.content.Context
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import java.time.Instant

object ReminderScheduler {
    fun scheduleBillReminder(
        context: Context,
        title: String,
        amount: Double,
        dueEpoch: Long
    ) {
        val delayMillis = (dueEpoch - Instant.now().toEpochMilli()).coerceAtLeast(0)
        val input = Data.Builder()
            .putString(ReminderWorker.KEY_TITLE, title)
            .putDouble(ReminderWorker.KEY_AMOUNT, amount)
            .build()
        val request = OneTimeWorkRequestBuilder<ReminderWorker>()
            .setInputData(input)
            .setInitialDelay(java.time.Duration.ofMillis(delayMillis))
            .build()
        WorkManager.getInstance(context).enqueue(request)
    }
}