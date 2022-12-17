package com.example.myappchat;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;


public class LoginActivity extends AppCompatActivity {


    private TextView logintext;
    private FirebaseAuth auth;
    Button loginbtn;
    ImageView loginimage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        InitializeFields();

      /* if ((FirebaseAuth.getInstance().getCurrentUser()) != null)
        {
            Intent intent=new Intent(LoginActivity.this,MainActivity.class);
            startActivity(intent);
            finish();
        }*/

        loginbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                sendUserToPhoneActivity();
            }
        });

    }



    private void sendUserToPhoneActivity()
    {
        Intent phoneintent=new Intent(LoginActivity.this,PhoneLoginActivity.class);
        startActivity(phoneintent);

    }

    private void InitializeFields()
    {
        loginimage=(ImageView)findViewById(R.id.login_image);

        loginbtn=(Button)findViewById(R.id.phone_login_button);

        logintext=(TextView)findViewById(R.id.login_using);

    }



}
