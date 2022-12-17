package com.example.myappchat;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.StorageReference;
import com.mikhaellopez.circularimageview.CircularImageView;

import java.util.ArrayList;
import java.util.Date;

public class GroupchatActivity extends AppCompatActivity {

    private TextView groupuserName,groupuserLastSeen;
    private RecyclerView grouprecyclerview;
    private String groupsaveCurrentTime,groupsaveCurrentDate;
    private EditText groupMessageInputText;
    private CircularImageView groupuserImage;
    private ImageButton groupSendMessageButton;
    private ImageView groupSendFilesButton;

    private String messageReceiverID,messageReceiverName,messageReceiverImage,messageSenderID;
    private ArrayList<Messages> usermessages;
    private MessagesAdapter messagesAdapter;
    private StorageReference storage;
    private DatabaseReference rootref;
    private FirebaseAuth auth;
    private String currentUserID,messageseen;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_groupchat);

        InitializeFields();


        rootref= FirebaseDatabase.getInstance().getReference();
        auth=FirebaseAuth.getInstance();

        groupSendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {

                SendMessage();

            }
        });

    }

    private void SendMessage()
    {
        String messageTxt=groupMessageInputText.getText().toString();

        if (TextUtils.isEmpty(messageTxt))
        {

            Toast.makeText(GroupchatActivity.this,"First write a message...",Toast.LENGTH_SHORT).show();
        }
        else {
            String msgtype = "text";

            Date date = new Date();

            Messages messages = new Messages(messageTxt, currentUserID, msgtype, date.getTime(),false);

            groupMessageInputText.setText("");
        }

    }

    private void InitializeFields()
    {

        currentUserID=FirebaseAuth.getInstance().getUid();
        messageseen="unseen";

        grouprecyclerview=(RecyclerView)findViewById(R.id.group_list_of_messages);
        groupSendMessageButton=(ImageButton) findViewById(R.id.groupsendmsgbutton);
        groupMessageInputText=(EditText)findViewById(R.id.groupinputtextmsg);
        groupSendFilesButton=(ImageView)findViewById(R.id.groupsendfilesimageView);

        usermessages=new ArrayList<>();

    }
}
