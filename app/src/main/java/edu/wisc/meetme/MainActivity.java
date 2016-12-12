package edu.wisc.meetme;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.app.Fragment;
import android.app.FragmentManager;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;



import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import static edu.wisc.meetme.R.layout.activity_main;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        CalendarFragment.OnFragmentInteractionListener,
        MessageFragment.OnFragmentInteractionListener,
        FriendsFragment.OnFragmentInteractionListener,
        ProfileFragment.OnFragmentInteractionListener {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);


    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.

        FragmentManager manager = getFragmentManager();

        int id = item.getItemId();


        if (id == R.id.nav_camera) {
            manager.beginTransaction().replace(R.id.content_main, new MessageFragment()).commit();
//            MessageFragment messageFragment = new MessageFragment();
//            FragmentManager manager = getSupportFragmentManager();
//            manager.beginTransaction().replace(
//                    R.id.content_main,
//                    messageFragment,
//                    messageFragment.getTag()
//            ).commit();
        } else if (id == R.id.nav_gallery) {
            manager.beginTransaction().replace(R.id.content_main, new FriendsFragment()).commit();
//            FriendsFragment friendsFragment = new FriendsFragment();
//            FragmentManager manager = getSupportFragmentManager();
//            manager.beginTransaction().replace(
//                    R.id.content_main,
//                    friendsFragment,
//                    friendsFragment.getTag()
//            ).commit();
        } else if (id == R.id.nav_slideshow) {
            manager.beginTransaction().replace(R.id.content_main, new GmapFragment()).commit();
////            if (!sMapFragment.isAdded()) {
////                sFm.beginTransaction().add(R.id.map, sMapFragment).commit();
////            } else {
////                sFm.beginTransaction().show(sMapFragment).commit();
////            }
//            sFm.beginTransaction().replace(R.id.content_main, new GmapFragment()).commit();

        } else if (id == R.id.nav_manage) {
            manager.beginTransaction().replace(R.id.content_main, new ProfileFragment()).commit();
//            ProfileFragment profileFragment = new ProfileFragment();
//            FragmentManager manager = getSupportFragmentManager();
//            manager.beginTransaction().replace(
//                    R.id.content_main,
//                    profileFragment,
//                    profileFragment.getTag()
//            ).commit();

//            Intent i = new Intent(MainActivity.this, ProfileActivity.class);
//            startActivity(i);
        }  else if (id == R.id.nav_send) {
            manager.beginTransaction().replace(R.id.content_main, new CalendarFragment()).commit();
//            System.out.print("Calender selected");
//            CalendarFragment calendarFragment = new CalendarFragment();
//            FragmentManager manager = getSupportFragmentManager();
//            manager.beginTransaction().replace(
//                    R.id.content_main,
//                    calendarFragment,
//                    calendarFragment.getTag()
//            ).commit();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

}
