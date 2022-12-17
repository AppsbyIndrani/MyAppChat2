package com.example.myappchat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.UploadTask;
import com.google.firebase.storage.StorageReference;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;
import java.util.Objects;

import static com.example.myappchat.DarkthemeActivity.sharedPref;

import org.w3c.dom.Text;

public class SettingsActivity extends AppCompatActivity {

    private Button UpdateAccountSettings;
    private EditText username;
    private EditText userstatus;
    private ProgressDialog loadingBar;
    private CircularImageView userprofileImage;
    private CardView cardView;
    private ImageView coverimg;
    private TextView text;
    private FirebaseAuth auth;
    private String currentUserID;
    private String downloadurl,coverurl;
    private DatabaseReference rootRef;
    private StorageReference UserProfileImageRef,coverImageRef;
    private Uri resultUri,uri,coverUri,pageuri;
    private static final int GalleryPic = 1,CoverPic=2;
    private String setUserName,setUserStatus,phoneno,retrieveUserName,retrieveStatus, retrieveimg,retrievecoverimg;
    private final int REQUEST_STORAGE = 111;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        sharedPref = new SharedPref(this);
        if(sharedPref.loadNightModeState()) {
            setTheme(R.style.darktheme);
        }
        else setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        auth=FirebaseAuth.getInstance();
        currentUserID= Objects.requireNonNull(auth.getCurrentUser()).getUid();

        rootRef= FirebaseDatabase.getInstance().getReference();


        UserProfileImageRef=FirebaseStorage.getInstance().getReference().child("Profile Images");
        coverImageRef=FirebaseStorage.getInstance().getReference().child("Cover Images");



        InitializeFields();


        userprofileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {

                if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE) !=
                        PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(SettingsActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_STORAGE);
                } else {
                    SelectImage();
                }


            }
        });

        coverimg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {

                if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE) !=
                        PackageManager.PERMISSION_GRANTED)
                {
                    ActivityCompat.requestPermissions(SettingsActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_STORAGE);
                } else {
                    SelectCoverImage();
                }

            }
        });


        UpdateAccountSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                UpdateAccountSettings();

            }
        });

        cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {

              Intent newintent=new Intent(SettingsActivity.this,DarkthemeActivity.class);
              startActivity(newintent);

            }
        });

        RetrieveUserInfo();
    }

    private void SelectCoverImage()
    {

        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
        startActivityForResult(intent, CoverPic);

    }


    private void SelectImage() {

        Intent coverintent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
        startActivityForResult(coverintent, GalleryPic);

    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GalleryPic)
        {

            if (resultCode == RESULT_OK && data != null) {

                if (data.getData() != null) {

                    uri = data.getData();
                    CropImage.activity()
                            .setGuidelines(CropImageView.Guidelines.ON)
                            .setAspectRatio(1, 1)
                            .start(this);


                }

            }

        }


            if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
                CropImage.ActivityResult result = CropImage.getActivityResult(data);
                if (resultCode == RESULT_OK) {

                    userprofileImage.setImageURI(result.getUri());
                    resultUri = result.getUri();


                }
            }

            if (requestCode == CoverPic) {
                if (resultCode == RESULT_OK && data != null) {

                    if (data.getData() != null)
                    {

                        coverimg.setImageURI(data.getData());
                        coverUri = data.getData();

                    }
                }
            }
    }


    private void RetrieveUserInfo()
    {
        rootRef.child("users").child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {
                if ((snapshot.exists()) && (snapshot.hasChild("name")) && (snapshot.hasChild("image")) && (snapshot.hasChild("status")) && (snapshot.hasChild("coverimage")))
                {
                     try {

                         retrieveUserName = snapshot.child("name").getValue().toString();
                         retrieveStatus = snapshot.child("status").getValue().toString();
                         retrieveimg = snapshot.child("image").getValue().toString();
                         retrievecoverimg = snapshot.child("coverimage").getValue().toString();
                     }catch (Exception e)
                     {
                         Toast.makeText(SettingsActivity.this,e.getMessage(),Toast.LENGTH_SHORT);
                     }

                        username.setText(retrieveUserName);
                    userstatus.setText(retrieveStatus);
                    Picasso.get().load(retrievecoverimg).into(coverimg);
                    Picasso.get().load(retrieveimg).into(userprofileImage);

                }

                else if ((snapshot.exists()) && (snapshot.hasChild("name")) && (snapshot.hasChild("image")) && (snapshot.hasChild("status")))
                {
                     try {
                         retrieveUserName = snapshot.child("name").getValue().toString();
                         retrieveStatus = snapshot.child("status").getValue().toString();
                         retrieveimg = snapshot.child("image").getValue().toString();
                     }catch (Exception e)
                     {
                         Toast.makeText(SettingsActivity.this,e.getMessage(),Toast.LENGTH_SHORT);
                     }

                    username.setText(retrieveUserName);
                    userstatus.setText(retrieveStatus);
                    Picasso.get().load(retrieveimg).into(userprofileImage);

                }

                else if ((snapshot.exists()) && (snapshot.hasChild("name")) && (snapshot.hasChild("image")) && (snapshot.hasChild("coverimage")))
                {
                    try {
                        retrieveUserName = snapshot.child("name").getValue().toString();
                        retrieveimg = snapshot.child("image").getValue().toString();
                        retrievecoverimg = snapshot.child("coverimage").getValue().toString();
                    }catch (Exception e)
                    {
                        Toast.makeText(SettingsActivity.this,e.getMessage(),Toast.LENGTH_SHORT);
                    }

                    username.setText(retrieveUserName);
                    Picasso.get().load(retrieveimg).into(userprofileImage);
                    Picasso.get().load(retrievecoverimg).into(coverimg);

                }

                else if ((snapshot.exists()) && (snapshot.hasChild("name")) && (snapshot.hasChild("coverimage")))
                {
                    try {
                         retrieveUserName = Objects.requireNonNull(snapshot.child("name").getValue()).toString();
                        retrievecoverimg = snapshot.child("coverimage").getValue().toString();
                    }catch (Exception e)
                    {
                        Toast.makeText(SettingsActivity.this,e.getMessage(),Toast.LENGTH_SHORT);
                    }

                    username.setText(retrieveUserName);
                    Picasso.get().load(retrievecoverimg).into(coverimg);

                }

                else if ((snapshot.exists()) && (snapshot.hasChild("name")) && (snapshot.hasChild("status")) && (snapshot.hasChild("coverimage")))
                {
                    try {
                         retrieveUserName = snapshot.child("name").getValue().toString();
                         retrieveStatus = snapshot.child("status").getValue().toString();
                         retrievecoverimg = snapshot.child("coverimage").getValue().toString();
                    }catch (Exception e)
                    {
                        Toast.makeText(SettingsActivity.this,e.getMessage(),Toast.LENGTH_SHORT);
                    }

                    username.setText(retrieveUserName);
                    userstatus.setText(retrieveStatus);
                    Picasso.get().load(retrievecoverimg).into(coverimg);

                }

                else if ((snapshot.exists()) && (snapshot.hasChild("name")) && (snapshot.hasChild("status")))
                {
                    try {
                        String retrieveUserName = snapshot.child("name").getValue().toString();
                        String retrieveStatus = snapshot.child("status").getValue().toString();
                        username.setText(retrieveUserName);
                        userstatus.setText(retrieveStatus);
                    }catch (Exception e)
                    {
                        Toast.makeText(SettingsActivity.this,e.getMessage(),Toast.LENGTH_SHORT);
                    }

                }
                else if ((snapshot.exists()) && (snapshot.hasChild("name")) && (snapshot.hasChild("image")))
                {
                    try {
                         retrieveUserName = snapshot.child("name").getValue().toString();
                         retrieveimg = snapshot.child("image").getValue().toString();
                    }catch (Exception e)
                    {
                        Toast.makeText(SettingsActivity.this,e.getMessage(),Toast.LENGTH_SHORT);
                    }

                    username.setText(retrieveUserName);
                    Picasso.get().load(retrieveimg).into(userprofileImage);

                }
                else if ((snapshot.exists()) && (snapshot.hasChild("name")))
                {
                    try {
                         retrieveUserName = Objects.requireNonNull(snapshot.child("name").getValue()).toString();
                        username.setText(retrieveUserName);
                    }catch (Exception e)
                    {
                        Toast.makeText(SettingsActivity.this,e.getMessage(),Toast.LENGTH_SHORT);
                    }

                }
                else
                {

                    Toast.makeText(SettingsActivity.this,"please set and update your proile information",Toast.LENGTH_SHORT);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    private void InitializeFields()
    {
        UpdateAccountSettings = (Button) findViewById(R.id.update_settings_button);
        username = (EditText) findViewById(R.id.set_user_name);
        userstatus = (EditText) findViewById(R.id.set_profile_status);
        userprofileImage = (CircularImageView) findViewById(R.id.set_profile_image);
        coverimg=(ImageView)findViewById(R.id.coverpicture);
        text = (TextView) findViewById(R.id.text1);
        loadingBar = new ProgressDialog(this);
        cardView=(CardView)findViewById(R.id.themecardview);
    }



    private void UpdateAccountSettings()
    {
         setUserName=username.getText().toString();
         setUserStatus=userstatus.getText().toString();
         try {
             phoneno = auth.getCurrentUser().getPhoneNumber();
         }catch (Exception e)
         {
             Toast.makeText(SettingsActivity.this,e.getMessage(),Toast.LENGTH_SHORT);
         }



        if (resultUri != null)
        {
            final StorageReference filePath = UserProfileImageRef.child(currentUserID + ".jpg");
            final UploadTask uploadTask = filePath.putFile(resultUri);
            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    String message = e.toString();
                    Toast.makeText(SettingsActivity.this, "Error " + message, Toast.LENGTH_SHORT).show();
                    loadingBar.dismiss();
                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(final UploadTask.TaskSnapshot taskSnapshot) {
                    //Toast.makeText(SettingsActivity.this, "Image uploaded successfully", Toast.LENGTH_SHORT).show();
                     Toast toast=new Toast(SettingsActivity.this);
                     View view= LayoutInflater.from(SettingsActivity.this)
                             .inflate(R.layout.toastcustomlayout,null);
                    TextView text=view.findViewById(R.id.txtToast);
                     toast.setView(view);
                     toast.setDuration(Toast.LENGTH_LONG);
                     toast.show();
                    Task<Uri> uriTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                        @Override
                        public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                            if (!task.isSuccessful()) {
                                throw Objects.requireNonNull(task.getException());
                            }

                            downloadurl = filePath.getDownloadUrl().toString();
                            return filePath.getDownloadUrl();
                        }
                    }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                        @Override
                        public void onComplete(@NonNull Task<Uri> task) {
                            if (task.isSuccessful())
                            {
                                Toast.makeText(SettingsActivity.this, "please wait,while Image is saved to database successfully", Toast.LENGTH_SHORT);
                                downloadurl = task.getResult().toString();
                                rootRef.child("users").child(currentUserID).child("image").setValue(downloadurl).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {




                                            Toast.makeText(SettingsActivity.this, "Image is added successfully to database", Toast.LENGTH_SHORT);

                                        }
                                        else {

                                            String message = task.getException().toString();
                                            Toast.makeText(SettingsActivity.this, "Error: " + message, Toast.LENGTH_SHORT);
                                        }

                                    }
                                });



                            } else {
                                String message = task.getException().toString();
                                Toast.makeText(SettingsActivity.this, "Error:" + message, Toast.LENGTH_SHORT);

                            }
                        }

                    });

                }
            });

        }

         if (coverUri != null)
         {

             final StorageReference coverPath = coverImageRef.child(currentUserID + ".jpg");
             final UploadTask coveruploadTask = coverPath.putFile(coverUri);
             coveruploadTask.addOnFailureListener(new OnFailureListener() {
                 @Override
                 public void onFailure(@NonNull Exception e) {
                     String message = e.toString();
                     Toast.makeText(SettingsActivity.this, "Error " + message, Toast.LENGTH_SHORT).show();
                     loadingBar.dismiss();
                 }
             }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                 @Override
                 public void onSuccess(final UploadTask.TaskSnapshot taskSnapshot) {
                     Toast.makeText(SettingsActivity.this, "cover Image uploaded successfully", Toast.LENGTH_SHORT).show();
                     Task<Uri> uriTask = coveruploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                         @Override
                         public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                             if (!task.isSuccessful()) {
                                 throw Objects.requireNonNull(task.getException());
                             }

                             coverurl = coverPath.getDownloadUrl().toString();
                             return coverPath.getDownloadUrl();
                         }
                     }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                         @Override
                         public void onComplete(@NonNull Task<Uri> task) {
                             if (task.isSuccessful())
                             {
                                 Toast.makeText(SettingsActivity.this, "please wait,while Image is saved to database successfully", Toast.LENGTH_SHORT);
                                 coverurl = task.getResult().toString();
                                 rootRef.child("users").child(currentUserID).child("coverimage").setValue(coverurl).addOnCompleteListener(new OnCompleteListener<Void>() {
                                     @Override
                                     public void onComplete(@NonNull Task<Void> task) {
                                         if (task.isSuccessful()) {




                                             Toast.makeText(SettingsActivity.this, "Image is added successfully to database", Toast.LENGTH_SHORT);

                                         }
                                         else {

                                             String message = task.getException().toString();
                                             Toast.makeText(SettingsActivity.this, "Error: " + message, Toast.LENGTH_SHORT);
                                         }

                                     }
                                 });



                             } else {
                                 String message = task.getException().toString();
                                 Toast.makeText(SettingsActivity.this, "Error:" + message, Toast.LENGTH_SHORT);

                             }
                         }

                     });

                 }
             });





         }


        if (TextUtils.isEmpty(setUserName))
        {
            Toast.makeText(this,"please write your username",Toast.LENGTH_SHORT).show();
        }




        else
        {
            HashMap<String,String> profileMap=new HashMap<>();
            profileMap.put("uid",currentUserID);
            profileMap.put("name",setUserName);
            profileMap.put("status",setUserStatus);



            rootRef.child("users").child(currentUserID).child("uid").setValue(currentUserID);
            rootRef.child("users").child(currentUserID).child("userno").setValue(phoneno);
            rootRef.child("users").child(currentUserID).child("name").setValue(setUserName);
            rootRef.child("users").child(currentUserID).child("status").setValue(setUserStatus).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task)
                {

                    if (task.isSuccessful())
                    {
                        Toast.makeText(SettingsActivity.this,"Profile updated successfully",Toast.LENGTH_SHORT).show();
                        sendUserToMainActivity();

                    }
                    else
                    {
                        try {
                            String message = task.getException().toString();
                            Toast.makeText(SettingsActivity.this,"Error:"+message,Toast.LENGTH_SHORT).show();
                        }catch (Exception e)
                        {
                            Toast.makeText(SettingsActivity.this,e.getMessage(),Toast.LENGTH_SHORT);
                        }

                    }

                }
            });

        }
    }

    private void sendUserToMainActivity()
    {
        Intent mainIntent=new Intent(SettingsActivity.this,MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }

}
