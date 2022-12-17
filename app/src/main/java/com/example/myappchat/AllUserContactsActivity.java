package com.example.myappchat;

import static com.example.myappchat.DarkthemeActivity.sharedPref;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Date;

public class AllUserContactsActivity extends AppCompatActivity {

    private RecyclerView rvallcontacts;
    private FirebaseUser currentUser;
    private FirebaseAuth auth;
    private String currentUserID,phonenumber,uname,uprofileimg,uonlinestatus,usersIDs;
    private DatabaseReference rootref,UserRef,contactsRef;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        sharedPref = new SharedPref(this);
        if(sharedPref.loadNightModeState()) {
            setTheme(R.style.darktheme);
        }
        else setTheme(R.style.AppTheme);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_user_contacts);

        rvallcontacts=findViewById(R.id.rvAllContacts);
        rootref= FirebaseDatabase.getInstance().getReference();
        auth=FirebaseAuth.getInstance();
        currentUser=auth.getCurrentUser();
        try {
            assert currentUser != null;
            currentUserID = currentUser.getUid();
            }
        catch (Exception e)
        {
            Toast.makeText(AllUserContactsActivity.this,e.getMessage(),Toast.LENGTH_LONG);
        }
        UserRef = FirebaseDatabase.getInstance().getReference().child("users");
        try {
            contactsRef = FirebaseDatabase.getInstance().getReference().child("contacts").child(currentUser.getUid());

        }catch (Exception e)
        {
            Toast.makeText(AllUserContactsActivity.this,e.getMessage(),Toast.LENGTH_SHORT);
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
                                Toast.makeText(AllUserContactsActivity.this, e.getMessage(), Toast.LENGTH_SHORT);
                            }
                        } else {
                            try {
                                uname = snapshot.child("name").getValue().toString();
                            }catch (Exception e)
                            {
                                Toast.makeText(AllUserContactsActivity.this, e.getMessage(), Toast.LENGTH_SHORT);
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
            Toast.makeText(AllUserContactsActivity.this,e.getMessage(),Toast.LENGTH_SHORT);
        }



        FirebaseRecyclerOptions<Contacts> options=new FirebaseRecyclerOptions.Builder<Contacts>().setQuery(contactsRef, Contacts.class).build();
        FirebaseRecyclerAdapter<Contacts, AlluserViewHolder> adapter=new FirebaseRecyclerAdapter<Contacts,AlluserViewHolder>(options)
        {

            @Override
            protected void onBindViewHolder(@NonNull final AlluserViewHolder holder, final int position, @NonNull Contacts model)
            {

                String senderId=FirebaseAuth.getInstance().getUid();
                String senderRoom=senderId + getRef(position).getKey();


                /*final String*/ usersIDs=getRef(position).getKey();
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

                            }catch (Exception e)
                            {
                                Toast.makeText(AllUserContactsActivity.this,e.getMessage(),Toast.LENGTH_SHORT);
                            }

                        }
                       /* else
                        {
                            holder.userStatus.setText("Tap to chat");
                        }*/
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
                                    retImage[0] = dataSnapshot.child("image").getValue().toString();
                                    Picasso.get().load(retImage[0]).placeholder(R.drawable.profile_image).into(holder.profileImage);
                                }


                                final String retName = dataSnapshot.child("name").getValue().toString();
                                final String retStatus = dataSnapshot.child("status").getValue().toString();
                                String usertoken = dataSnapshot.child("token").getValue().toString();
                                uonlinestatus = dataSnapshot.child("userState").child("State").getValue().toString();


                                if (uonlinestatus.equals("online")) {
                                    holder.onlinestatus.setVisibility(View.VISIBLE);
                                } else {
                                    holder.onlinestatus.setVisibility(View.INVISIBLE);
                                }


                                holder.userName.setText(retName);
                                holder.userStatus.setText(retStatus);

                                holder.itemView.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {

                                        Intent chatIntent = new Intent(AllUserContactsActivity.this, ChatActivity.class);
                                        chatIntent.putExtra("visit_user_id", usersIDs);
                                        chatIntent.putExtra("visit_user_name", retName);
                                        chatIntent.putExtra("visit_image", retImage[0]);
                                        chatIntent.putExtra("messagesendername", uname);
                                        chatIntent.putExtra("token", usertoken);
                                        startActivity(chatIntent);
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
                    Toast.makeText(AllUserContactsActivity.this,e.getMessage(),Toast.LENGTH_SHORT);
                }



            }

            @NonNull
            @Override
            public AllUserContactsActivity.AlluserViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i)
            {
                View view= LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.row_conversation,viewGroup, false);
                AlluserViewHolder viewHolder=new AlluserViewHolder(view);
                return viewHolder;
            }

        };

        rvallcontacts.setAdapter(adapter);
        try {
            adapter.startListening();
        }catch (Exception e)
        {
            Toast.makeText(this,e.getMessage(), Toast.LENGTH_SHORT).show();
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

}