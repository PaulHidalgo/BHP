package com.bhp.securitytest

import android.Manifest
import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.DatePicker
import android.widget.TableRow
import android.widget.TextView
import com.bhp.securitytest.Utils.formatDefaultDate
import com.bhp.securitytest.db.UserDatabase
import com.bhp.securitytest.db.UserRegisterDao
import com.bhp.securitytest.enums.VisitTable
import com.codekidlabs.storagechooser.StorageChooser
import kotlinx.android.synthetic.main.activity_visit.*
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import org.supercsv.cellprocessor.FmtDate
import org.supercsv.cellprocessor.Optional
import org.supercsv.cellprocessor.StrReplace
import org.supercsv.io.CsvBeanWriter
import org.supercsv.prefs.CsvPreference
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStreamWriter
import java.util.*

class VisitActivity : BaseActivity(), DatePickerDialog.OnDateSetListener {

    private var mTask: GetDataTask? = null
    private var mExportTask: VisitActivity.ExportTask? = null

    var data: List<UserRegisterDao.UserRegisterQuery>? = null
    private var datePicker: DatePickerDialog? = null
    private var date: Calendar? = Calendar.getInstance()
    private lateinit var chooser: StorageChooser
    private lateinit var opt: VisitTable
    private lateinit var path: String

    companion object {
        const val EXTRA_OPT = "extra_opt_table"

        fun intent(context: Context, opt: VisitTable?): Intent {
            val intent = Intent(context, VisitActivity::class.java)
            val extras = Bundle()
            extras.putSerializable(EXTRA_OPT, opt)
            intent.putExtras(extras)

            return intent
        }
    }

    @Suppress("NON_EXHAUSTIVE_WHEN")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_visit)

        datePicker = DatePickerDialog(this, R.style.DialogTheme, this, date!!.get(Calendar.YEAR), date!!.get(Calendar.MONTH), date!!.get(Calendar.DAY_OF_MONTH))
        datePicker!!.datePicker.minDate = date!!.timeInMillis

        if (intent.extras != null) {
            opt = intent.getSerializableExtra(EXTRA_OPT) as VisitTable
        }

        if (opt == VisitTable.USER) {
            query_button.visibility = View.GONE
            attemptQuery()
        }
        query_button.setOnClickListener {
            attemptQuery()
            //datePicker!!.show()
        }

        chooser = StorageChooser.Builder()
                .withActivity(this@VisitActivity)
                .withFragmentManager(fragmentManager)
                .withMemoryBar(true)
                .allowCustomPath(true)
                .setType(StorageChooser.DIRECTORY_CHOOSER)
                .build()

        chooser.setOnSelectListener { path ->
            run {
                this.path = path
                attemptExportData()
            }
        }

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        if (opt == VisitTable.ADMIN) {
            menuInflater.inflate(R.menu.dashboard_menu, menu)
        }
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when (item?.itemId) {
            R.id.menu_export -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                            || checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                        if (shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE) || shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                            AlertDialog.Builder(this@VisitActivity).setTitle(R.string.bhp).setMessage(R.string.error_no_data_fetch)
                                    .setPositiveButton(R.string.accept, null).setCancelable(false).show()
                        } else {
                            requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE), DashboardActivity.REQUEST_DIRECTORY)
                        }
                    } else {
                        chooser.show()
                    }
                } else {
                    chooser.show()
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }


    fun attemptQuery() {
        if (mTask != null) {
            return
        }
        mTask = GetDataTask()
        mTask!!.execute(null as Void?)
    }

    private fun attemptExportData() {
        if (data!!.isEmpty()) {
            AlertDialog.Builder(this).setTitle(R.string.app_name).setMessage(R.string.error_no_data_fetch)
                    .setPositiveButton(R.string.accept, null).setCancelable(false).show()
            return
        }

        if (mExportTask != null) {
            return
        }
        showProgress(true)

        mExportTask = ExportTask()
        mExportTask!!.execute(null as Void?)
    }


    fun createTable() {

        when (opt) {
            VisitTable.ADMIN -> {
                val rowHead = LayoutInflater.from(this).inflate(R.layout.attrib_row, null) as TableRow
                (rowHead.findViewById<View>(R.id.txt_date) as TextView).text = ("Fecha")
                (rowHead.findViewById<View>(R.id.txt_type_doc) as TextView).text = ("Tipo Doc.")
                (rowHead.findViewById<View>(R.id.txt_ci) as TextView).text = ("ID")
                (rowHead.findViewById<View>(R.id.txt_name) as TextView).text = ("Nombre")
                (rowHead.findViewById<View>(R.id.txt_state) as TextView).text = ("Estado")
                (rowHead.findViewById<View>(R.id.txt_hour) as TextView).text = ("Hora")
                tbl_visitors!!.addView(rowHead)
            }

            VisitTable.USER -> {
                val rowHead = LayoutInflater.from(this).inflate(R.layout.attrib_row_visit, null) as TableRow
                (rowHead.findViewById<View>(R.id.txt_ci) as TextView).text = ("Cédula")
                (rowHead.findViewById<View>(R.id.txt_name) as TextView).text = ("Nombre")
                (rowHead.findViewById<View>(R.id.txt_hour) as TextView).text = ("Hora Ingreso")
                tbl_visitors!!.addView(rowHead)
            }
        }



        tbl_visitors!!.requestLayout()
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

    /**
     * Represents an asynchronous get data of visitors
     */
    @SuppressLint("StaticFieldLeak")
    inner class GetDataTask internal constructor() : AsyncTask<Void, Void, Boolean>() {
        var db: UserDatabase = UserDatabase.getInstance(this@VisitActivity)!!

        override fun onPreExecute() {
            tbl_visitors.removeAllViews()
            showProgress(true)
        }

        override fun doInBackground(vararg p0: Void?): Boolean {
            when (opt) {

                VisitTable.ADMIN -> {
                    //TODO Apply filters date
                    data = db.userRegisterDao().getAllUsersRegister()
                }
                VisitTable.USER -> data = db.userRegisterDao().getUsersRegisterByState("E", Utils.parseDate(Date()), Utils.parseDate(Date()))
            }
            if (data!!.isEmpty()) {
                return false
            }

            return true
        }

        @SuppressLint("SetTextI18n")
        override fun onPostExecute(succces: Boolean?) {
            mTask = null
            showProgress(false)

            if (succces!!) {
                if (data!!.isNotEmpty()) {
                    //Create header of table
                    createTable()

                    //Create body of table
                    for (i in 0 until data!!.size) {
                        when (opt) {
                            VisitTable.ADMIN -> {
                                val row = LayoutInflater.from(this@VisitActivity).inflate(R.layout.attrib_row, null) as TableRow
                                (row.findViewById<View>(R.id.txt_date) as TextView).text = (Utils.parseDateString(data!![i].dateUser!!))
                                var typeDoc = "Pasaporte"
                                if (data!![i].idType.equals("C")) {
                                    typeDoc = "Cédula"
                                }
                                (row.findViewById<View>(R.id.txt_type_doc) as TextView).text = typeDoc
                                (row.findViewById<View>(R.id.txt_ci) as TextView).text = (data!![i].userId)
                                (row.findViewById<View>(R.id.txt_name) as TextView).text = data!![i].userName + " " + data!![i].userLastName
                                (row.findViewById<View>(R.id.txt_state) as TextView).text = (data!![i].descState)
                                (row.findViewById<View>(R.id.txt_hour) as TextView).text = (Utils.parseDateString(data!![i].hourUser!!))
                                tbl_visitors!!.addView(row)
                            }
                            VisitTable.USER -> {
                                val row = LayoutInflater.from(this@VisitActivity).inflate(R.layout.attrib_row_visit, null) as TableRow
                                (row.findViewById<View>(R.id.txt_ci) as TextView).text = (data!![i].userId)
                                (row.findViewById<View>(R.id.txt_name) as TextView).text = data!![i].userName + " " + data!![i].userLastName
                                (row.findViewById<View>(R.id.txt_hour) as TextView).text = (Utils.parseDateString(data!![i].hourUser!!))
                                tbl_visitors!!.addView(row)
                            }
                        }


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
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    inner class ExportTask internal constructor() : AsyncTask<Void, Void, Boolean>() {
        lateinit var file: File


        override fun doInBackground(vararg params: Void): Boolean? {
            var writer: CsvBeanWriter? = null
            try {
                file = File("$path/BHP_VISITORS${DateTimeFormat.forPattern("yyyyMMddHHmmss").print(DateTime.now())}.csv")
                if (!file.exists()) {
                    file.createNewFile()
                }
                writer = CsvBeanWriter(OutputStreamWriter(FileOutputStream(file), Charsets.ISO_8859_1), CsvPreference.STANDARD_PREFERENCE)

                writer.writeHeader("Fecha", "Tipo Identificación", "ID", "Nombre", "Apellido", "Estado", "Hora")
                data!!.forEach { userRegisterQuery ->
                    run {
                        writer.write(userRegisterQuery,
                                arrayOf(UserRegisterDao.UserRegisterQuery::dateUser.name, UserRegisterDao.UserRegisterQuery::idType.name, UserRegisterDao.UserRegisterQuery::userId.name, UserRegisterDao.UserRegisterQuery::userName.name, UserRegisterDao.UserRegisterQuery::userLastName.name, UserRegisterDao.UserRegisterQuery::descState.name, UserRegisterDao.UserRegisterQuery::hourUser.name),
                                arrayOf(Optional(FmtDate(formatDefaultDate)), StrReplace("C", "Cédula", StrReplace("P", "Pasaporte")), null, null, null, null, null))
                    }
                }
                return true
            } catch (e: Exception) {
                Log.e("FmtLongDate", e.message, e)
            } finally {
                writer?.close()
            }
            return false
        }

        override fun onPostExecute(success: Boolean?) {
            mExportTask = null
            showProgress(false)

            if (success!!) {
                AlertDialog.Builder(this@VisitActivity).setTitle(R.string.app_name).setMessage(getString(R.string.export_succeeded, file.absolutePath))
                        .setPositiveButton(R.string.accept, null).setCancelable(false).show()
            } else {
                AlertDialog.Builder(this@VisitActivity).setTitle(R.string.app_name).setMessage(R.string.export_failed)
                        .setPositiveButton(R.string.accept, null).setCancelable(false).show()
            }
        }

        override fun onCancelled() {
            mExportTask = null
            showProgress(false)
        }

    }

}
