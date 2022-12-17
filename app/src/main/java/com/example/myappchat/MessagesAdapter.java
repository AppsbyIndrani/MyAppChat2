package com.example.myappchat;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.motion.widget.OnSwipe;
import androidx.recyclerview.widget.RecyclerView;

import com.github.pgreze.reactions.ReactionPopup;
import com.github.pgreze.reactions.ReactionsConfig;
import com.github.pgreze.reactions.ReactionsConfigBuilder;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static android.view.View.GONE;

public class MessagesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    Context context;
    ArrayList<Messages> messageslist;
    final int ITEM_SENT=1;
    final int ITEM_RECIEVE=2;
    private boolean isseenornot=false;
    private final String senderRoom;
   private final String receiverRoom;
    private String seenmsg;
    private GestureDetector gestureDetector;
   FirebaseRemoteConfig remoteConfig;


    public MessagesAdapter(Context context, ArrayList<Messages> messageslist,String senderRoom,String receiverRoom)
    {

        remoteConfig=FirebaseRemoteConfig.getInstance();
        gestureDetector=new GestureDetector(this.context, new GestureListener());
        this.context=context;
        this.messageslist=messageslist;
        this.senderRoom=senderRoom;
        this.receiverRoom=receiverRoom;

    }


    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        if (viewType == ITEM_SENT)
        {
            View view= LayoutInflater.from(context).inflate(R.layout.item_send,parent,false);
            return new SentViewHolder(view);
        }else {
            View view=LayoutInflater.from(context).inflate(R.layout.item_recieve,parent,false);
            return new RecieverViewHolder(view);
        }


    }

    @Override
    public int getItemViewType(int position)
    {
        Messages message=messageslist.get(position);
        if (FirebaseAuth.getInstance().getUid().equals(message.getSenderID()))
        {
            return ITEM_SENT;
        }else {
            return ITEM_RECIEVE;

        }

    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, @SuppressLint("RecyclerView") int position)
    {

        Messages messages=messageslist.get(position);



        int reactions[]=new int[]{
                R.drawable.ic_fb_like,
                R.drawable.ic_fb_love,
                R.drawable.ic_fb_laugh,
                R.drawable.ic_fb_wow,
                R.drawable.ic_fb_sad,
                R.drawable.ic_fb_angry



    };

        ReactionsConfig config = new ReactionsConfigBuilder(context)
                .withReactions(reactions)
                .build();

        ReactionPopup popup = new ReactionPopup(context, config, (pos) ->
        {

            messages.setFeeling(pos);

            FirebaseDatabase.getInstance().getReference().child("Chats").child(senderRoom).child("Messages").child(messages.getMessageID()).setValue(messages);
            FirebaseDatabase.getInstance().getReference().child("Chats").child(receiverRoom).child("Messages").child(messages.getMessageID()).setValue(messages);

            return true;
        });


        if (holder.getClass() == SentViewHolder.class)
        {

            SentViewHolder viewHolder=(SentViewHolder)holder;
           viewHolder.sendersImage.setVisibility(GONE);
            viewHolder.senderMessageText.setVisibility(GONE);
            viewHolder.senderPDF.setVisibility(GONE);
            viewHolder.sendervideos.setVisibility(GONE);

            if (messages.getMessage().equals("photo"))
            {

                viewHolder.sendersImage.setVisibility(View.VISIBLE);
                Picasso.get().load(messages.getImageurl()).placeholder(R.drawable.picture).into(viewHolder.sendersImage);
                SimpleDateFormat dateFormat=new SimpleDateFormat("hh:mm a");
                viewHolder.sendertimetxt.setText(dateFormat.format(new Date(messages.getTimestamp())));


            }
            else if ((messages.getMessage().equals("pdf")) || (messages.getMessage().equals("docx")))
            {
                viewHolder.setfeeling.setVisibility(View.INVISIBLE);
                viewHolder.senderPDF.setVisibility(View.VISIBLE);
                 viewHolder.senderPDF.setBackgroundResource(R.drawable.file);
                SimpleDateFormat dateFormat=new SimpleDateFormat("hh:mm a");
                viewHolder.sendertimetxt.setText(dateFormat.format(new Date(messages.getTimestamp())));

            }
            else if (messages.getMessage().equals("videos"))
            {
                viewHolder.setfeeling.setVisibility(View.INVISIBLE);
               viewHolder.sendervideos.setVisibility(View.VISIBLE);

               SimpleDateFormat dateFormat=new SimpleDateFormat("hh:mm a");
                viewHolder.sendertimetxt.setText(dateFormat.format(new Date(messages.getTimestamp())));
               viewHolder.sendervideos.setVideoPath(messages.getVideourl());
                MediaController mediaController=new MediaController(this.context);

                mediaController.setAnchorView(viewHolder.sendervideos);
                viewHolder.sendervideos.setMediaController(mediaController);
            }
            else
            {
                SimpleDateFormat dateFormat=new SimpleDateFormat("hh:mm a");
                viewHolder.senderMessageText.setVisibility(View.VISIBLE);
                viewHolder.senderMessageText.setLinksClickable(true);
                viewHolder.senderMessageText.setText(messages.getMessage());
                viewHolder.sendertimetxt.setText(dateFormat.format(new Date(messages.getTimestamp())));

            }

               if (((messages.getType().equals("text")) || (messages.getMessage().equals("photo"))) && (messageslist.get(position).getFeeling() == -1))
               {

                   viewHolder.setfeeling.setOnTouchListener(
                           new TextView.OnTouchListener()
                           {

                               public boolean onTouch(View v, MotionEvent event)
                               {
                                   popup.onTouch(v, event);
                                   return false;
                               }

                           }

                           );




               /* viewHolder.senderMessageText.setOnTouchListener(new View.OnTouchListener() {

                    @Override
                    public boolean onTouch(View v, MotionEvent event)
                    {

                            popup.onTouch(v, event);

                            return false;

                    }
                });*/

                   viewHolder.setfeeling.setOnTouchListener(new View.OnTouchListener() {

                       @Override
                       public boolean onTouch(View v, MotionEvent event) {
                              
                               popup.onTouch(v, event);

                               return false;
                       }
                   });


               }

               if (((messages.getType().equals("text")) || (messages.getMessage().equals("photo"))) && (messageslist.get(position).getFeeling() >= 0))
               {
                viewHolder.sendersemoji.setImageResource(reactions[messageslist.get(position).getFeeling()]);
                viewHolder.sendersemoji.setVisibility(View.VISIBLE);
               }
               else
               {
                viewHolder.sendersemoji.setVisibility(GONE);
               }



               viewHolder.senderMessageText.setOnLongClickListener(new View.OnLongClickListener() {
                      @Override
                      public boolean onLongClick(View v) {

                          viewHolder.senderMessageText.setOnTouchListener(null);

                          View view = LayoutInflater.from(context).inflate(R.layout.delete_dialog, null);
                          TextView t1 = view.findViewById(R.id.everyone);
                          TextView t2 = view.findViewById(R.id.delme);
                          TextView t3 = view.findViewById(R.id.delcancel);

                          AlertDialog dialog = new AlertDialog.Builder(context)
                                  .setTitle("Delete Message")
                                  .setView(view)
                                  .create();


                          t1.setOnClickListener(new View.OnClickListener() {
                              @Override
                              public void onClick(View v) {

                                  messages.setMessage("This message is removed");
                                  messages.setFeeling(-1);
                                  viewHolder.senderMessageText.setVisibility(View.INVISIBLE);
                                  FirebaseDatabase.getInstance().getReference().child("Chats").child(senderRoom).child("Messages")
                                          .child(messages.getMessageID()).setValue(messages);

                                  FirebaseDatabase.getInstance().getReference().child("Chats").child(receiverRoom).child("Messages")
                                          .child(messages.getMessageID()).setValue(messages);

                                  dialog.dismiss();
                              }
                          });

                          t2.setOnClickListener(new View.OnClickListener() {
                              @Override
                              public void onClick(View v) {


                                  FirebaseDatabase.getInstance().getReference().child("Chats").child(senderRoom).child("Messages")
                                          .child(messages.getMessageID()).setValue(null);

                                  dialog.dismiss();


                              }
                          });

                          t3.setOnClickListener(new View.OnClickListener() {
                              @Override
                              public void onClick(View v) {
                                  dialog.dismiss();

                              }
                          });

                          dialog.show();

                          return false;


                      }
                  });






           viewHolder.sendersImage.setOnLongClickListener(new View.OnLongClickListener() {
               @Override
               public boolean onLongClick(View v)
               {
                   viewHolder.sendersImage.setOnTouchListener(null);

                   View view= LayoutInflater.from(context).inflate(R.layout.delete_dialog_image,null);
                   TextView t1=view.findViewById(R.id.everyoneimg);
                   TextView t2=view.findViewById(R.id.delmeimg);
                   TextView t3=view.findViewById(R.id.delcancelimg);
                   TextView t4=view.findViewById(R.id.viewimage);

                   AlertDialog dialog=new AlertDialog.Builder(context)
                           .setTitle("Delete Message")
                           .setView(view)
                           .create();


                   t1.setOnClickListener(new View.OnClickListener() {
                       @Override
                       public void onClick(View v) {

                           messages.setMessage("This message is removed");
                           messages.setFeeling(-1);
                           viewHolder.setfeeling.setVisibility(View.INVISIBLE);
                           FirebaseDatabase.getInstance().getReference().child("Chats").child(senderRoom).child("Messages")
                                   .child(messages.getMessageID()).setValue(messages);

                           FirebaseDatabase.getInstance().getReference().child("Chats").child(receiverRoom).child("Messages")
                                   .child(messages.getMessageID()).setValue(messages);

                           dialog.dismiss();
                       }
                   });

                   t2.setOnClickListener(new View.OnClickListener() {
                       @Override
                       public void onClick(View v)
                       {


                           FirebaseDatabase.getInstance().getReference().child("Chats").child(senderRoom).child("Messages")
                                   .child(messages.getMessageID()).setValue(null);

                           dialog.dismiss();


                       }
                   });

                   t3.setOnClickListener(new View.OnClickListener() {
                       @Override
                       public void onClick(View v)
                       {
                           dialog.dismiss();

                       }
                   });

                   t4.setOnClickListener(new View.OnClickListener() {
                       @Override
                       public void onClick(View v)
                       {

                           Intent intent=new Intent(viewHolder.itemView.getContext(),ImageviewerActivity.class);
                           intent.putExtra("url",messageslist.get(position).getImageurl());
                           viewHolder.itemView.getContext().startActivity(intent);


                       }
                   });

                   dialog.show();

                   return false;


               }
           });

         viewHolder.senderPDF.setOnLongClickListener(new View.OnLongClickListener() {
             @Override
             public boolean onLongClick(View v)
             { View view= LayoutInflater.from(context).inflate(R.layout.delete_dialog_pdf,null);
                 TextView t1=view.findViewById(R.id.everyonepdf);
                 TextView t2=view.findViewById(R.id.delmepdf);
                 TextView t3=view.findViewById(R.id.delcancelpdf);
                 TextView t4=view.findViewById(R.id.viewpdf);

                 AlertDialog dialog=new AlertDialog.Builder(context)
                         .setTitle("Delete Message")
                         .setView(view)
                         .create();


                 t1.setOnClickListener(new View.OnClickListener() {
                     @Override
                     public void onClick(View v) {

                         messages.setMessage("This message is removed");
                         messages.setFeeling(-1);
                         viewHolder.setfeeling.setVisibility(View.INVISIBLE);
                         FirebaseDatabase.getInstance().getReference().child("Chats").child(senderRoom).child("Messages")
                                 .child(messages.getMessageID()).setValue(messages);

                         FirebaseDatabase.getInstance().getReference().child("Chats").child(receiverRoom).child("Messages")
                                 .child(messages.getMessageID()).setValue(messages);

                         dialog.dismiss();
                     }
                 });

                 t2.setOnClickListener(new View.OnClickListener() {
                     @Override
                     public void onClick(View v)
                     {


                         FirebaseDatabase.getInstance().getReference().child("Chats").child(senderRoom).child("Messages")
                                 .child(messages.getMessageID()).setValue(null);

                         dialog.dismiss();


                     }
                 });

                 t3.setOnClickListener(new View.OnClickListener() {
                     @Override
                     public void onClick(View v)
                     {
                         dialog.dismiss();

                     }
                 });

                 t4.setOnClickListener(new View.OnClickListener() {
                     @Override
                     public void onClick(View v)
                     {

                         Intent intent=new Intent(Intent.ACTION_VIEW, Uri.parse(messageslist.get(position).getPdfurl()));
                         viewHolder.itemView.getContext().startActivity(intent);


                     }
                 });

                 dialog.show();


                 return false;
             }
         });

         viewHolder.sendervideos.setOnLongClickListener(new View.OnLongClickListener() {
             @Override
             public boolean onLongClick(View v)
             {

                 View view= LayoutInflater.from(context).inflate(R.layout.delete_dialog_sender_videos,null);
                 TextView t1=view.findViewById(R.id.everyonevideo);
                 TextView t2=view.findViewById(R.id.delmevideo);
                 TextView t3=view.findViewById(R.id.delcancelvideo);
                 TextView t4=view.findViewById(R.id.viewvideo);

                 AlertDialog dialog=new AlertDialog.Builder(context)
                         .setTitle("Delete Message")
                         .setView(view)
                         .create();


                 t1.setOnClickListener(new View.OnClickListener() {
                     @Override
                     public void onClick(View v) {

                         messages.setMessage("This message is removed");
                         messages.setFeeling(-1);
                         viewHolder.setfeeling.setVisibility(View.INVISIBLE);

                         FirebaseDatabase.getInstance().getReference().child("Chats").child(senderRoom).child("Messages")
                                 .child(messages.getMessageID()).setValue(messages);

                         FirebaseDatabase.getInstance().getReference().child("Chats").child(receiverRoom).child("Messages")
                                 .child(messages.getMessageID()).setValue(messages);

                         dialog.dismiss();
                     }
                 });

                 t2.setOnClickListener(new View.OnClickListener() {
                     @Override
                     public void onClick(View v)
                     {


                         FirebaseDatabase.getInstance().getReference().child("Chats").child(senderRoom).child("Messages")
                                 .child(messages.getMessageID()).setValue(null);

                         dialog.dismiss();


                     }
                 });

                 t3.setOnClickListener(new View.OnClickListener() {
                     @Override
                     public void onClick(View v)
                     {
                         dialog.dismiss();

                     }
                 });

                 t4.setOnClickListener(new View.OnClickListener() {
                     @Override
                     public void onClick(View v)
                     {

                         Intent intent=new Intent(viewHolder.itemView.getContext(),VideoActivity.class);
                         intent.putExtra("url",messageslist.get(position).getVideourl());
                         viewHolder.itemView.getContext().startActivity(intent);

                     }
                 });

                 dialog.show();



                 return false;
             }
         });

        } else
            {
                RecieverViewHolder viewHolder=(RecieverViewHolder) holder;

                viewHolder.receiversImage.setVisibility(GONE);
                viewHolder.receiverMessageText.setVisibility(GONE);
                viewHolder.receivervideo.setVisibility(GONE);
                viewHolder.receiversPDF.setVisibility(GONE);

                if (messages.getMessage().equals("photo"))
                {

                    viewHolder.receiversImage.setVisibility(View.VISIBLE);
                    Picasso.get().load(messages.getImageurl()).into(viewHolder.receiversImage);
                    @SuppressLint("SimpleDateFormat") SimpleDateFormat dateFormat=new SimpleDateFormat("hh:mm a");
                    viewHolder.receivertimetxt.setText(dateFormat.format(new Date(messages.getTimestamp())));
                   /* viewHolder.receiversImage.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v)
                        {

                            Intent intent=new Intent(viewHolder.itemView.getContext(),ImageviewerActivity.class);
                            intent.putExtra("url",messageslist.get(position).getImageurl());
                            viewHolder.itemView.getContext().startActivity(intent);


                        }
                    });*/


                }
                else if ((messages.getMessage().equals("pdf")) || (messages.getMessage().equals("docx")))
                {
                    viewHolder.setreceiverfeeling.setVisibility(View.INVISIBLE);
                    viewHolder.receiversPDF.setVisibility(View.VISIBLE);
                    viewHolder.receiversPDF.setBackgroundResource(R.drawable.file);
                    SimpleDateFormat dateFormat=new SimpleDateFormat("hh:mm a");
                    viewHolder.receivertimetxt.setText(dateFormat.format(new Date(messages.getTimestamp())));
                   /* viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v)
                        {

                            Intent intent=new Intent(Intent.ACTION_VIEW, Uri.parse(messageslist.get(position).getPdfurl()));
                            viewHolder.itemView.getContext().startActivity(intent);

                        }
                    });*/
                }
                else if (messages.getMessage().equals("videos"))
                {
                    viewHolder.setreceiverfeeling.setVisibility(View.INVISIBLE);
                    viewHolder.receivervideo.setVisibility(View.VISIBLE);
                    @SuppressLint("SimpleDateFormat") SimpleDateFormat dateFormat=new SimpleDateFormat("hh:mm a");
                    viewHolder.receivertimetxt.setText(dateFormat.format(new Date(messages.getTimestamp())));
                    viewHolder.receivervideo.setVideoPath(messages.getVideourl());
                    MediaController mediaController=new MediaController(this.context);

                    mediaController.setAnchorView(viewHolder.receivervideo);
                    viewHolder.receivervideo.setMediaController(mediaController);

                }
                else
                {
                    @SuppressLint("SimpleDateFormat") SimpleDateFormat dateFormat=new SimpleDateFormat("hh:mm a");
                    viewHolder.receiverMessageText.setVisibility(View.VISIBLE);
                    viewHolder.receiverMessageText.setLinksClickable(true);
                    viewHolder.receiverMessageText.setText(messages.getMessage());
                    viewHolder.receivertimetxt.setText(dateFormat.format(new Date(messages.getTimestamp())));

                }


                if (((messages.getType().equals("text")) || (messages.getMessage().equals("photo"))) && (messageslist.get(position).getFeeling() == -1))
                   {

                    viewHolder.setreceiverfeeling.setOnTouchListener(new View.OnTouchListener() {
                        @Override
                        public boolean onTouch(View v, MotionEvent event) {


                                popup.onTouch(v, event);
                                return false;
                        }
                    });

                       viewHolder.setreceiverfeeling.setOnTouchListener(new View.OnTouchListener() {
                           @Override
                           public boolean onTouch(View v, MotionEvent event) {


                                   popup.onTouch(v, event);
                                   return false;
                           }
                       });


                   }
                   if (((messages.getType().equals("text")) || (messages.getMessage().equals("photo"))) && (messageslist.get(position).getFeeling() >= 0))
                   {
                    viewHolder.recieversemoji.setImageResource(reactions[messageslist.get(position).getFeeling()]);
                    viewHolder.recieversemoji.setVisibility(View.VISIBLE);
                   }
                   else
                   {
                    viewHolder.recieversemoji.setVisibility(GONE);
                   }


                viewHolder.receiverMessageText.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v)
                    {

                        viewHolder.receiverMessageText.setOnClickListener(null);

                        View view= LayoutInflater.from(context).inflate(R.layout.delete_dialog_reciever,null);
                        TextView t2=view.findViewById(R.id.delmeforreciever);
                        TextView t3=view.findViewById(R.id.delcancelforreceiver);

                        AlertDialog dialog=new AlertDialog.Builder(context)
                                .setTitle("Delete Message")
                                .setView(view)
                                .create();



                        t2.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v)
                            {


                                FirebaseDatabase.getInstance().getReference().child("Chats").child(senderRoom).child("Messages")
                                        .child(messages.getMessageID()).setValue(null);

                                dialog.dismiss();


                            }
                        });

                        t3.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v)
                            {
                                dialog.dismiss();

                            }
                        });

                        dialog.show();

                        return false;


                    }
                });


              viewHolder.receiversImage.setOnLongClickListener(new View.OnLongClickListener() {
                  @Override
                  public boolean onLongClick(View v)
                  {

                      viewHolder.receiversImage.setOnClickListener(null);

                      View view= LayoutInflater.from(context).inflate(R.layout.delete_dialog_receiver_image,null);
                      TextView t2=view.findViewById(R.id.delmeimagereciever);
                      TextView t3=view.findViewById(R.id.delcancelimagereceiver);
                      TextView t4=view.findViewById(R.id.viewimagereciever);

                      AlertDialog dialog=new AlertDialog.Builder(context)
                              .setTitle("Delete Message")
                              .setView(view)
                              .create();



                      t2.setOnClickListener(new View.OnClickListener() {
                          @Override
                          public void onClick(View v)
                          {


                              FirebaseDatabase.getInstance().getReference().child("Chats").child(senderRoom).child("Messages")
                                      .child(messages.getMessageID()).setValue(null);

                              dialog.dismiss();


                          }
                      });

                      t3.setOnClickListener(new View.OnClickListener() {
                          @Override
                          public void onClick(View v)
                          {
                              dialog.dismiss();

                          }
                      });


                      t4.setOnClickListener(new View.OnClickListener() {
                          @Override
                          public void onClick(View v)
                          {

                              Intent intent=new Intent(viewHolder.itemView.getContext(),ImageviewerActivity.class);
                              intent.putExtra("url",messageslist.get(position).getImageurl());
                              viewHolder.itemView.getContext().startActivity(intent);


                          }
                      });

                      dialog.show();

                      return false;
                  }
              });


              viewHolder.receiversPDF.setOnLongClickListener(new View.OnLongClickListener() {
                  @Override
                  public boolean onLongClick(View v)
                  {

                      View view= LayoutInflater.from(context).inflate(R.layout.delete_dialog_receivers_pdf,null);
                      TextView t2=view.findViewById(R.id.delmepdfreciever);
                      TextView t3=view.findViewById(R.id.delcancelpdfreceiver);
                      TextView t4=view.findViewById(R.id.viewpdfreciever);

                      AlertDialog dialog=new AlertDialog.Builder(context)
                              .setTitle("Delete Message")
                              .setView(view)
                              .create();



                      t2.setOnClickListener(new View.OnClickListener() {
                          @Override
                          public void onClick(View v)
                          {


                              FirebaseDatabase.getInstance().getReference().child("Chats").child(senderRoom).child("Messages")
                                      .child(messages.getMessageID()).setValue(null);

                              dialog.dismiss();


                          }
                      });

                      t3.setOnClickListener(new View.OnClickListener() {
                          @Override
                          public void onClick(View v)
                          {
                              dialog.dismiss();

                          }
                      });


                      t4.setOnClickListener(new View.OnClickListener() {
                          @Override
                          public void onClick(View v)
                          {

                              Intent intent=new Intent(Intent.ACTION_VIEW, Uri.parse(messageslist.get(position).getPdfurl()));
                              viewHolder.itemView.getContext().startActivity(intent);

                          }
                      });

                      dialog.show();


                      return false;
                  }
              });

              viewHolder.receivervideo.setOnLongClickListener(new View.OnLongClickListener() {
                  @Override
                  public boolean onLongClick(View v)
                  {

                      View view= LayoutInflater.from(context).inflate(R.layout.delete_dialog_receiver_video,null);
                      TextView t2=view.findViewById(R.id.delmevideoreciever);
                      TextView t3=view.findViewById(R.id.delcancelvideoreceiver);
                      TextView t4=view.findViewById(R.id.viewvideoreciever);

                      AlertDialog dialog=new AlertDialog.Builder(context)
                              .setTitle("Delete Message")
                              .setView(view)
                              .create();



                      t2.setOnClickListener(new View.OnClickListener() {
                          @Override
                          public void onClick(View v)
                          {


                              FirebaseDatabase.getInstance().getReference().child("Chats").child(senderRoom).child("Messages")
                                      .child(messages.getMessageID()).setValue(null);

                              dialog.dismiss();


                          }
                      });

                      t3.setOnClickListener(new View.OnClickListener() {
                          @Override
                          public void onClick(View v)
                          {
                              dialog.dismiss();

                          }
                      });


                      t4.setOnClickListener(new View.OnClickListener() {
                          @Override
                          public void onClick(View v)
                          {

                              Intent intent=new Intent(viewHolder.itemView.getContext(),VideoActivity.class);
                              intent.putExtra("url",messageslist.get(position).getVideourl());
                              viewHolder.itemView.getContext().startActivity(intent);


                          }
                      });

                      dialog.show();


                      return false;
                  }
              });

            }

    }

    @Override
    public int getItemCount() {
        return messageslist.size();
    }


    static class GestureListener extends GestureDetector.SimpleOnGestureListener{

        @Override
        public void onLongPress(MotionEvent e)
        {


            super.onLongPress(e);
        }


    }


    public static class SentViewHolder extends RecyclerView.ViewHolder {


        public TextView senderMessageText,sendertimetxt;
        public CircularImageView receiverProfileImage;
        public VideoView sendervideos;
        public ImageView sendersemoji,sendersImage,senderseenimage,senderseenimage2,senderPDF,setfeeling;


        public SentViewHolder(@NonNull View itemView) {
            super(itemView);

            senderMessageText = (TextView) itemView.findViewById(R.id.sendermessage);
            sendersemoji = (ImageView) itemView.findViewById(R.id.senderfeeling);
            sendersImage=(ImageView)itemView.findViewById(R.id.senderimage);
            sendertimetxt=(TextView)itemView.findViewById(R.id.sendertimetext);
            senderseenimage=(ImageView)itemView.findViewById(R.id.senderseen);
            senderseenimage2=(ImageView)itemView.findViewById(R.id.senderseen2);
            senderPDF=(ImageView)itemView.findViewById(R.id.senderpdf);
            sendervideos=(VideoView)itemView.findViewById(R.id.sendervideo);
            setfeeling=(ImageView)itemView.findViewById(R.id.senderfeelingset);
        }
    }

    public static class RecieverViewHolder extends RecyclerView.ViewHolder {

        public TextView receiverMessageText,receivertimetxt;
        public ImageView recieversemoji,receiversImage,receiversPDF,setreceiverfeeling;
        public VideoView receivervideo;

        public RecieverViewHolder(@NonNull View itemView) {
            super(itemView);

            receiverMessageText = (TextView) itemView.findViewById(R.id.recievermessage);
            recieversemoji = (ImageView) itemView.findViewById(R.id.recieverfeeling);
            receiversImage=(ImageView)itemView.findViewById(R.id.receiverimage);
            receivertimetxt=(TextView)itemView.findViewById(R.id.receivertimetext);
            receiversPDF=(ImageView)itemView.findViewById(R.id.receiverpdf);
            receivervideo=(VideoView)itemView.findViewById(R.id.receiversvideos);
            setreceiverfeeling=(ImageView)itemView.findViewById(R.id.receiverfeelingset);
        }
    }




}


