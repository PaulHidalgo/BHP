package com.bhp.securitytest.services

import android.app.Activity
import android.app.IntentService
import android.content.Intent
import android.os.Handler

class PushReceiverIntentService : IntentService("PushReceiverIntentService") {

    private val callback = Handler.Callback { throw IllegalArgumentException("PUSH_RECEIVED NOT HANDLED!") }


    override fun onHandleIntent(intent: Intent?) {
        val extras = intent!!.getExtras()
        val broadcast = Intent()
        broadcast.putExtras(extras)
        broadcast.setAction("com.bhp.securitytest.broadcast.BROADCAST_NOTIFICATION")

        sendOrderedBroadcast(broadcast, null, null, Handler(callback), Activity.RESULT_OK, null, extras)
    }

}
