package com.example.myappchat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.squareup.picasso.Picasso;

import static com.example.myappchat.DarkthemeActivity.sharedPref;

public class FindFriendsActivity extends AppCompatActivity {

    private RecyclerView FindFriendsRecyclerList;
    private DatabaseReference UserRef;
    private String curuser;
    private FirebaseAuth mauth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        sharedPref = new SharedPref(this);
        if(sharedPref.loadNightModeState()==true) {
            setTheme(R.style.darktheme);
        }
        else setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_friends);

        UserRef = FirebaseDatabase.getInstance().getReference().child("users");
        mauth = FirebaseAuth.getInstance();
        curuser=mauth.getCurrentUser().getUid();


        FindFriendsRecyclerList=(RecyclerView)findViewById(R.id.find_friends_recycler_list);
        FindFriendsRecyclerList.setLayoutManager(new LinearLayoutManager(this));



    }

    @Override
    protected void onStart()
    {
        super.onStart();

        FirebaseRecyclerOptions<Contacts> options=new FirebaseRecyclerOptions.Builder<Contacts>().setQuery(UserRef, Contacts.class).build();

        FirebaseRecyclerAdapter<Contacts, FindFriendViewHolder> adapter=new FirebaseRecyclerAdapter<Contacts, FindFriendViewHolder>(options)
        {
            @Override
            protected void onBindViewHolder(@NonNull FindFriendViewHolder holder, @SuppressLint("RecyclerView") int position, @NonNull Contacts model)
            {
                holder.userName.setText(model.getName());
                holder.userStatus.setText(model.getStatus());
                Picasso.get().load(model.getImage()).placeholder(R.drawable.profile_image).into(holder.profileImage);

                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v)
                    {
                        String visit_user_id=getRef(position).getKey();

                        if (!curuser.equals(visit_user_id)) {

                            Intent profileIntent = new Intent(FindFriendsActivity.this, ProfileActivity.class);
                            profileIntent.putExtra("visit_user_id", visit_user_id);
                            startActivity(profileIntent);
                        }
                    }
                });


            }

            @NonNull
            @Override
            public FindFriendViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i)
            {
                View view= LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.row_conversation,viewGroup, false);
                FindFriendViewHolder viewHolder=new FindFriendViewHolder(view);
                return viewHolder;
            }
        };

        FindFriendsRecyclerList.setAdapter(adapter);
        adapter.startListening();
    }

    public static class FindFriendViewHolder extends RecyclerView.ViewHolder
    {
        TextView userName,userStatus;
        CircularImageView profileImage;
        View myview;

        public FindFriendViewHolder(@NonNull View itemView)
        {
            super(itemView);
            userName=itemView.findViewById(R.id.user_profile_name);
            userStatus=itemView.findViewById(R.id.last_msg);
            profileImage=itemView.findViewById(R.id.users_profile_image);
            myview=itemView.findViewById(R.id.view1);
            myview.setVisibility(View.INVISIBLE);


        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {

        getMenuInflater().inflate(R.menu.find_friends_menu,menu);
         MenuItem item=menu.findItem(R.id.findfriends_search_option);

        SearchView searchView=(SearchView)item.getActionView();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                processFindFriendssearch(s);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                processFindFriendssearch(s);
                return false;
            }
        });

        return  super.onCreateOptionsMenu(menu);
    }


    private void processFindFriendssearch(String s)
    {
        FirebaseRecyclerOptions<Contacts> options=new FirebaseRecyclerOptions.Builder<Contacts>()
                .setQuery(UserRef.orderByChild("name").startAt(s).endAt(s+"\uf8ff"), Contacts.class).build();

        FirebaseRecyclerAdapter<Contacts, FindFriendViewHolder> adapter=new FirebaseRecyclerAdapter<Contacts, FindFriendViewHolder>(options)
        {
            @Override
            protected void onBindViewHolder(@NonNull FindFriendViewHolder holder, @SuppressLint("RecyclerView") int position, @NonNull Contacts model)
            {
                holder.userName.setText(model.getName());
                holder.userStatus.setText(model.getStatus());
                Picasso.get().load(model.getImage()).placeholder(R.drawable.profile_image).into(holder.profileImage);

                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v)
                    {
                        String visit_user_id=getRef(position).getKey();

                        if (!curuser.equals(visit_user_id)) {

                            Intent profileIntent = new Intent(FindFriendsActivity.this, ProfileActivity.class);
                            profileIntent.putExtra("visit_user_id", visit_user_id);
                            startActivity(profileIntent);
                            Toast.makeText(FindFriendsActivity.this,"User is: " +visit_user_id,Toast.LENGTH_SHORT).show();
                        }
                    }
                });


            }

            @NonNull
            @Override
            public FindFriendViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i)
            {
                View view= LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.row_conversation,viewGroup, false);
                FindFriendViewHolder viewHolder=new FindFriendViewHolder(view);
                return viewHolder;
            }
        };

        FindFriendsRecyclerList.setAdapter(adapter);
        adapter.startListening();

    }
}
