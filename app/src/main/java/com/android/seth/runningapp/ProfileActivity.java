package com.android.seth.runningapp;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class ProfileActivity extends AppCompatActivity {
    private String[] drawerOptions = {"Begin", "History", "Settings"};
    private ActionBarDrawerToggle mDrawerToggle;
    private ActionBar actionBar;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.navigation_drawer);
        DrawerLayout mDrawerLayout = (DrawerLayout) findViewById(R.id.navigation_drawer);
        ListView mDrawerList = (ListView) findViewById(R.id.left_drawer);
        mDrawerList.setAdapter(new ArrayAdapter<>(this, R.layout.drawer_list_item, drawerOptions));
        setListViewOnitemClick(mDrawerList);
        actionBar = getSupportActionBar();

        mDrawerToggle = new ActionBarDrawerToggle(
                this,                             /* host Activity */
                mDrawerLayout,                    /* DrawerLayout object */
                R.string.drawer_open,  /* "open drawer" description for accessibility */
                R.string.drawer_close  /* "close drawer" description for accessibility */
        ) {
            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                actionBar.setTitle("RunTime");
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                actionBar.setTitle("RunTime");
            }
        };
        mDrawerLayout.addDrawerListener(mDrawerToggle);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);
    }

    /**
     * Allows App Icon to change upon Navigation Drawer opening/closing.
     *
     * @param savedInstanceState    Instance state of activity.
     */
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    /**
     * Handles activity changes such as orientation.
     *
     * @param newConfig     new Activity configuration.
     */
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    /**
     * Sets onClickListener for each ListView item in the Navigation Drawer.
     *
     * @param mDrawerList   navigation drawer listview to set listener for
     */
    private void setListViewOnitemClick(ListView mDrawerList) {
        mDrawerList.setOnItemClickListener(new ListView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent activityToStart = null;
                switch(position){
                    case 0:
                        activityToStart = new Intent(ProfileActivity.this, RunActivity.class);
                        break;
                    case 1:
                        activityToStart = new Intent(ProfileActivity.this, HistoryActivity.class);
                        break;
                    case 2:
                        return;
                }
                if(activityToStart != null) {
                    ProfileActivity.this.startActivity(activityToStart);
                }
            }
        });
    }

    /**
     * Handles opening Navigation Drawer with touching App Icon.
     *
     * @param item  Selected MenuItem
     * @return  superclass implementation
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Pass the event to ActionBarDrawerToggle, if it returns
        // true, then it has handled the app icon touch event
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        // Handle your other action bar items
        return super.onOptionsItemSelected(item);
    }
}

// TODO: Add content for main screen
// TODO: Improve looks of Nav Drawer