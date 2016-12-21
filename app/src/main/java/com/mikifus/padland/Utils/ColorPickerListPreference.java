package com.mikifus.padland.Utils;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.preference.ListPreference;
import android.util.AttributeSet;

import com.mikifus.padland.R;

/**
 * Created by mikifus on 2/10/16.
 */

public class ColorPickerListPreference extends ListPreference {

    private Context mContext;
    private HSVColorPickerDialog dialog;
    private boolean showing = false;

    public ColorPickerListPreference(Context ctxt, AttributeSet attrs) {
        super(ctxt, attrs);
        mContext = ctxt;
    }
    @Override
    protected void showDialog(Bundle state) {
        dialog = new HSVColorPickerDialog( mContext, getIntValue(), new HSVColorPickerDialog.OnColorSelectedListener() {
            @Override
            public void colorSelected(Integer color) {
                // Do something with the selected color
                String hexColor = String.format("#%06X", (0xFFFFFF & color));
                setValue(hexColor);
            }
        });
        dialog.setTitle( R.string.settings_default_color_dialogtitle );
        dialog.show();
        showing = true;
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                showing = false;
            }
        });
    }

    public void reload() {
        if( dialog != null && showing ) {
            dialog.dismiss();
            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    showDialog(null);
                }
            }, 300);
        }
    }

    private int getIntValue() {
        return Integer.parseInt(getValue().substring(1), 16);
    }
}
