package com.example.myappchat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
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

import static com.example.myappchat.DarkthemeActivity.sharedPref;

public class RequestsActivity extends AppCompatActivity {


    private RecyclerView myRequestsList;

    private DatabaseReference ChatRequestRef,UsersRef,contactsRef;
    private FirebaseAuth auth;
    private String currentUserID,rqstsendername,type;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        sharedPref = new SharedPref(this);
        if(sharedPref.loadNightModeState()==true) {
            setTheme(R.style.darktheme);
        }
        else setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_requests);

        auth=FirebaseAuth.getInstance();
        currentUserID=auth.getCurrentUser().getUid();
        ChatRequestRef= FirebaseDatabase.getInstance().getReference().child("Chat Request");
        UsersRef=FirebaseDatabase.getInstance().getReference().child("users");
        contactsRef=FirebaseDatabase.getInstance().getReference().child("contacts");

        myRequestsList=(RecyclerView) findViewById(R.id.chat_requests_list);
        myRequestsList.setLayoutManager(new LinearLayoutManager(this));

       UsersRef.child(currentUserID).addValueEventListener(new ValueEventListener() {
           @Override
           public void onDataChange(@NonNull DataSnapshot snapshot)
           {
               rqstsendername=snapshot.child("name").getValue().toString();
           }

           @Override
           public void onCancelled(@NonNull DatabaseError error) {

           }
       });


    }

    @Override
    protected void onStart()
    {
        super.onStart();

        FirebaseRecyclerOptions<Contacts> options=new FirebaseRecyclerOptions.Builder<Contacts>().setQuery(ChatRequestRef.child(currentUserID),Contacts.class).build();
        FirebaseRecyclerAdapter<Contacts, RequestViewHolder> adapter=new FirebaseRecyclerAdapter<Contacts, RequestViewHolder>(options)
        {
            @Override
            protected void onBindViewHolder(@NonNull final RequestViewHolder holder, int position, @NonNull Contacts model)
            {
                holder.itemView.findViewById(R.id.request_accept_btn).setVisibility(View.VISIBLE);
                holder.itemView.findViewById(R.id.request_cancel_btn).setVisibility(View.VISIBLE);


                final String list_user_id = getRef(position).getKey();

                DatabaseReference getTypeRef = getRef(position).child("request_type").getRef();

                getTypeRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                    {
                        if (dataSnapshot.exists()) {
                            try {
                                 type = dataSnapshot.getValue().toString();
                            }catch (Exception e)
                            {
                                Toast.makeText(RequestsActivity.this,e.getMessage(),Toast.LENGTH_SHORT).show();
                            }

                            if (type.equals("received"))
                            {
                                try {
                                    UsersRef.child(list_user_id).addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            if (dataSnapshot.hasChild("image")) {

                                                final String requestProfileImage = dataSnapshot.child("image").getValue().toString();

                                                Picasso.get().load(requestProfileImage).into(holder.profileImage);

                                            }

                                            final String requestUserStatus = dataSnapshot.child("status").getValue().toString();
                                            final String requestUserName = dataSnapshot.child("name").getValue().toString();

                                            holder.userStatus.setText("Wants to connect with you");
                                            holder.userName.setText(requestUserName);

                                            holder.AcceptButton.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    contactsRef.child(currentUserID).child(list_user_id).child("contactname").setValue(requestUserName);

                                                    contactsRef.child(currentUserID).child(list_user_id).child("Contact").setValue("saved").addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {

                                                            if (task.isSuccessful()) {
                                                                contactsRef.child(list_user_id).child(currentUserID).child("contactname").setValue(rqstsendername);
                                                                contactsRef.child(list_user_id).child(currentUserID).child("Contact").setValue("saved").addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                    @Override
                                                                    public void onComplete(@NonNull Task<Void> task) {

                                                                        if (task.isSuccessful()) {
                                                                            ChatRequestRef.child(currentUserID).child(list_user_id).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                @Override
                                                                                public void onComplete(@NonNull Task<Void> task) {

                                                                                    if (task.isSuccessful()) {
                                                                                        ChatRequestRef.child(list_user_id).child(currentUserID).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                            @Override
                                                                                            public void onComplete(@NonNull Task<Void> task) {

                                                                                                if (task.isSuccessful()) {
                                                                                                    Toast.makeText(RequestsActivity.this, "Request Accepted", Toast.LENGTH_SHORT).show();
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
                                            });

                                            holder.CancelButton.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    ChatRequestRef.child(currentUserID).child(list_user_id).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {

                                                            if (task.isSuccessful()) {
                                                                ChatRequestRef.child(list_user_id).child(currentUserID).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                    @Override
                                                                    public void onComplete(@NonNull Task<Void> task) {

                                                                        if (task.isSuccessful()) {
                                                                            Toast.makeText(RequestsActivity.this, "Request cancelled", Toast.LENGTH_SHORT).show();
                                                                        }
                                                                    }
                                                                });
                                                            }
                                                        }
                                                    });

                                                }
                                            });


                                        }


                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                        }
                                    });
                                }catch (Exception e)
                                {
                                    Toast.makeText(RequestsActivity.this,e.getMessage(),Toast.LENGTH_SHORT).show();
                                }
                            }

                            else if (type.equals("sent"))
                            {
                                final Button request_sent_btn=holder.itemView.findViewById(R.id.request_accept_btn);
                                request_sent_btn.setText("Request sent");

                                holder.itemView.findViewById(R.id.request_cancel_btn).setVisibility(View.INVISIBLE);

                                try {
                                    UsersRef.child(list_user_id).addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            if (dataSnapshot.hasChild("image")) {

                                                final String requestProfileImage = dataSnapshot.child("image").getValue().toString();


                                                Picasso.get().load(requestProfileImage).into(holder.profileImage);

                                            }

                                            final String requestUserStatus = dataSnapshot.child("status").getValue().toString();
                                            final String requestUserName = dataSnapshot.child("name").getValue().toString();

                                            holder.userStatus.setText("You have sent request to " + requestUserName);
                                            holder.userName.setText(requestUserName);


                                            request_sent_btn.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    CharSequence options[] = new CharSequence[]
                                                            {
                                                                    "Cancel Chat Request"
                                                            };

                                                    AlertDialog.Builder builder = new AlertDialog.Builder(RequestsActivity.this);
                                                    builder.setTitle("Already sent a request");

                                                    builder.setItems(options, new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialog, int i) {

                                                            if (i == 0) {
                                                                ChatRequestRef.child(currentUserID).child(list_user_id).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                    @Override
                                                                    public void onComplete(@NonNull Task<Void> task) {

                                                                        if (task.isSuccessful()) {
                                                                            ChatRequestRef.child(list_user_id).child(currentUserID).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                @Override
                                                                                public void onComplete(@NonNull Task<Void> task) {

                                                                                    if (task.isSuccessful()) {
                                                                                        Toast.makeText(RequestsActivity.this, "You have cancelled the chat request.", Toast.LENGTH_SHORT).show();
                                                                                    }
                                                                                }
                                                                            });
                                                                        }
                                                                    }
                                                                });
                                                            }
                                                        }
                                                    });

                                                    builder.show();
                                                }
                                            });
                                        }


                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                        }
                                    });
                                }catch (Exception e)
                                {
                                    Toast.makeText(RequestsActivity.this,e.getMessage(),Toast.LENGTH_SHORT).show();
                                }

                            }

                        }
                    }


                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }


            @NonNull
            @Override
            public RequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i)
            {
                View view=LayoutInflater.from(parent.getContext()).inflate(R.layout.row_conversation,parent,false);
                RequestViewHolder viewHolder=new RequestViewHolder(view);
                return viewHolder;
            }
        };


        myRequestsList.setAdapter(adapter);
        adapter.startListening();
    }


    public static  class RequestViewHolder extends RecyclerView.ViewHolder
    {
        TextView userName,userStatus;
        CircularImageView profileImage;
        Button AcceptButton,CancelButton;

        public RequestViewHolder(@NonNull View itemView)
        {
            super(itemView);

            userName=itemView.findViewById(R.id.user_profile_name);
            userStatus=itemView.findViewById(R.id.last_msg);
            profileImage=itemView.findViewById(R.id.users_profile_image);
            AcceptButton=itemView.findViewById(R.id.request_accept_btn);
            CancelButton=itemView.findViewById(R.id.request_cancel_btn);
        }
    }

}
