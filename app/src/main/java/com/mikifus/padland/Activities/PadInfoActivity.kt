package com.mikifus.padland.Activities;

import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import com.mikifus.padland.Database.PadGroupModel.PadGroupViewModel
import com.mikifus.padland.Database.PadModel.Pad
import com.mikifus.padland.Database.PadModel.PadViewModel
import com.mikifus.padland.Dialogs.Managers.IManagesDeletePadDialog
import com.mikifus.padland.Dialogs.Managers.IManagesEditPadDialog
import com.mikifus.padland.Dialogs.Managers.ManagesDeletePadDialog
import com.mikifus.padland.Dialogs.Managers.ManagesEditPadDialog
import com.mikifus.padland.R
import com.mikifus.padland.Utils.Offline.OfflinePadFetcher
import com.mikifus.padland.Utils.Offline.OfflinePadStore
import com.mikifus.padland.Utils.PadClipboardHelper
import com.mikifus.padland.Utils.PadShareHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.DateFormat
import java.util.Date

class PadInfoActivity: AppCompatActivity(),
    IManagesEditPadDialog by ManagesEditPadDialog(),
    IManagesDeletePadDialog by ManagesDeletePadDialog() {

    override var padViewModel: PadViewModel? = null
    override var padGroupViewModel: PadGroupViewModel? = null

    private var mPadViewButton: MaterialButton? = null
    private var mCopyButton: ImageButton? = null
    private var mNameTextView: TextView? = null
    private var mUrlTextView: TextView? = null
    private var mCreateDateTextView: TextView? = null
    private var mLastUsedDateTextView: TextView? = null
    private var mAccessCountTextView: TextView? = null
    private var mOfflineAccessContainer: View? = null
    private var mOfflineAccessCheckBox: CheckBox? = null
    private var mOfflineCopyView: View? = null
    private var mOfflineCopyStatusTextView: TextView? = null

    private val offlinePadStore by lazy { OfflinePadStore(this) }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_pad_info)
        setSupportActionBar(findViewById(R.id.activity_toolbar))

        mPadViewButton = findViewById(R.id.button_pad_view)
        mCopyButton = findViewById(R.id.button_copy)
        mNameTextView = findViewById(R.id.txt_padinfo_pad_name)
        mUrlTextView = findViewById(R.id.txt_padinfo_pad_url)
        mCreateDateTextView = findViewById(R.id.txt_padinfo_createdate)
        mLastUsedDateTextView = findViewById(R.id.txt_padinfo_lastuseddate)
        mAccessCountTextView = findViewById(R.id.txt_padinfo_times_accessed)
        mOfflineAccessContainer = findViewById(R.id.padinfo_offline_access_container)
        mOfflineAccessCheckBox = findViewById(R.id.checkbox_padinfo_offline_access)
        mOfflineCopyView = findViewById(R.id.padinfo_offline_copy)
        mOfflineCopyStatusTextView = findViewById(R.id.txt_padinfo_offline_copy_status)

        initEvents()
    }

    override fun onResume() {
        super.onResume()

        initViewModels()
    }

    private fun initViewModels() {
        if(padViewModel == null) {
            padViewModel = ViewModelProvider(this)[PadViewModel::class.java]
        }
        if(padGroupViewModel == null) {
            padGroupViewModel = ViewModelProvider(this)[PadGroupViewModel::class.java]
        }

        padViewModel!!.pad.removeObservers(this) // Remove because called from onResume
        padViewModel!!.pad.observe(this@PadInfoActivity) { pad ->
            if(pad == null || pad.mId == 0L) {
                finish() // It was deleted
            } else {
                onPadUpdate(pad)
            }
        }
        lifecycleScope.launch(Dispatchers.IO) {
            padViewModel!!.getById(intent!!.extras!!.getLong("padId"))
        }
    }

    private fun initEvents() {
        mPadViewButton?.setOnClickListener {
            onViewButtonClick()
        }
        mCopyButton?.setOnClickListener {
            if(padViewModel!!.pad.value != null) {
                PadClipboardHelper.copyToClipboard(this, listOf(padViewModel!!.pad.value!!.mUrl))

                Toast.makeText(
                    this@PadInfoActivity,
                    getString(R.string.copy_copied),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
        mOfflineAccessCheckBox?.setOnCheckedChangeListener { _, isChecked ->
            onOfflineAccessChanged(isChecked)
        }
        mOfflineCopyView?.setOnClickListener {
            onOfflineCopyClick()
        }
    }

    private fun onPadUpdate(pad: Pad) {
        mNameTextView?.text = pad.mLocalName.ifBlank { pad.mName }
        mUrlTextView?.text = pad.mUrl
        mCreateDateTextView?.text = pad.mCreateDate.toString()
        mLastUsedDateTextView?.text = pad.mLastUsedDate.toString()
        mAccessCountTextView?.text = pad.mAccessCount.toString()
        mOfflineAccessCheckBox?.isChecked = pad.mOfflineAccess
        updateOfflineCopyStatus(pad)
    }

    private fun onOfflineAccessChanged(isChecked: Boolean) {
        val pad = padViewModel!!.pad.value ?: return
        // Set from onPadUpdate
        if (pad.mOfflineAccess == isChecked) {
            return
        }

        val updatedPad = pad.copy(mOfflineAccess = isChecked)
        lifecycleScope.launch(Dispatchers.IO) {
            padViewModel!!.updatePad(updatedPad)
            if (!isChecked) {
                offlinePadStore.delete(pad.mId)
            }

            withContext(Dispatchers.Main) {
                if (isChecked) {
                    // First copy right away
                    OfflinePadFetcher.update(this@PadInfoActivity, updatedPad) { updated ->
                        padViewModel?.pad?.value?.let { updateOfflineCopyStatus(it) }
                        if (!updated) {
                            Toast.makeText(
                                this@PadInfoActivity,
                                getString(R.string.padinfo_offline_copy_failed),
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                }
                updateOfflineCopyStatus(updatedPad)
            }
        }
    }

    /**
     * Only Etherpad Lite pads have offline access (for now).
     */
    private fun updateOfflineCopyStatus(pad: Pad) {
        val isAvailable = OfflinePadFetcher.isAvailable(pad)
        mOfflineAccessContainer?.visibility = if (isAvailable) View.VISIBLE else View.GONE
        mOfflineCopyView?.visibility = if (isAvailable && pad.mOfflineAccess) View.VISIBLE else View.GONE

        val savedTime = offlinePadStore.getSavedTime(pad.mId)
        mOfflineCopyStatusTextView?.text = when {
            OfflinePadFetcher.isUpdating(pad.mId) -> getString(R.string.padinfo_offline_copy_updating)
            savedTime != null -> getString(R.string.padinfo_offline_copy_saved,
                DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT).format(Date(savedTime)))
            else -> getString(R.string.padinfo_offline_copy_missing)
        }
        // Nothing to open yet
        mOfflineCopyView?.isEnabled = savedTime != null
    }

    private fun onOfflineCopyClick() {
        val pad = padViewModel!!.pad.value ?: return
        val padViewIntent = Intent(this@PadInfoActivity, PadViewActivity::class.java)
        padViewIntent.putExtra("padId", pad.mId)
        padViewIntent.putExtra(PadViewActivity.EXTRA_OFFLINE_COPY, true)
        startActivity(padViewIntent)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.pad_info, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(padViewModel!!.pad.value == null) {
            return super.onOptionsItemSelected(item)
        }
        when (item.itemId) {
            R.id.menuitem_share -> {
                sharePad()
            }
            R.id.menuitem_edit -> {
                showEditPadDialog(this,
                    padViewModel!!.pad.value!!.mId,
                    findViewById(R.id.menuitem_edit))
            }
            R.id.menuitem_delete -> {
                showDeletePadDialog(this, listOf(padViewModel!!.pad.value!!.mId))
            }
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    private fun sharePad() {
        if (padViewModel!!.pad.value != null) {
            PadShareHelper.share(
                this,
                getString(R.string.share_auto_text),
                listOf(padViewModel!!.pad.value!!.mUrl)
            )
        }
    }

    private fun onViewButtonClick() {
        if(padViewModel!!.pad.value != null) {
            val padViewIntent = Intent(this@PadInfoActivity, PadViewActivity::class.java)
            padViewIntent.putExtra("padId", padViewModel!!.pad.value!!.mId)
            padViewIntent.setFlags(FLAG_ACTIVITY_NEW_TASK)
            startActivity(padViewIntent)
        }
    }
}
