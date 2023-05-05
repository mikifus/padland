package com.mikifus.padland

import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.View
import android.widget.TextView

/**
 * Just displays an about message
 * @author
 */
class About : PadLandActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)

        // text2 has links specified by putting <a> tags in the string
        // resource.  By default these links will appear but not
        // respond to user input.  To make them active, you need to
        // call setMovementMethod() on the TextView object.
        val t = findViewById<View>(R.id.textView) as TextView
        if (t != null) {
            t.movementMethod = LinkMovementMethod.getInstance()
        }
        val t2 = findViewById<View>(R.id.textView2) as TextView
        if (t2 != null) {
            t2.movementMethod = LinkMovementMethod.getInstance()
        }
    }
}