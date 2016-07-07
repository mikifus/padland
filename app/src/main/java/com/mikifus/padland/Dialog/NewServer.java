package com.mikifus.padland.Dialog;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.mikifus.padland.R;

import java.util.regex.Pattern;

/**
 * Created by mikifus on 7/07/16.
 */
public class NewServer extends DialogFragment {

    private EditText fieldName;
    private EditText fieldUrl;
    private EditText fieldPadprefix;
    private CheckBox checkLite;
    private CheckBox checkJquery;
    private Button advancedButton;
    private LinearLayout advancedLayout;

    public static final Pattern NAME_VALIDATION = Pattern.compile(
            "[a-zA-Z0-9\\+\\.\\_\\%\\-\\+\\@\\ ]{2,256}"
    );

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = getActivity().getLayoutInflater().inflate(R.layout.dialog_new_server, null);
        fieldName = (EditText) view.findViewById(R.id.txt_server_name);
        fieldUrl = (EditText) view.findViewById(R.id.txt_server_url);
        fieldPadprefix = (EditText) view.findViewById(R.id.txt_server_padprefix);
        checkLite = (CheckBox) view.findViewById(R.id.chk_lite);
        checkJquery = (CheckBox) view.findViewById(R.id.chk_jquery);
        advancedButton = (Button) view.findViewById(R.id.advanced_options);
        advancedLayout = (LinearLayout) view.findViewById(R.id.advanced_options_layout);

        setViewEvents();
//        mEditText.requestFocus();

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(view);
        builder.setTitle(R.string.serverlist_dialog_new_server_title);
        builder.setPositiveButton(getString(R.string.ok),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        String text = String.valueOf(fieldName.getText());
                        if( NAME_VALIDATION.matcher(text).matches() ) {
//                            saveNewServer( text );
                            dialog.dismiss();
                        } else {
                            Toast.makeText(getContext(), getString(R.string.padlist_dialog_new_padgroup_invalid), Toast.LENGTH_LONG).show();
                        }
                    }
                }
        )
                .setNegativeButton(R.string.cancel,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                dialog.dismiss();
                            }
                        }
                );
        AlertDialog alertDialog = builder.create();
        alertDialog.setCanceledOnTouchOutside(false);
        return alertDialog;
    }

    private void setViewEvents() {
        advancedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if( advancedLayout.getVisibility() == View.VISIBLE ) {
                    advancedLayout.setVisibility(View.GONE);
                } else {
                    advancedLayout.setVisibility(View.VISIBLE);
                }
            }
        });
        checkLite.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if( isChecked ) {
                    fieldPadprefix.setText("p/");
                } else {
                    fieldPadprefix.setText("");
                }
                checkJquery.setChecked(isChecked);
            }
        });
    }
}
