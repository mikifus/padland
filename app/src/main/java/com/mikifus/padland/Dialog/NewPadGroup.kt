package com.mikifus.padland.Dialog

import android.app.Dialog
import android.content.ContentValues
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.mikifus.padland.Models.PadModel
import com.mikifus.padland.PadListActivity
import com.mikifus.padland.R
import java.util.regex.Pattern

/**
 * Created by mikifus on 10/03/16.
 */
class NewPadGroup : DialogFragment() {
    private var mEditText: EditText? = null

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
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = requireActivity().layoutInflater.inflate(R.layout.dialog_new_padgroup, null)
        mEditText = view.findViewById<View>(R.id.txt_padgroup_name) as EditText
        //        mEditText.requestFocus();
        val builder = AlertDialog.Builder(requireActivity())
        builder.setView(view)
        builder.setTitle(R.string.padlist_dialog_new_padgroup_title)
        builder.setPositiveButton(getString(R.string.ok)
        ) { dialog, whichButton ->
            val text = mEditText!!.text.toString()
            if (NAME_VALIDATION.matcher(text).matches()) {
                saveNewPadGroup(text)
                dialog.dismiss()
            } else {
                Toast.makeText(context, getString(R.string.padlist_dialog_new_padgroup_invalid), Toast.LENGTH_LONG).show()
            }
        }
                .setNegativeButton(R.string.cancel
                ) { dialog, whichButton -> dialog.dismiss() }
        return builder.create()
    }

    private fun saveNewPadGroup(title: String) {
//        Toast.makeText(getContext(), "It should save it", Toast.LENGTH_LONG).show();
        val values = ContentValues()
        values.put(PadModel.NAME, title)
        val activity = activity as PadListActivity?
        activity!!.padlistDb!!.savePadgroupData(0, values)
        activity.notifyDataSetChanged()
    }

    companion object {
        val NAME_VALIDATION = Pattern.compile(
                "[a-zA-Z0-9\\+\\.\\_\\%\\-\\+\\@\\ ]{2,256}"
        )
    }
}