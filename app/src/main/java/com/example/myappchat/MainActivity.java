package com.example.myappchat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.cooltechworks.views.shimmer.ShimmerRecyclerView;
import com.devlomi.circularstatusview.CircularStatusView;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Objects;

import static com.example.myappchat.DarkthemeActivity.sharedPref;

public class MainActivity extends AppCompatActivity {

    private FirebaseUser currentUser;
    private FirebaseAuth auth;
    private FloatingActionButton fabbtnContacts;
    private BottomNavigationView navigationView;
    private String currentUserID,phonenumber,uname,uprofileimg,uonlinestatus;
    private ArrayList mobileArray;
    private DatabaseReference rootref,UserRef,contactsRef,storiesref;
    private ShimmerRecyclerView statusList;
    private ShimmerRecyclerView userrecyclerlist;
    private  String usersIDs;
    private String saveCurrentTime,saveCurrentDate,statusid;
    private TopStatusAdapter statusAdapter;
    private ProgressDialog dialog;
    private ArrayList<UserStatus> userstatuses;



    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        sharedPref = new SharedPref(this);
        if(sharedPref.loadNightModeState()==true) {
            setTheme(R.style.darktheme);
        }
        else setTheme(R.style.AppTheme);

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        FirebaseRemoteConfig mFirebaseremoteconfig=FirebaseRemoteConfig.getInstance();
        FirebaseRemoteConfigSettings configSettings=new FirebaseRemoteConfigSettings.Builder()
                .setMinimumFetchIntervalInSeconds(3600).build();
        mFirebaseremoteconfig.setConfigSettingsAsync(configSettings);

       mFirebaseremoteconfig.fetchAndActivate().addOnSuccessListener(new OnSuccessListener<Boolean>() {
           @Override
           public void onSuccess(Boolean aBoolean)
           {

               Toast.makeText(MainActivity.this,"success",Toast.LENGTH_SHORT).show();

           }
       });

        currentUser=FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser == null)
        {
            sendUserToLoginActivity();
        }else
        {

            VerifyUserExistence();

        }


        dialog=new ProgressDialog(this);
        dialog.setMessage("Uploading status...");
        dialog.setCancelable(false);

        rootref=FirebaseDatabase.getInstance().getReference();
        auth=FirebaseAuth.getInstance();
        statusid=FirebaseAuth.getInstance().getUid();

        try {

            currentUserID = currentUser.getUid();
        }catch (Exception e)
        {
            Toast.makeText(MainActivity.this,e.getMessage(),Toast.LENGTH_SHORT);
        }


        storiesref=FirebaseDatabase.getInstance().getReference().child("stories");

        UserRef = FirebaseDatabase.getInstance().getReference().child("users");

           try {
               contactsRef = FirebaseDatabase.getInstance().getReference().child("contacts").child(currentUser.getUid());

           }catch (Exception e)
           {
               Toast.makeText(MainActivity.this,e.getMessage(),Toast.LENGTH_SHORT);
           }

        InitializeFields();

           fabbtnContacts.setOnClickListener(new View.OnClickListener() {
               @Override
               public void onClick(View v)
               {
                 Intent intent=new Intent(MainActivity.this,AllUserContactsActivity.class);
                 startActivity(intent);

               }
           });


           try {
               FirebaseMessaging.getInstance().getToken().addOnSuccessListener(new OnSuccessListener<String>() {
                @Override
                public void onSuccess(String token) {
                    try {
                        currentUserID = FirebaseAuth.getInstance().getUid();
                        HashMap<String, Object> map = new HashMap<>();
                        map.put("token", token);
                        rootref.child("users").child(currentUserID).updateChildren(map);
                        Toast.makeText(MainActivity.this, token, Toast.LENGTH_SHORT);


                    } catch (Exception e) {
                        Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }

                }
            });

        }catch (Exception e)
        {
            Toast.makeText(this,e.getMessage(),Toast.LENGTH_SHORT).show();
        }




   try {
       UserRef.child(currentUserID).addValueEventListener(new ValueEventListener() {
           @Override
           public void onDataChange(@NonNull DataSnapshot snapshot) {
               if (snapshot.exists()) {
                   if (snapshot.child("image").exists()) {
                       try {
                           uname = snapshot.child("name").getValue().toString();
                           uprofileimg = snapshot.child("image").getValue().toString();
                       } catch (Exception e) {
                           Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT);
                       }
                   } else {
                       try {
                           uname = snapshot.child("name").getValue().toString();
                       }catch (Exception e)
                       {
                           Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT);
                       }
                   }


               }

           }


           @Override
           public void onCancelled(@NonNull DatabaseError error) {

           }
       });
   }catch (Exception e)
   {
       Toast.makeText(MainActivity.this,e.getMessage(),Toast.LENGTH_SHORT);
   }




        navigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @SuppressLint("NonConstantResourceId")
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item)
            {
                switch (item.getItemId())
                {
                    case R.id.main_status_option:
                        Intent intent=new Intent();
                        intent.setType("image/*");
                        intent.setAction(Intent.ACTION_GET_CONTENT);
                        startActivityForResult(intent,75);
                        break;

                }
                return false;
            }
        });


        rootref.child("stories").addValueEventListener(new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {
                if(snapshot.exists())
                {
                    userstatuses.clear();
                    for (DataSnapshot storySnapshot : snapshot.getChildren())
                    {
                        UserStatus status=new UserStatus();
                        status.setName(storySnapshot.child("name").getValue(String.class));
                        status.setUserid(storySnapshot.child("userstatusid").getValue(String.class));
                        status.setProfileimage(storySnapshot.child("profileimage").getValue(String.class));
                        try {

                            status.setLastUpdated(storySnapshot.child("lastUpdated").getValue(Long.class));
                        }catch(Exception e){
                            Toast.makeText(MainActivity.this,e.getMessage(),Toast.LENGTH_SHORT);
                        }

                        ArrayList<Status> statuses=new ArrayList<>();

                        for (DataSnapshot statusSnapshot : storySnapshot.child("statuses").getChildren())
                        {
                            Status samplestatus=statusSnapshot.getValue(Status.class);
                            statuses.add(samplestatus);
                        }
                        status.setStatuses(statuses);

                        userstatuses.add(status);


                    }
                    statusList.hideShimmerAdapter();
                    statusAdapter.notifyDataSetChanged();
                }



            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        FirebaseRecyclerOptions<Contacts> options=new FirebaseRecyclerOptions.Builder<Contacts>().setQuery(contactsRef, Contacts.class).build();
        FirebaseRecyclerAdapter<Contacts, AlluserViewHolder> adapter=new FirebaseRecyclerAdapter<Contacts, AlluserViewHolder>(options)
        {

            @Override
            protected void onBindViewHolder(@NonNull final AlluserViewHolder holder,@SuppressLint("RecyclerView") int position, @NonNull Contacts model)
            {

                String senderId=FirebaseAuth.getInstance().getUid();
                String senderRoom=senderId + getRef(position).getKey();


                /*final String*/ usersIDs=getRef(position).getKey();
                    //getRef(position).getKey();
                final String[] retImage = {"default_image"};

                rootref.child("Chats").child(senderRoom).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot)
                    {
                        if (snapshot.exists()) {
                            try {

                                String lastmsg = snapshot.child("lastMsg").getValue(String.class);
                                long time = snapshot.child("lastmsgtime").getValue(Long.class);
                                @SuppressLint("SimpleDateFormat") SimpleDateFormat dateFormat = new SimpleDateFormat("hh:mm a");

                                holder.userTime.setText(dateFormat.format(new Date(time)));

                                holder.userStatus.setText(lastmsg);
                            }catch (Exception e)
                            {
                                Toast.makeText(MainActivity.this,e.getMessage(),Toast.LENGTH_SHORT);
                            }

                        }
                        else
                        {
                            holder.userStatus.setText("Tap to chat");
                        }


                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });




                try {
                    UserRef.child(usersIDs).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                            if (dataSnapshot.exists()) {
                                if (dataSnapshot.hasChild("image")) {
                                    retImage[0] = Objects.requireNonNull(dataSnapshot.child("image").getValue()).toString();
                                    Picasso.get().load(retImage[0]).placeholder(R.drawable.profile_image).into(holder.profileImage);
                                }



                                final String retName = Objects.requireNonNull(dataSnapshot.child("name").getValue()).toString();
                                final String retStatus = dataSnapshot.child("status").getValue().toString();
                                String usertoken = dataSnapshot.child("token").getValue().toString();
                                uonlinestatus = dataSnapshot.child("userState").child("State").getValue().toString();


                                if (uonlinestatus.equals("online")) {
                                    holder.onlinestatus.setVisibility(View.VISIBLE);
                                } else {
                                    holder.onlinestatus.setVisibility(View.INVISIBLE);
                                }


                                holder.userName.setText(retName);


                                holder.itemView.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {

                                        usersIDs=getRef(position).getKey();

                                        Intent chatIntent = new Intent(MainActivity.this, ChatActivity.class);
                                        chatIntent.putExtra("visit_user_id", usersIDs);
                                        chatIntent.putExtra("visit_user_name", retName);
                                        chatIntent.putExtra("visit_image", retImage[0]);
                                        chatIntent.putExtra("messagesendername", uname);
                                        chatIntent.putExtra("token", usertoken);
                                        startActivity(chatIntent);
                                        Toast.makeText(MainActivity.this,"User is: " +usersIDs,Toast.LENGTH_SHORT).show();
                                    }
                                });


                            }


                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }catch (Exception e)
                {
                    Toast.makeText(MainActivity.this,e.getMessage(),Toast.LENGTH_SHORT);
                }



            }

            @NonNull
            @Override
            public AlluserViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i)
            {
                View view= LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.row_conversation,viewGroup, false);
                AlluserViewHolder viewHolder=new AlluserViewHolder(view);
                return viewHolder;
            }

        };

        userrecyclerlist.setAdapter(adapter);
        try {

            adapter.startListening();
        }catch (Exception e)
        {
            Toast.makeText(this,e.getMessage(), Toast.LENGTH_SHORT);
        }
        userrecyclerlist.hideShimmerAdapter();

    }

    private void InitializeFields()
    {
        Calendar calendar=Calendar.getInstance();

        @SuppressLint("SimpleDateFormat") SimpleDateFormat currentDate=new SimpleDateFormat("MMM dd, yyyy");
        saveCurrentDate=currentDate.format(calendar.getTime());
        @SuppressLint("SimpleDateFormat") SimpleDateFormat currentTime=new SimpleDateFormat("hh:mm a");
        saveCurrentTime=currentTime.format(calendar.getTime());

        navigationView=(BottomNavigationView)findViewById(R.id.bottomNavigationView);
        userrecyclerlist=(ShimmerRecyclerView)findViewById(R.id.users_recycler_list);
        fabbtnContacts=(FloatingActionButton)findViewById(R.id.fabbtnContact);

        statusList=(ShimmerRecyclerView)findViewById(R.id.statuslist);


        userstatuses=new ArrayList<>();

        statusAdapter=new TopStatusAdapter(this,userstatuses);


        LinearLayoutManager linearLayoutManager=new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(RecyclerView.HORIZONTAL);
        statusList.setLayoutManager(linearLayoutManager);

        statusList.setAdapter(statusAdapter);
        statusList.showShimmerAdapter();
        userrecyclerlist.showShimmerAdapter();



        // userrecyclerlist.setLayoutManager(new LinearLayoutManager(this));

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
    {

        super.onActivityResult(requestCode, resultCode, data);

        if (data != null) {

            if (data.getData() != null)
            {
                dialog.show();

                FirebaseStorage storage=FirebaseStorage.getInstance();
                Date date=new Date();
                StorageReference reference=storage.getReference().child("status").child(date.getTime() + "");

                reference.putFile(data.getData()).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task)
                    {
                        if (task.isSuccessful())
                        {
                            reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri)
                                {
                                    UserStatus userstatus=new UserStatus();
                                    userstatus.setName(uname);
                                    userstatus.setProfileimage(uprofileimg);
                                    userstatus.setLastUpdated(date.getTime());
                                    userstatus.setUserid(statusid);

                                    HashMap<String,Object> obj=new HashMap<>();
                                    obj.put("name",userstatus.getName());
                                    obj.put("profileimage",userstatus.getProfileimage());
                                    obj.put("userstatusid",userstatus.getUserid());
                                    obj.put("lastUpdated",userstatus.getLastUpdated());

                                    String imageUrl=uri.toString();

                                    Status status=new Status(imageUrl, userstatus.getLastUpdated());
                                    try {
                                        rootref.child("stories").child(FirebaseAuth.getInstance().getUid()).updateChildren(obj);
                                        rootref.child("stories").child(FirebaseAuth.getInstance().getUid()).child("statuses").push().setValue(status);
                                    }catch (Exception e)
                                    {
                                        Toast.makeText(MainActivity.this,e.getMessage(),Toast.LENGTH_SHORT);
                                    }
                                    dialog.dismiss();

                                }
                            });
                        }

                    }
                });

            }
        }
    }

    public static class AlluserViewHolder extends RecyclerView.ViewHolder
    {
        TextView userName,userStatus,userTime;
        CircularImageView profileImage;
        ImageView onlinestatus;

        public AlluserViewHolder(@NonNull View itemView)
        {
            super(itemView);
            userName=itemView.findViewById(R.id.user_profile_name);
            userStatus=itemView.findViewById(R.id.last_msg);
            profileImage=itemView.findViewById(R.id.users_profile_image);
            userTime=itemView.findViewById(R.id.usertime);
            onlinestatus=itemView.findViewById(R.id.user_online_status);

        }
    }





    @Override
    protected void onStart()
    {
        super.onStart();
        updateUserStatus("online");
    }

    @Override
    protected void onResume()
    {

        super.onResume();

        currentUser=auth.getCurrentUser();

        if (currentUser != null)
        {
            updateUserStatus("online");
        }

    }

    @Override
    protected void onPause()
    {
        super.onPause();

        currentUser=auth.getCurrentUser();

        if (currentUser != null)
        {
            updateUserStatus("offline");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        currentUser=auth.getCurrentUser();

        if (currentUser != null)
        {
            updateUserStatus("offline");
        }
    }

    private void VerifyUserExistence()
    {
        currentUser=FirebaseAuth.getInstance().getCurrentUser();
        try {
            currentUserID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        }catch (Exception e)
        {
            Toast.makeText(MainActivity.this,e.getMessage(),Toast.LENGTH_SHORT).show();
        }
        FirebaseDatabase.getInstance().getReference().child("users").child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if ((dataSnapshot.child("name").exists()))
                {
                    Toast.makeText(MainActivity.this, "welcome", Toast.LENGTH_SHORT);

                }
                else
                {
                    sendUserToSettingsActivity();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }


    private void sendUserToLoginfirstActivity()
    {

        Intent loginIntent=new Intent(MainActivity.this,LoginActivity.class);
        startActivity(loginIntent);

    }

    private void sendUserToLoginActivity()
    {

     Intent loginIntent=new Intent(MainActivity.this,LoginActivity.class);
     loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
     startActivity(loginIntent);
     finish();

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {


        getMenuInflater().inflate(R.menu.options_menu,menu);
        MenuItem item=menu.findItem(R.id.main_search_option);

        SearchView searchView=(SearchView)item.getActionView();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                processsearch(s);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                processsearch(s);
                return false;
            }
        });


        return  super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item)
    {
         super.onOptionsItemSelected(item);


        if (item.getItemId() == R.id.main_settings_option)
         {

               sendUserToSettingsActivity();
         }

        if (item.getItemId() == R.id.main_requests_option)
        {

            sendUserTORequestActivity();

        }

        if (item.getItemId() == R.id.main_friends_option)
        {

             sendUserToFindFriendsActivity();
        }


        if (item.getItemId() == R.id.main_sign_out_option)
        {
            auth.signOut();
            sendUserToLoginActivity();
        }

       /* if (item.getItemId() == R.id.main_search_option)
        {
            SearchView searchView=(SearchView)item.getActionView();

            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String s)
                {

                   processsearch(s);
                    return false;
                }
                @Override
                public boolean onQueryTextChange(String s)
                {
                    processsearch(s);
                    return false;
                }
            });

        }*/

        return true;
    }

    private void processsearch(String s)
    {
        FirebaseRecyclerOptions<Contacts> options=new FirebaseRecyclerOptions.Builder<Contacts>()
                .setQuery(contactsRef.orderByChild("contactname").startAt(s).endAt(s+"\uf8ff"), Contacts.class).build();

        FirebaseRecyclerAdapter<Contacts, AlluserViewHolder> adapter=new FirebaseRecyclerAdapter<Contacts, AlluserViewHolder>(options)
        {

            @Override
            protected void onBindViewHolder(@NonNull final AlluserViewHolder holder, final int position, @NonNull Contacts model)
            {

                String senderId=FirebaseAuth.getInstance().getUid();
                String senderRoom=senderId + getRef(position).getKey();

                final String usersIDs=getRef(position).getKey();
                final String[] retImage = {"default_image"};

                rootref.child("Chats").child(senderRoom).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot)
                    {
                        if (snapshot.exists()) {
                            try {
                                String lastmsg = snapshot.child("lastMsg").getValue(String.class);
                                long time = snapshot.child("lastmsgtime").getValue(Long.class);
                                SimpleDateFormat dateFormat = new SimpleDateFormat("hh:mm a");

                                holder.userTime.setText(dateFormat.format(new Date(time)));

                                holder.userStatus.setText(lastmsg);
                            }catch(Exception e){
                                Toast.makeText(MainActivity.this,e.getMessage(),Toast.LENGTH_SHORT);
                            }

                        }
                        else
                        {
                            holder.userStatus.setText("Tap to chat");
                        }


                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });



                UserRef.child(usersIDs).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                    {

                        if (dataSnapshot.exists())
                        {
                            if (dataSnapshot.hasChild("image"))
                            {
                                retImage[0] =dataSnapshot.child("image").getValue().toString();
                                Picasso.get().load(retImage[0]).placeholder(R.drawable.profile_image).into(holder.profileImage);
                            }

                            try {


                                final String retName = dataSnapshot.child("name").getValue().toString();
                                final String retStatus = dataSnapshot.child("status").getValue().toString();
                                uonlinestatus = dataSnapshot.child("userState").child("State").getValue().toString();


                                if (uonlinestatus.equals("online")) {
                                    holder.onlinestatus.setVisibility(View.VISIBLE);
                                } else {
                                    holder.onlinestatus.setVisibility(View.INVISIBLE);
                                }


                                holder.userName.setText(retName);


                                holder.itemView.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        Intent chatIntent = new Intent(MainActivity.this, ChatActivity.class);
                                        chatIntent.putExtra("visit_user_id", usersIDs);
                                        chatIntent.putExtra("visit_user_name", retName);
                                        chatIntent.putExtra("visit_image", retImage[0]);
                                        startActivity(chatIntent);
                                    }
                                });
                            }catch (Exception e)
                            {
                                Toast.makeText(MainActivity.this,e.getMessage(),Toast.LENGTH_SHORT).show();
                            }
                        }


                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError)
                    {

                    }
                });




            }

            @NonNull
            @Override
            public AlluserViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i)
            {
                View view= LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.row_conversation,viewGroup, false);
                AlluserViewHolder viewHolder=new AlluserViewHolder(view);
                return viewHolder;
            }

        };
        adapter.startListening();
        userrecyclerlist.setAdapter(adapter);

    }


    private void sendUserTORequestActivity()
    {
       Intent requestintent=new Intent(MainActivity.this,RequestsActivity.class);
       startActivity(requestintent);

    }

    private void sendUserToFindFriendsActivity()
    {
        Intent frndsIntent = new Intent(MainActivity.this, FindFriendsActivity.class);
        startActivity(frndsIntent);
    }

    private void sendUserToSettingsActivity()
    {

            Intent settingIntent = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(settingIntent);

    }


    private void updateUserStatus(String state)
    {


        HashMap<String,Object> onlineStateMap=new HashMap<>();
        onlineStateMap.put("time", saveCurrentTime);
        onlineStateMap.put("date", saveCurrentDate);
        onlineStateMap.put("State",state);
        try {
            currentUserID = auth.getCurrentUser().getUid();
            rootref.child("users").child(currentUserID).child("userState").updateChildren(onlineStateMap);
        }catch (Exception e)
        {
            Toast.makeText(MainActivity.this,e.getMessage(),Toast.LENGTH_SHORT);
        }

    }




}
