package com.bits.securityapp.Fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bits.securityapp.Adapter.NotificationAdapter;
import com.bits.securityapp.Data.SosDetails;
import com.bits.securityapp.HomeActivity;
import com.bits.securityapp.R;

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
