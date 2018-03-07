package com.mikifus.padland.Dialog;

import android.content.ContentValues;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.URLUtil;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.mikifus.padland.Models.Pad;
import com.mikifus.padland.Models.PadGroup;
import com.mikifus.padland.Models.PadGroupModel;
import com.mikifus.padland.Models.PadModel;
import com.mikifus.padland.Models.ServerModel;
import com.mikifus.padland.PadContentProvider;
import com.mikifus.padland.R;
import com.mikifus.padland.Utils.PadUrl;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by mikifus on 27/02/18.
 */

public class EditPadDialog extends FormDialog {
    public static final String TAG = "EditPadDialog";
    private EditText fieldName;
    private EditText fieldLocalName;
    private Spinner fieldGroup;
    private SpinnerGroupAdapter<PadGroup> spinnerAdapter;
    private long edit_pad_id = 0;

    public EditPadDialog(String title, FormDialogCallBack callback) {
        super(title, callback);
        view = R.layout.dialog_pad_edit;
    }

    public void editPadId(long id) {
        edit_pad_id = id;
    }
    protected void setViewEvents() {
        fieldName = (EditText) main_view.findViewById(R.id.txt_pad_name);
        fieldLocalName = (EditText) main_view.findViewById(R.id.txt_pad_local_name);
        fieldGroup = (Spinner) main_view.findViewById( R.id.group_spinner );

        PadGroupModel padGroupModel = new PadGroupModel(getContext());
        ArrayList<PadGroup> allPadGroups = padGroupModel.getAllPadgroups();
        allPadGroups.add(new PadGroup(getContext()));
        spinnerAdapter = new SpinnerGroupAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, allPadGroups);

        fieldGroup.setAdapter(spinnerAdapter);

        if (edit_pad_id > 0) {
            PadModel model = new PadModel(getContext());
            Pad pad = model.getPadById(edit_pad_id);

            fieldName.setText(pad.getName());
            fieldLocalName.setText(pad.getRawLocalName());

            PadGroup padGroup = padGroupModel.getPadGroup(pad.getId());
            if( padGroup == null ) {
                padGroup = padGroupModel.getUnclassifiedPadGroup();
            }
            Log.d(TAG, String.valueOf(spinnerAdapter.getPosition(padGroup)));
            fieldGroup.setSelection(spinnerAdapter.getPosition(padGroup));
        }
    }

    protected boolean validateForm() {
        ContentValues contentValues = getContentValues();

        PadModel model = new PadModel(getContext());
        Pad pad = model.getPadById(edit_pad_id);

        ServerModel serverModel = new ServerModel(getContext());
        String prefix = serverModel.getServerPrefixFromUrl(getContext(), pad.getServer());
         // Multiple can be returned. TODO: Connect pads with servers by ID.

        if( prefix == null ) {
            Toast.makeText(getContext(), getString(R.string.new_pad_wrong_server), Toast.LENGTH_LONG).show();
            return false;
        }

        PadUrl padUrl = new PadUrl.Builder()
                .padName(contentValues.getAsString(PadModel.NAME))
                .padServer(pad.getServer())
                .padPrefix(prefix)
                .build();

        if( !URLUtil.isValidUrl(padUrl.getString()) )
        {
            Toast.makeText(getContext(), getString(R.string.new_pad_name_invalid), Toast.LENGTH_LONG).show();
            return false;
        }

        return true;
    }

    protected void saveData() {
        PadModel model = new PadModel(getContext());
        PadGroupModel padGroupModel = new PadGroupModel(getContext());
        ContentValues contentValues = getContentValues();
        ContentValues groupContentValues = getGroupContentValues();

        model.savePad(edit_pad_id, contentValues);
        padGroupModel.savePadgroupRelation(groupContentValues.getAsLong(PadContentProvider._ID_GROUP), edit_pad_id);
    }

    protected ContentValues getContentValues() {
        ContentValues values = super.getContentValues();
        String localName = String.valueOf(fieldLocalName.getText());
        values.put(PadModel.LOCAL_NAME, localName);

        String padName = String.valueOf(fieldName.getText()).trim();
        values.put(PadModel.NAME, padName);

        PadModel model = new PadModel(getContext());
        Pad pad = model.getPadById(edit_pad_id);

        ServerModel serverModel = new ServerModel(getContext());
        String prefix = serverModel.getServerPrefixFromUrl(getContext(), pad.getServer());
        // Multiple can be returned. TODO: Connect pads with servers by ID.
        PadUrl padUrl = new PadUrl.Builder()
                .padName(padName)
                .padServer(pad.getServer())
                .padPrefix(prefix)
                .build();

        values.put(PadModel.URL, padUrl.getString());

        return values;
    }

    protected ContentValues getGroupContentValues() {
        ContentValues values = super.getContentValues();

        long group = spinnerAdapter.getItem( fieldGroup.getSelectedItemPosition() ).getId();
        values.put(PadContentProvider._ID_GROUP, group);

        return values;
    }

    class SpinnerGroupAdapter<P> extends ArrayAdapter<PadGroup> {

        private final LayoutInflater mInflater;
        private final Context mContext;
        private final List<PadGroup> items;
        private final int mResource;

        public SpinnerGroupAdapter(@NonNull Context context, int resource, @NonNull List<PadGroup> objects) {
            super(context, resource, objects);
            mContext = context;
            mInflater = LayoutInflater.from(context);
            mResource = resource;
            items = objects;
        }

        @Override
        public PadGroup getItem(int position) {
            return items.get(position);
        }

        @Override
        public View getDropDownView(int position, @Nullable View convertView,
                                    @NonNull ViewGroup parent) {
            return createItemView(position, convertView, parent);
        }

        @Override
        public @NonNull View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            return createItemView(position, convertView, parent);
        }

        private View createItemView(int position, View convertView, ViewGroup parent){
            final View view = mInflater.inflate(mResource, parent, false);

            TextView text = view.findViewById(android.R.id.text1);

            PadGroup padGroup = getItem(position);

            text.setText(padGroup.getName());

            return view;
        }
    }
}
