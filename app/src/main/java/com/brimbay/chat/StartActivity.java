package com.brimbay.chat;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import com.google.firebase.auth.FirebaseAuth;

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
			if(mAuth == null || mAuth.getUid() == null){
				startActivity(new Intent(selfRef, AccountActivity.class));
			}else{
				startActivity(new Intent(selfRef, MainActivity.class));
			}
			finishAffinity();
		},2000);
	}
}
