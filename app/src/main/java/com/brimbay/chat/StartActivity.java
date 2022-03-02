package com.brimbay.chat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import database.AppDatabase;
import models.User;
import utils.AppExecutors;
import utils.Constants;

public class StartActivity extends AppCompatActivity {
	private StartActivity selfRef;
	
	private FirebaseAuth mAuth;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		selfRef = this;
		mAuth = FirebaseAuth.getInstance();
		setContentView(R.layout.activity_start);
		
		new Handler(Looper.getMainLooper()).postDelayed(() -> {
			AppExecutors.getInstance().diskIO().execute(() -> {
				if(mAuth == null || mAuth.getUid() == null){
					startActivity(new Intent(selfRef, AccountActivity.class));
				}else{
					User user = AppDatabase.newInstance(selfRef).userDao().findByID(mAuth.getUid());
					if (user != null) {
						startActivity(new Intent(selfRef, MainActivity.class));
					}else{
						mAuth.signOut();
						startActivity(new Intent(selfRef, AccountActivity.class));
					}
				}
				finishAffinity();
			});
		},2000);
	}
	
}
