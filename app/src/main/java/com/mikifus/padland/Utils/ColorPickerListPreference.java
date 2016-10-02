package com.mikifus.padland.Utils;

import android.content.Context;
import android.os.Bundle;
import android.preference.ListPreference;
import android.util.AttributeSet;

import com.mikifus.padland.R;

/**
 * Created by mikifus on 2/10/16.
 */

public class ColorPickerListPreference extends ListPreference {
    private int mClickedDialogEntryIndex;

    private Context mContext;
    private HSVColorPickerDialog.Builder mBuilder;

    public ColorPickerListPreference(Context ctxt, AttributeSet attrs) {
        super(ctxt, attrs);
        mContext = ctxt;
    }
    @Override
    protected void showDialog(Bundle state) {
        HSVColorPickerDialog cpd = new HSVColorPickerDialog( mContext, getIntValue(), new HSVColorPickerDialog.OnColorSelectedListener() {
            @Override
            public void colorSelected(Integer color) {
                // Do something with the selected color
                String hexColor = String.format("#%06X", (0xFFFFFF & color));
//                Log.d("COLOR PICKER SELECTED", String.valueOf(hexColor));
                setValue(hexColor);
            }
        });
        cpd.setTitle( R.string.settings_default_color_dialogtitle );
        cpd.show();
    }

    protected int getIntValue() {
        return Integer.parseInt(getValue().substring(1), 16);
    }
}
