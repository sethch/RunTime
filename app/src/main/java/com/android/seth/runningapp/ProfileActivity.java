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
import android.widget.ListView;
import android.widget.TextView;

import com.android.seth.runningapp.navdrawer.DrawerItem;
import com.android.seth.runningapp.navdrawer.DrawerItemAdapter;
import com.android.seth.runningapp.util.UtilityFunctions;
import com.android.seth.runningapp.util.Workout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Locale;

public class ProfileActivity extends AppCompatActivity {
    private DatabaseReference databaseReference;
    private FirebaseUser user;

    private String[] drawerOptions = {"Header", "Begin", "History", "Settings"};
    private int[] iconIdList = {0, R.mipmap.ic_begin, R.mipmap.ic_history, R.mipmap.ic_settings};
    private ActionBarDrawerToggle mDrawerToggle;
    DrawerItemAdapter drawerItemAdapter;
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;

    private TextView milesWeekTextView;
    private TextView milesTotalTextView;
    private TextView numWorkoutsTextView;
    private TextView bestPaceTextView;

    private final float[] milesWeek = new float[1];
    private final float[] milesAllTime = new float[1];
    private final int[] numWorkouts = new int[1];
    private final float[] bestPace = new float[1];

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initializeLayout();
        initializeListView();
        setListViewOnItemClick(mDrawerList);
        ActionBar actionBar = getSupportActionBar();
        FirebaseDatabase instance = FirebaseDatabase.getInstance();
        databaseReference = instance.getReference();
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();

        mDrawerToggle = new ActionBarDrawerToggle(
                this,                             /* host Activity */
                mDrawerLayout,                    /* DrawerLayout object */
                R.string.drawer_open,  /* "open drawer" description for accessibility */
                R.string.drawer_close  /* "close drawer" description for accessibility */
        ) {
            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
            }
        };
        mDrawerLayout.addDrawerListener(mDrawerToggle);
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
        }
        setStats();
    }

    /**
     * Initializes layout and views needed for ProfileActivity.
     */
    private void initializeLayout() {
        setContentView(R.layout.activity_profile);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.navigation_drawer);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);
        milesWeekTextView = (TextView) findViewById(R.id.miles_week);
        milesTotalTextView = (TextView) findViewById(R.id.miles_total);
        numWorkoutsTextView = (TextView) findViewById(R.id.num_workouts);
        bestPaceTextView = (TextView) findViewById(R.id.best_pace);
    }

    /**
     * Allows App Icon to change upon Navigation Drawer opening/closing.
     *
     * @param savedInstanceState Instance state of activity.
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
     * @param newConfig new Activity configuration.
     */
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    /**
     * Sets onClickListener for each ListView item in the Navigation Drawer.
     *
     * @param mDrawerList navigation drawer listview to set listener for
     */
    private void setListViewOnItemClick(ListView mDrawerList) {
        mDrawerList.setOnItemClickListener(new ListView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent activityToStart = null;
                switch (position) {
                    case 1:
                        activityToStart = new Intent(ProfileActivity.this, RunActivity.class);
                        break;
                    case 2:
                        activityToStart = new Intent(ProfileActivity.this, HistoryActivity.class);
                        break;
                    case 3:
                        activityToStart = new Intent(ProfileActivity.this, SettingsActivity.class);
                        break;
                }
                if (activityToStart != null) {
                    ProfileActivity.this.startActivity(activityToStart);
                }
            }
        });
    }

    /**
     * Handles opening Navigation Drawer with touching App Icon.
     *
     * @param item Selected MenuItem
     * @return superclass implementation
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

    /**
     * Sets TextViews for main content by querying FireBase.
     */
    private void setStats() {
        final long currentDate = System.currentTimeMillis();
        final long oneWeekAgo = currentDate - (1000 * 60 * 60 * 24 * 7);
        bestPace[0] = 0f;

        Query query = databaseReference
                .child("users")
                .child(user.getUid())
                .child("workouts")
                .orderByChild("date");
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                        Workout workout = ds.getValue(Workout.class);
                        if (workout != null) {
                            numWorkouts[0]++;
                            long date = workout.getDate();
                            float miles = workout.getDistanceMiles();
                            milesAllTime[0] += miles;
                            if (date >= oneWeekAgo) {
                                milesWeek[0] += miles;
                            }
                            int duration = workout.getDuration();
                            float pace = duration / miles;
                            if (pace > bestPace[0] && miles > 0.1) {
                                bestPace[0] = pace;
                            }
                        }
                    }
                }
                setTextViews();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    /**
     * Called from onDataChange for retrieving user info for TextViews.
     */
    private void setTextViews() {
        milesWeekTextView.setText(String.format(Locale.US, "%.1f", milesWeek[0]));
        milesTotalTextView.setText(String.format(Locale.US, "%.1f", milesAllTime[0]));
        String numWorkoutsString = String.valueOf(numWorkouts[0]);
        numWorkoutsTextView.setText(numWorkoutsString);
        bestPaceTextView.setText(UtilityFunctions.getPaceString(bestPace[0]));
    }

    /**
     * Populates the ListView.
     */
    private void initializeListView() {
        ArrayList<DrawerItem> drawerItemArrayList = new ArrayList<>();
        drawerItemAdapter = new DrawerItemAdapter(this, R.layout.drawer_list_item, drawerItemArrayList);
        mDrawerList.setAdapter(drawerItemAdapter);
        for (int i = 0; i < iconIdList.length; i++) {
            DrawerItem drawerItem = new DrawerItem(iconIdList[i], drawerOptions[i]);
            drawerItemAdapter.add(drawerItem);
        }
    }
}

// TODO: Improve looks of Nav Drawer
// TODO: Improve looks of main content

// TODO: Async load history activity from service
// TODO: Mess more with icon size/color