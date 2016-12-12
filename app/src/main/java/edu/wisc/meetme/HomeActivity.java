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
import android.widget.ArrayAdapter;

//<<<<<<< HEAD
//=======
import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.api.GoogleApiClient;

import org.json.JSONArray;
import org.json.JSONException;

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

    //public void goToMap(View view) {
    //    Intent i = new Intent(HomeActivity.this, MapsActivity.class);
    //    startActivity(i);
    //}
//>>>>>>> refs/remotes/vperiyasamy/master

    ArrayList<User> testFriends;
    ArrayList<String> onlineNames;
    ArrayList<User> allFriends = new ArrayList<User>();
    ArrayList<User> online = new ArrayList<User>();
    ArrayList<User> offline = new ArrayList<User>();
    int[] testPrefs = {0,1,1,1,0};
    ArrayAdapter<String> onlineAdapter;
    User testme = new User(0,"me", "me", true); //test User representing app user
    boolean startup;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        ///TEST CODE DELETE LATER//////////////////
        onlineNames.add("Billy Bob");
        onlineNames.add("Abby Smith");
        onlineNames.add("Johnny Appleseed");
        /////////////////////////////////////////////
        startup = true;
        refreshFriends(getCurrentFocus());
        onlineAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, R.id.friendsActive, onlineNames);

//<<<<<<< HEAD
        //When the user gets online, should send a ping to the server asking for list of active friends
        //refreshFriends(getCurrentFocus());

    }

    //Queries server for recommendation, once receives info, displays popup with info.
    //Query should return:
    // 1. Name of restaurant
    // 2. Location
    public void getRecommendation(View v){
        //Query server, receive JSON with restaurant info

        //Filter through info

        //Display info in popup

    }

    //Button to set self as available. Query server to update user availability
    public void setAvailable(View v){
        //Access app user's online status and set as active

        //Send query to server to update user info with online status

        //Send refresh request as well for the heck of it?
        refreshFriends(getCurrentFocus());

    }

    //Method to refresh the active friends list. Should ping server for updated list.
    public void refreshFriends(View v){
        JSONArray serverList;
        //Call to server here
        //Use main user's id/phone number, make HTTP request, getting back a JSON
        serverList = new JSONArray(); //Replace with function call for a server query!!//////////////////////////////////////////////
        //Returns JSONArray where every third element are
        // 1. ID (phone number),
        // 2. First Name,
        // 3. Last Name,
        // 4. Active/Inactive Status

        //If refreshing on startup, create all new user objects
        if(startup){
            for (int i = 0; i < serverList.length(); i += 4) {
                try {
                    int id = (int) serverList.get(i);
                    String[] name = {serverList.getString(i + 1), serverList.getString(i + 2)};
                    boolean active = (boolean) serverList.get(i + 3);
                    User curr = new User(id, name[0], name[1], active);
                    allFriends.add(curr);
                    if(active){
                        online.add(curr);
                    }
                    else{
                        offline.add(curr);
                    }
                } catch (JSONException e) {
                    System.out.println("Error in JSON decoding");
                    e.printStackTrace();
                }
            }
            startup = false;
        }
        //Otherwise, check whether user is already created, and if so, edit online status.
        else{
            for(int i = 0; i < serverList.length(); i+= 4){
                try {
                    int currindex = 0;
                    int id = (int) serverList.get(i);
                    String[] name = {serverList.getString(i + 1), serverList.getString(i + 2)};
                    boolean active = (boolean) serverList.get(i + 3);
                    boolean usercreated = false;
                    for(int j = 0; j < allFriends.size(); j++){
                        if(id == allFriends.get(j).getID()){
                            usercreated = true;
                            currindex = j;
                        }
                    }
                    //If the user isn't already created, add them to the correct lists
                    if(!usercreated){
                        User curr = new User(id, name[0], name[1], active);
                        allFriends.add(curr);
                        if(active){
                            online.add(curr);
                        }
                        else{
                            offline.add(curr);
                        }
                    }
                    //If the user is created, edit their online status if needed and move to correct list
                    else{
                        User curr = allFriends.get(currindex);
                        boolean lastState = curr.isOnline();
                        //Check if the user's last online status matches the current online status
                        if(lastState != active){
                            //If lastState was online, need to remove user from online friends and place them in offline friends
                            if(lastState){
                                for(User user : online){
                                    if(user.getID() == curr.getID()){
                                        online.remove(user);
                                        offline.add(user);
                                        user.setOnline(active);
                                    }
                                }
                            }
                            //Similarly, if last status was offline, need to move to online list
                            else{
                                for(User user : offline){
                                    if(user.getID() == curr.getID()){
                                        offline.remove(user);
                                        online.add(user);
                                        user.setOnline(active);
                                    }
                                }
                            }
                        }
                    }

                } catch (JSONException e) {
                    System.out.println("Error in JSON decoding");
                    e.printStackTrace();
                }
            }
        }

        online = gpsSort(online);
        offline = alphaSort(offline);

    }


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
        double udis, qdis;
        boolean uadded;
        for(User u: users){
            uadded = false;
            if(sortedList.size() == 0){
                sortedList.add(u);
            }
            else {
                //compare u's distance from user with the distances within sortedList
                for (User q : sortedList) {
                    Location uloc = u.getLocation();
                    Location meloc = testme.getLocation(); ///Replace later with reference to main user/////////////
                    Location qloc = q.getLocation();
                    //Once gps is properly coded, will have to change how distance is calculated///
                    udis = getDistanceFromLatLonInKm(uloc.getLatitude(), uloc.getLongitude(),
                            meloc.getLatitude(), meloc.getLongitude());
                    qdis = getDistanceFromLatLonInKm(qloc.getLatitude(), qloc.getLongitude(),
                            meloc.getLatitude(), meloc.getLongitude());
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

    public double getDistanceFromLatLonInKm(double lat1,double lon1, double lat2, double lon2) {
        int R = 6371; // Radius of the earth in km
        double dLat = deg2rad(lat2-lat1);  // deg2rad below
        double dLon = deg2rad(lon2-lon1);
        double a =
                Math.sin(dLat/2) * Math.sin(dLat/2) +
                        Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) *
                                Math.sin(dLon/2) * Math.sin(dLon/2)
                ;
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        double d = R * c; // Distance in km
        return d;
    }

    public double deg2rad(double deg) {
        return deg * (Math.PI/180);
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
                if(!uadded)
                    sortedList.add(u);
            }
        }
        return sortedList;
    }

}
