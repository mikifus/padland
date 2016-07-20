package com.mikifus.padland.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.mikifus.padland.Models.Server;
import com.mikifus.padland.Models.ServerModel;
import com.mikifus.padland.R;
import com.mikifus.padland.ServerListActivity;

import java.util.ArrayList;

/**
 * Created by mikifus on 29/05/16.
 */
public class ServerListAdapter extends ArrayAdapter {

//    private ServerListActivity mContext;
    private int layout_resource;
    private ServerModel serverModel;
    private ArrayList<Server> items;

    public ServerListAdapter(ServerListActivity context, int resource) {
        super(context, resource);
//        mContext = context;
        layout_resource = resource;
        serverModel = new ServerModel(context);
        items = serverModel.getEnabledServerList();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        Server server = getItem(position);

        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(layout_resource, parent, false);
        }

        ((TextView) convertView.findViewById(R.id.name)).setText(server.name);
        ((TextView) convertView.findViewById(R.id.url)).setText(server.url);

        // Return the completed view to render on screen
        return convertView;
    }

    @Override
    public int getCount() {
        return serverModel.getServerCount();
    }

    @Override
    public Server getItem(int position) {
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return items.get(position).getId();
    }

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
        items = serverModel.getEnabledServerList();
    }
}
