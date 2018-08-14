package com.bhp.securitytest

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import com.bhp.securitytest.broadcast.AlarmNotifications
import com.bhp.securitytest.broadcast.AlarmRegisterExit
import com.bhp.securitytest.db.Register
import com.bhp.securitytest.db.User
import com.bhp.securitytest.db.UserDatabase
import com.bhp.securitytest.enums.VisitTable
import kotlinx.android.synthetic.main.activity_initial.*
import java.util.*


class InitialActivity : AppCompatActivity(), View.OnClickListener {

    private var mSeedDBTask: SeedDBTask? = null

    @SuppressLint("SimpleDateFormat")
    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.sign_in_button -> startActivity(Intent(this@InitialActivity, LoginActivity::class.java))
            R.id.register_button -> startActivity(Intent(this@InitialActivity, RegisterActivity::class.java))
            R.id.visit_button -> startActivity(VisitActivity.intent(this@InitialActivity, VisitTable.USER))
        }
    }

    @SuppressLint("StringFormatMatches", "PrivateResource")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_initial)
        sign_in_button.setOnClickListener(this)
        register_button.setOnClickListener(this)
        visit_button.setOnClickListener(this)

        mSeedDBTask = SeedDBTask()
        mSeedDBTask!!.execute(null as Void?)

        //Alarm for exit automatic
        val calendar = Calendar.getInstance()
        if (android.os.Build.VERSION.SDK_INT >= 23) {
            calendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH),
                    23, 30, 0)
        } else {
            calendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH),
                    23, 30, 0)
        }

        setAlarmRegisterExit(calendar.timeInMillis)
        setAlarmNotifications()

        if (BuildConfig.DEBUG) {
            version.visibility = View.VISIBLE
            val versionNumber = getPackageManager().getPackageInfo(getPackageName(), 0).versionCode
            version.text = (getString(R.string.template_version, versionNumber))
        }
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    @SuppressLint("StaticFieldLeak")
    inner class SeedDBTask internal constructor() : AsyncTask<Void, Void, Boolean>() {
        var db: UserDatabase = UserDatabase.getInstance(this@InitialActivity)!!

        override fun doInBackground(vararg params: Void): Boolean? {
            val user = db.userDao().findByIdAndType("1792739942", "C")
            val register = db.registerDao().getAll()

            if (user == null) {
                db.userDao().insert(User("1792739942", "C", "admin", "admin", "admin@bhp.com", "BHP", "admin"))
            }

            if (register.isEmpty()) {
                db.registerDao().insert(Register(1, "E", "Entrada"))
                db.registerDao().insert(Register(2, "S", "Salida"))
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

    private fun setAlarmRegisterExit(time: Long) {
        //getting the alarm manager
        val am = getSystemService(Context.ALARM_SERVICE) as AlarmManager

        //creating a new intent specifying the broadcast receiver
        val i = Intent(this, AlarmRegisterExit::class.java)

        //creating a pending intent using the intent
        val pi = PendingIntent.getBroadcast(this, 0, i, 0)

        //setting the repeating alarm that will be fired every day
        am.setRepeating(AlarmManager.RTC, time, AlarmManager.INTERVAL_DAY, pi)
    }

    private fun setAlarmNotifications() {
        //getting the alarm manager
        val am = getSystemService(Context.ALARM_SERVICE) as AlarmManager

        //creating a new intent specifying the broadcast receiver
        val i = Intent(this, AlarmNotifications::class.java)

        //creating a pending intent using the intent
        val pi = PendingIntent.getBroadcast(this, 0, i, 0)

        // Set the alarm to start at 8:30 a.m.
        val calendar: Calendar = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
            set(Calendar.HOUR_OF_DAY, 8)
            set(Calendar.MINUTE, 0)
        }

        //setting the repeating alarm that will be fired every day
        am.setRepeating(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, 1000 * 60 * 5, pi)
    }
}