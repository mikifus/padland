package com.mikifus.padland.Dialog;

import android.app.Dialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.Button;

import com.mikifus.padland.R;

/**
 * Created by mikifus on 27/02/18.
 */

public class FormDialog extends DialogFragment {
    public static final String TAG = "FormDialog";
    protected int view;
    protected String title;
    protected Dialog currentDialog;

    protected FormDialogCallBack callbackObject;
    protected View main_view;

    public FormDialog(String title, FormDialogCallBack callback) {
        callbackObject = callback;
        this.view = 0/*R.layout.dialog_new_server*/;
        this.title = title;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        main_view = getActivity().getLayoutInflater().inflate(view, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(main_view);
        builder.setTitle(title);
        builder.setPositiveButton(getString(R.string.ok),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        //Do nothing here because we override this button later to change the close behaviour.
                        //However, we still need this because on older versions of Android unless we
                        //pass a handler the button doesn't get instantiated
                    }
                }
        )
                .setNegativeButton(R.string.cancel,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                callbackObject.onDialogDismiss();
                                dialog.dismiss();
                            }
                        }
                );
        AlertDialog alertDialog = builder.create();
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.setCancelable(false);
        currentDialog = alertDialog;

        setViewEvents();

        return alertDialog;
    }

    protected boolean validateForm(){
        return true;
    }
    protected void saveData() {}

    @Override
    public void onStart() {
        super.onStart();
        final AlertDialog d = (AlertDialog)getDialog();
        if(d != null)
        {
            Button positiveButton = d.getButton(Dialog.BUTTON_POSITIVE);
            positiveButton.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    if( validateForm() ) {
                        saveData();
                        callbackObject.onDialogSuccess();
                        callbackObject.onDialogDismiss();
                        d.dismiss();
                    }
                }
            });
        }
    }

    protected ContentValues getContentValues() {
        return new ContentValues();
    }

    protected void setViewEvents() {}


    public interface FormDialogCallBack {
        public void onDialogDismiss();
        public void onDialogSuccess();
    }
}
