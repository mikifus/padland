package com.mikifus.padland;

import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.widget.TextView;

/**
 * Just displays an about message
 * @author
 */
public class About extends PadLandActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        // text2 has links specified by putting <a> tags in the string
        // resource.  By default these links will appear but not
        // respond to user input.  To make them active, you need to
        // call setMovementMethod() on the TextView object.
        TextView t = (TextView) findViewById(R.id.textView);
        if (t != null) {
            t.setMovementMethod(LinkMovementMethod.getInstance());
        }

        TextView t2 = (TextView) findViewById(R.id.textView2);
        if (t2 != null) {
            t2.setMovementMethod(LinkMovementMethod.getInstance());
        }
    }

}
