package com.mikifus.padland;

import android.content.Context;
import android.util.Log;
import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;
import org.xmlpull.v1.XmlSerializer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by mikifus on 12/01/15.
 */
public class PadLandXML {
    Context context;

    public PadLandXML(Context context){
        this.context = context;
    }

    public void savePadList(Map<Integer, Map<String, String>> data) throws IOException {
        String result = _generateXML(data);
        FileOutputStream os = null;

        try {
            os = context
                    .getApplicationContext()
                    .openFileOutput("padland_padlist.xml", Context.MODE_WORLD_READABLE);
            os.write(result.getBytes());
            os.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (os != null) {
                    os.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private String _generateXML(Map<Integer, Map<String, String>> data) throws IOException {
        XmlSerializer xmlSerializer = Xml.newSerializer();
        StringWriter writer = new StringWriter();

        xmlSerializer.setOutput(writer);
        // start DOCUMENT
        xmlSerializer.startDocument("UTF-8", true);

        xmlSerializer.startTag("", "padlist");
        // START
        for(Map.Entry<Integer, Map<String, String>> entry_element : data.entrySet())
        {
            if(entry_element.getValue().containsKey(null)){
                Log.w("Warning", "Corrupted pad data, not saving this row. Position: "+entry_element.getKey());
                continue;
            }
            Map<String, String> element = (Map<String, String>) entry_element.getValue();

            xmlSerializer.startTag("","pad");

            xmlSerializer.startTag("","name");
                xmlSerializer.text(element.get("name"));
            xmlSerializer.endTag("", "name");

            xmlSerializer.startTag("", "server");
                xmlSerializer.text(element.get("server"));
            xmlSerializer.endTag("", "server");

            xmlSerializer.startTag("","url");
                xmlSerializer.text(element.get("url"));
            xmlSerializer.endTag("", "url");

            xmlSerializer.endTag("","pad");
        }

        // END
        xmlSerializer.endTag("", "padlist");

        // end DOCUMENT
        xmlSerializer.endDocument();

        return writer.toString();
    }
    public Map loadPadList() throws XmlPullParserException, IOException {
        HashMap<Integer, Map<String, String>> userData = new HashMap<Integer, Map<String, String>>();
        FileInputStream os;
        String raw_data = null;

        if(!fileExists("padland_padlist.xml"))
        {
            Log.i("xmlParser","File doesn't exist!");
            return userData;
        }

        try {
            os = context
                    .getApplicationContext()
                    .openFileInput("padland_padlist.xml");

            InputStreamReader isr = new InputStreamReader(os);
            char[] inputBuffer = new char[os.available()];
            isr.read(inputBuffer);
            raw_data = new String(inputBuffer);
            isr.close();
            os.close();
        }
        catch (FileNotFoundException e3) {
            // TODO Auto-generated catch block
            e3.printStackTrace();
        }
        catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        if(raw_data == null) return userData;

        XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
        factory.setNamespaceAware(true);
        XmlPullParser xpp = factory.newPullParser();

        xpp.setInput( new StringReader( raw_data ) );
        int eventType = xpp.getEventType();
        int array_position = 0;
        while (eventType != XmlPullParser.END_DOCUMENT) {
            String _tagname = xpp.getName();
            if(eventType == XmlPullParser.START_DOCUMENT) {
                // Nothing
            } else if(eventType == XmlPullParser.START_TAG) {
                if(_tagname.equals("pad")){
                    userData.put(array_position, new HashMap<String, String>());
                    Log.d("xmlParser", "INTO PAD");
                }
            } else if(eventType == XmlPullParser.TEXT) {
                Log.d("xmlParser","getText = "+xpp.getText());
                userData.get(array_position).put(_tagname, xpp.getText());
            } else if(eventType == XmlPullParser.END_TAG) {
                if(_tagname.equals("pad")){
                    Log.d("xmlParser","OUT OF PAD");
                    ++array_position;
                    Log.d("xmlParser","pos:" + array_position);
                }
            }
            eventType = xpp.next();
        }

        return userData;
    }

    public boolean fileExists(String filename) {
        File file = context.getFileStreamPath(filename);
        if(file == null || !file.exists()) {
            return false;
        }
        return true;
    }
}
