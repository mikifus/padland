package com.mikifus.padland.Dialog;

import android.app.Dialog;
import android.content.ContentValues;
import android.os.Bundle;
import android.webkit.URLUtil;
import android.widget.EditText;
import android.widget.Toast;

import com.mikifus.padland.Models.Pad;
import com.mikifus.padland.Models.PadModel;
import com.mikifus.padland.R;
import com.mikifus.padland.Utils.PadUrl;

/**
 * Created by mikifus on 27/02/18.
 */

public class EditPadDialog extends FormDialog {
    public static final String TAG = "EditPadDialog";
    private EditText fieldName;
    private EditText fieldLocalName;
    private long edit_pad_id = 0;

    public EditPadDialog(String title, FormDialogCallBack callback) {
        super(title, callback);
        view = R.layout.dialog_pad_edit;
    }
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        Dialog d = super.onCreateDialog(savedInstanceState);

        fieldName = (EditText) main_view.findViewById(R.id.txt_pad_name);
        fieldLocalName = (EditText) main_view.findViewById(R.id.txt_pad_local_name);

        return d;
    }

    public void editPadId(long id) {
        edit_pad_id = id;
        if( fieldName != null ) {
            PadModel model = new PadModel(getContext());
            Pad pad = model.getPadById(id);

            fieldName.setText(pad.getName());
            fieldLocalName.setText(pad.getLocalName());

//            if( server.getPadPrefix().equals(DEFAULT_PADPREFIX_VALUE) && server.jquery )
//            {
//                checkLite.setChecked(true);
//            }
        }
//        if( currentDialog != null ) {
//            currentDialog.setTitle(R.string.serverlist_dialog_edit_server_title);
//        }
    }

    protected boolean validateForm() {
        ContentValues contentValues = getContentValues();

        PadModel model = new PadModel(getContext());
        Pad pad = model.getPadById(edit_pad_id);

        PadUrl padUrl = new PadUrl.Builder()
                .padName(contentValues.getAsString(PadModel.NAME))
                .padServer(pad.getServer())
//                .padPrefix(pad.)
                .build();

        if( !URLUtil.isValidUrl(padUrl.getString()) )
        {
            Toast.makeText(getContext(), getString(R.string.new_pad_name_invalid), Toast.LENGTH_LONG).show();
            return false;
        }

        return true;
    }

    protected ContentValues getContentValues() {
//        Toast.makeText(getContext(), "Server saved", Toast.LENGTH_LONG).show();
        ContentValues values = super.getContentValues();
        String localName = String.valueOf(fieldLocalName.getText());
        values.put(PadModel.LOCAL_NAME, localName);

        String padName = String.valueOf(fieldName.getText()).trim();
        values.put(PadModel.NAME, padName);

        return values;
    }
}
