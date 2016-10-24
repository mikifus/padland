package com.mikifus.padland.Dialog;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.EditText;

import com.mikifus.padland.R;

/**
 * Created by mikifus on 21/09/16.
 */
public abstract class BasicAuthDialog extends DialogFragment {

    private EditText mUsername;
    private EditText mPassword;
    private View mView;
    private Dialog dialog;

    public BasicAuthDialog() {
        // Empty constructor required for DialogFragment
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        mView = getActivity().getLayoutInflater().inflate(R.layout.dialog_auth, null);
        mUsername = (EditText) mView.findViewById(R.id.txt_username);
        mPassword = (EditText) mView.findViewById(R.id.txt_password);
        mUsername.requestFocus();

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(mView);
        builder.setTitle(R.string.padview_dialog_basicatuh_title);
        builder.setPositiveButton(getString(R.string.ok),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        String username = String.valueOf(mUsername.getText());
                        String password = String.valueOf(mPassword.getText());

                        onPositiveButtonClick(username, password);
                    }
                });
        builder.setNegativeButton(R.string.cancel,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        dialog.dismiss();
                        onNegativeButtonClick();
                    }
                }
        );
        dialog = builder.create();
        onDialogCreated(dialog, mView);
        return dialog;
    }

    protected void onDialogCreated(Dialog dialog, View view) {

    }
    protected abstract void onPositiveButtonClick(String username, String password);
    protected abstract void onNegativeButtonClick();
}
