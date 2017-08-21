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

    public DrawerItemAdapter(Context context, int resourceId, ArrayList<DrawerItem> drawerItemArrayList) {
        super(context, resourceId, drawerItemArrayList);
    }

    /**
     * Instantiates ListView for ProfileActivity navigation drawer. List
     * item 0 is the header and the rest are ListView navigation options.
     *
     * @param position    Position in DrawerItemArrayList
     * @param convertView View to return after inflating layout
     * @param parent      Parent ViewGroup
     * @return Inflated layout
     */
    @Override
    @NonNull
    public View getView(final int position, View convertView, @NonNull ViewGroup parent) {
        if (position == 0) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.header, parent, false);
        } else {
            final DrawerItem drawerItem = getItem(position);
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.drawer_list_item, parent, false);
            }
            ImageView icon = (ImageView) convertView.findViewById(R.id.drawer_option_icon);
            TextView optionName = (TextView) convertView.findViewById(R.id.drawer_option_TextView);
            if (drawerItem != null) {
                icon.setImageResource(drawerItem.getImageId());
                icon.setFocusable(false);
                optionName.setText(drawerItem.getOptionName());
            }
        }
        return convertView;
    }
}
