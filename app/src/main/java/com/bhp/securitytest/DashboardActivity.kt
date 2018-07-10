package com.bhp.securitytest

import android.Manifest
import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.*
import android.widget.TextView
import com.bhp.securitytest.db.User
import com.bhp.securitytest.db.UserDatabase
import com.codekidlabs.storagechooser.StorageChooser
import kotlinx.android.synthetic.main.activity_dashboard.*
import org.joda.time.DateTime
import org.joda.time.LocalDate
import org.joda.time.format.DateTimeFormat
import org.supercsv.cellprocessor.FmtBool
import org.supercsv.cellprocessor.Optional
import org.supercsv.cellprocessor.StrReplace
import org.supercsv.cellprocessor.Trim
import org.supercsv.io.CsvBeanWriter
import org.supercsv.prefs.CsvPreference
import org.supercsv.util.CsvContext
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStreamWriter

class DashboardActivity : BaseActivity() {
    private var mTask: GetDataTask? = null
    private var mExportTask: ExportTask? = null
    private var users: MutableList<User> = mutableListOf()
    private lateinit var chooser: StorageChooser
    private lateinit var path: String

    companion object {
        const val REQUEST_DIRECTORY = 0

        fun intent(context: Context): Intent {
            return Intent(context, DashboardActivity::class.java)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        list.adapter = ListAdapter(users)

        showProgress(true)
        mTask = GetDataTask()
        mTask!!.execute(null as Void?)

        chooser = StorageChooser.Builder()
                .withActivity(this@DashboardActivity)
                .withFragmentManager(fragmentManager)
                .withMemoryBar(true)
                .allowCustomPath(true)
                .setType(StorageChooser.DIRECTORY_CHOOSER)
                .build()

        chooser.setOnSelectListener { path ->
            run {
                this.path = path
                exportData()
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.dashboard_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when (item?.itemId) {
            R.id.menu_export -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                            || checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                        if (shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE) || shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                            AlertDialog.Builder(this@DashboardActivity).setTitle(R.string.app_name).setMessage(R.string.error_no_data_fetch)
                                    .setPositiveButton(R.string.accept, null).setCancelable(false).show()
                        } else {
                            requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE), REQUEST_DIRECTORY)
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

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_DIRECTORY) {
            var ok = true
            grantResults.forEach { res -> ok = (res == PackageManager.PERMISSION_GRANTED) }
            if (ok) {
                chooser.show()
            }
        }
    }

    private fun exportData() {
        if (users.isEmpty()) {
            AlertDialog.Builder(this@DashboardActivity).setTitle(R.string.app_name).setMessage(R.string.error_no_data_fetch)
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

    inner class ListAdapter(var data: List<User>) : RecyclerView.Adapter<ViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.list_element, parent, false))
        }

        override fun getItemCount(): Int {
            return data.size
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = data[position]
            holder.name.text = "${item.name} ${item.lastname}"

            if (item.date > 0) {
                if (Utils.isValid(item.date)) {
                    holder.remaining.text = getString(R.string.remaining_template, Utils.getRemainingTime(resources, item.date))
                } else {
                    holder.remaining.setText(R.string.expired)
                }
                if (item.success!!) {
                    holder.status.setText(R.string.succeeded)
                    holder.status.setTextColor(Color.GREEN)
                } else {
                    holder.status.setText(R.string.failed)
                    holder.status.setTextColor(Color.RED)
                }
            } else {
                holder.remaining.setText(R.string.course_not_taken)
            }
        }
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    private fun showProgress(show: Boolean) {
        val shortAnimTime = resources.getInteger(android.R.integer.config_shortAnimTime).toLong()

        list.visibility = if (show) View.GONE else View.VISIBLE
        list.animate()
                .setDuration(shortAnimTime)
                .alpha((if (show) 0 else 1).toFloat())
                .setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        list.visibility = if (show) View.GONE else View.VISIBLE
                    }
                })

        list_progress.visibility = if (show) View.VISIBLE else View.GONE
        list_progress.animate()
                .setDuration(shortAnimTime)
                .alpha((if (show) 1 else 0).toFloat())
                .setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        list_progress.visibility = if (show) View.VISIBLE else View.GONE
                    }
                })
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var name = view.findViewById<TextView>(R.id.txt_name)!!
        var remaining = view.findViewById<TextView>(R.id.txt_remaining)!!
        var status = view.findViewById<TextView>(R.id.txt_status)!!
    }

    /**
     * Represents an asynchronous get data task used to authenticate
     * the user.
     */
    inner class GetDataTask internal constructor() : AsyncTask<Void, Void, Boolean>() {
        var db: UserDatabase = UserDatabase.getInstance(this@DashboardActivity)!!

        override fun doInBackground(vararg params: Void): Boolean? {
//            Log.d("Registers-->", "" + db.registerDao().getAll())
            users.addAll(db.userDao().getAll())

            if (users.isEmpty()) {
                return false
            }
            list.adapter.notifyDataSetChanged()
            return true
        }

        override fun onPostExecute(success: Boolean?) {
            mTask = null
            showProgress(false)

            if (!success!!) {
                AlertDialog.Builder(this@DashboardActivity).setTitle(R.string.app_name).setMessage(R.string.error_no_data_fetch)
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
                file = File("$path/BHP_${DateTimeFormat.forPattern("yyyyMMddHHmmss").print(DateTime.now())}.csv")
                if (!file.exists()) {
                    file.createNewFile()
                }
                writer = CsvBeanWriter(OutputStreamWriter(FileOutputStream(file), Charsets.ISO_8859_1), CsvPreference.STANDARD_PREFERENCE)
                writer.writeHeader("Identificación", "Tipo Identificación", "Nombre", "Apellido", User::email.name, "Empresa", "Cargo", "Estado", "Expira")
                users.forEach { user ->
                    run {
                        writer.write(user,
                                arrayOf(User::id.name, User::idType.name, User::name.name, User::lastname.name, User::email.name, User::company.name, User::position.name, User::success.name, User::date.name),
                                arrayOf(null, StrReplace("C", "Cédula", StrReplace("P", "Pasaporte")), null, null, null, null, null, Optional(FmtBool("exitoso", "fallido")), Optional(FmtLongDate())))
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
                AlertDialog.Builder(this@DashboardActivity).setTitle(R.string.app_name).setMessage(getString(R.string.export_succeeded, file.absolutePath))
                        .setPositiveButton(R.string.accept, null).setCancelable(false).show()
            } else {
                AlertDialog.Builder(this@DashboardActivity).setTitle(R.string.app_name).setMessage(R.string.export_failed)
                        .setPositiveButton(R.string.accept, null).setCancelable(false).show()
            }
        }

        override fun onCancelled() {
            mExportTask = null
            showProgress(false)
        }

        inner class FmtLongDate : Trim() {
            override fun execute(value: Any?, context: CsvContext?): String? {
                var result: String? = null

                if (value != null && value is Long && value > 0) {
                    result = LocalDate(value).plusYears(2).toString("dd/MM/yyyy")
                }
                return next.execute<String>(result, context)
            }
        }
    }
}
