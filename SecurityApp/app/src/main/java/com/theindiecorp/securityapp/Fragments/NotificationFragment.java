package com.theindiecorp.securityapp.Fragments;

import android.app.Notification;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.theindiecorp.securityapp.Adapter.NotificationAdapter;
import com.theindiecorp.securityapp.Data.SecurityApp;
import com.theindiecorp.securityapp.Data.SosDetails;
import com.theindiecorp.securityapp.HomeActivity;
import com.theindiecorp.securityapp.R;

import java.util.ArrayList;

public class NotificationFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_notification,container,false);

        RecyclerView recyclerView = view.findViewById(R.id.notification_lists);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        final NotificationAdapter notificationAdapter = new NotificationAdapter(getContext(),new ArrayList<SosDetails>());
        recyclerView.setAdapter(notificationAdapter);
        
        notificationAdapter.setSosDetails(HomeActivity.sosDetails);
        notificationAdapter.notifyDataSetChanged();

        return view;
    }
}
