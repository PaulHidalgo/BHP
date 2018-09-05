package com.bhp.securitytest.broadcast

import android.annotation.SuppressLint
import android.app.Activity
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.support.v4.app.NotificationCompat
import com.bhp.securitytest.InitialActivity
import com.bhp.securitytest.R
import com.bhp.securitytest.Utils
import com.bhp.securitytest.db.UserDatabase
import com.bhp.securitytest.db.UserRegisterDao
import com.bhp.securitytest.services.PushReceiverIntentService
import java.util.*

class AlarmNotifications : BroadcastReceiver() {

    private var mSeedDBTask: SeedDBTask? = null
    var data: List<UserRegisterDao.UserRegisterQuery>? = null

    override fun onReceive(context: Context, intent: Intent) {
        attemptVerifyDB(context, intent)
        resultCode = Activity.RESULT_OK
    }

    private fun attemptVerifyDB(mContext: Context, intent: Intent) {
        if (mSeedDBTask != null) {
            return
        }
        mSeedDBTask = SeedDBTask(mContext, intent)
        mSeedDBTask!!.execute()
    }

    @SuppressLint("PrivateResource")
    private fun buildNotification(mContext: Context, intent: Intent, body: String) {
        // Create an explicit intent for an Activity in your app
        intent.setClass(mContext, InitialActivity::class.java)
//        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        intent.putExtra("Body", body)
        val pendingIntent = PendingIntent.getActivity(mContext, 0, intent, 0)


        val notificationManager = mContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val mBuilder = NotificationCompat.Builder(mContext)
                .setSmallIcon(R.drawable.notification_icon_background)
                .setContentTitle("Visitantes")
                .setContentText(body)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)

        // Get the current date & time
        val calendar = Calendar.getInstance()
        val hourOfDay = calendar.get(Calendar.HOUR_OF_DAY)
        //Show notification only ina range of hours
        if (hourOfDay in 8..20) {
            val intentService = Intent(mContext, PushReceiverIntentService::class.java)
            intentService.putExtra("Body", body)
            mContext.startService(intentService)

            notificationManager.notify(1, mBuilder.build())
        }

    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    @SuppressLint("StaticFieldLeak")
    inner class SeedDBTask internal constructor(private val mContext: Context, val intent: Intent) : AsyncTask<Void, Void, Boolean>() {
        var db: UserDatabase = UserDatabase.getInstance(mContext)!!
        var body: String = ""
        val dataTemp: MutableList<UserRegisterDao.UserRegisterQuery> = mutableListOf()

        override fun doInBackground(vararg params: Void): Boolean? {
            data = db.userRegisterDao().getUsersRegisterByState("E", Utils.parseDate(Date()), Utils.parseDate(Date()))
            if (data!!.isNotEmpty()) {
                var temp = ""
                for (i in 0 until data!!.size) {
                    if (temp != data!![i].userId) {
                        val countRegister = db.userRegisterDao().getCountUserDay(data!![i].userId, Utils.parseDate(Date()))
                        if (countRegister % 2 != 0) {
                            temp = data!![i].userId

                            val dataFilter = db.userRegisterDao().getUsersRegisterByStateId(data!![i].userId, "E", Utils.parseDate(Date()), Utils.parseDate(Date()))
                            if (dataFilter.isNotEmpty()) {

                                dataTemp.add(dataFilter.last())
                            }
                        }
                    }
                }

                data = dataTemp
            }

            if (data!!.isNotEmpty()) {
                for (i in 0 until data!!.size) {
                    body += data!![i].userId + " - " + data!![i].userName + " " + data!![i].userLastName
                }
            } else {
                body = "No existen visitantes"
            }
            return true
        }

        override fun onPostExecute(success: Boolean?) {
            buildNotification(mContext, intent, body)
            mSeedDBTask = null
        }

        override fun onCancelled() {
            mSeedDBTask = null
        }
    }
}
