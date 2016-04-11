package com.mikifus.padland.Dialog;

import android.app.Dialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.mikifus.padland.PadContentProvider;
import com.mikifus.padland.PadListActivity;
import com.mikifus.padland.R;

import java.util.regex.Pattern;

/**
 * Created by mikifus on 10/03/16.
 */
public class NewPadGroup extends DialogFragment {

    private EditText mEditText;
    public static final Pattern NAME_VALIDATION = Pattern.compile(
            "[a-zA-Z0-9\\+\\.\\_\\%\\-\\+\\@\\ ]{2,256}"
    );

    public NewPadGroup() {
        // Empty constructor required for DialogFragment
    }

//    @Override
//    public View onCreateView(LayoutInflater inflater, ViewGroup container,
//                             Bundle savedInstanceState) {
//        View view = inflater.inflate(R.layout.dialog_new_padgroup, container);
////        mEditText = (EditText) view.findViewById(R.id.txt_padgroup_name);
//
//        // Show soft keyboard automatically
////        mEditText.requestFocus();
////        getDialog().getWindow().setSoftInputMode(
////                WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
//
//        return view;
//    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = getActivity().getLayoutInflater().inflate(R.layout.dialog_new_padgroup, null);
        mEditText = (EditText) view.findViewById(R.id.txt_padgroup_name);
//        mEditText.requestFocus();

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(view);
        builder.setTitle(R.string.padlist_dialog_new_padgroup_title);
        builder.setPositiveButton(getString(R.string.ok),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        String text = String.valueOf(mEditText.getText());
                        if( NAME_VALIDATION.matcher(text).matches() ) {
                            saveNewPadGroup( text );
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
        return builder.create();
    }

    private void saveNewPadGroup( String title ) {
//        Toast.makeText(getContext(), "It should save it", Toast.LENGTH_LONG).show();
        ContentValues values = new ContentValues();
        values.put(PadContentProvider.NAME, title);
        PadListActivity activity = (PadListActivity) getActivity();
        activity.padlandDb.savePadgroupData(0, values);
        activity.notifyDataSetChanged();
    }
}
