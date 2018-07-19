package com.bhp.securitytest.presentation

import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter
import android.support.v4.view.ViewPager
import android.support.v7.app.AlertDialog
import android.view.MenuItem
import android.view.animation.AnimationUtils
import com.bhp.securitytest.BaseActivity
import com.bhp.securitytest.R
import com.bhp.securitytest.Utils
import com.bhp.securitytest.db.User
import com.bhp.securitytest.db.UserDatabase
import kotlinx.android.synthetic.main.activity_presentation.*
import java.util.*

class PresentationActivity : BaseActivity(), PresentationActivityFragment.QuestionsCallback {
    override fun onFail() {
        pager.setCurrentItem(0, true)
    }

    override fun onQuestionsAnswered(q1: Boolean, q2: Boolean, success: Boolean) {
        if (mResponseSaveTask != null) {
            return
        }
        mResponseSaveTask = ResponseSaveTask(q1, q2, success)
        mResponseSaveTask!!.execute(null as Void?)
    }

    private var menuItem: MenuItem? = null
    private var mResponseSaveTask: ResponseSaveTask? = null
    private lateinit var user: User

    companion object {

        const val EXTRA_USER = "extra_user"

        fun intent(context: Context, user: User): Intent {
            val intent = Intent(context, PresentationActivity::class.java)
            val extras = Bundle()
            extras.putParcelable(EXTRA_USER, user)
            intent.putExtras(extras)
            return intent
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_presentation)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setTitle(R.string.presentation_1_title)


        btn_left.setOnClickListener { handleNavigationLeft() }
        btn_right.setOnClickListener { handleNavigationRight() }

        val animation = AnimationUtils.loadAnimation(this, R.anim.fade_in_out)
        btn_left.animation = animation
        btn_right.animation = animation

        pager.adapter = ScreenSlidePagerAdapter(supportFragmentManager)
        pager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {
            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
            }

            override fun onPageSelected(position: Int) {
                animation.start()
                menuItem?.isVisible = false
                when (position) {
                    0 -> supportActionBar?.setTitle(R.string.presentation_1_title)
                    1 -> supportActionBar?.setTitle(R.string.presentation_2_title)
                    2 -> supportActionBar?.setTitle(R.string.presentation_3_title)
                    3 -> supportActionBar?.setTitle(R.string.presentation_4_title)
                    4 -> supportActionBar?.setTitle(R.string.presentation_5_title)
                    5 -> supportActionBar?.setTitle(R.string.presentation_6_title)
                    6 -> {
                        menuItem?.isVisible = true
                        supportActionBar?.setTitle(R.string.question_title)
                    }
                }
            }
        })

        user = intent.getParcelableExtra(EXTRA_USER)

        if (user.date > 0) {
            if (Utils.isValid(user.date)) {
                AlertDialog.Builder(this).setTitle(R.string.app_name).setMessage(getString(R.string.course_valid_temp, Utils.getRemainingTime(resources, user.date)))
                        .setPositiveButton(R.string.accept, null).show()
            } else {
                AlertDialog.Builder(this).setTitle(R.string.app_name).setMessage(R.string.course_expired)
                        .setPositiveButton(R.string.accept, null).show()
            }
        }
    }

    private fun handleNavigationRight() {
        pager.setCurrentItem(pager.currentItem + 1, true)
    }

    private fun handleNavigationLeft() {
        pager.setCurrentItem(pager.currentItem - 1, true)
    }

    override fun onBackPressed() {
        if (pager.currentItem == 0) {
            AlertDialog.Builder(this).setTitle(R.string.app_name).setMessage(R.string.message_exit).setPositiveButton(R.string.yes) { dialog, which ->
                super.onBackPressed()
            }.setNegativeButton(R.string.no, null).show()
        } else {
            handleNavigationLeft()
        }
    }

    inner class ScreenSlidePagerAdapter(fm: FragmentManager) : FragmentStatePagerAdapter(fm) {
        override fun getItem(position: Int): Fragment {
            return when (position) {
                0 -> PresentationActivityFragment.instance(R.layout.fragment_presentation_1)
                1 -> PresentationActivityFragment.instance(R.layout.fragment_presentation_2)
                2 -> PresentationActivityFragment.instance(R.layout.fragment_presentation_3)
                3 -> PresentationActivityFragment.instance(R.layout.fragment_presentation_4)
                4 -> PresentationActivityFragment.instance(R.layout.fragment_presentation_5)
                5 -> PresentationActivityFragment.instance(R.layout.fragment_presentation_6)
                6 -> {
                    val fragment = PresentationActivityFragment.instance(R.layout.fragment_presentation_7)
                    fragment.callback = this@PresentationActivity
                    fragment
                }
                else -> PresentationActivityFragment.instance(R.layout.fragment_presentation_1)
            }
        }

        override fun getCount(): Int {
            return 7
        }
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    inner class ResponseSaveTask internal constructor(private val q1: Boolean, private val q2: Boolean, private val questionsOk: Boolean) : AsyncTask<Void, Void, Boolean>() {
        var db: UserDatabase = UserDatabase.getInstance(this@PresentationActivity)!!

        override fun doInBackground(vararg params: Void): Boolean? {
            user.q1 = q1
            user.q2 = q2
            user.date = Date().time
            user.success = questionsOk
            db.userDao().update(user)
            return true
        }

        override fun onPostExecute(success: Boolean?) {
            mResponseSaveTask = null
            if (questionsOk && success!!) {
                AlertDialog.Builder(this@PresentationActivity).setTitle(R.string.app_name)
                        .setMessage("${getString(R.string.question_msg_ok)}\n${getString(R.string.course_valid_temp, Utils.getRemainingTime(resources, user.date))}")
                        .setPositiveButton(R.string.accept) { dialog, which ->
                            finish()
                        }.setCancelable(false).show()
            }
        }

        override fun onCancelled() {
            mResponseSaveTask = null
        }
    }
}
