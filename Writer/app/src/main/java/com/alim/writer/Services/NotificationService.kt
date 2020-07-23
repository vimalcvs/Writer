package com.alim.writer.Services

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class NotificationService : FirebaseMessagingService() {

    override fun onMessageReceived(p0: RemoteMessage) {
        super.onMessageReceived(p0)

        Log.println(Log.ASSERT, "Service", "From: " + p0.from)
        if (p0.data.isNotEmpty()) {
            Log.println(Log.ASSERT, "Service", "Got Data")
        } else {
            Log.println(Log.ASSERT, "Service", "DATA Empty")
        }

        if (p0.notification != null) {
            Log.println(Log.ASSERT, "Service", "Got Data")
            //Log.println(Log.ASSERT, "Service","Message Notification Body: " + p0.notification.body.toString())
        }
    }

    override fun onNewToken(p0: String) {
        super.onNewToken(p0)
    }
}
