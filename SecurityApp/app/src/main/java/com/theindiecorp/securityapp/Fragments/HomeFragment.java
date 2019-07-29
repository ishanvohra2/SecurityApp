package com.theindiecorp.securityapp.Fragments;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.google.android.gms.dynamic.IObjectWrapper;
import com.google.android.gms.internal.maps.zzt;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.theindiecorp.securityapp.Data.SosDetails;
import com.theindiecorp.securityapp.Data.UserLocation;
import com.theindiecorp.securityapp.R;
import com.theindiecorp.securityapp.SosActivity;

import java.util.ArrayList;
import java.util.Iterator;

public class HomeFragment extends Fragment implements OnMapReadyCallback {

    GoogleMap map;
    Context mContext;
    FusedLocationProviderClient fusedLocationProviderClient;
    public static Location currentLocation;
    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COARSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private Boolean locationPermissionsGranted = false;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1234;
    public static final float DEFAULT_ZOOM = 15f;

    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
    ArrayList<MarkerOptions> markers = new ArrayList<>();


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_home, container, false);

        getLocationPermission();
        getDeviceLocation();

        databaseReference.child("userLocation").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                markers = new ArrayList<>();
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    if(!snapshot.getKey().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())){
                        UserLocation userLocation = new UserLocation();
                        userLocation.setId(snapshot.getKey());
                        userLocation.setLat(snapshot.child("lat").getValue(Double.class));
                        userLocation.setLng(snapshot.child("lng").getValue(Double.class));
                        MarkerOptions m = new MarkerOptions().position(new LatLng(userLocation.getLat(),userLocation.getLng())).title(userLocation.getId());
                        if (!markers.contains(m)) {
                            markers.add(m);
                        }
                        Iterator<MarkerOptions> iterator = markers.iterator();
                        while (iterator.hasNext()){
                            if(!iterator.next().getTitle().equals(m.getTitle()))
                                map.addMarker(iterator.next());
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        CircularImageView sosBtn = view.findViewById(R.id.sosBtn);
        sosBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String id = databaseReference.push().getKey();
                SosDetails sosDetails = new SosDetails();
                sosDetails.setUserId(FirebaseAuth.getInstance().getCurrentUser().getUid());
                sosDetails.setLat(currentLocation.getLatitude());
                sosDetails.setLng(currentLocation.getLongitude());
                sosDetails.setNumberOfVolunteers(0);
                sosDetails.setSafe(false);
                databaseReference.child("sosDetails").child(id).setValue(sosDetails);

                startActivity(new Intent(getContext(), SosActivity.class)
                .putExtra("sosId",id));
            }
        });

        CountDownTimer countDownTimer = new CountDownTimer(3000,1000) {
            @Override
            public void onTick(long l) {

            }

            @Override
            public void onFinish() {

                if(currentLocation!=null){
                        UserLocation userLocation = new UserLocation();
                        userLocation.setLat(currentLocation.getLatitude());
                        userLocation.setLng(currentLocation.getLongitude());
                        databaseReference.child("userLocation").child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                .setValue(userLocation);
                }
            }
        }.start();

        return view;
    }

    private void getLocationPermission() {
        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};

        if (ContextCompat.checkSelfPermission(mContext.getApplicationContext(), FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if (ContextCompat.checkSelfPermission(mContext.getApplicationContext(), COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                locationPermissionsGranted = true;
                initMap();
            }
        } else {
            ActivityCompat.requestPermissions((Activity) mContext, permissions, LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        locationPermissionsGranted = false;

        switch (requestCode) {
            case LOCATION_PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0) {
                    for (int i = 0; i < grantResults.length; i++) {
                        if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                            locationPermissionsGranted = false;
                            return;
                        }
                    }
                    locationPermissionsGranted = true;
                    //Initialize the map
                    initMap();
                }
        }
    }

    private void initMap() {
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.map = googleMap;
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        map.setMyLocationEnabled(true);
        if(currentLocation !=null){
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(currentLocation.getLatitude(),currentLocation.getLongitude()), 15.0f));z
        }
    }

    private void getDeviceLocation(){
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(mContext);

        try{
            final Task location = fusedLocationProviderClient.getLastLocation();
            location.addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if(task.isSuccessful()){
                        currentLocation = (Location) task.getResult();
                    }
                }
            });
        }catch (SecurityException e){
            Log.e("LocationActivity: ", e.getMessage() );
        }
    }

}
