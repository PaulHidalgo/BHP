package com.bhp.securitytest

import android.os.Bundle
import android.view.View
import kotlinx.android.synthetic.main.activity_option.*

class OptionActivity : BaseActivity(),View.OnClickListener {

    override fun onClick(v: View?) {
       when(v?.id){
           R.id.courses_button->{}
           R.id.io_button->{}
       }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_option)

        courses_button.setOnClickListener(this)
        io_button.setOnClickListener(this)
    }
}
