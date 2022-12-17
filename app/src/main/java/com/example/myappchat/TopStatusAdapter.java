package com.example.myappchat;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.devlomi.circularstatusview.CircularStatusView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;
import omari.hamza.storyview.StoryView;
import omari.hamza.storyview.callback.StoryClickListeners;
import omari.hamza.storyview.model.MyStory;

public class TopStatusAdapter extends RecyclerView.Adapter<TopStatusAdapter.TopStatusViewHolder>
{

    Context context;
    ArrayList<UserStatus> userStatuses;
    String name1;


    public TopStatusAdapter(Context context,ArrayList<UserStatus> userStatuses)
    {
        this.context=context;
        this.userStatuses=userStatuses;
    }

    @NonNull
    @Override
    public TopStatusViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        View view= LayoutInflater.from(context).inflate(R.layout.item_status,parent,false);

        return new TopStatusViewHolder(view);

    }

    @Override
    public void onBindViewHolder(@NonNull TopStatusViewHolder holder, int position)
    {

        FirebaseDatabase.getInstance().getReference().child("users").child(FirebaseAuth.getInstance().getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot)
                    {
                        name1=snapshot.child("name").getValue().toString();

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

        UserStatus userStatus=userStatuses.get(position);
        String statususerid=userStatuses.get(position).getUserid();
        int currentposition=position;

        String currentuserid=FirebaseAuth.getInstance().getCurrentUser().getUid();
        Status lastStatus=userStatus.getStatuses().get(userStatus.getStatuses().size() - 1);
        Long timeofuploa=userStatus.getLastUpdated();
        Status status=new Status();
        Picasso.get().load(lastStatus.getImageurl()).into(holder.statusimage);

        holder.numofstatus.setPortionsCount(userStatus.getStatuses().size());


        holder.numofstatus.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v)
           {
               ArrayList<MyStory> myStories=new ArrayList<>();
               for (Status status : userStatus.getStatuses())
               {
                  myStories.add(new MyStory(status.getImageurl()));

               }

               new StoryView.Builder(((MainActivity)context).getSupportFragmentManager())
                       .setStoriesList(myStories)
                       .setStoryDuration(5000)
                       .setTitleText(userStatus.getName())
                       .setSubtitleText("")
                       .setTitleLogoUrl(userStatus.getProfileimage())
                       .setStoryClickListeners(new StoryClickListeners()
                       {

                           @Override
                           public void onDescriptionClickListener(int position)
                           {


                           }

                           @Override
                           public void onTitleIconClickListener(int position)
                           {


                           }
                       })
                       .build()
                       .show();


               }




       });



           if (currentuserid.equals(statususerid)) {
               holder.numofstatus.setOnLongClickListener(new View.OnLongClickListener() {
                   @Override
                   public boolean onLongClick(View v) {

                       View view = LayoutInflater.from(context).inflate(R.layout.status_delete, null);

                       TextView t1 = view.findViewById(R.id.delforstatus);
                       TextView t2 = view.findViewById(R.id.delcancelforstatus);
                       AlertDialog dialog = new AlertDialog.Builder(context)
                               .setTitle("Delete Status")
                               .setView(view)
                               .create();
                       t1.setOnClickListener(new View.OnClickListener() {
                           @Override
                           public void onClick(View v) {
                               removeitemview(userStatus);
                               FirebaseDatabase.getInstance().getReference().child("stories").child(currentuserid)
                                       .setValue(null);


                               dialog.dismiss();

                           }
                       });

                       t2.setOnClickListener(new View.OnClickListener() {
                           @Override
                           public void onClick(View v) {

                               dialog.dismiss();

                           }
                       });

                       dialog.show();
                       return false;
                   }
               });

           }
    }

    private void removeitemview(UserStatus userStatus)
    {
        int curpos=userStatuses.indexOf(userStatus);
        userStatuses.remove(curpos);
        notifyItemRemoved(curpos);
    }


    @Override
    public int getItemCount()
    {
        return userStatuses.size();
    }

    public class TopStatusViewHolder extends RecyclerView.ViewHolder
    {

        public CircleImageView statusimage;
        public CircularStatusView numofstatus;

        public TopStatusViewHolder(@NonNull View itemView)
        {
            super(itemView);
            statusimage=(CircleImageView)itemView.findViewById(R.id.statusimg);
            numofstatus=(CircularStatusView)itemView.findViewById(R.id.circular_status_view);



        }
    }


}
