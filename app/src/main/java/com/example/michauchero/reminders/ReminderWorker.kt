package com.example.michauchero.reminders

import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.michauchero.R

class ReminderWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val title = inputData.getString(KEY_TITLE) ?: "Recordatorio de pago"
        val amount = inputData.getDouble(KEY_AMOUNT, 0.0)

        NotificationUtils.ensureChannel(applicationContext)

        val text = if (amount > 0.0) {
            "Vence: $title ($${"%.2f".format(amount)})"
        } else title

        val notification = NotificationCompat.Builder(applicationContext, NotificationUtils.CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Recordatorio de pago")
            .setContentText(text)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        if (NotificationManagerCompat.from(applicationContext).areNotificationsEnabled()) {
            NotificationManagerCompat.from(applicationContext).notify(NOTIFICATION_ID, notification)
        }

        return Result.success()
    }

    companion object {
        const val KEY_TITLE = "title"
        const val KEY_AMOUNT = "amount"
        const val NOTIFICATION_ID = 1001
    }
}