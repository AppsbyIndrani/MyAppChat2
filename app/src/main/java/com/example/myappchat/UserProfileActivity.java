package com.example.myappchat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.example.myappchat.DarkthemeActivity.sharedPref;

import java.util.Objects;

public class UserProfileActivity extends AppCompatActivity {

    private ImageView uimage,usercallimg,uservideocallimg;
    private CircularImageView cirimage;
    private TextView utextview1,utextview2;
    private String callernumber;
    private DatabaseReference usersRef;
    private String currentuserID,receiveruserID,rname,rimage,covimage,rstatus;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {

        sharedPref = new SharedPref(this);
        if(sharedPref.loadNightModeState()==true) {
            setTheme(R.style.darktheme);
        }
        else setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        InitializeFields();

        usersRef= FirebaseDatabase.getInstance().getReference().child("users");




        usersRef.child(receiveruserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {
                if (snapshot.exists())
                {

                    callernumber= Objects.requireNonNull(snapshot.child("userno").getValue()).toString();

                    if (snapshot.hasChild("coverimage") && snapshot.hasChild("status"))
                    {
                        covimage= Objects.requireNonNull(snapshot.child("coverimage").getValue()).toString();
                        rstatus= Objects.requireNonNull(snapshot.child("status").getValue()).toString();

                        utextview2.setText(rstatus);
                        Picasso.get().load(covimage).into(uimage);

                    }
                    else if (snapshot.hasChild("coverimage"))
                    {
                        covimage= Objects.requireNonNull(snapshot.child("coverimage").getValue()).toString();
                        Picasso.get().load(covimage).into(uimage);

                    }
                    else if (snapshot.hasChild("status"))
                    {

                        rstatus= Objects.requireNonNull(snapshot.child("status").getValue()).toString();
                        utextview2.setText(rstatus);

                    }
                    else
                    {
                        Toast.makeText(UserProfileActivity.this,"gg",Toast.LENGTH_SHORT);
                    }



                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        Picasso.get().load(rimage).placeholder(R.drawable.profile_image).into(cirimage);
        utextview1.setText(rname);


         if (rimage!=null) {
             cirimage.setOnClickListener(new View.OnClickListener() {
                 @Override
                 public void onClick(View v) {

                     Intent intent = new Intent(cirimage.getContext(), ImageviewerActivity.class);
                     intent.putExtra("url", rimage);
                     cirimage.getContext().startActivity(intent);

                 }
             });
         }
        usercallimg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {

                //Toast.makeText(UserProfileActivity.this,"userno is + :"+callernumber,Toast.LENGTH_SHORT).show();

                Intent callIntent = new Intent(Intent.ACTION_CALL);
                callIntent.setData(Uri.parse("tel:"+callernumber));

                if (ActivityCompat.checkSelfPermission(UserProfileActivity.this,
                        Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                startActivity(callIntent);



               /* try {
                    //Uri u= Uri.parse(callernumber);

                    Intent i= new Intent(Intent.ACTION_CALL);
                    i.setData(Uri.parse(callernumber));
                    startActivity(i);

                } catch (SecurityException s)
                {
                    Toast.makeText(UserProfileActivity.this,"Error ocurred",Toast.LENGTH_SHORT).show();
                }*/


            }
        });

        uservideocallimg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {


                Intent callIntent = new Intent(Intent.ACTION_CALL);
                callIntent.setData(Uri.parse("tel:"+callernumber));

                if (ActivityCompat.checkSelfPermission(UserProfileActivity.this,
                        Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                startActivity(callIntent);


                 /* Intent videointent=new Intent(UserProfileActivity.this,VideoActivity.class);
                  videointent.putExtra("username",rname);
                  videointent.putExtra("userprofilepic",rimage);
                  startActivity(videointent);*/
            }
        });



    }

    private void InitializeFields()
    {
        uimage=(ImageView)findViewById(R.id.coverprofilepicture);
        cirimage=(CircularImageView)findViewById(R.id.userprofileimage);
        utextview1=(TextView)findViewById(R.id.userproname);
        utextview2=(TextView)findViewById(R.id.userstatustext);
        usercallimg=(ImageView)findViewById(R.id.callimg);
        uservideocallimg=(ImageView)findViewById(R.id.videoview);

        receiveruserID=getIntent().getExtras().get("receiversID").toString();
        rname=getIntent().getExtras().get("receiversuname").toString();
        try {
            rimage = getIntent().getExtras().get("receiversproimage").toString();
        }catch (Exception e)
        {
            Toast.makeText(UserProfileActivity.this,e.getMessage(),Toast.LENGTH_SHORT).show();
        }

    }
}
