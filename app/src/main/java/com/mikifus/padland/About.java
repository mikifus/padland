package com.mikifus.padland;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by mikifus on 12/01/15.
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
        TextView t2 = (TextView) findViewById(R.id.textView);
        t2.setMovementMethod(LinkMovementMethod.getInstance());
    }

}
