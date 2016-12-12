package edu.wisc.meetme;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

/**
 * Created by Yaphet on 11/12/16.
 */

public class LoginActivity extends Activity {



    public void goToHome(View view) {
        Intent i = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(i);
    }

    public void goToRegister(View view) {
        Intent i = new Intent(LoginActivity.this, RegisterActivity.class);
        startActivity(i);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Permanent storage, access level, app only
        SharedPreferences sharedPreferences = this.getSharedPreferences("edu.wisc.meetme", Context.MODE_PRIVATE);

        // Save the data to local memory
        sharedPreferences.edit().putString("Username", "Android").apply();

        // Get the data back from local memory
        String userName = sharedPreferences.getString("Username", ""); // "" menas default value

        // Test
        Toast.makeText(getApplicationContext(), userName, Toast.LENGTH_LONG).show();
    }
}
