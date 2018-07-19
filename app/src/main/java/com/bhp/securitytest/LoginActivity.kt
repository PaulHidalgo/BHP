package com.bhp.securitytest

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.os.AsyncTask
import android.os.Bundle
import android.support.design.widget.TextInputLayout
import android.text.InputFilter
import android.text.InputType
import android.text.TextUtils
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import com.bhp.securitytest.db.User
import com.bhp.securitytest.db.UserDatabase
import com.bhp.securitytest.db.UserRegister
import kotlinx.android.synthetic.main.activity_login.*

/**
 * A login screen that offers login via email/password.
 */
class LoginActivity : BaseActivity() {
    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private var mAuthTask: UserLoginTask? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Set up the login form.
        id.setOnEditorActionListener(TextView.OnEditorActionListener { _, id, _ ->
            if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                attemptLogin()
                return@OnEditorActionListener true
            }
            false
        })

        sign_in_button.setOnClickListener { attemptLogin() }

        switch_id.setOnCheckedChangeListener { _, isChecked ->
            id.setText("")
            if (isChecked) {
                id.setHint(R.string.prompt_passport)
                id.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS or InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS
                id.filters = arrayOf(InputFilter.LengthFilter(15))
                (id.parent.parent as TextInputLayout).hint = getString(R.string.prompt_passport)
            } else {
                id.setHint(R.string.prompt_id)
                id.inputType = InputType.TYPE_CLASS_NUMBER
                id.filters = arrayOf(InputFilter.LengthFilter(10))
                (id.parent.parent as TextInputLayout).hint = getString(R.string.prompt_id)
            }
        }

        if (BuildConfig.DEBUG) {
            id.setText("1792739942")
        }
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private fun attemptLogin() {
        if (mAuthTask != null) {
            return
        }

        // Reset errors.
        id.error = null

        // Store values at the time of the login attempt.
        val idStr = id.text.toString()
        val idTypeStr = if (switch_id.isChecked) "P" else "C"

        var cancel = false
        var focusView: View? = null

        // Check for a valid id.
        if (TextUtils.isEmpty(idStr)) {
            id.error = getString(R.string.error_field_required)
            focusView = id
            cancel = true
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView?.requestFocus()
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true)
            mAuthTask = UserLoginTask(idStr, idTypeStr)
            mAuthTask!!.execute(null as Void?)
        }
    }

    private fun isIdValid(id: String): Boolean {
        return id.length == 10
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    private fun showProgress(show: Boolean) {
        val shortAnimTime = resources.getInteger(android.R.integer.config_shortAnimTime).toLong()

        login_form.visibility = if (show) View.GONE else View.VISIBLE
        login_form.animate()
                .setDuration(shortAnimTime)
                .alpha((if (show) 0 else 1).toFloat())
                .setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        login_form.visibility = if (show) View.GONE else View.VISIBLE
                    }
                })

        login_progress.visibility = if (show) View.VISIBLE else View.GONE
        login_progress.animate()
                .setDuration(shortAnimTime)
                .alpha((if (show) 1 else 0).toFloat())
                .setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        login_progress.visibility = if (show) View.VISIBLE else View.GONE
                    }
                })
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    inner class UserLoginTask internal constructor(private val mId: String, private val mIdType: String) : AsyncTask<Void, Void, User>() {
        var db: UserDatabase = UserDatabase.getInstance(this@LoginActivity)!!

        override fun doInBackground(vararg params: Void): User? {
            val user = db.userDao().findByIdAndType(mId.toUpperCase(), mIdType)

            if (user != null) {
                return user
            }
            return null
        }

        override fun onPostExecute(user: User?) {
            mAuthTask = null
            showProgress(false)

            if (user == null) {
                id.error = getString(R.string.error_incorrect_data)
                id.requestFocus()
            } else {
                if (user.id == "1792739942") {
                    startActivity(OptionActivity.intent(this@LoginActivity, null))
                } else {
                    startActivity(OptionActivity.intent(this@LoginActivity, user))
                }
                finish()
            }
        }

        override fun onCancelled() {
            mAuthTask = null
            showProgress(false)
        }
    }
}
