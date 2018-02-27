package com.mikifus.padland.Models;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.mikifus.padland.PadContentProvider;

import java.util.ArrayList;

/**
 * Created by mikifus on 8/07/16.
 */
public class ServerModel extends BaseModel {

    public static final String TAG = "ServerModel";

    public static final String TABLE = "padland_servers";

    public static final String _ID = "_id";
    public static final String NAME = "name"; // Name of the server
    public static final String URL = "url"; // Domain, with protocol please
    public static final String PADPREFIX = "padprefix"; // the full address including server and name
    public static final String JQUERY = "jquery"; // the full address including server and name
    public static final String POSITION = "position"; // Position inside a sortable data set
    public static final String ENABLED = "enabled"; // Position inside a sortable data set

    // Database creation sql statement
    private static final String SERVERS_TABLE_CREATE_QUERY =
            "CREATE TABLE "+ TABLE + "( " +
            " "+ _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            " "+ NAME + " text not null," +
            " "+ URL+ " TEXT NOT NULL, " +
            " "+ PADPREFIX +" TEXT NOT NULL, " +
            " "+ POSITION+ " INTEGER NOT NULL DEFAULT 0, "+
            " "+ JQUERY+ " INTEGER NOT NULL DEFAULT 0, "+ // Actually boolean
            " "+ ENABLED+ " INTEGER NOT NULL DEFAULT 1 "+ // Actually boolean
            ");";


    public ServerModel(Context context) {
        super(context);
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        database.execSQL(SERVERS_TABLE_CREATE_QUERY);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if( newVersion > 1 ) {
            Log.w(TAG,
                    "Upgrading database from version " + oldVersion + " to "
                            + newVersion + ", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS " + TABLE);
            onCreate(db);
        }
    }


    /**
     * Self explanatory name.
     * Field to compare must be specified by its identifier. Accepts only one comparation value.
     * @param field Field name wich will be compared
     * @param comparation
     * @return
     */
    private Cursor _getServerDataFromDatabase(String field, String comparation ){
        String QUERY;
        Cursor c = null;
        String[] comparation_set = new String[]{ comparation };

        QUERY =
                "SELECT * " +
                        "FROM " + ServerModel.TABLE + " " +
                        "WHERE " + field + "=?" +
                " ORDER BY " + POSITION + " ASC,"+ _ID +" DESC ";

        c = db.rawQuery(QUERY, comparation_set);
        return c;
    }

    /**
     * Queries the database and compares to pad_id
     * @param pad_id
     * @return
     */
    public Cursor _getServerDataById( long pad_id ){
        return this._getServerDataFromDatabase( ServerModel._ID, String.valueOf( pad_id ) );
    }

    /**
     * Queries the database and compares to padUrl
     * @param padUrl
     * @return
     */
    public Cursor _getServerDataByUrl(String padUrl){
        return this._getServerDataFromDatabase( ServerModel.URL, padUrl );
    }
    /**
     * Saves a new server if server_id=0 or updates an existing one.
     *
     * @param server_id
     * @param values
     * @return
     */
    public boolean saveServerData(long server_id, ContentValues values) {
        if (server_id > 0) {
            String[] where_value = {String.valueOf(server_id)};
            int result = db.update(ServerModel.TABLE, values, PadContentProvider._ID + "=?", where_value);
            return (result > 0);
        } else {
            Log.d("INSERT", "Contents = " + values.toString());
            long result = db.insert(ServerModel.TABLE, null, values);
            return (result > 0);
        }
    }

    public int getServerCount() {
        String QUERY;
        Cursor c = null;
        String[] comparation_set = new String[]{};

        QUERY =
                "SELECT " + ServerModel._ID + " " +
                    "FROM " + ServerModel.TABLE + " ";

        Cursor cursor = db.rawQuery(QUERY, comparation_set);

        int count = cursor.getCount();
        cursor.close();
        return count;
    }

    public Cursor _getServerDataByPosition(int position) {
        String QUERY;
        Cursor c = null;
        String[] comparation_set = new String[]{};

        QUERY =
                "SELECT * " +
                    "FROM " + ServerModel.TABLE + " " +
//                            "WHERE " + field + "=?" +
                    " ORDER BY " + POSITION + " ASC,"+ _ID +" DESC " +
                    " LIMIT " + position + ", 1";

        c = db.rawQuery(QUERY, comparation_set);
        return c;
    }

    public Server getServerAt(int position) {
        Server server = new Server();
        Cursor cursor = _getServerDataByPosition(position);
        cursor.moveToFirst();
        while (!cursor.isAfterLast())
        {
            int id = cursor.getInt(0);
            String name = cursor.getString(1);
            String url = cursor.getString(2);
            String padprefix = cursor.getString(3);
            String pos = cursor.getString(4);
            int jquery = cursor.getInt(5);

            server.id = id;
            server.name = name;
            server.url = url;
            server.url_padprefix = padprefix;
            server.position = pos;
            server.jquery = jquery == 1;

            break;
        }
        cursor.close();

        return server;
    }

    public Server getServerById(long id) {
        Cursor cursor = _getServerDataById(id);
        cursor.moveToFirst();
        Server server = null;
        while (!cursor.isAfterLast())
        {
            server = cursorToServer(cursor);
            cursor.moveToNext();
        }
        cursor.close();

        return server;
    }

    public ArrayList<Server> getEnabledServerList() {
        Server server;
        ArrayList<Server> servers = new ArrayList<>();
        Cursor cursor = _getServerDataFromDatabase(ENABLED, "1");
        cursor.moveToFirst();
        while (!cursor.isAfterLast())
        {
            server = cursorToServer(cursor);
            servers.add(server);
            cursor.moveToNext();
        }
        cursor.close();

        return servers;
    }

    private Server cursorToServer( Cursor cursor ){
        int id = cursor.getInt(0);
        String name = cursor.getString(1);
        String url = cursor.getString(2);
        String padprefix = cursor.getString(3);
        String pos = cursor.getString(4);
        int jquery = cursor.getInt(5);

        Server server;
        server = new Server();
        server.id = id;
        server.name = name;
        server.url = url;
        server.url_padprefix = padprefix;
        server.position = pos;
        server.jquery = jquery == 1;

        return server;
    }

    public boolean deleteServer(long server_id) {
        String[] where_value = {String.valueOf(server_id)};
        int result = db.delete(ServerModel.TABLE, PadContentProvider._ID + "=?", where_value);
        return result > 0;
    }
}