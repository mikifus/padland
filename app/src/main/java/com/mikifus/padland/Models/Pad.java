package com.mikifus.padland.Models;

/**
 * Created by mikifus on 27/02/18.
 */


import android.content.Context;
import android.database.Cursor;

import java.text.DateFormat;
import java.util.Date;

/**
 * The padData subclass is the summary of information the App needs
 * to deal with the documents. It has the info and returns it
 * in the right format.
 */
public class Pad
{
    private long id;
    private String name;
    private String local_name;
    private String server;
    private String url;
    private long last_used_date;
    private long create_date;
    private long access_count;

    public Pad(Cursor c) {
        if(c != null && c.getCount() > 0) {
            id = c.getLong(0);
            name = c.getString(1);
            local_name = c.getString(2);
            server =  c.getString(3);
            url = c.getString(4);
            last_used_date = c.getLong(5);
            create_date = c.getLong(6);
            access_count = c.getLong(7);
        }
    }
    public long getId() {
        return id;
    }
    public String getName() {
        return name;
    }
    public String getLocalName() {
        if(local_name == null || local_name.isEmpty()) {
            return name;
        }
        return local_name;
    }
    public String getRawLocalName() {
        if(local_name == null || local_name.isEmpty()) {
            return "";
        }
        return local_name;
    }
    public String getServer() {
        return server;
    }
    public String getUrl() {
        return url;
    }
    public String getLastUsedDate(Context context) {
        return lon_to_date( last_used_date, context );
    }
    public long getAccessCount() {
        return access_count;
    }
    public String getCreateDate(Context context) {
        return lon_to_date( create_date, context );
    }
    public String lon_to_date( long TimeinMilliSeccond, Context context ){
        DateFormat formatter = android.text.format.DateFormat.getDateFormat( context.getApplicationContext() );
        Date dateObj = new Date( TimeinMilliSeccond * 1000 );
        return formatter.format( dateObj );
    }
}
