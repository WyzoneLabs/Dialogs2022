package utils;

import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.brimbay.chat.ChatActivity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import models.ChatRequest;
import models.Friend;

/**
 * Created by Kevine James on 3/2/2022.
 * Copyright (c) 2022 Brimbay. All rights reserved.
 */
public class ChatUtils {
	private final Context selfRef;
	
	public ChatUtils(Context context) {
		this.selfRef = context;
	}
	
	public void sendChatRequest(Friend friend, String uid){
		if (friend == null || friend.id.equals("") || uid == null) return;
		String room_id = friend.getRoomId(uid);
		
		DatabaseReference db =  FirebaseDatabase.getInstance().getReference(Constants.FB_CHAT_REQUEST_BD).child(friend.id);
		String key = db.push().getKey();
		if (key != null) {
			ChatRequest request = new ChatRequest(key,uid,friend.id, room_id, System.currentTimeMillis(),ChatRequest.PENDING);
			
			db.addListenerForSingleValueEvent(new ValueEventListener() {
				@Override
				public void onDataChange(@NonNull DataSnapshot snapshot) {
					if(snapshot.child(uid).exists()){
						ChatRequest req = snapshot.child(uid).getValue(ChatRequest.class);
						if (req != null && DateUtils.getTimePassed(req.timestamp) >= 60) {
							snapshot.child(uid).getRef().child("is_accepted").setValue(ChatRequest.PENDING);
							snapshot.child(uid).getRef().child("timestamp").setValue(System.currentTimeMillis());
							Toast.makeText(selfRef, "Chat session successfully sent", Toast.LENGTH_SHORT).show();
						}else{
							Toast.makeText(selfRef, "You have already sent chat request", Toast.LENGTH_SHORT).show();
						}
						return;
					}
					db.child(uid).setValue(request).addOnSuccessListener(unused -> Toast.makeText(selfRef, "Chat session successfully sent", Toast.LENGTH_SHORT).show());
				}
				
				@Override
				public void onCancelled(@NonNull DatabaseError error) {
				
				}
			});
			
			db.addValueEventListener(new ValueEventListener() {
				@Override
				public void onDataChange(@NonNull DataSnapshot snapshot) {
					if(snapshot.child(uid).exists()){
						ChatRequest req = snapshot.child(uid).getValue(ChatRequest.class);
						
						if (req != null ) {
							if(req.is_accepted == ChatRequest.REJECTED){
								Toast.makeText(selfRef, "Your chat request was rejected by "+friend.first_name, Toast.LENGTH_LONG).show();
								snapshot.child(uid).getRef().removeValue();
							}else if(req.is_accepted == ChatRequest.ACCEPTED){
								Intent intent = new Intent(selfRef,    ChatActivity.class);
								intent.setAction(   ChatActivity.INTENT_CHAT_ACTION);
								intent.putExtra(   ChatActivity.INTENT_CHAT_FRIEND_DATA,friend.id);
								selfRef.startActivity(intent);
								Toast.makeText(selfRef, "Your chat request was accepted", Toast.LENGTH_SHORT).show();
								snapshot.child(uid).getRef().removeValue();
							}
						}
					}
				}
				
				@Override
				public void onCancelled(@NonNull DatabaseError error) {
				
				}
			});
		}
	}
}
