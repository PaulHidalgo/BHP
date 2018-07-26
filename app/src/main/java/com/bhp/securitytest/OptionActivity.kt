package com.bhp.securitytest

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.view.View
import com.bhp.securitytest.db.User
import com.bhp.securitytest.db.UserDatabase
import com.bhp.securitytest.db.UserRegister
import com.bhp.securitytest.enums.VisitTable
import com.bhp.securitytest.presentation.PresentationActivity
import kotlinx.android.synthetic.main.activity_option.*
import org.joda.time.DateTime
import java.util.*

class OptionActivity : BaseActivity(), View.OnClickListener {

    private var mVerifyLoginTask: VerifyLoginTask? = null
    private lateinit var user: User

    companion object {

        fun intent(context: Context, user: User?): Intent {
            val intent = Intent(context, OptionActivity::class.java)
            val extras = Bundle()
            if (user != null) {
                extras.putParcelable(PresentationActivity.EXTRA_USER, user)
                intent.putExtras(extras)
            }
            return intent
        }
    }

    @Suppress("CAST_NEVER_SUCCEEDS")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_option)

        if (intent.extras == null) {
            ll_courses.visibility = View.GONE
            ll_admin.visibility = View.VISIBLE
        } else {
            user = intent.getParcelableExtra(PresentationActivity.EXTRA_USER)
            attemptEntryExit(StateUser.ENTRY)
        }

        courses_button.setOnClickListener(this)
        io_button.setOnClickListener(this)
        btn_courses.setOnClickListener(this)
        btn_visitors.setOnClickListener(this)

    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.courses_button -> startActivity(PresentationActivity.intent(this@OptionActivity, user))
            R.id.io_button -> {
                AlertDialog.Builder(this).setTitle(R.string.bhp).setMessage(R.string.message_register_exit).setPositiveButton(R.string.yes) { dialog, which ->
                    attemptEntryExit(StateUser.EXIT)
                }.setNegativeButton(R.string.no, null).show()

            }
            R.id.btn_courses -> startActivity(DashboardActivity.intent(this@OptionActivity))
            R.id.btn_visitors -> startActivity(VisitActivity.intent(this@OptionActivity, VisitTable.ADMIN))

        }

    }

    private fun attemptEntryExit(mStateUser: StateUser) {
        if (mVerifyLoginTask != null) {
            return
        }
        mVerifyLoginTask = VerifyLoginTask(user.id, Utils.parseDate(Date()), mStateUser)
        mVerifyLoginTask!!.execute(null as Void?)
    }


    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    @SuppressLint("StaticFieldLeak")
    inner class VerifyLoginTask internal constructor(private val mId: String, private val mDate: Date, private val mStateUser: StateUser) : AsyncTask<Void, Void, Boolean>() {
        var db: UserDatabase = UserDatabase.getInstance(this@OptionActivity)!!
        var count: Int? = null
        var userRegister: UserRegister? = null
        var hour = DateTime()

        override fun doInBackground(vararg params: Void): Boolean? {
            count = db.userRegisterDao().getCountUserDay(mId, mDate)

            when (mStateUser) {
                StateUser.ENTRY -> {
                    if (count!! % 2 == 0) {
                        userRegister = UserRegister(mId.toUpperCase(), 1, Utils.parseDate(Date()), hour)
                        db.userRegisterDao().insert(userRegister!!)
                        return true
                    }
                    return false
                }

                StateUser.EXIT -> {
                    if (count!! % 2 == 0) {
                        return false
                    }
                    userRegister = UserRegister(mId.toUpperCase(), 2, Utils.parseDate(Date()), hour)
                    db.userRegisterDao().insert(userRegister!!)
                    return true
                }
            }
        }

        override fun onPostExecute(success: Boolean?) {
            mVerifyLoginTask = null
            when (mStateUser) {
                StateUser.ENTRY -> {
                    if (success!!) {
                        AlertDialog.Builder(this@OptionActivity).setTitle(R.string.bhp).setMessage(getString(R.string.register_entry, Utils.parseDateString(hour)))
                                .setPositiveButton(R.string.accept, null).setCancelable(false).show()
                    }
                }

                StateUser.EXIT -> {
                    if (success!!) {
                        AlertDialog.Builder(this@OptionActivity).setTitle(R.string.bhp).setMessage(getString(R.string.register_exit))
                                .setPositiveButton(R.string.accept) { dialog, which -> finish() }.setCancelable(false).show()
                    } else {
                        AlertDialog.Builder(this@OptionActivity).setTitle(R.string.bhp).setMessage(getString(R.string.error_register_exit))
                                .setPositiveButton(R.string.accept, null).setCancelable(false).show()

                    }
                }
            }
        }

        override fun onCancelled() {
            mVerifyLoginTask = null
        }
    }

    enum class StateUser {
        ENTRY, EXIT
    }

}
