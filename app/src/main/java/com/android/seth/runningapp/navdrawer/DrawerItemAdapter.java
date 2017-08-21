package com.android.seth.runningapp.navdrawer;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.seth.runningapp.R;

import java.util.ArrayList;

public class DrawerItemAdapter extends ArrayAdapter<DrawerItem> {
    private ArrayList<DrawerItem> drawerItemsArrayList;

    public DrawerItemAdapter(Context context, int resourceId, ArrayList<DrawerItem> drawerItemArrayList){
        super(context, resourceId, drawerItemArrayList);
    }

    @Override
    @NonNull
    public View getView(final int position, View convertView, @NonNull ViewGroup parent){
        final DrawerItem drawerItem = getItem(position);
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.drawer_list_item, parent, false);
        }
        ImageView icon = (ImageView) convertView.findViewById(R.id.drawer_option_icon);
        TextView optionName = (TextView) convertView.findViewById(R.id.drawer_option_TextView);
        if(drawerItem != null) {
            icon.setImageResource(drawerItem.getImageId());
            icon.setFocusable(false);
            optionName.setText(drawerItem.getOptionName());
        }
        return convertView;
    }
}
