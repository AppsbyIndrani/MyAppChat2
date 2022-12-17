package com.example.myappchat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.media.Image;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.squareup.picasso.Picasso;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import static com.example.myappchat.DarkthemeActivity.sharedPref;

public class ProfileActivity extends AppCompatActivity {

    private String receiverUserID,Current_State,senderUserID,sendertoken,ID,sendername,rqstmsg="sent you a request",receivertoken;
    private CircularImageView userProfileImage;
    private ImageView userCoverImage;
    private TextView userProfileName,userProfileStatus;
    private Button sendMessageRequestButton,DeclineMessageRequestButton;
    private DatabaseReference UserRef,ChatRequestRef,ContactsRef,NotoficationRef,uRef;
    private FirebaseAuth auth;
    private String currentuserID;



    @Override
    protected void onCreate(Bundle savedInstanceState) {

        sharedPref = new SharedPref(this);
        if(sharedPref.loadNightModeState()==true) {
            setTheme(R.style.darktheme);
        }
        else setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        auth = FirebaseAuth.getInstance();
        try {
            currentuserID = auth.getCurrentUser().getUid();
        }catch (Exception e)
        {
            Toast.makeText(ProfileActivity.this,e.getMessage(),Toast.LENGTH_SHORT).show();
        }
        UserRef = FirebaseDatabase.getInstance().getReference().child("users");
        NotoficationRef = FirebaseDatabase.getInstance().getReference().child("Notifications");
        ChatRequestRef = FirebaseDatabase.getInstance().getReference().child("Chat Request");
        ContactsRef = FirebaseDatabase.getInstance().getReference().child("contacts");

        ID=auth.getUid();
        try {
            receiverUserID = getIntent().getExtras().get("visit_user_id").toString();
            senderUserID = auth.getCurrentUser().getUid();
            ID = auth.getUid();
        }catch (Exception e)
        {
            Toast.makeText(ProfileActivity.this,e.getMessage(),Toast.LENGTH_SHORT).show();
        }

        userProfileImage = (CircularImageView) findViewById(R.id.visit_profile_picture);
        userCoverImage=(ImageView)findViewById(R.id.visit_cover_picture);
        userProfileName = (TextView) findViewById(R.id.visit_user_name);
        userProfileStatus = (TextView) findViewById(R.id.visit_profile_status);
        sendMessageRequestButton = (Button) findViewById(R.id.send_message_request_button);
        DeclineMessageRequestButton = (Button) findViewById(R.id.decline_message_request_button);
        Current_State = "new";

        RetrieveUserInfo();
        try {
            UserRef.child(FirebaseAuth.getInstance().getUid()).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    sendertoken = snapshot.child("token").getValue().toString();
                    sendername = snapshot.child("name").getValue().toString();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }catch (Exception e)
        {
            Toast.makeText(ProfileActivity.this,e.getMessage(),Toast.LENGTH_SHORT);
        }


    }



    private void RetrieveUserInfo()
    {
        UserRef.child(receiverUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {
                if ((snapshot.exists()) && (snapshot.hasChild("name")) && (snapshot.hasChild("image")) && (snapshot.hasChild("status")) && (snapshot.hasChild("coverimage")))
                {
                    try {
                        String UserName = snapshot.child("name").getValue().toString();
                        String UserStatus = snapshot.child("status").getValue().toString();
                        String profileimg = snapshot.child("image").getValue().toString();
                        String coverImg = snapshot.child("coverimage").getValue().toString();
                        receivertoken = snapshot.child("token").getValue().toString();

                        userProfileName.setText(UserName);
                        userProfileStatus.setText(UserStatus);
                        Picasso.get().load(coverImg).into(userCoverImage);
                        Picasso.get().load(profileimg).into(userProfileImage);
                    }catch (Exception e)
                    {
                        Toast.makeText(ProfileActivity.this,e.getMessage(),Toast.LENGTH_SHORT);
                    }

                    ManageChatRequests();

                }

                else if ((snapshot.exists()) && (snapshot.hasChild("name")) && (snapshot.hasChild("image")) && (snapshot.hasChild("status")))
                {
                    try {
                        String UserName = snapshot.child("name").getValue().toString();
                        String UserStatus = snapshot.child("status").getValue().toString();
                        String profileimg = snapshot.child("image").getValue().toString();
                        receivertoken = snapshot.child("token").getValue().toString();

                        userProfileName.setText(UserName);
                        userProfileStatus.setText(UserStatus);
                        Picasso.get().load(profileimg).into(userProfileImage);
                    }catch (Exception e)
                    {
                        Toast.makeText(ProfileActivity.this,e.getMessage(),Toast.LENGTH_SHORT);
                    }
                    ManageChatRequests();

                }

                else if ((snapshot.exists()) && (snapshot.hasChild("name")) && (snapshot.hasChild("image")) && (snapshot.hasChild("coverimage")))
                {
                    try {
                        String UserName = snapshot.child("name").getValue().toString();
                        String profileimg = snapshot.child("image").getValue().toString();
                        String coverImg = snapshot.child("coverimage").getValue().toString();
                        receivertoken = snapshot.child("token").getValue().toString();

                        userProfileName.setText(UserName);
                        Picasso.get().load(profileimg).into(userProfileImage);
                        Picasso.get().load(coverImg).into(userCoverImage);
                    }catch (Exception e)
                    {
                        Toast.makeText(ProfileActivity.this,e.getMessage(),Toast.LENGTH_SHORT);
                    }
                    ManageChatRequests();

                }

                else if ((snapshot.exists()) && (snapshot.hasChild("name")) && (snapshot.hasChild("coverimage")))
                {
                    String UserName=snapshot.child("name").getValue().toString();
                    String coverImg=snapshot.child("coverimage").getValue().toString();
                    receivertoken=snapshot.child("token").getValue().toString();

                    userProfileName.setText(UserName);
                    Picasso.get().load(coverImg).into(userCoverImage);

                    ManageChatRequests();


                }

                else if ((snapshot.exists()) && (snapshot.hasChild("name")) && (snapshot.hasChild("status")) && (snapshot.hasChild("coverimage")))
                {
                    String UserName=snapshot.child("name").getValue().toString();
                    String UserStatus=snapshot.child("status").getValue().toString();
                    String coverImg=snapshot.child("coverimage").getValue().toString();
                    receivertoken=snapshot.child("token").getValue().toString();

                    userProfileName.setText(UserName);
                    userProfileStatus.setText(UserStatus);
                    Picasso.get().load(coverImg).into(userCoverImage);

                    ManageChatRequests();


                }

                else if ((snapshot.exists()) && (snapshot.hasChild("name")) && (snapshot.hasChild("status")))
                {

                    String UserName=snapshot.child("name").getValue().toString();
                    String UserStatus=snapshot.child("status").getValue().toString();
                    receivertoken=snapshot.child("token").getValue().toString();

                    userProfileName.setText(UserName);
                    userProfileStatus.setText(UserStatus);

                    ManageChatRequests();



                }
                else if ((snapshot.exists()) && (snapshot.hasChild("name")) && (snapshot.hasChild("image")))
                {
                    String UserName=snapshot.child("name").getValue().toString();
                    String profileimg=snapshot.child("image").getValue().toString();
                    receivertoken=snapshot.child("token").getValue().toString();

                    userProfileName.setText(UserName);
                    Picasso.get().load(profileimg).into(userProfileImage);

                    ManageChatRequests();


                }
                else
                {
                    String UserName=snapshot.child("name").getValue().toString();
                    receivertoken=snapshot.child("token").getValue().toString();

                    userProfileName.setText(UserName);
                    ManageChatRequests();

                }


            }
                @Override
                public void onCancelled (@NonNull DatabaseError databaseError){

                }

            });
    }

    private void ManageChatRequests()
    {

        if (!senderUserID.equals(receiverUserID))
        {
            sendMessageRequestButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v)
                {
                    sendMessageRequestButton.setEnabled(false);
                    if (Current_State.equals("new"))
                    {
                        SendChatRequest();
                    }
                    if (Current_State.equals("request_sent"))
                    {
                        CancelChatRequest();
                    }
                    if (Current_State.equals("request_received"))
                    {
                        AcceptChatRequest();
                    }
                    if (Current_State.equals("friends"))
                    {
                        RemoveSpecificContact();
                    }
                }
            });
        }
        else
        {

            sendMessageRequestButton.setVisibility(View.INVISIBLE);
        }

        ChatRequestRef.child(senderUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                if (dataSnapshot.hasChild(receiverUserID))
                {
                    String request_type=dataSnapshot.child(receiverUserID).child("request_type").getValue().toString();

                    if (request_type.equals("sent"))
                    {
                        Current_State="request_sent";
                        sendMessageRequestButton.setText("Cancel Chat Request");
                    }
                    else if (request_type.equals("received"))
                    {
                        Current_State="request_received";
                        sendMessageRequestButton.setText("Accept Chat Request");

                        DeclineMessageRequestButton.setVisibility(View.VISIBLE);
                        DeclineMessageRequestButton.setEnabled(true);

                        DeclineMessageRequestButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v)
                            {
                                CancelChatRequest();
                            }
                        });
                    }
                }
                else
                {
                    ContactsRef.child(senderUserID).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                        {
                            if (dataSnapshot.hasChild(receiverUserID))
                            {
                                Current_State="friends";
                                sendMessageRequestButton.setText("Remove this contact");
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {

            }
        });


    }

    private void RemoveSpecificContact()
    {
        ContactsRef.child(senderUserID).child(receiverUserID).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    ContactsRef.child(receiverUserID).child(senderUserID).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                sendMessageRequestButton.setEnabled(true);
                                Current_State = "new";
                                sendMessageRequestButton.setText("Send Message");

                                DeclineMessageRequestButton.setVisibility(View.INVISIBLE);
                                DeclineMessageRequestButton.setEnabled(false);
                            }
                        }
                    });
                }
            }
        });

    }

    private void CancelChatRequest()
    {
        ChatRequestRef.child(senderUserID).child(receiverUserID).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    ChatRequestRef.child(receiverUserID).child(senderUserID).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                sendMessageRequestButton.setEnabled(true);
                                Current_State = "new";
                                sendMessageRequestButton.setText("Send Message");

                                DeclineMessageRequestButton.setVisibility(View.INVISIBLE);
                                DeclineMessageRequestButton.setEnabled(false);
                            }
                        }
                    });
                }
            }
        });

    }

    private void SendChatRequest() 
    {

        ChatRequestRef.child(senderUserID).child(receiverUserID).child("request_type").setValue("sent").addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    ChatRequestRef.child(receiverUserID).child(senderUserID).child("request_type").setValue("received").addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                ChatRequestRef.child(receiverUserID).child(senderUserID).child("request_type").setValue("received").addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful())
                                        {

                                            HashMap<String,String> chatNotificationMap=new HashMap<>();
                                            chatNotificationMap.put("from",senderUserID);
                                            chatNotificationMap.put("type","request");

                                            NotoficationRef.child(receiverUserID).push().setValue(chatNotificationMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task)
                                                {
                                                    if (task.isSuccessful())
                                                    {
                                                        sendMessageRequestButton.setEnabled(true);
                                                        Current_State = "request_sent";
                                                        sendMessageRequestButton.setText("Cancel Chat Request");
                                                        sendNotification(sendername,rqstmsg,receivertoken);
                                                    }

                                                }
                                            });


                                        }
                                    }
                                });
                            }
                        }
                    });
                }
            }
        });
    }
        

    
    private void AcceptChatRequest()
    {
        ContactsRef.child(senderUserID).child(receiverUserID).child("contacts").setValue("saved").addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    ContactsRef.child(receiverUserID).child(senderUserID).child("contacts").setValue("saved").addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                ChatRequestRef.child(senderUserID).child(receiverUserID).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            ContactsRef.child(receiverUserID).child(senderUserID).child("contacts").setValue("saved").addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()) {
                                                        ChatRequestRef.child(senderUserID).child(receiverUserID).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task)
                                                            {
                                                                sendMessageRequestButton.setEnabled(true);
                                                                Current_State="friends";
                                                                sendMessageRequestButton.setText("Remove this contact");

                                                                DeclineMessageRequestButton.setVisibility(View.INVISIBLE);
                                                                DeclineMessageRequestButton.setEnabled(false);
                                                            }
                                                        });
                                                    }
                                                }
                                            });
                                        }
                                    }
                                });
                            }
                        }
                    });
                }
            }
        });

    }



    private void sendNotification(String name,String message,String token)
    {
        try {


            RequestQueue requestQueue = Volley.newRequestQueue(this);
            String url = "https://fcm.googleapis.com/fcm/send";
            JSONObject data = new JSONObject();
            data.put("title", name);
            data.put("body", message);
            JSONObject notificationdata=new JSONObject();
            notificationdata.put("notification",data);
            notificationdata.put("to",token);

            JsonObjectRequest request=new JsonObjectRequest(url, notificationdata, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response)
                {
                    Toast.makeText(ProfileActivity.this,"success",Toast.LENGTH_SHORT).show();

                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error)
                {

                    Toast.makeText(ProfileActivity.this,error.getLocalizedMessage(),Toast.LENGTH_SHORT).show();
                }
            }) {
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError
                {
                    HashMap<String,String> map=new HashMap<>();
                    String key="key=AAAAvuTMfXw:APA91bGpHF6dJ3tnNYRYQ-B271YYPBm4y1bsuTgkh9yed7tGwmr431GoC6nk5bobix9gWQab0-mnKpSAa-39PUwyGGmH3z2fOmX40TNH2blUgvakqyg0O5_XW_NukrMlA_lP9dDKxA6A";
                    map.put("Authorization",key);
                    map.put("content-Type","application/json");
                    return map;
                }
            };

            requestQueue.add(request);

        }catch (Exception e){
            Toast.makeText(ProfileActivity.this,e.getMessage(),Toast.LENGTH_SHORT);
        }

    }




}
