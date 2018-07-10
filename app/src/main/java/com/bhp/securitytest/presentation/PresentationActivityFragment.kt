package com.bhp.securitytest.presentation

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AlertDialog
import android.view.*
import com.bhp.securitytest.R
import kotlinx.android.synthetic.main.fragment_presentation_7.*

/**
 * A placeholder fragment containing a simple view.
 */
class PresentationActivityFragment : Fragment() {

    var questions: Boolean = false
    var callback: QuestionsCallback? = null

    interface QuestionsCallback {
        fun onQuestionsAnswered(q1: Boolean, q2: Boolean, success: Boolean)
        fun onFail()
    }

    companion object {

        const val ARG_LAYOUT_ID = "arg_layout_id"

        fun instance(layoutId: Int): PresentationActivityFragment {
            val fragment = PresentationActivityFragment()
            val args = Bundle()
            args.putInt(ARG_LAYOUT_ID, layoutId)
            fragment.arguments = args
            return fragment
        }
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val layoutId: Int = arguments?.getInt(ARG_LAYOUT_ID)!!
        questions = layoutId == R.layout.fragment_presentation_7
        setHasOptionsMenu(questions)
        return inflater.inflate(layoutId, container, false)
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater?.inflate(R.menu.presentation_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when (item?.itemId) {
            R.id.menu_finish -> {
                saveResponses()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun saveResponses() {
        var q1 = when (question_1.checkedRadioButtonId) {
            R.id.question_1_yes -> true
            R.id.question_1_no -> false
            else -> null
        }

        var q2 = when (question_2.checkedRadioButtonId) {
            R.id.question_2_yes -> true
            R.id.question_2_no -> false
            else -> null
        }

        question_1.clearCheck()
        question_2.clearCheck()

        if (q1 == null || q2 == null) {
            AlertDialog.Builder(context!!).setTitle(R.string.app_name).setMessage(R.string.error_questions_unanswered)
                    .setPositiveButton(R.string.accept, null).show()
            return
        }

        var questionsOk = false

        if (!q1 && q2) {
            questionsOk = true
        }

        if (!questionsOk) {
            AlertDialog.Builder(context!!).setTitle(R.string.app_name).setMessage(R.string.error_questions_wrong)
                    .setPositiveButton(R.string.question_redo) { dialog, which ->
                        callback?.onFail()
                    }.setCancelable(false).show()
        }

        callback?.onQuestionsAnswered(q1, q1, questionsOk)
    }

}
