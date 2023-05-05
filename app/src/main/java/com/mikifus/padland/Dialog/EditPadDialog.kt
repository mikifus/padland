package com.mikifus.padland.Dialog

import android.content.ContentValues
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.URLUtil
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import com.mikifus.padland.Models.PadGroup
import com.mikifus.padland.Models.PadGroupModel
import com.mikifus.padland.Models.PadModel
import com.mikifus.padland.Models.ServerModel
import com.mikifus.padland.PadContentProvider
import com.mikifus.padland.R
import com.mikifus.padland.Utils.PadUrl

/**
 * Created by mikifus on 27/02/18.
 */
class EditPadDialog(title: String, callback: FormDialogCallBack) : FormDialog(title, callback) {
    private var fieldName: EditText? = null
    private var fieldLocalName: EditText? = null
    private var fieldGroup: Spinner? = null
    private var spinnerAdapter: SpinnerGroupAdapter<PadGroup?>? = null
    private var edit_pad_id: Long = 0

    init {
        view = R.layout.dialog_pad_edit
    }

    fun editPadId(id: Long) {
        edit_pad_id = id
    }

    override fun setViewEvents() {
        fieldName = main_view!!.findViewById<View>(R.id.txt_pad_name) as EditText
        fieldLocalName = main_view!!.findViewById<View>(R.id.txt_pad_local_name) as EditText
        fieldGroup = main_view!!.findViewById<View>(R.id.group_spinner) as Spinner
        val padGroupModel = PadGroupModel(context)
        val allPadGroups = padGroupModel.allPadgroups
        allPadGroups!!.add(PadGroup(context))
        spinnerAdapter = SpinnerGroupAdapter<PadGroup>(context, android.R.layout.simple_spinner_dropdown_item, allPadGroups)
        fieldGroup!!.adapter = spinnerAdapter
        if (edit_pad_id > 0) {
            val model = PadModel(context)
            val pad = model.getPadById(edit_pad_id)
            fieldName.setText(pad.name)
            fieldLocalName.setText(pad.rawLocalName)
            var padGroup = padGroupModel.getPadGroup(pad.id)
            if (padGroup == null) {
                padGroup = padGroupModel.unclassifiedPadGroup
            }
            Log.d(TAG, spinnerAdapter.getPosition(padGroup).toString())
            fieldGroup!!.setSelection(spinnerAdapter.getPosition(padGroup))
        }
    }

    override fun validateForm(): Boolean {
        val contentValues = contentValues
        val model = PadModel(context)
        val pad = model.getPadById(edit_pad_id)
        val serverModel = ServerModel(context)
        val prefix = serverModel.getServerPrefixFromUrl(context, pad.server)
        // Multiple can be returned. TODO: Connect pads with servers by ID.
        if (prefix == null) {
            Toast.makeText(context, getString(R.string.new_pad_wrong_server), Toast.LENGTH_LONG).show()
            return false
        }
        val padUrl = PadUrl.Builder()
                .padName(contentValues!!.getAsString(PadModel.Companion.NAME))
                .padServer(pad.server)
                .padPrefix(prefix)
                .build()
        if (!URLUtil.isValidUrl(padUrl.string)) {
            Toast.makeText(context, getString(R.string.new_pad_name_invalid), Toast.LENGTH_LONG).show()
            return false
        }
        return true
    }

    override fun saveData() {
        val model = PadModel(context)
        val padGroupModel = PadGroupModel(context)
        val contentValues = contentValues
        val groupContentValues = groupContentValues
        model.savePad(edit_pad_id, contentValues)
        padGroupModel.savePadgroupRelation(groupContentValues!!.getAsLong(PadContentProvider.Companion._ID_GROUP), edit_pad_id)
    }

    // Multiple can be returned. TODO: Connect pads with servers by ID.
    protected override val contentValues: ContentValues?
        protected get() {
            val values = super.getContentValues()
            val localName = fieldLocalName!!.text.toString()
            values!!.put(PadModel.Companion.LOCAL_NAME, localName)
            val padName = fieldName!!.text.toString().trim { it <= ' ' }
            values!!.put(PadModel.Companion.NAME, padName)
            val model = PadModel(context)
            val pad = model.getPadById(edit_pad_id)
            val serverModel = ServerModel(context)
            val prefix = serverModel.getServerPrefixFromUrl(context, pad.server)
            // Multiple can be returned. TODO: Connect pads with servers by ID.
            val padUrl = PadUrl.Builder()
                    .padName(padName)
                    .padServer(pad.server)
                    .padPrefix(prefix)
                    .build()
            values.put(PadModel.Companion.URL, padUrl.string)
            return values
        }
    protected val groupContentValues: ContentValues?
        protected get() {
            val values = super.getContentValues()
            val group = spinnerAdapter!!.getItem(fieldGroup!!.selectedItemPosition).getId()
            values!!.put(PadContentProvider.Companion._ID_GROUP, group)
            return values
        }

    internal inner class SpinnerGroupAdapter<P>(private val mContext: Context, resource: Int, objects: List<PadGroup?>) : ArrayAdapter<PadGroup?>(mContext, resource, objects) {
        private val mInflater: LayoutInflater
        private val items: List<PadGroup?>
        private val mResource: Int

        init {
            mInflater = LayoutInflater.from(mContext)
            mResource = resource
            items = objects
        }

        override fun getItem(position: Int): PadGroup? {
            return items[position]
        }

        override fun getDropDownView(position: Int, convertView: View?,
                                     parent: ViewGroup): View {
            return createItemView(position, convertView, parent)
        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            return createItemView(position, convertView, parent)
        }

        private fun createItemView(position: Int, convertView: View?, parent: ViewGroup): View {
            val view = mInflater.inflate(mResource, parent, false)
            val text = view.findViewById<TextView>(android.R.id.text1)
            val padGroup = getItem(position)
            text.text = padGroup.getName()
            return view
        }
    }

    companion object {
        const val TAG = "EditPadDialog"
    }
}