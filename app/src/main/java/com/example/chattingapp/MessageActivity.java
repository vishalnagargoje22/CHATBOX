package com.example.chattingapp;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.chattingapp.Adapter.MessageAdapter;
import com.example.chattingapp.Model.Chat;
import com.example.chattingapp.Model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessageActivity extends AppCompatActivity {
    CircleImageView profile_image;
    FirebaseUser fuser;
    TextView username;
    DatabaseReference reference;
    Intent intent;
    ImageButton btn_send;
    EditText txt_send;
    MessageAdapter messageAdapter;
    String userid;
    List<Chat> mchat;
    RecyclerView recyclerView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        Toolbar toolbar= findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            finish();
            }
        });


        profile_image= findViewById(R.id.profile_image);
        username= findViewById(R.id.username);
        btn_send = findViewById(R.id.btn_send);
        txt_send= findViewById(R.id.text_send);
        recyclerView=findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager= new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setStackFromEnd(true);

        recyclerView.setLayoutManager(linearLayoutManager);
        intent= getIntent();
        userid=intent.getStringExtra("userid");
        fuser= FirebaseAuth.getInstance().getCurrentUser();

        btn_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String msg= txt_send.getText().toString();
                if(!msg.equals("")){
                    sendMessage(fuser.getUid(),userid,msg);
                }else{
                    Toast.makeText(MessageActivity.this, "You can't send empty ,messages", Toast.LENGTH_SHORT).show();
                }
                txt_send.setText("");
            }
        });

        reference= FirebaseDatabase.getInstance().getReference("Users").child(userid);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user= snapshot.getValue(User.class);
            username.setText(user.getUsername());
            if (user.getImageURL().equals("default")){
                profile_image.setImageResource(R.mipmap.ic_launcher);
            }
            else{
                Glide.with(MessageActivity.this).load(user.getImageURL());
            }
            readMessages(fuser.getUid(),userid,user.getImageURL());

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    private void sendMessage(String sender, String receiver,String message){

        DatabaseReference reference= FirebaseDatabase.getInstance().getReference();
        HashMap<String,Object> hashMap= new HashMap<>();
        hashMap.put("sender", sender);
        hashMap.put("receiver",receiver);
        hashMap.put("message",message);
        reference.child("Chats").push().setValue(hashMap);
// add user to chat fragment
    final DatabaseReference ChatRef= FirebaseDatabase.getInstance().getReference("Chatlist").child(fuser.getUid()).child(userid);
    ChatRef.addListenerForSingleValueEvent(new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot snapshot) {
            if(!snapshot.exists()){
                ChatRef.child("id").setValue(userid);

            }
        }

        @Override
        public void onCancelled(@NonNull DatabaseError error) {

        }
    });

    }
    private void readMessages(final String myid, final String userid, final String imageurl){
        mchat= new ArrayList<>();
        reference= FirebaseDatabase.getInstance().getReference("Chats");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                mchat.clear();
                for (DataSnapshot snapshot1: snapshot.getChildren()){
                    Chat chat= snapshot1.getValue(Chat.class);
                    if (chat.getReceiver().equals(myid) && chat.getSender().equals(userid)||chat.getReceiver().equals(userid)&& chat.getSender().equals(myid)){
                        mchat.add(chat);
                    }

                }

                messageAdapter= new MessageAdapter(MessageActivity.this, mchat, imageurl);
                recyclerView.setAdapter(messageAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}