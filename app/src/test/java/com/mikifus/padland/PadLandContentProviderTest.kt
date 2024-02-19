package com.mikifus.padland

import android.content.ContentResolver
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.content.pm.ProviderInfo
import android.database.Cursor
import androidx.room.Room
import androidx.test.core.app.ActivityScenario.launch
import androidx.test.core.app.ApplicationProvider
import com.mikifus.padland.Database.PadGroupModel.PadGroup
import com.mikifus.padland.Database.PadGroupModel.PadGroupsAndPadList
import com.mikifus.padland.Database.PadGroupModel.PadGroupsWithPadList
import com.mikifus.padland.Database.PadListDatabase
import com.mikifus.padland.Database.PadModel.Pad
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.AfterClass
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowContentResolver
import java.sql.Date


@RunWith(RobolectricTestRunner::class)
@Config(application = PadlandApp::class)
class PadLandContentProviderTest {
    private lateinit var database: PadListDatabase

    private var mContentResolver: ContentResolver? = null
    private var mShadowContentResolver: ShadowContentResolver? = null
    private var mProvider: PadLandContentProvider? = null

    private val examplePadData = ContentValues().apply {
        put("name", "AAAAA")
        put("local_name", "AAAAA")
        put("server", "https://pad.aquilenet.fr/p/")
        put("url", "https://pad.aquilenet.fr/p/AAAAA")
        put("create_date", Date(System.currentTimeMillis()).toString())
        put("access_count", 0)
    }
    private val examplePadGroupData = ContentValues().apply {
        put("name", "AAAAA GROUP")
    }

    @Before
    fun setupBefore() {
        database = PadListDatabase
            .getMainThreadInstance(ApplicationProvider.getApplicationContext())
        val providerInfo = ProviderInfo()
        providerInfo.authority = PadLandContentProvider.AUTHORITY
        providerInfo.grantUriPermissions = true

        mContentResolver = ApplicationProvider.getApplicationContext<Context>().contentResolver
        val controller = Robolectric
            .buildContentProvider(PadLandContentProvider::class.java)
            .create(providerInfo)

        mShadowContentResolver = shadowOf(mContentResolver)

        mProvider = controller.get()
    }

    @After
    fun cleanupAfter() {
        // Clean relations first
        database.openHelper.writableDatabase.delete(PadGroupsAndPadList.TABLE_NAME, null, null)
        database.openHelper.writableDatabase.delete(PadGroup.TABLE_NAME, null, null)
        database.openHelper.writableDatabase.delete(Pad.TABLE_NAME, null, null)

        database.openHelper.writableDatabase.delete("sqlite_sequence", null, null)
    }

    @Test
    fun queryPads() {
        mProvider!!.insert(PadLandContentProvider.URI_PAD_LIST, examplePadData)
        mProvider!!.insert(PadLandContentProvider.URI_PAD_LIST, examplePadData)

        val padCursor: Cursor = mProvider!!.query(
            PadLandContentProvider.URI_PAD_LIST,
            arrayOf("url"),
            "",
            arrayOf(),
            ""
        )

        assert(padCursor.count == 2)
        padCursor.close()
    }

    @Test
    fun queryPad() {
        val resultUri = mProvider!!.insert(PadLandContentProvider.URI_PAD_LIST, examplePadData)
        assert(resultUri.toString() == PadLandContentProvider.URI_PAD_LIST.toString() + "/1")

        val padCursor: Cursor = mProvider!!.query(
            resultUri,
            arrayOf("url"),
            "_id = ?",
            arrayOf("1"),
            ""
        )

        padCursor.moveToNext()
        assert(padCursor.getString(padCursor.getColumnIndex("url")) == examplePadData.get("url"))
        padCursor.close()
    }

    @Test
    fun insertPad() {
        val resultUri = mProvider!!.insert(PadLandContentProvider.URI_PAD_LIST, examplePadData)
        assert(resultUri.toString() == PadLandContentProvider.URI_PAD_LIST.toString() + "/1")
    }

    @Test
    fun updatePad() {
        val resultUri = mProvider!!.insert(PadLandContentProvider.URI_PAD_LIST, examplePadData)
        assert(resultUri.toString() == PadLandContentProvider.URI_PAD_LIST.toString() + "/1")


        val rowsUpdated = mProvider!!.update(
            resultUri,
            examplePadData,
            "_id = ?",
            arrayOf("1")
        )

        assert(rowsUpdated == 1)
    }

    @Test
    fun deletePad() {
        val resultUri = mProvider!!.insert(PadLandContentProvider.URI_PAD_LIST, examplePadData)
        assert(resultUri.toString() == PadLandContentProvider.URI_PAD_LIST.toString() + "/1")

        val rowsDeleted = mProvider!!.delete(
            resultUri,
            "_id = ?",
            arrayOf("1")
        )

        assert(rowsDeleted == 1)
    }

    @Test
    fun queryGroups() {
        val padGroup = PadGroup.fromContentValues(examplePadGroupData)
        database.padGroupDao().insertAll(padGroup.value!!, padGroup.value!!, padGroup.value!!)

        val padGroupCursor: Cursor = mProvider!!.query(
            PadLandContentProvider.URI_PAD_GROUP_LIST,
            arrayOf("name"),
            "",
            arrayOf(),
            ""
        )

        padGroupCursor.moveToNext()
        assert(padGroupCursor.count == 3)
        padGroupCursor.close()
    }

    @Test
    fun queryPadListAndPadGroups() {
        val padGroup = PadGroup.fromContentValues(examplePadGroupData)
        val pad = Pad.fromContentValues(examplePadData)

        runBlocking {
            database.padGroupDao().insertAll(padGroup.value!!)
            database.padDao().insertAll(
                listOf(
                    pad.value!!,
                    pad.value!!
                )
            )
            database.padGroupDao().insertPadGroupWithPadlist(
                PadGroupsAndPadList(
                    mGroupId = 1L,
                    mPadId = 2L,
                )
            )
        }

        val padGroupCursor: Cursor = mProvider!!.query(
            ContentUris.withAppendedId(PadLandContentProvider.URI_PAD_LIST_PAD_GROUP, 1),
            arrayOf("_id_pad"),
            "_id_group = ?",
            arrayOf(1L.toString()),
            ""
        )

        padGroupCursor.moveToNext()
        assert(padGroupCursor.count == 1)
        assert(padGroupCursor.getLong(padGroupCursor.getColumnIndex("_id_pad")) == 2L)
        padGroupCursor.close()
    }
}