package com.mikifus.padland;

import android.app.Application;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by mikifus on 13/01/15.
 */
public class PadlandApp extends Application {
    private Map<Integer, Map<String, String>> padList;
    PadLandXML xml;

    public boolean isInPadList(String url){
        for(Map.Entry<Integer, Map<String, String>> entry_element : padList.entrySet())
        {
            Map<String, String> element = entry_element.getValue();
            if(element.containsValue(url)){
                return true;
            }
        }
        return false;
    }

    public Map getPadByUrl(String url){
        for(Map.Entry<Integer, Map<String, String>> entry_element : padList.entrySet())
        {
            Map<String, String> element = entry_element.getValue();
            if(element.containsValue(url)){
                return element;
            }
        }
        return null;
    }

    public void addToPadList(String name, String server, String url) throws IOException {
        Map<String, String> newpad = new HashMap();
            newpad.put("name", name);
            newpad.put("server", server);
            newpad.put("url", url);
        padList.put(padList.size(), newpad);

        try {
            xml.savePadList(padList);
        } catch (IOException e) {
            e.printStackTrace();
        }

        //return true;
    }

    public void setPadList(Map list){
        padList = list;
    }
    
    public Map getPadList(){
        if(padList == null){
            _loadPadList();
        }
        return padList;
    }
    
    private Map _loadPadList(){
        xml = new PadLandXML(this);
        padList = new HashMap<Integer, Map<String, String>>();
        try {
            padList = xml.loadPadList();
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        setPadList(padList);
        
        return padList;
    }
}
