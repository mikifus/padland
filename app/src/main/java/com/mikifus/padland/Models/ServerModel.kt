package com.mikifus.padland.Models

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.util.Log
import com.mikifus.padland.PadContentProvider
import com.mikifus.padland.PadlandDbHelper
import com.mikifus.padland.R
import java.util.Arrays

/**
 * Created by mikifus on 8/07/16.
 */
class ServerModel(context: Context?) : BaseModel(context) {
    private override val context: Context? = null

    /**
     * Self explanatory name.
     * Field to compare must be specified by its identifier. Accepts only one comparation value.
     * @param field Field name wich will be compared
     * @param comparation
     * @return
     */
    private fun _getServerDataFromDatabase(field: String, comparation: String): Cursor? {
        var QUERY: String
        var c: Cursor? = null
        val comparation_set = arrayOf(comparation)
        c = db.query(TABLE,
                serverFieldsList,
                "$field LIKE ?",
                comparation_set,  // AKA id
                null,
                null,
                POSITION + " ASC, " + _ID + " DESC"
        )
        //        QUERY =
//                "SELECT * " +
//                        "FROM " + ServerModel.TABLE + " " +
//                        "WHERE " + field + " LIKE ?" +
//                " ORDER BY " + POSITION + " ASC,"+ _ID +" DESC ";
//
//        c = db.rawQuery(QUERY, comparation_set);
        return c
    }

    /**
     * Queries the database and compares to pad_id
     * @param pad_id
     * @return
     */
    fun _getServerDataById(pad_id: Long): Cursor? {
        return _getServerDataFromDatabase(_ID, pad_id.toString())
    }

    /**
     * Queries the database and compares to padUrl
     * @param padUrl
     * @return
     */
    fun _getServerDataByUrl(padUrl: String): Cursor? {
        return _getServerDataFromDatabase(URL, padUrl)
    }

    /**
     * Saves a new server if server_id=0 or updates an existing one.
     *
     * @param server_id
     * @param values
     * @return
     */
    fun saveServerData(server_id: Long, values: ContentValues?): Boolean {
        return if (server_id > 0) {
            val where_value = arrayOf(server_id.toString())
            val result = db.update(TABLE, values, PadContentProvider.Companion._ID + "=?", where_value)
            result > 0
        } else {
            Log.d("INSERT", "Contents = " + values.toString())
            val result = db.insert(TABLE, null, values)
            result > 0
        }
    }

    val serverCount: Int
        get() {
            val QUERY: String
            val c: Cursor? = null
            val comparation_set = arrayOf<String>()
            QUERY = "SELECT " + _ID + " " +
                    "FROM " + TABLE + " "
            val cursor = db.rawQuery(QUERY, comparation_set)
            val count = cursor.count
            cursor.close()
            return count
        }

    fun _getServerDataByPosition(position: Int): Cursor? {
        val QUERY: String
        var c: Cursor? = null
        val comparation_set = arrayOf<String>()
        QUERY = "SELECT * " +
                "FROM " + TABLE + " " +  //                            "WHERE " + field + "=?" +
                " ORDER BY " + POSITION + " ASC," + _ID + " DESC " +
                " LIMIT " + position + ", 1"
        c = db.rawQuery(QUERY, comparation_set)
        return c
    }

    fun getServerAt(position: Int): Server {
        val server = Server()
        val cursor = _getServerDataByPosition(position)
        cursor!!.moveToFirst()
        while (!cursor.isAfterLast) {
            val id = cursor.getInt(0)
            val name = cursor.getString(1)
            val url = cursor.getString(2)
            val padprefix = cursor.getString(3)
            val pos = cursor.getString(4)
            val jquery = cursor.getInt(5)
            server.id = id
            server.name = name
            server.url = url
            server.url_padprefix = padprefix
            server.position = pos
            server.jquery = jquery == 1
            break
        }
        cursor.close()
        return server
    }

    fun getServerById(id: Long): Server? {
        val cursor = _getServerDataById(id)
        cursor!!.moveToFirst()
        var server: Server? = null
        while (!cursor.isAfterLast) {
            server = cursorToServer(cursor)
            cursor.moveToNext()
        }
        cursor.close()
        return server
    }

    fun getServerByUrl(url: String): Server? {
        val cursor = _getServerDataByUrl(url)
        cursor!!.moveToFirst()
        var server: Server? = null
        while (!cursor.isAfterLast) {
            server = cursorToServer(cursor)
            cursor.moveToNext()
        }
        cursor.close()
        return server
    }

    val enabledServerList: ArrayList<Server>
        get() {
            var server: Server
            val servers = ArrayList<Server>()
            val cursor = _getServerDataFromDatabase(ENABLED, "1")
            cursor!!.moveToFirst()
            while (!cursor.isAfterLast) {
                server = cursorToServer(cursor)
                servers.add(server)
                cursor.moveToNext()
            }
            cursor.close()
            return servers
        }

    /**
     * TODO: Put cursor loading to Server class constructor
     * @param cursor
     * @return
     */
    private fun cursorToServer(cursor: Cursor?): Server {
        val id = cursor!!.getInt(0)
        val name = cursor.getString(1)
        val url = cursor.getString(2)
        val padprefix = cursor.getString(3)
        val pos = cursor.getString(4)
        val jquery = cursor.getInt(5)
        val server: Server
        server = Server()
        server.id = id
        server.name = name
        server.url = url
        server.url_padprefix = padprefix
        server.position = pos
        server.jquery = jquery == 1
        return server
    }

    fun deleteServer(server_id: Long): Boolean {
        val where_value = arrayOf(server_id.toString())
        val result = db.delete(TABLE, PadContentProvider.Companion._ID + "=?", where_value)
        return result > 0
    }

    /**
     * Returns a string with the server urls.
     * Includes custom servers.
     * @return String[]
     */
    fun getServerUrlList(context: Context?): Array<String?> {
        val server_list: Array<String?>

        // Load the custom servers
        val custom_servers = enabledServerList
        val server_names = ArrayList<String?>()
        for (server in custom_servers) {
            server_names.add(server.getUrl())
        }

        // Server list to provide a fallback value
        val collection: MutableCollection<String?> = ArrayList()
        collection.addAll(server_names)
        collection.addAll(Arrays.asList(*context!!.resources.getStringArray(R.array.etherpad_servers_url_home)))
        server_list = collection.toTypedArray()
        return server_list
    }

    /**
     * Returns a string with the server urls and the prefix to see a pad.
     * Includes custom servers.
     * @return String[]
     */
    fun getServerUrlPrefixList(context: Context?): Array<String?> {
        val server_list: Array<String?>
        // Load the custom servers
        val custom_servers = enabledServerList
        val server_names = ArrayList<String?>()
        for (server in custom_servers) {
            server_names.add(server.padPrefixWithUrl)
        }

        // Server list to provide a fallback value
//        server_list.getResources().getStringArray( R.array.etherpad_servers_name );
        val collection: MutableCollection<String?> = ArrayList()
        collection.addAll(server_names)
        collection.addAll(Arrays.asList(*context!!.resources.getStringArray(R.array.etherpad_servers_url_padprefix)))
        server_list = collection.toTypedArray()
        return server_list
    }

    fun getServerPrefixFromUrl(context: Context?, server: String?): String? {
        var c = 0
        val serverUrlList = getServerUrlList(context)
        val serverUrlPrefixList = getServerUrlPrefixList(context)
        for (s in serverUrlList) {
            if (s == server) {
                break
            }
            c++
        }
        return if (c < serverUrlPrefixList.size && serverUrlPrefixList[c] != null) {
            serverUrlPrefixList[c]
        } else null
    }

    companion object {
        const val TAG = "ServerModel"
        protected const val OLD_DATABASE_NAME = "commments.db"
        protected val DATABASE_NAME: String = PadlandDbHelper.Companion.DATABASE_NAME
        protected val DATABASE_VERSION: Int = BaseModel.Companion.DATABASE_VERSION
        const val TABLE = "padland_servers"
        const val _ID = "_id"
        const val NAME = "name" // Name of the server
        const val URL = "url" // Domain, with protocol please
        const val PADPREFIX = "padprefix" // the full address including server and name
        const val JQUERY = "jquery" // the full address including server and name
        const val POSITION = "position" // Position inside a sortable data set
        const val ENABLED = "enabled" // Position inside a sortable data set

        // Database creation sql statement
        const val SERVERS_TABLE_CREATE_QUERY = "CREATE TABLE " + TABLE + "( " +
                " " + _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                " " + NAME + " text not null," +
                " " + URL + " TEXT NOT NULL, " +
                " " + PADPREFIX + " TEXT NOT NULL, " +
                " " + POSITION + " INTEGER NOT NULL DEFAULT 0, " +
                " " + JQUERY + " INTEGER NOT NULL DEFAULT 0, " +  // Actually boolean
                " " + ENABLED + " INTEGER NOT NULL DEFAULT 1 " +  // Actually boolean
                ");"
        val serverFieldsList: Array<String>
            get() = arrayOf(
                    _ID,
                    NAME,
                    URL,
                    PADPREFIX,
                    POSITION,
                    JQUERY,
                    ENABLED
            )
    }
}