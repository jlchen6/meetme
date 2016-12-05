package edu.wisc.meetme;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.apache.http.NameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
//import org.apache.commons.compress.utils.IOUtils;

import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.Toast;
import android.app.Activity;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;

/**
 * Created by Yaphet on 11/12/16.
 *
 * populated by Vish 11/30/16
 */

public class RegisterActivity extends Activity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        Button registerButton = (Button)findViewById(R.id.register);
        registerButton.setOnClickListener(new View.OnClickListener() {
                                           public void onClick(View v) {
                                               //Store
                                               String[ ] aStr = new String[4] ;
                                               aStr[0] = ((EditText)findViewById(R.id.phoneno)).getText().toString();
                                               aStr[1] = ((EditText)findViewById(R.id.email)).getText().toString();
                                               aStr[2] = ((EditText)findViewById(R.id.firstname)).getText().toString();
                                               aStr[3] = ((EditText)findViewById(R.id.lastname)).getText().toString();

                                               if (!aStr[0].isEmpty() && !aStr[1].isEmpty() && !aStr[2].isEmpty() && !aStr[3].isEmpty())
                                               {
                                                   //Execute register request
                                                   httpRegister hR = new httpRegister();
                                                   hR.execute(aStr);
                                               }
                                               else
                                               {
                                                   // DISPLAY ERROR MESSAGE TO USER THAT FIELDS ARE INCOMPLETE
                                               }
                                           }
                                       }
        );
    }


    protected class httpRegister extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... strs) {
            String reply = null;
            String temp=""; //capture acknowledgement from server, if any

            //Construct an HTTP POST
            HttpClient httpclient = new DefaultHttpClient();
            HttpPost registerUser = new HttpPost("http://meetmeece454.appspot.com/registeruser");

            // Values to be sent from android app to server
            ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();

            // "tag" is the name of the text form on the webserver
            // "value" is the value that the client is submitting to the server
            // These two are specified by the server. The cilent side program must respect.
            nameValuePairs.add(new BasicNameValuePair("phone", strs[0]));
            nameValuePairs.add(new BasicNameValuePair("email", strs[1]));
            nameValuePairs.add(new BasicNameValuePair("first", strs[2]));
            nameValuePairs.add(new BasicNameValuePair("last", strs[3]));

            try {
                UrlEncodedFormEntity httpEntity = new UrlEncodedFormEntity(nameValuePairs);
                registerUser.setEntity(httpEntity);

                //Execute HTTP POST
                HttpResponse response = httpclient.execute(registerUser);
                //Capture acknowledgement from server
                // In this demo app, the server returns "Update" if the tag already exists;
                // Otherwise, the server returns "New"
                temp = EntityUtils.toString(response.getEntity());
            }
            catch (ClientProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                System.out.println("HTTP IO Exception.");
                e.printStackTrace();
            }


            // Decompose the server's acknowledgement into a JSON array
            try {
                JSONArray jsonArray = new JSONArray(temp);
                reply = jsonArray.getString(0);

            } catch (JSONException e) {
                System.out.println("Error in JSON decoding");
                e.printStackTrace();
            }

            return reply;
        }

        // Process the server's acknowledgement
        @Override
        protected void onPostExecute(String res) {

            if (res.equalsIgnoreCase("Success")) {
                Toast.makeText(getApplicationContext(),
                        "Registered Successfully!", Toast.LENGTH_SHORT).show();
            }
            else {
                Toast.makeText(getApplicationContext(),
                        "Registration unsuccessful. Please try again later", Toast.LENGTH_SHORT).show();
            }
            // Clean the text field
            ((EditText)findViewById(R.id.phoneno)).setText("");
            ((EditText)findViewById(R.id.email)).setText("");
            ((EditText)findViewById(R.id.firstname)).setText("");
            ((EditText)findViewById(R.id.lastname)).setText("");
        }
    }

}
