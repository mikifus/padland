package com.mikifus.padland.Dialog;

import android.content.ContentValues;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.mikifus.padland.Models.Server;
import com.mikifus.padland.Models.ServerModel;
import com.mikifus.padland.R;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Pattern;

/**
 * Created by mikifus on 7/07/16.
 */
public class NewServerDialog extends FormDialog {
    public static final String TAG = "NewServerDialog";
    public static final String DEFAULT_PADPREFIX_VALUE = "/p/";

    private EditText fieldName;
    private EditText fieldUrl;
    private EditText fieldPadprefix;
    private CheckBox checkLite;
    private CheckBox checkJquery;
    private Button advancedButton;
    private LinearLayout advancedLayout;
//    private Dialog currentDialog;
    private long edit_server_id = 0;

    public static final Pattern NAME_VALIDATION = Pattern.compile(
            "[a-zA-Z0-9\\+\\.\\_\\%\\-\\@\\ ]{2,256}"
    );
    public static final Pattern URL_VALIDATION = Patterns.WEB_URL;
    public static final Pattern PADPREFIX_VALIDATION = Pattern.compile(
            "[a-zA-Z0-9\\+\\_\\-\\/\\ \\\\]{1,256}[/]{1}"
    );

    public NewServerDialog(String title, FormDialogCallBack callback) {
        super(title, callback);
        view = R.layout.dialog_new_server;
    }

    protected void saveData() {
        ServerModel serverModel = new ServerModel(getActivity());
        ContentValues contentValues = getContentValues();
        String url = contentValues.getAsString(ServerModel.URL);
        String prefix = contentValues.getAsString(ServerModel.PADPREFIX);
        String final_prefix = url;
        if( !final_prefix.endsWith(prefix) ) {
            final_prefix = final_prefix + prefix;
        }
        contentValues.put(ServerModel.PADPREFIX, final_prefix);
        serverModel.saveServerData(edit_server_id, contentValues);
    }

    protected boolean validateForm() {
        ContentValues contentValues = getContentValues();

        if( ! NAME_VALIDATION.matcher(contentValues.getAsString(ServerModel.NAME)).matches() ) {
            // TODO: Change toast for something better.
            Toast.makeText(getContext(), getString(R.string.serverlist_dialog_new_server_name_invalid), Toast.LENGTH_LONG).show();
            return false;
        }

        String urlString = contentValues.getAsString(ServerModel.URL);
        String host = "";
        URL urlParsed;
        try {
            urlParsed = new URL(urlString);
            host = urlParsed.getHost();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        if( ! URL_VALIDATION.matcher(urlString).matches()
                || host.isEmpty() ) {
            Toast.makeText(getContext(), getString(R.string.serverlist_dialog_new_server_url_invalid), Toast.LENGTH_LONG).show();
            return false;
        }

        if( ! contentValues.getAsString(ServerModel.PADPREFIX).isEmpty()
                && ! PADPREFIX_VALIDATION.matcher(contentValues.getAsString(ServerModel.PADPREFIX)).matches()) {
            Toast.makeText(getContext(), getString(R.string.serverlist_dialog_new_server_padprefix_invalid), Toast.LENGTH_LONG).show();
            return false;
        }

        if(contentValues.getAsInteger(ServerModel.JQUERY) == null) {
            Log.e(TAG, "Something is wrong here");
            return false;
        }

        return true;
    }

    protected ContentValues getContentValues() {
//        Toast.makeText(getContext(), "Server saved", Toast.LENGTH_LONG).show();
        ContentValues values = super.getContentValues();
        String name = String.valueOf(fieldName.getText()).trim();
        values.put(ServerModel.NAME, name);

        String url = String.valueOf(fieldUrl.getText()).trim();
        try {
            URL url_object = new URL(url);
            url = url_object.toString();
            url = url.replaceAll("/$", ""); // Remove trailing slash
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        values.put(ServerModel.URL, url);

        String padprefix = String.valueOf(fieldPadprefix.getText());
        if(!padprefix.isEmpty()) {
            // Must start and end with /
            if(!padprefix.startsWith("/")) {
                padprefix = "/" + padprefix;
            }
            if(!padprefix.endsWith("/")) {
                padprefix = padprefix + "/";
            }
        }
        values.put(ServerModel.PADPREFIX, padprefix);

        int jquery = String.valueOf(checkJquery.isChecked()) == "true" ? 1 : 0;
        values.put(ServerModel.JQUERY, jquery);

        return values;
    }

    public void editServerId(long id) {
        edit_server_id = id;
//        if( fieldName != null ) {
//            ServerModel serverModel = new ServerModel(getContext());
//            Server server = serverModel.getServerById(id);
//
//            fieldName.setText(server.getName());
//            fieldUrl.setText(server.getUrl());
//            fieldPadprefix.setText(server.getPadPrefix());
//            checkJquery.setChecked(server.jquery);
//
//            if( server.getPadPrefix().equals(DEFAULT_PADPREFIX_VALUE) && server.jquery )
//            {
//                checkLite.setChecked(true);
//            }
//        }
//        if( currentDialog != null ) {
//            currentDialog.setTitle(R.string.serverlist_dialog_edit_server_title);
//        }
    }

    protected void setViewEvents() {
        fieldName = main_view.findViewById(R.id.txt_server_name);
        fieldUrl = main_view.findViewById(R.id.txt_server_url);
        fieldPadprefix = main_view.findViewById(R.id.txt_server_padprefix);
        checkLite = main_view.findViewById(R.id.chk_lite);
        checkJquery = main_view.findViewById(R.id.chk_jquery);
        advancedButton = main_view.findViewById(R.id.advanced_options);
        advancedLayout = main_view.findViewById(R.id.advanced_options_layout);
        fieldPadprefix.setText(DEFAULT_PADPREFIX_VALUE);

        if(edit_server_id > 0) {
            ServerModel serverModel = new ServerModel(getContext());
            Server server = serverModel.getServerById(edit_server_id);

            fieldName.setText(server.getName());
            fieldUrl.setText(server.getUrl());
            fieldPadprefix.setText(server.getPadPrefix());
            checkJquery.setChecked(server.jquery);

            if (server.getPadPrefix().equals(DEFAULT_PADPREFIX_VALUE) && server.jquery) {
                checkLite.setChecked(true);
            }
        }

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
                    fieldPadprefix.setText( DEFAULT_PADPREFIX_VALUE );
                } else {
                    fieldPadprefix.setText("");
                }
                checkJquery.setChecked(isChecked);
            }
        });
    }
}
