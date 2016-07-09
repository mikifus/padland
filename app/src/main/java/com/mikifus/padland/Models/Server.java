package com.mikifus.padland.Models;

/**
 * Created by mikifus on 29/05/16.
 */
public class Server {
    public int id;
    public String name;
    public String url;
    public String url_padprefix;
    public boolean jquery;
    public String position;

    public String getName() {
        return name;
    }

    public String getUrl() {
        return url;
    }

    public String getPadPrefix() {
        return url_padprefix;
    }
}
