package edu.wisc.meetme;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import org.w3c.dom.Text;

public class ProfileActivity extends Activity {


    public void updateProfile(View view) {
        System.out.print("Button tapped.");
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

//        Intent intent = getIntent();
//
////        if (intent.getStringExtra(ChangeID.ID_MESSAGE) != null) {
//            String idMessage = intent.getStringExtra(ChangeID.ID_MESSAGE);
//            userID.setText(idMessage);
////        } else if (intent.getStringExtra(ChangePhone.PHONE_MESSAGE) != null) {
//            String phoneMessage = intent.getStringExtra(ChangePhone.PHONE_MESSAGE);
//            phoneNumber.setText(phoneMessage);
////        } else {
//            String interestMessage = intent.getStringExtra(ChangeInterest.INTEREST_MESSAGE);
//            interests.setText(interestMessage);
////        }
    }
}
