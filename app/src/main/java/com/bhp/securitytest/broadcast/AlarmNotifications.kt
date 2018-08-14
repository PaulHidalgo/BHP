package com.bhp.securitytest.broadcast

import android.annotation.SuppressLint
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.support.v4.app.NotificationCompat
import android.util.Log
import com.bhp.securitytest.InitialActivity
import com.bhp.securitytest.R
import com.bhp.securitytest.Utils
import com.bhp.securitytest.db.UserDatabase
import com.bhp.securitytest.db.UserRegister
import org.joda.time.DateTime
import java.util.*

class AlarmNotifications : BroadcastReceiver() {

    private var mSeedDBTask: SeedDBTask? = null

    override fun onReceive(context: Context, intent: Intent) {
        buildNotification(context, intent)
    }

    @SuppressLint("PrivateResource")
    private fun buildNotification(mContext: Context, intent: Intent) {
        // Create an explicit intent for an Activity in your app
        intent.setClass(mContext, InitialActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        val pendingIntent = PendingIntent.getActivity(mContext, 0, intent, 0)


        val notificationManager = mContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val mBuilder = NotificationCompat.Builder(mContext)
                .setSmallIcon(R.drawable.notification_icon_background)
                .setContentTitle("Test Title")
                .setContentText("Test Texto")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)

        // Get the current date & time
        val calendar = Calendar.getInstance()
        val hourOfDay = calendar.get(Calendar.HOUR_OF_DAY)
        //Show notification only ina range of hours
        if (hourOfDay in 9..18) {
            notificationManager.notify(1, mBuilder.build())
        }

    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    @SuppressLint("StaticFieldLeak")
    inner class SeedDBTask internal constructor(private val mContext: Context) : AsyncTask<Void, Void, Boolean>() {
        var db: UserDatabase = UserDatabase.getInstance(mContext)!!

        override fun doInBackground(vararg params: Void): Boolean? {
            val userRegisters = db.userRegisterDao().getAllUsersRegisterGroupBy(Utils.parseDate(Date()), Utils.parseDate(Date()))

            if (userRegisters.isNotEmpty()) {
                for (i in 0 until userRegisters.size) {
                    val verifyEntry = db.userRegisterDao().getCountUserDay(userRegisters[i].user_id.toUpperCase(), Utils.parseDate(Date()))
                    if (verifyEntry % 2 != 0) {
                        val userRegister = UserRegister(userRegisters[i].user_id.toUpperCase(), 2, Utils.parseDate(Date()), DateTime())
                        db.userRegisterDao().insert(userRegister)
                    }
                }
            }
            return true
        }

        override fun onPostExecute(success: Boolean?) {
            mSeedDBTask = null
        }

        override fun onCancelled() {
            mSeedDBTask = null
        }
    }
}
