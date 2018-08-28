package com.bhp.securitytest

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import com.bhp.securitytest.enums.VisitTable

abstract class NotificationBaseActivity : AppCompatActivity() {

    private var bodyNotifiation: String? = null

    private val notificationReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            // Do something with this incoming message here
            // Since we will process the message and update the UI, we don't need to show a message in Status Bar
            // To do this, we call abortBroadcast()
            abortBroadcast()
            if (intent.extras != null) {
                bodyNotifiation = intent.getStringExtra("Body")
                if (bodyNotifiation != null) {
                    intent.removeExtra("Body")
                    displayPopup(bodyNotifiation!!)
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        val filter = IntentFilter("com.bhp.securitytest.broadcast.BROADCAST_NOTIFICATION")
        filter.setPriority(1)
        registerReceiver(notificationReceiver, filter)
    }

    override fun onStop() {
        super.onStop()
        unregisterReceiver(notificationReceiver)
    }

    fun displayPopup(body: String) {
        AlertDialog.Builder(this@NotificationBaseActivity).setTitle(R.string.bhp).setMessage(body)
                .setPositiveButton(R.string.visitors)
                { dialog, which ->
                    startActivity(VisitActivity.intent(this@NotificationBaseActivity, VisitTable.USER))
                }
                .setNegativeButton(R.string.close, null)
                .setCancelable(true).show()
    }
}