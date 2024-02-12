package com.mikifus.padland.Activities

import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.mikifus.padland.R

/**
 * Just displays an about message
 * @author
 */
class AboutActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)

        // text2 has links specified by putting <a> tags in the string
        // resource.  By default these links will appear but not
        // respond to user input.  To make them active, you need to
        // call setMovementMethod() on the TextView object.
        val textView = findViewById<View>(R.id.textView) as TextView
        textView.movementMethod = LinkMovementMethod.getInstance()
        
        val textView1 = findViewById<View>(R.id.textView2) as TextView
        textView1.movementMethod = LinkMovementMethod.getInstance()
    }
}