package com.bhp.securitytest.broadcast

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import com.bhp.securitytest.Utils
import com.bhp.securitytest.db.UserDatabase
import com.bhp.securitytest.db.UserRegister
import org.joda.time.DateTime
import java.util.*

class AlarmRegisterExit : BroadcastReceiver() {

    private var mSeedDBTask: SeedDBTask? = null

    override fun onReceive(context: Context, intent: Intent) {
        attemptVerifyDB(context)
    }

    private fun attemptVerifyDB(mContext: Context) {
        if (mSeedDBTask != null) {
            return
        }
        mSeedDBTask = SeedDBTask(mContext)
        mSeedDBTask!!.execute()
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
