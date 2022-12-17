package com.example.myappchat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
//import com.google.gson.JsonObject;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.squareup.picasso.Picasso;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.zip.Inflater;

public class ChatActivity extends AppCompatActivity {

    //#0288D1
    private String messageReceiverID,messageReceiverName,messageReceiverImage,sendername,messagetoken,msgseen,actualmsg;
    private  ArrayList<Messages> usermessages;
    private MessagesAdapter messagesAdapter;
    private StorageReference storage;
    private  EditText e1;
    private TextView userName,userLastSeen;
    private int REQUEST_CAMERA=100;
    private String saveCurrentTime,saveCurrentDate,randomKey,currentpath;
    private CircularImageView userImage;
    private RecyclerView messageslist;
    static final int REQUEST_TAKE_PHOTO=1;
    private Toolbar ChatToolbar;
    private LinearLayoutManager linearLayoutManager;
    private FirebaseAuth auth;
    private DatabaseReference rootref;
    private ImageButton SendMessageButton,SendFilesButton;
    private EditText MessageInputText;
    private String checker="",myUri="",pdfUri="",currentUserID,state,date,time;
    private String senderRoom,receiverRoom;
    private FirebaseUser currentuser;
    private ImageView sendfilesbutton,takepic;
    private View actionBarView;
    private StorageTask uploadTask,pdfuploadTask;
    private Uri fileUri,cameraimageuri;
    private ProgressDialog loadingBar;
    ValueEventListener seenListener;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        InitializeFields();

         Toast.makeText(ChatActivity.this,messagetoken,Toast.LENGTH_SHORT).show();

        rootref=FirebaseDatabase.getInstance().getReference();
        auth=FirebaseAuth.getInstance();
        currentuser=auth.getCurrentUser();
        storage=FirebaseStorage.getInstance().getReference();

        try {

            userName.setText(messageReceiverName);
            Picasso.get().load(messageReceiverImage).placeholder(R.drawable.profile_image).into(userImage);
        }catch (Exception e)
        {
            Toast.makeText(ChatActivity.this,e.getMessage(),Toast.LENGTH_SHORT).show();
        }


        SendMessageButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {

                SendMessage();

            }
        });

        DisplayLastSeen();

        actionBarView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {

               Intent userprofileintent=new Intent(ChatActivity.this,UserProfileActivity.class);
               userprofileintent.putExtra("receiversproimage",messageReceiverImage);
               userprofileintent.putExtra("receiversuname",messageReceiverName);
               userprofileintent.putExtra("receiversID",messageReceiverID);

               startActivity(userprofileintent);

            }
        });

        sendfilesbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                CharSequence[] options =new CharSequence[]
                        {
                                "Images",
                                "PDF files",
                                "Ms Word files",
                                "Videos"
                        };
                AlertDialog.Builder builder=new AlertDialog.Builder(ChatActivity.this);
                builder.setTitle("Select file");

                builder.setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i)
                    {
                        if (i == 0)
                        {
                            checker="image";

                            Intent intent=new Intent();
                            intent.setAction(Intent.ACTION_GET_CONTENT);
                            intent.setType("image/*");
                            startActivityForResult(Intent.createChooser(intent,"Select Image"),438);
                        }
                        if (i == 1)
                        {
                            checker="pdf";

                            Intent intent=new Intent();
                            intent.setType("application/pdf");
                            intent.setAction(Intent.ACTION_GET_CONTENT);
                            startActivityForResult(Intent.createChooser(intent,"Select PDF file"),438);
                        }
                        if (i == 2)
                        {
                            checker="docx";

                            Intent intent=new Intent();
                            intent.setAction(Intent.ACTION_GET_CONTENT);
                            intent.setType("application/msword");
                            startActivityForResult(Intent.createChooser(intent,"Select Ms Word file"),438);
                        }
                        if (i == 3)
                        {
                            checker="videos";

                            Intent intent=new Intent();
                            intent.setAction(Intent.ACTION_GET_CONTENT);
                            intent.setType("video/mp4");
                            startActivityForResult(Intent.createChooser(intent,"Select Video file"),438);
                        }
                    }
                });
                builder.show();
            }


        });




        takepic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {

                if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA) !=
                        PackageManager.PERMISSION_GRANTED)
                {
                    ActivityCompat.requestPermissions(ChatActivity.this, new String[]{Manifest.permission.CAMERA},
                            REQUEST_CAMERA);
                } else {
                    dispatchtakepicture();
                }
               /*Intent intent=new Intent(ChatActivity.this,ImagefromCameraActivity.class);
               startActivity(intent);*/
            }
        });

        final Handler handler=new Handler();
        MessageInputText.addTextChangedListener(new TextWatcher()
        {


            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after)
            {




            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s)
            {

                updateUserStatus("Typing....");

                handler.removeCallbacksAndMessages(null);

                handler.postDelayed(userStoppedtyping,1000);



            }

            final Runnable userStoppedtyping=new Runnable() {
                @Override
                public void run()
                {

                    updateUserStatus("online");

                }
            };
        });





    }

    private void TakePictureFromCamera()
    {


        Intent intent=new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent,100);
    }



    private void seenMessage(String chatuserid)
    {

        rootref=FirebaseDatabase.getInstance().getReference().child("Chats");
        seenListener=rootref.child("Chats").child(receiverRoom).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {

                for (DataSnapshot snapshot1 : snapshot.getChildren())
                {
                    Messages messages=snapshot.getValue(Messages.class);

                        if (!(currentUserID.startsWith(receiverRoom))) {
                            HashMap<String, Object> hashMap = new HashMap<>();
                            hashMap.put("isseen", true);
                            snapshot.getRef().updateChildren(hashMap);
                        }

                }

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


        if (currentuser != null)
        {
            updateUserStatus("online");
        }

        rootref.child("Chats").child(senderRoom).child("Messages").addValueEventListener(new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {
                if (snapshot.exists()) {
                    usermessages.clear();
                    for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                        Messages messages = snapshot1.getValue(Messages.class);
                        try {
                            assert messages != null;
                            messages.setMessageID(snapshot1.getKey());
                        }catch (Exception e)
                        {
                            Toast.makeText(ChatActivity.this,e.getMessage(),Toast.LENGTH_SHORT);
                        }
                        usermessages.add(messages);
                    }
                    messagesAdapter.notifyDataSetChanged();
                    try {
                        messageslist.smoothScrollToPosition(Objects.requireNonNull(messageslist.getAdapter()).getItemCount());
                    }catch (Exception e)
                    {
                        Toast.makeText(ChatActivity.this,e.getMessage(),Toast.LENGTH_SHORT);
                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });



    }

    @Override
    protected void onResume()
    {
        super.onResume();

        if (currentuser != null)
        {
            updateUserStatus("online");
        }

    }

    @Override
    protected void onPause() {
        super.onPause();

        if (seenListener != null && rootref != null) {
            rootref.removeEventListener(seenListener);
        }

        if (currentuser != null)
        {
            updateUserStatus("offline");
        }

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);



               if (requestCode == 438 && resultCode == RESULT_OK && data != null && data.getData() != null) {

                   loadingBar.setMessage("uploading file");
                   loadingBar.setCancelable(false);
                   loadingBar.show();

                   fileUri = data.getData();




                   if (checker.equals("pdf") || (checker.equals("docx"))) {

                       StorageReference reference = FirebaseStorage.getInstance().getReference().child("Document files").child("upload" + System.currentTimeMillis() + ".pdf");
                       reference.putFile(fileUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                           @Override
                           public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                               loadingBar.dismiss();

                               Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                               while (!uriTask.isComplete()) ;


                               Uri uri = uriTask.getResult();
                               String fileupload = uri.toString();

                               String messageTxt = MessageInputText.getText().toString();


                               String msgtype = "documents";

                               Date date = new Date();

                               final Messages messages = new Messages(messageTxt, currentUserID, msgtype, date.getTime(), false);
                               messages.setMessage(checker);
                               messages.setPdfurl(fileupload);


                               String randomKey = rootref.push().getKey();

                               HashMap<String, Object> lastMsjObj = new HashMap<>();
                               lastMsjObj.put("lastMsg", messages.getMessage());
                               lastMsjObj.put("lastmsgtime", date.getTime());

                               rootref.child("Chats").child(senderRoom).updateChildren(lastMsjObj);
                               rootref.child("Chats").child(receiverRoom).updateChildren(lastMsjObj);
                               try {
                                   assert randomKey != null;
                                   rootref.child("Chats").child(senderRoom).child("Messages").child(randomKey).setValue(messages).addOnSuccessListener(new OnSuccessListener<Void>() {
                                       @Override
                                       public void onSuccess(Void aVoid) {

                                           rootref.child("Chats").child(receiverRoom).child("Messages").child(randomKey).setValue(messages).addOnSuccessListener(new OnSuccessListener<Void>() {
                                               @Override
                                               public void onSuccess(Void aVoid) {
                                                   Toast.makeText(ChatActivity.this, "Message sent successfully", Toast.LENGTH_SHORT).show();

                                               }
                                           });


                                       }
                                   });
                               }catch (Exception e)
                               {
                                   Toast.makeText(ChatActivity.this,e.getMessage(),Toast.LENGTH_SHORT).show();
                               }

                               MessageInputText.setText("");

                               Toast.makeText(ChatActivity.this, fileupload, Toast.LENGTH_SHORT).show();


                           }
                       }).addOnFailureListener(new OnFailureListener() {
                           @Override
                           public void onFailure(@NonNull Exception e) {
                               Toast.makeText(ChatActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();


                           }
                       });

                   } else if (checker.equals("image"))
                   {
                       Calendar calendar = Calendar.getInstance();
                       StorageReference reference = FirebaseStorage.getInstance().getReference().child("chats").child(calendar.getTimeInMillis() + "");

                       reference.putFile(fileUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                           @Override
                           public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                               loadingBar.dismiss();

                               if (task.isSuccessful()) {
                                   reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                       @Override
                                       public void onSuccess(Uri uri) {
                                           String filepath = uri.toString();
                                           String messageTxt = MessageInputText.getText().toString();

                                           String msgtype = "image";

                                           Date date = new Date();

                                           final Messages messages = new Messages(messageTxt, currentUserID, msgtype, date.getTime(), false);
                                           messages.setMessage("photo");
                                           messages.setImageurl(filepath);


                                           String randomKey = rootref.push().getKey();

                                           HashMap<String, Object> lastMsjObj = new HashMap<>();
                                           lastMsjObj.put("lastMsg", messages.getMessage());
                                           lastMsjObj.put("lastmsgtime", date.getTime());


                                           rootref.child("Chats").child(senderRoom).updateChildren(lastMsjObj);
                                           rootref.child("Chats").child(receiverRoom).updateChildren(lastMsjObj);
                                           try {
                                               assert randomKey != null;
                                               rootref.child("Chats").child(senderRoom).child("Messages").child(randomKey).setValue(messages).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                   @Override
                                                   public void onSuccess(Void aVoid) {

                                                       rootref.child("Chats").child(receiverRoom).child("Messages").child(randomKey).setValue(messages).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                           @Override
                                                           public void onSuccess(Void aVoid) {
                                                               Toast.makeText(ChatActivity.this, "Message sent successfully", Toast.LENGTH_SHORT).show();

                                                           }
                                                       });


                                                   }
                                               });
                                           }catch (Exception e)
                                           {
                                               Toast.makeText(ChatActivity.this,e.getMessage(),Toast.LENGTH_SHORT).show();
                                           }

                                           MessageInputText.setText("");

                                           // Toast.makeText(ChatActivity.this, filepath, Toast.LENGTH_SHORT).show();

                                       }
                                   });
                               }

                           }
                       });

                   }else
                   {
                       Calendar calendar = Calendar.getInstance();
                       StorageReference reference = FirebaseStorage.getInstance().getReference().child("chats").child(calendar.getTimeInMillis() + ".mp4");

                       reference.putFile(fileUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                           @Override
                           public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task)
                           {
                               loadingBar.dismiss();
                               if (task.isSuccessful())
                               {
                                   reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                       @Override
                                       public void onSuccess(Uri uri) {
                                           String filepath = uri.toString();
                                           String messageTxt = MessageInputText.getText().toString();


                                           String msgtype = "video";

                                           Date date = new Date();

                                           final Messages messages = new Messages(messageTxt, currentUserID, msgtype, date.getTime(), false);
                                           messages.setMessage("videos");
                                           messages.setVideourl(filepath);


                                           String randomKey = rootref.push().getKey();

                                           HashMap<String, Object> lastMsjObj = new HashMap<>();
                                           lastMsjObj.put("lastMsg", messages.getMessage());
                                           lastMsjObj.put("lastmsgtime", date.getTime());


                                           rootref.child("Chats").child(senderRoom).updateChildren(lastMsjObj);
                                           rootref.child("Chats").child(receiverRoom).updateChildren(lastMsjObj);

                                           rootref.child("Chats").child(senderRoom).child("Messages").child(randomKey).setValue(messages).addOnSuccessListener(new OnSuccessListener<Void>() {
                                               @Override
                                               public void onSuccess(Void aVoid) {

                                                   rootref.child("Chats").child(receiverRoom).child("Messages").child(randomKey).setValue(messages).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                       @Override
                                                       public void onSuccess(Void aVoid) {
                                                           Toast.makeText(ChatActivity.this, "Message sent successfully", Toast.LENGTH_SHORT).show();

                                                       }
                                                   });


                                               }
                                           });

                                           MessageInputText.setText("");

                                           // Toast.makeText(ChatActivity.this, filepath, Toast.LENGTH_SHORT).show();

                                       }
                                   });
                               }
                           }
                       });
                   }


               }
               else if (requestCode==REQUEST_CAMERA && resultCode == Activity.RESULT_OK)
               {

                   loadingBar.setMessage("uploading file");
                   loadingBar.setCancelable(false);
                   loadingBar.show();

                   File f=new File(currentpath);
                   cameraimageuri=Uri.fromFile(f);

                   Calendar calendar = Calendar.getInstance();
                   StorageReference reference = FirebaseStorage.getInstance().getReference().child("chats").child(calendar.getTimeInMillis() + "");

                   reference.putFile(cameraimageuri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                       @Override
                       public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                           loadingBar.dismiss();

                           if (task.isSuccessful()) {
                               reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                   @Override
                                   public void onSuccess(Uri uri) {
                                       String filepath = uri.toString();
                                       String messageTxt = MessageInputText.getText().toString();


                                       String msgtype = "image";

                                       Date date = new Date();

                                       final Messages messages = new Messages(messageTxt, currentUserID, msgtype, date.getTime(), false);
                                       messages.setMessage("photo");
                                       messages.setImageurl(filepath);


                                       String randomKey = rootref.push().getKey();

                                       HashMap<String, Object> lastMsjObj = new HashMap<>();
                                       lastMsjObj.put("lastMsg", messages.getMessage());
                                       lastMsjObj.put("lastmsgtime", date.getTime());


                                       rootref.child("Chats").child(senderRoom).updateChildren(lastMsjObj);
                                       rootref.child("Chats").child(receiverRoom).updateChildren(lastMsjObj);

                                       rootref.child("Chats").child(senderRoom).child("Messages").child(randomKey).setValue(messages).addOnSuccessListener(new OnSuccessListener<Void>() {
                                           @Override
                                           public void onSuccess(Void aVoid) {

                                               rootref.child("Chats").child(receiverRoom).child("Messages").child(randomKey).setValue(messages).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                   @Override
                                                   public void onSuccess(Void aVoid) {
                                                       Toast.makeText(ChatActivity.this, "Message sent successfully", Toast.LENGTH_SHORT).show();

                                                   }
                                               });


                                           }
                                       });

                                       MessageInputText.setText("");

                                       // Toast.makeText(ChatActivity.this, filepath, Toast.LENGTH_SHORT).show();
                                   }
                               });
                           }

                       }
                   });

               }


    }



    private void InitializeFields()
    {



        try {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }catch (Exception e)
        {
            Toast.makeText(ChatActivity.this,e.getMessage(),Toast.LENGTH_SHORT);
        }
        try {
            getSupportActionBar().setDisplayShowCustomEnabled(true);
        }catch(Exception e)
        {
            Toast.makeText(ChatActivity.this,e.getMessage(),Toast.LENGTH_SHORT);
        }

        try {

        LayoutInflater layoutInflater=(LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        actionBarView = layoutInflater.inflate(R.layout.custom_chat_bar, null);

        }catch (Exception e)
        {
            Toast.makeText(ChatActivity.this,e.getMessage(),Toast.LENGTH_SHORT).show();
        }
        try {
            Objects.requireNonNull(getSupportActionBar()).setCustomView(actionBarView);

        }catch (Exception e)
        {
            Toast.makeText(ChatActivity.this,e.getMessage(),Toast.LENGTH_SHORT).show();
        }

        loadingBar=new ProgressDialog(this);

        currentUserID=FirebaseAuth.getInstance().getUid();
        msgseen="unseen";

        try {
            messageReceiverID = getIntent().getExtras().get("visit_user_id").toString();
            messageReceiverName = getIntent().getExtras().get("visit_user_name").toString();
            messageReceiverImage = getIntent().getExtras().get("visit_image").toString();
            messagetoken = getIntent().getExtras().get("token").toString();
            sendername = getIntent().getExtras().get("messagesendername").toString();
        }catch (Exception e)
        {
            Toast.makeText(ChatActivity.this,e.getMessage(),Toast.LENGTH_SHORT);
        }

        senderRoom=currentUserID + messageReceiverID;
        receiverRoom=messageReceiverID + currentUserID;


        SendMessageButton=(ImageButton) findViewById(R.id.sendmsgbutton);
        MessageInputText=(EditText)findViewById(R.id.inputtextmsg);
        takepic=(ImageView)findViewById(R.id.cameraimage);
        sendfilesbutton=(ImageView)findViewById(R.id.sendfilesimageView);
        messageslist=(RecyclerView)findViewById(R.id.chat_list_of_messages);
        userName=(TextView)findViewById(R.id.custom_profile_name);
        userImage=(CircularImageView)findViewById(R.id.custom_profile_image);
        userLastSeen=(TextView)findViewById(R.id.custom_user_last_seen);
        usermessages=new ArrayList<>();
        messagesAdapter=new MessagesAdapter(this,usermessages,senderRoom,receiverRoom);
        messageslist.setAdapter(messagesAdapter);
        messageslist.setLayoutManager(new LinearLayoutManager(this));

        Calendar calendar=Calendar.getInstance();

        @SuppressLint("SimpleDateFormat") SimpleDateFormat currentDate=new SimpleDateFormat("MMM dd, yyyy");
        saveCurrentDate=currentDate.format(calendar.getTime());
        @SuppressLint("SimpleDateFormat") SimpleDateFormat currentTime=new SimpleDateFormat("hh:mm a");
        saveCurrentTime=currentTime.format(calendar.getTime());


    }




    private void SendMessage()
    {
        String messageTxt=MessageInputText.getText().toString();


        if (TextUtils.isEmpty(messageTxt))
        {
             Toast toast=new Toast(this);
             LayoutInflater inflater= LayoutInflater.from(this);
             View v=inflater.inflate(R.layout.sendmsg1,null);
             toast.setView(v);
             toast.setDuration(Toast.LENGTH_LONG);
             toast.show();
            //Toast.makeText(ChatActivity.this,"First write a message...",Toast.LENGTH_SHORT).show();
        }

        else {


            String msgtype="text";

             actualmsg="Message from "+sendername;
            Date date = new Date();

            final Messages messages=new Messages(messageTxt,currentUserID,msgtype,date.getTime(),false);

            String randomKey = rootref.push().getKey();

            HashMap<String,Object> lastMsjObj=new HashMap<>();
            lastMsjObj.put("lastMsg",messages.getMessage());
            lastMsjObj.put("lastmsgtime",date.getTime());

            rootref.child("Chats").child(senderRoom).updateChildren(lastMsjObj);
            rootref.child("Chats").child(receiverRoom).updateChildren(lastMsjObj);
            try {
                assert randomKey != null;
                rootref.child("Chats").child(senderRoom).child("Messages").child(randomKey).setValue(messages).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                        rootref.child("Chats").child(receiverRoom).child("Messages").child(randomKey).setValue(messages).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                sendNotification(actualmsg, messages.getMessage(), messagetoken);
                                Toast toast =new Toast(ChatActivity.this);
                                LayoutInflater inflater1=LayoutInflater.from(ChatActivity.this);
                                View v1=inflater1.inflate(R.layout.sendmsg2,null);
                                toast.setView(v1);
                                toast.setDuration(Toast.LENGTH_LONG);
                                toast.show();
                                //Toast.makeText(ChatActivity.this, "Message sent successfully", Toast.LENGTH_SHORT).show();

                            }
                        });


                    }
                });
            }catch (Exception e)
            {
                Toast.makeText(ChatActivity.this,e.getMessage(),Toast.LENGTH_SHORT).show();
            }

           MessageInputText.setText("");


        }

    }

    private void DisplayLastSeen()
    {
        rootref.child("users").child(messageReceiverID).addValueEventListener(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {

                if (dataSnapshot.child("userState").hasChild("State"))
                {
                    try {
                        state = Objects.requireNonNull(dataSnapshot.child("userState").child("State").getValue()).toString();
                        date = Objects.requireNonNull(dataSnapshot.child("userState").child("date").getValue()).toString();
                        time = Objects.requireNonNull(dataSnapshot.child("userState").child("time").getValue()).toString();
                    }catch (Exception e)
                    {
                        Toast.makeText(ChatActivity.this,e.getMessage(),Toast.LENGTH_SHORT);
                    }

                    if (state.equals("online"))
                    {
                        try{
                            userLastSeen.setText("online");
                        }
                        catch (Exception e)
                        {
                            Toast.makeText(ChatActivity.this,e.getMessage(),Toast.LENGTH_SHORT);
                        }

                    }
                    else if (state.equals("Typing...."))
                    {
                      try {
                          userLastSeen.setText("Typing....");
                      }catch (Exception e)
                      {
                          Toast.makeText(ChatActivity.this,e.getMessage(),Toast.LENGTH_SHORT);
                      }

                    }
                    else if (state.equals("offline"))
                    {
                        try {
                            userLastSeen.setText("Last seen: " + date + " " + time);
                        }catch (Exception e)
                        {
                            Toast.makeText(ChatActivity.this,e.getMessage(),Toast.LENGTH_SHORT);
                        }
                    }
                }
                else
                {
                    try {
                        userLastSeen.setText("offline");
                    }catch (Exception e)
                    {
                        Toast.makeText(ChatActivity.this,e.getMessage(),Toast.LENGTH_SHORT);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {

            }
        });
   }

    private void updateUserStatus(String state)
    {

        HashMap<String,Object> onlineStateMap=new HashMap<>();
        onlineStateMap.put("time", saveCurrentTime);
        onlineStateMap.put("date", saveCurrentDate);
        onlineStateMap.put("State",state);
        try {
            currentUserID = Objects.requireNonNull(auth.getCurrentUser()).getUid();
        }catch (Exception e)
        {
            Toast.makeText(ChatActivity.this,e.getMessage(),Toast.LENGTH_SHORT).show();
        }
        rootref.child("users").child(currentUserID).child("userState").updateChildren(onlineStateMap);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        super.onCreateOptionsMenu(menu);

        getMenuInflater().inflate(R.menu.chat_menu,menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item)
    {
        super.onOptionsItemSelected(item);

        if (item.getItemId() == R.id.main_clearchat_option)
        {

           FirebaseDatabase.getInstance().getReference().child("Chats").child(senderRoom).child("Messages")
                   .setValue(null);
           FirebaseDatabase.getInstance().getReference().child("Chats").child(senderRoom)
                   .child("lastMsg").setValue(null);
           FirebaseDatabase.getInstance().getReference().child("Chats").child(senderRoom)
                   .child("lastmsgtime").setValue(null);
           Toast.makeText(ChatActivity.this,"chat deleted",Toast.LENGTH_SHORT).show();

        }

        if (item.getItemId() == android.R.id.home)
        {
            sendUserToMainActivity();
        }

        return true;
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
                    Toast.makeText(ChatActivity.this,"success",Toast.LENGTH_SHORT);

                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error)
                {

                    Toast.makeText(ChatActivity.this,error.getLocalizedMessage(),Toast.LENGTH_SHORT);
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

        }

    }


  private File createimagefile() throws IOException
  {
      String timestamp=new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
      String imagefilename="JPEG_" + timestamp + "_";
      File storageDir=getExternalFilesDir(Environment.DIRECTORY_PICTURES);
      File image=File.createTempFile(imagefilename,".jpg",storageDir);


      currentpath=image.getAbsolutePath();
      return image;
  }


  private void dispatchtakepicture()
  {
      Intent takepicintent=new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
      if (takepicintent.resolveActivity(getPackageManager()) != null)
      {
          File photofile=null;
          try {
              photofile=createimagefile();
          }catch (IOException ex){

          }

          if (photofile!=null)
          {
              Uri photouri= FileProvider.getUriForFile(this,"com.example.android.fileprovider",photofile);
              takepicintent.putExtra(MediaStore.EXTRA_OUTPUT,photouri);
              startActivityForResult(takepicintent,REQUEST_CAMERA);
          }
      }
  }


    private void sendUserToMainActivity()
    {
        Intent mainintent=new Intent(ChatActivity.this,MainActivity.class);
        startActivity(mainintent);
    }
}
