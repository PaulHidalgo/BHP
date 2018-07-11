package com.bhp.securitytest

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.os.AsyncTask
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.widget.DatePicker
import android.widget.TableRow
import android.widget.TextView
import com.bhp.securitytest.db.UserDatabase
import com.bhp.securitytest.db.UserRegisterDao
import kotlinx.android.synthetic.main.activity_visit.*
import java.util.*

class VisitActivity : BaseActivity(), DatePickerDialog.OnDateSetListener {

    private var mTask: GetDataTask? = null
    private var datePicker: DatePickerDialog? = null
    private var date: Calendar? = Calendar.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_visit)
        datePicker = DatePickerDialog(this, R.style.DialogTheme, this, date!!.get(Calendar.YEAR), date!!.get(Calendar.MONTH), date!!.get(Calendar.DAY_OF_MONTH))
        datePicker!!.datePicker.minDate = date!!.timeInMillis

        query_button.setOnClickListener {
            attemptQuery()
//            datePicker!!.show()
        }

    }


    fun attemptQuery() {
        if (mTask != null) {
            return
        }
        mTask = GetDataTask()
        mTask!!.execute(null as Void?)
    }


    fun createTable() {
        val rowHead = LayoutInflater.from(this).inflate(R.layout.attrib_row, null) as TableRow
        (rowHead.findViewById<View>(R.id.txt_date) as TextView).text = ("Fecha")
        (rowHead.findViewById<View>(R.id.txt_ci) as TextView).text = ("Cédula")
        (rowHead.findViewById<View>(R.id.txt_name) as TextView).text = ("Nombre")
        (rowHead.findViewById<View>(R.id.txt_state) as TextView).text = ("Estado")
        (rowHead.findViewById<View>(R.id.txt_hour) as TextView).text = ("Hora")
        tbl_visitors!!.addView(rowHead)

        tbl_visitors!!.requestLayout()
    }

    /**
     * Represents an asynchronous get data of visitors
     */
    @SuppressLint("StaticFieldLeak")
    inner class GetDataTask internal constructor() : AsyncTask<Void, Void, Boolean>() {
        var db: UserDatabase = UserDatabase.getInstance(this@VisitActivity)!!
        var data: List<UserRegisterDao.UserRegisterQuery>? = null

        override fun onPreExecute() {
            tbl_visitors.removeAllViews()
            showProgress(true)
        }

        override fun doInBackground(vararg p0: Void?): Boolean {
            data = db.userRegisterDao().getUsersRegisterByState("E", Utils.parseDate(Date()), Utils.parseDate(Date()))
            if (data!!.isEmpty()) {
                return false
            }

            return true
        }

        override fun onPostExecute(succces: Boolean?) {
            mTask = null
            showProgress(false)

            if (succces!!) {
                if (data!!.isNotEmpty()) {
                    //Create header of table
                    createTable()

                    //Create body of table
                    for (i in 0 until data!!.size) {
                        val row = LayoutInflater.from(this@VisitActivity).inflate(R.layout.attrib_row, null) as TableRow
                        (row.findViewById<View>(R.id.txt_date) as TextView).text = (Utils.parseDateString(data!![i].dateUser!!))
                        (row.findViewById<View>(R.id.txt_ci) as TextView).text = (data!![i].userId)
                        (row.findViewById<View>(R.id.txt_name) as TextView).text = (data!![i].userName)
                        (row.findViewById<View>(R.id.txt_state) as TextView).text = (data!![i].descState)
                        (row.findViewById<View>(R.id.txt_hour) as TextView).text = (Utils.parseDateString(data!![i].hourUser!!))
                        tbl_visitors!!.addView(row)
                    }
                    tbl_visitors!!.requestLayout()
                }
            } else {
                AlertDialog.Builder(this@VisitActivity).setTitle(R.string.app_name).setMessage(R.string.error_no_data_fetch)
                        .setPositiveButton(R.string.accept, null).setCancelable(false).show()

            }

        }

        override fun onCancelled() {
            mTask = null
            showProgress(false)
        }
    }

    /**
     * Show the progress IU
     */
    private fun showProgress(show: Boolean) {
        if (show) {
            table_progress.visibility = View.VISIBLE
        } else {
            table_progress.visibility = View.GONE
        }
    }

    override fun onDateSet(v: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
        date!!.set(Calendar.YEAR, year)
        date!!.set(Calendar.MONTH, month)
        date!!.set(Calendar.DAY_OF_MONTH, dayOfMonth)
    }

}
