package com.example.myappchat;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mikhaellopez.circularimageview.CircularImageView;

import java.util.ArrayList;

public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.UsersViewHolder>
{

    Context context;


    public UsersAdapter()
    {

    }

    @NonNull
    @Override
    public UsersViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        return null;

    }

    @Override
    public void onBindViewHolder(@NonNull UsersViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return 0;
    }





    public class UsersViewHolder extends RecyclerView.ViewHolder {

        CircularImageView profileImage;
        ImageView onlinestatus;
        TextView userlastmsg,userName,timeoflstmsg;

        public UsersViewHolder(@NonNull View itemView)
        {

            super(itemView);
            profileImage=itemView.findViewById(R.id.users_profile_image);
            userName=itemView.findViewById(R.id.user_profile_name);
            userlastmsg=itemView.findViewById(R.id.last_msg);
            onlinestatus=itemView.findViewById(R.id.user_online_status);
            timeoflstmsg=itemView.findViewById(R.id.usertime);
        }
    }
}
