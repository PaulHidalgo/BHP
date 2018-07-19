package com.bhp.securitytest

import android.annotation.SuppressLint
import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import com.bhp.securitytest.db.Register
import com.bhp.securitytest.db.User
import com.bhp.securitytest.db.UserDatabase
import com.bhp.securitytest.enums.VisitTable
import kotlinx.android.synthetic.main.activity_initial.*

class InitialActivity : AppCompatActivity(), View.OnClickListener {

    private var mSeedDBTask: SeedDBTask? = null

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.sign_in_button -> startActivity(Intent(this@InitialActivity, LoginActivity::class.java))
            R.id.register_button -> startActivity(Intent(this@InitialActivity, RegisterActivity::class.java))
            R.id.visit_button -> startActivity(VisitActivity.intent(this@InitialActivity, VisitTable.USER))
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_initial)
        sign_in_button.setOnClickListener(this)
        register_button.setOnClickListener(this)
        visit_button.setOnClickListener(this)

        mSeedDBTask = SeedDBTask()
        mSeedDBTask!!.execute(null as Void?)
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
}