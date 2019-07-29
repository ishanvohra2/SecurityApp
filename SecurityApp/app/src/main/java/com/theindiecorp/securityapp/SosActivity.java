package com.theindiecorp.securityapp;

import android.app.Notification;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.theindiecorp.securityapp.Adapter.VolunteersListAdapter;
import com.theindiecorp.securityapp.Data.SecurityApp;

import java.util.ArrayList;

public class SosActivity extends AppCompatActivity {

    private ImageView safeBtn;
    private RecyclerView volunteersList;
    private DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();

    private NotificationManagerCompat notificationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sos);

        notificationManager = NotificationManagerCompat.from(this);

        final String sosId = getIntent().getStringExtra("sosId");

        safeBtn = findViewById(R.id.mark_safe_btn);
        volunteersList = findViewById(R.id.volunteers_list);

        volunteersList.setLayoutManager(new LinearLayoutManager(this));

        final VolunteersListAdapter volunteersListAdapter = new VolunteersListAdapter(this,new ArrayList<String>());
        volunteersList.setAdapter(volunteersListAdapter);

        databaseReference.child("sosDetails").child(sosId).child("volunteers").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ArrayList<String> userIds = new ArrayList<>();
                if(dataSnapshot.exists()){
                    for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                        userIds.add(snapshot.getKey());
                        sendNotification();
                    }
                    volunteersListAdapter.setUserIds(userIds);
                    volunteersListAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        safeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                databaseReference.child("sosDetails").child(sosId).child("isSafe").setValue(true);
                startActivity(new Intent(SosActivity.this,HomeActivity.class));
                finish();
            }
        });
    }

    private void sendNotification() {

        Notification notification = new NotificationCompat.Builder(this, SecurityApp.CHANNEL_2_ID)
                .setSmallIcon(R.drawable.sos)
                .setContentTitle("SOS")
                .setContentText("Someone volunteered for help")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .build();

        notificationManager.notify(2, notification);
    }
}
