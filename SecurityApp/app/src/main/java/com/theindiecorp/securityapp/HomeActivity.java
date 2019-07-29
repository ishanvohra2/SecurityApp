package com.theindiecorp.securityapp;

import android.app.Notification;
import android.location.Location;
import android.os.Bundle;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.annotation.NonNull;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.theindiecorp.securityapp.Data.SecurityApp;
import com.theindiecorp.securityapp.Data.SosDetails;
import com.theindiecorp.securityapp.Fragments.AccountFragment;
import com.theindiecorp.securityapp.Fragments.HomeFragment;
import com.theindiecorp.securityapp.Fragments.NotificationFragment;

import java.util.ArrayList;

public class HomeActivity extends AppCompatActivity {

    private void loadFragment(Fragment fragment) {
        // load fragment
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.home_frame_container, fragment);
        transaction.commit();
    }



    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    loadFragment(new HomeFragment());
                    return true;
                case R.id.account:
                    loadFragment(new AccountFragment());
                    return true;
                case R.id.notifications:
                    loadFragment(new NotificationFragment());
                    return true;
            }
            return false;
        }
    };

    private NotificationManagerCompat notificationManager;

    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();

    public static ArrayList<SosDetails> sosDetails = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        BottomNavigationView navView = findViewById(R.id.nav_view);
        navView.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        notificationManager = NotificationManagerCompat.from(this);

        databaseReference.child("sosDetails").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                sosDetails = new ArrayList<>();
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    SosDetails s = snapshot.getValue(SosDetails.class);
                    s.setId(snapshot.getKey());
                    if(!s.getUserId().equals(FirebaseAuth.getInstance().getCurrentUser().getUid()) && !s.isSafe()){
                        Location userLoc = new Location("SOS");
                        userLoc.setLatitude(s.getLat());
                        userLoc.setLongitude(s.getLng());
                        if(userLoc.distanceTo(HomeFragment.currentLocation) <=3000){
                            sosDetails.add(s);
                            sendNotification();
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        loadFragment(new HomeFragment());
    }

    private void sendNotification() {

        Notification notification = new NotificationCompat.Builder(this, SecurityApp.CHANNEL_1_ID)
                .setSmallIcon(R.drawable.sos)
                .setContentTitle("Help Alert!")
                .setContentText("Someone needs help.")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .build();

        notificationManager.notify(1, notification);
    }
}
