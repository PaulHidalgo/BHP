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
import com.bhp.securitytest.presentation.PresentationActivity
import kotlinx.android.synthetic.main.activity_register.*
import org.joda.time.DateTime
import java.util.*

class RegisterActivity : BaseActivity() {

    private var mRegisterTask: UserRegisterTask? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        position.setOnEditorActionListener(TextView.OnEditorActionListener { _, id, _ ->
            if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                attemptRegister()
                return@OnEditorActionListener true
            }
            false
        })

        register_button.setOnClickListener { attemptRegister() }
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
    }

    private fun attemptRegister() {
        if (mRegisterTask != null) {
            return
        }

        // Reset errors.
        name.error = null
        lastname.error = null
        email.error = null
        id.error = null
        company.error = null
        position.error = null

        // Store values at the time of the login attempt.
        val nameStr = name.text.toString().trim()
        val lastnameStr = lastname.text.toString().trim()
        val emailStr = email.text.toString().trim()
        val idStr = id.text.toString().trim()
        val idTypeStr = if (switch_id.isChecked) "P" else "C"
        val companyStr = company.text.toString().trim()
        val positionStr = position.text.toString().trim()

        var cancel = false
        var focusView: View? = null

        // Check for a valid name.
        if (TextUtils.isEmpty(nameStr)) {
            name.error = getString(R.string.error_field_required)
            focusView = name
            cancel = true
        }
        // Check for a valid lastname.
        if (TextUtils.isEmpty(lastnameStr)) {
            lastname.error = getString(R.string.error_field_required)
            focusView = lastname
            cancel = true
        }
        // Check for a valid email address.
        if (TextUtils.isEmpty(emailStr)) {
            email.error = getString(R.string.error_field_required)
            focusView = email
            cancel = true
        } else if (!isEmailValid(emailStr)) {
            email.error = getString(R.string.error_invalid_email)
            focusView = email
            cancel = true
        }

        // Check for a valid id.
        if (TextUtils.isEmpty(idStr)) {
            id.error = getString(R.string.error_field_required)
            focusView = id
            cancel = true
        }
        // Check for a valid password, if the user entered one.
        if (TextUtils.isEmpty(companyStr)) {
            company.error = getString(R.string.error_field_required)
            focusView = company
            cancel = true
        }
        // Check for a valid password, if the user entered one.
        if (TextUtils.isEmpty(positionStr)) {
            position.error = getString(R.string.error_field_required)
            focusView = position
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
            mRegisterTask = UserRegisterTask(Utils.stripSpecialCharacters(nameStr), Utils.stripSpecialCharacters(lastnameStr), Utils.stripSpecialCharacters(emailStr), Utils.stripSpecialCharacters(idStr), Utils.stripSpecialCharacters(idTypeStr), Utils.stripSpecialCharacters(companyStr), Utils.stripSpecialCharacters(positionStr))
            mRegisterTask!!.execute(null as Void?)
        }
    }

    private fun isEmailValid(email: String): Boolean {
        return email.contains("@")
    }

    private fun isPasswordValid(password: String): Boolean {
        return password.length > 4
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    private fun showProgress(show: Boolean) {
        val shortAnimTime = resources.getInteger(android.R.integer.config_shortAnimTime).toLong()

        register_form.visibility = if (show) View.GONE else View.VISIBLE
        register_form.animate()
                .setDuration(shortAnimTime)
                .alpha((if (show) 0 else 1).toFloat())
                .setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        register_form.visibility = if (show) View.GONE else View.VISIBLE
                    }
                })

        register_progress.visibility = if (show) View.VISIBLE else View.GONE
        register_progress.animate()
                .setDuration(shortAnimTime)
                .alpha((if (show) 1 else 0).toFloat())
                .setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        register_progress.visibility = if (show) View.VISIBLE else View.GONE
                    }
                })
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    inner class UserRegisterTask internal constructor(private val mName: String, private val mLastname: String, private val mEmail: String, private val mId: String, private val mIdType: String, private val mCompany: String, private val mPosition: String) : AsyncTask<Void, Void, Boolean>() {
        var db: UserDatabase = UserDatabase.getInstance(this@RegisterActivity)!!
        var user: User? = null
        var userRegister: UserRegister? = null

        override fun doInBackground(vararg params: Void): Boolean? {
            user = db.userDao().findByIdAndType(mId.toUpperCase(), mIdType)

            if (user == null) {
                user = User(mId.toUpperCase(), mIdType, mName, mLastname, mEmail, mCompany, mPosition)
                userRegister = UserRegister(mId.toUpperCase(), 1, Utils.parseDate(Date()), DateTime())

                db.userDao().insert(user!!)
                db.userRegisterDao().insert(userRegister!!)
                return true
            }
            return false
        }

        override fun onPostExecute(success: Boolean?) {
            mRegisterTask = null
            showProgress(false)

            if (success!!) {
                startActivity(PresentationActivity.intent(this@RegisterActivity, user!!))
                finish()
            } else {
                id.error = getString(R.string.error_user_already_exists)
                id.requestFocus()
            }
        }

        override fun onCancelled() {
            mRegisterTask = null
            showProgress(false)
        }
    }
}