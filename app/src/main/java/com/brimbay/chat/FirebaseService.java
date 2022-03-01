package com.brimbay.chat;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;

public class FirebaseService extends Service {
	private static final String TAG = FirebaseService.class.getSimpleName();
	public FirebaseService() {
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.d(TAG, "Service Started");
		return START_STICKY_COMPATIBILITY;
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	
//	private void sendChatRequest(){
//		String key = mChatSessionReference.push().getKey();
//		if (key != null) {
//			mChatSessionReference.child(key).setValue(true).addOnSuccessListener(new OnSuccessListener<Void>() {
//				@Override
//				public void onSuccess(Void unused) {
//					Toast.makeText(selfRef, "Chat session successfully sent", Toast.LENGTH_SHORT).show();
//				}
//			});
//		}
//	}
}
