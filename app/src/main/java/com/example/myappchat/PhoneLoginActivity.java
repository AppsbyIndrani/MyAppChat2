package com.example.myappchat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.hbb20.CountryCodePicker;
import com.mukesh.OtpView;

import java.util.concurrent.TimeUnit;

public class PhoneLoginActivity extends AppCompatActivity {

    private Button sendVerificationCodeButton,VerifyButton;
    private EditText InputPhoneNumber;
    private FirebaseAuth auth;


    private OtpView otpView;
    String verificationCode;
    CountryCodePicker ccp;

    private ImageView phoneimage;

    private ProgressDialog loadingBar;

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks callbacks;
    private String mVerificationId;
    private  PhoneAuthProvider.ForceResendingToken mResendToken;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_login);

        auth=FirebaseAuth.getInstance();



        InitializeFields();

        ccp.registerCarrierNumberEditText(InputPhoneNumber);

        InputPhoneNumber.requestFocus();

        getSupportActionBar().hide();
        otpView.requestFocus();


        loadingBar=new ProgressDialog(this);


        sendVerificationCodeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String cc=ccp.getSelectedCountryCodeWithPlus();
                String phonenumber=cc + InputPhoneNumber.getText().toString();

                if (TextUtils.isEmpty(phonenumber))
                {
                    Toast.makeText(PhoneLoginActivity.this,"phone number is required",Toast.LENGTH_SHORT).show();
                }
                else
                {


                    loadingBar.setTitle("Phone verification");
                    loadingBar.setMessage("Please wait while we are authenticate your account");
                    loadingBar.setCanceledOnTouchOutside(false);
                    loadingBar.show();

                    PhoneAuthProvider.getInstance().verifyPhoneNumber(
                            phonenumber,        // Phone number to verify
                            60,                 // Timeout duration
                            TimeUnit.SECONDS,   // Unit of timeout
                            PhoneLoginActivity.this,               // Activity (for callback binding)
                            callbacks);
                }
            }


        });

        VerifyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                sendVerificationCodeButton.setVisibility(View.INVISIBLE);
                InputPhoneNumber.setVisibility(View.INVISIBLE);
                try {
                    verificationCode = otpView.getText().toString();
                }catch (Exception e)
                {
                    Toast.makeText(PhoneLoginActivity.this,e.getMessage(),Toast.LENGTH_SHORT);
                }

                if (TextUtils.isEmpty(verificationCode))
                {
                    Toast.makeText(PhoneLoginActivity.this,"please write your verification code",Toast.LENGTH_SHORT).show();
                }
                else {

                    loadingBar.setTitle("Verification code");
                    loadingBar.setMessage("Please wait while we are authenticate your account");
                    loadingBar.setCanceledOnTouchOutside(false);
                    loadingBar.show();


                    PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId,verificationCode);
                    signInWithPhoneAuthCredential(credential);

                }
            }
        });

        callbacks=new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                signInWithPhoneAuthCredential(phoneAuthCredential);
            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {
                loadingBar.dismiss();
                Toast.makeText(PhoneLoginActivity.this, "Invalid phone number,please enter correct phone number with your country code", Toast.LENGTH_SHORT).show();
                sendVerificationCodeButton.setVisibility(View.VISIBLE);
                InputPhoneNumber.setVisibility(View.VISIBLE);

                VerifyButton.setVisibility(View.INVISIBLE);
                otpView.setVisibility(View.INVISIBLE);

            }

            public void onCodeSent(@NonNull String verificationId,
                                   @NonNull PhoneAuthProvider.ForceResendingToken token) {

                // Save verification ID and resending token so we can use them later
                mVerificationId = verificationId;
                mResendToken = token;

                loadingBar.dismiss();

                Toast.makeText(PhoneLoginActivity.this, "code has been sent", Toast.LENGTH_SHORT).show();

                sendVerificationCodeButton.setVisibility(View.INVISIBLE);
                InputPhoneNumber.setVisibility(View.INVISIBLE);
                ccp.setVisibility(View.INVISIBLE);

                VerifyButton.setVisibility(View.VISIBLE);
                otpView.setVisibility(View.VISIBLE);

                InputMethodManager imm=(InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                try {
                    imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
                }catch (Exception e)
                {
                    Toast.makeText(PhoneLoginActivity.this,e.getMessage(),Toast.LENGTH_SHORT);
                }
                otpView.requestFocus();



            }

        };





    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        auth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful())
                        {
                            String currentUserID=auth.getCurrentUser().getUid();

                                    loadingBar.dismiss();
                                    sendUserToMainActivity();
                                    Toast.makeText(PhoneLoginActivity.this,"You are loggedIn successfully",Toast.LENGTH_SHORT).show();
                        }
                        else
                        {
                            String message=task.getException().toString();
                            Toast.makeText(PhoneLoginActivity.this,"Error:" + message,Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void sendUserToMainActivity()
    {
        Intent ProfileIntent=new Intent(PhoneLoginActivity.this,MainActivity.class);
        startActivity(ProfileIntent);
        finishAffinity();
    }

    private void InitializeFields()
    {

        VerifyButton=(Button)findViewById(R.id.verify_button);
        InputPhoneNumber=(EditText)findViewById(R.id.phone_number_input);
        otpView=(OtpView)findViewById(R.id.otp_view);
        ccp=(CountryCodePicker)findViewById(R.id.countrycode);
        phoneimage=(ImageView)findViewById(R.id.phoneimageView);
        sendVerificationCodeButton=(Button)findViewById(R.id.send_ver_code_button);

    }
}
