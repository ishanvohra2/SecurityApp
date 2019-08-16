package com.theindiecorp.securityapp.Fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.theindiecorp.securityapp.Data.User;
import com.theindiecorp.securityapp.HomeActivity;
import com.theindiecorp.securityapp.LoginActivity;
import com.theindiecorp.securityapp.R;

import java.util.concurrent.TimeUnit;

import static com.theindiecorp.securityapp.LoginActivity.userId;

public class AccountFragment extends Fragment {

    private EditText name,phone,otp;
    private EditText emergency1,emergency2,emergency3;
    private TextView saveBtn, verifyBtn;
    private String codeSent;

    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_account,container,false);

        name = view.findViewById(R.id.name);
        phone = view.findViewById(R.id.phone_number);
        otp = view.findViewById(R.id.otp);
        saveBtn = view.findViewById(R.id.update);
        verifyBtn = view.findViewById(R.id.verify_otp);
        emergency1 = view.findViewById(R.id.emergency_contact1);
        emergency2 = view.findViewById(R.id.emergency_contact2);
        emergency3 = view.findViewById(R.id.emergency_contact3);

        final String[] phoneTxt = new String[1];

        databaseReference.child("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                phone.setText(dataSnapshot.child("phoneNo").getValue(String.class));
                name.setText(dataSnapshot.child("name").getValue(String.class));
                phoneTxt[0] = dataSnapshot.child("phoneNo").getValue(String.class);
                if(dataSnapshot.child("emergency1").exists()){
                    emergency1.setText(dataSnapshot.child("emergency1").getValue(String.class));
                }
                if(dataSnapshot.child("emergency2").exists()){
                    emergency2.setText(dataSnapshot.child("emergency2").getValue(String.class));
                }
                if(dataSnapshot.child("emergency3").exists()){
                    emergency3.setText(dataSnapshot.child("emergency3").getValue(String.class));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(phone.getText().toString().equals(phoneTxt[0])){
                    databaseReference.child("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("name").setValue(name.getText().toString());
                    databaseReference.child("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("phoneNo").setValue(phone.getText().toString());
                    databaseReference.child("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("emergency3").setValue("+91" + emergency3.getText().toString());
                    databaseReference.child("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("emergency2").setValue("+91" + emergency2.getText().toString());
                    databaseReference.child("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("emergency1").setValue("+91" + emergency1.getText().toString());
                    Toast.makeText(getContext(),"Saved",Toast.LENGTH_LONG).show();
                }
                else{
                    otp.setVisibility(View.VISIBLE);
                    if (!phone.getText().toString().startsWith("+91"))
                        phone.setText(String.format("+91%s", phone.getText().toString()));
                    sendVerificationCode();
                }
            }
        });

        verifyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                verifySignInCode();
            }
        });

        Button logoutBtn = view.findViewById(R.id.logoutbtn);
        logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseAuth.getInstance().signOut();
                if(FirebaseAuth.getInstance()!=null){
                    startActivity(new Intent(getContext(),LoginActivity.class));
                }
            }
        });

        return view;
    }

    private void sendVerificationCode() {

        String phonetxt = phone.getText().toString();

        if (phonetxt.isEmpty()) {
            phone.setError("Phone no. is required");
            phone.requestFocus();
            otp.setVisibility(View.INVISIBLE);
            return;
        }

        if (phonetxt.length() < 10) {
            phone.setError("Enter valid phone number");
            phone.requestFocus();
            otp.setVisibility(View.INVISIBLE);
            return;
        }

        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phonetxt,
                60,
                TimeUnit.SECONDS,
                getActivity(),
                mCallbacks
        );

        Log.e("code", "sent");

        otp.setVisibility(View.VISIBLE);
        verifyBtn.setVisibility(View.VISIBLE);
    }

    PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        @Override
        public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {

        }

        @Override
        public void onVerificationFailed(FirebaseException e) {

        }

        @Override
        public void onCodeSent(String s, PhoneAuthProvider.ForceResendingToken forceResendingToken) {
            super.onCodeSent(s, forceResendingToken);
            codeSent = s;
        }
    };
    private void verifySignInCode() {
        String code = otp.getText().toString();
        if (code.length() != 6) {
            otp.setError("Enter a valid OTP");
            return;
        }

        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(codeSent, code);
        signInWithPhoneAuthCredential(credential);
        verifyBtn.setVisibility(View.GONE);
        Toast.makeText(getContext(),"Saved",Toast.LENGTH_LONG).show();
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        final FirebaseAuth auth = FirebaseAuth.getInstance();

        final String phonetxt = phone.getText().toString();
        auth.signInWithCredential(credential)
                .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            userId = auth.getCurrentUser().getUid();
                            User user = new User();
                            user.setName(name.getText().toString());
                            user.setPhoneNo(phonetxt);
                            user.setId(userId);
                            writeNewUser(user);

                        } else {
                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                // The verification code entered was invalid
                                Toast.makeText(getContext(), "Invalid code", Toast.LENGTH_LONG).show();
                            }
                        }
                    }
                });
    }

    private void writeNewUser(User user) {
            DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
            mDatabase.child("users").child(user.getId()).setValue(user);

    }
}
