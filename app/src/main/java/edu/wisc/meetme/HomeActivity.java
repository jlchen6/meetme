package edu.wisc.meetme;

import android.*;
import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.View;

//<<<<<<< HEAD
//=======
import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.api.GoogleApiClient;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.StringTokenizer;

//public class HomeActivity extends Activity {


public class HomeActivity extends Activity implements LocationListener {

//    LocationManager locationManager;
//    String provider;
//    /**
//     * ATTENTION: This was auto-generated to implement the App Indexing API.
//     * See https://g.co/AppIndexing/AndroidStudio for more information.
//     */
//    private GoogleApiClient client = new GoogleApiClient.Builder(this).build();


    public void goToProfile(View view) {
        Intent i = new Intent(HomeActivity.this, ProfileActivity.class);
        startActivity(i);

    }

    public void goToMap(View view) {
        Intent i = new Intent(HomeActivity.this, MapsActivity.class);
        startActivity(i);

    }
//>>>>>>> refs/remotes/vperiyasamy/master

    ArrayList<User> testFriends;
    ArrayList<User> online = new ArrayList<User>();
    ArrayList<User> offline = new ArrayList<User>();
    int[] testPrefs = {0,1,1,1,0};
    User testme = new User(0,"me", testPrefs); //test User representing app user
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

//<<<<<<< HEAD
        //When the user gets online, should send a ping to the server telling it the user is online
    }

    //Code for sorting Friends
    private void friendsort(ArrayList<User> a){

        //First split up friends into online and offline
        for(int i = 0; i < a.size(); i++){
            User currfriend = a.get(i);
            //Check if currfriend is online
            //If currfriend is online, add them to online list
            if(currfriend.isOnline()){
                online.add(currfriend);
            }
            //else add them to the offline list
            else {
                offline.add(currfriend);
            }
        }

        //Next, order online friends by distance from user
        //Test code sets up stuff for sorting////////////////
        testme.testsetlocation(0);
        ////////////////////////////////////////////////////

        online = gpsSort(online);
        offline = alphaSort(offline);
//=======
//        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
//        // Get the default location service provider
//        provider = locationManager.getBestProvider(new Criteria(), false);
//        // Get the last known location of the device
//        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//            // TODO: Consider calling
//            //    ActivityCompat#requestPermissions
//            // here to request the missing permissions, and then overriding
//            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
//            //                                          int[] grantResults)
//            // to handle the case where the user grants the permission. See the documentation
//            // for ActivityCompat#requestPermissions for more details.
//            return;
//        }
//        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//            // TODO: Consider calling
//            //    ActivityCompat#requestPermissions
//            // here to request the missing permissions, and then overriding
//            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
//            //                                          int[] grantResults)
//            // to handle the case where the user grants the permission. See the documentation
//            // for ActivityCompat#requestPermissions for more details.
//            return;
//        }
//        Location location = locationManager.getLastKnownLocation(provider);
//
//        if (location != null) {
//            Log.i("Location Info", "Location achieved!");
//        } else {
//            Log.i("Location Info", "No location :(");
//        }
//
//        // ATTENTION: This was auto-generated to implement the App Indexing API.
//        // See https://g.co/AppIndexing/AndroidStudio for more information.
//        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }


    @Override
    protected void onResume() {
        super.onResume();

//        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//            // TODO: Consider calling
//            //    ActivityCompat#requestPermissions
//            // here to request the missing permissions, and then overriding
//            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
//            //                                          int[] grantResults)
//            // to handle the case where the user grants the permission. See the documentation
//            // for ActivityCompat#requestPermissions for more details.
//            return;
//        }
//        locationManager.requestLocationUpdates(provider, 400, 1, this);

    }

    @Override
    protected void onPause() {
        super.onPause();
//
//        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//            // TODO: Consider calling
//            //    ActivityCompat#requestPermissions
//            // here to request the missing permissions, and then overriding
//            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
//            //                                          int[] grantResults)
//            // to handle the case where the user grants the permission. See the documentation
//            // for ActivityCompat#requestPermissions for more details.
//            return;
//        }
//        locationManager.removeUpdates(this);

    }

    @Override
    public void onLocationChanged(Location location) {

        Double lat = location.getLatitude();
        Double lng = location.getLongitude();

        Log.i("Latitude", lat.toString());
        Log.i("Longitude", lng.toString());
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    public Action getIndexApiAction() {
        Thing object = new Thing.Builder()
                .setName("Home Page") // TODO: Define a title for the content shown.
                // TODO: Make sure this auto-generated URL is correct.
                .setUrl(Uri.parse("http://[ENTER-YOUR-URL-HERE]"))
                .build();
        return new Action.Builder(Action.TYPE_VIEW)
                .setObject(object)
                .setActionStatus(Action.STATUS_TYPE_COMPLETED)
                .build();
    }

    @Override
    public void onStart() {
        super.onStart();

//        // ATTENTION: This was auto-generated to implement the App Indexing API.
//        // See https://g.co/AppIndexing/AndroidStudio for more information.
//        client.connect();
//        AppIndex.AppIndexApi.start(client, getIndexApiAction());
    }

    @Override
    public void onStop() {
        super.onStop();

//        // ATTENTION: This was auto-generated to implement the App Indexing API.
//        // See https://g.co/AppIndexing/AndroidStudio for more information.
//        AppIndex.AppIndexApi.end(client, getIndexApiAction());
//        client.disconnect();
//>>>>>>> refs/remotes/vperiyasamy/master
    }

    //Sorts the given list based on distance from app-user
    private ArrayList<User> gpsSort(ArrayList<User> users){
        ArrayList<User> sortedList = new ArrayList<User>();
        int udis, qdis;
        boolean uadded;
        for(User u: users){
            uadded = false;
            if(sortedList.size() == 0){
                sortedList.add(u);
            }
            else {
                //compare u's distance from user with the distances within sortedList
                for (User q : sortedList) {

                    //Once gps is properly coded, will have to change how distance is calculated///
                    udis = Math.abs(u.getLocation() - testme.getLocation());
                    qdis = Math.abs(q.getLocation() - testme.getLocation());
                    ///////////////////////////////////////////////////////////////////////////////

                    //If u's distance is less than q's, insert u before q in the sortedList.
                    if(udis < qdis){
                        sortedList.add(sortedList.indexOf(q),u);
                        uadded = true;
                        break;
                    }
                }
                //If u's distance wasn't less than any user within sortedList, just add to end.
                if(!uadded)
                    sortedList.add(u);
            }
        }
        return sortedList;
    }

    //Needs work. Will look at alphabetic sorts later.
    private ArrayList<User> alphaSort(ArrayList<User> users){
        ArrayList<User> sortedList = new ArrayList<User>();
        ArrayList<String> names = new ArrayList<String>();
        boolean uadded;
        for(User u: users){
            uadded = false;
            if(sortedList.size() == 0){
                sortedList.add(u);
            }
            else {
                //compare first letter of u's name with first letter of users in sortedList
                for (User q : sortedList) {
                    names.add(u.getName());
                    names.add(q.getName());
                    Collections.sort(names);
                    if(names.get(0).equals(u.getName())){
                        sortedList.add(sortedList.indexOf(q),u);
                        uadded = true;
                        break;
                    }
                }
                //If u's distance wasn't less than any user within sortedList, just add to end.
                if(!uadded)
                    sortedList.add(u);
            }
        }
        return sortedList;
    }

}
