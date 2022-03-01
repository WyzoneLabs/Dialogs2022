package com.brimbay.chat;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.brimbay.dialogx.dialogs.MessageDialog;
import com.brimbay.dialogx.interfaces.OnDialogButtonClickListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.brimbay.chat.databinding.ActivityMainBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

import fragments.main.HomeFragment;
import fragments.video.JoinChannelVideo;
import models.ChatRequest;
import models.Friend;
import models.Message;
import models.User;
import utils.AppExecutors;
import utils.Constants;
import utils.DateUtils;

public class MainActivity extends AppCompatActivity {
	private MainActivity selfRef;
	
	private static final String TAG = MainActivity.class.getSimpleName();
	
	private ActivityMainBinding binding;
	private FragmentManager mFragmentManager;
	private FirebaseAuth mAuth;
	
	private DatabaseReference mChatRequestDBParentRef;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		selfRef = this;
		mAuth = FirebaseAuth.getInstance();
		
		mChatRequestDBParentRef = FirebaseDatabase.getInstance().getReference(Constants.FB_CHAT_REQUEST_BD);
		
		mFragmentManager = getSupportFragmentManager();
		binding = ActivityMainBinding.inflate(getLayoutInflater());
		setContentView(binding.getRoot());
		initUI();
		listenToChatRequests(mAuth.getUid());
		
		HomeFragment fragment = HomeFragment.newInstance();
		addFragment(fragment,null);
	}
	
	//region Member Methods
	private void initUI(){
//		binding.toolbarBox.avatar.setOnClickListener(view -> onBackPressed());
		binding.toolbarBox.avatar.setOnClickListener(view -> {
			Friend friend = new Friend();
			friend.id = Objects.equals(mAuth.getUid(), "wCiuoykn8VVAl9TPrzSj3uu2kKk1") ?"qkNDAviF2QV1AIralt6hpyTGVac2":"wCiuoykn8VVAl9TPrzSj3uu2kKk1";
			friend.first_name = "Test";
			friend.last_name = "One";
			friend.phone = "+6736767840033";
			friend.room_id = friend.getRoomId(mAuth.getUid());
//
//			Intent intent = new Intent(selfRef, MessagingActivity.class);
//			intent.setAction(MessagingActivity.INTENT_CHAT_ACTION);
//			intent.putExtra(MessagingActivity.INTENT_CHAT_FRIEND_DATA,friend);
//			startActivity(intent);
			
			sendChatRequest(friend, mAuth.getUid());
		});
		binding.toolbarBox.title.setText(getString(R.string.app_name));
	}
	
	private void addFragment(Fragment fragment, @Nullable String tag){
		FragmentTransaction transaction = mFragmentManager.beginTransaction();
		transaction.replace(R.id.mn_body, fragment, tag != null?tag:"current_page").commitNow();
	}
	//endregion
	
	//region Firebase
	private void sendChatRequest(Friend friend,String uid){
		if (friend == null || friend.id.equals("") || uid == null) return;
		String room_id = friend.getRoomId(uid);
		
		DatabaseReference db = mChatRequestDBParentRef.child(friend.id);
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
								Toast.makeText(selfRef, "Your chat request was rejected by "+friend.first_name, Toast.LENGTH_SHORT).show();
								snapshot.child(uid).getRef().removeValue();
							}else if(req.is_accepted == ChatRequest.ACCEPTED){
								Intent intent = new Intent(selfRef, MessagingActivity.class);
								intent.setAction(MessagingActivity.INTENT_CHAT_ACTION);
								intent.putExtra(MessagingActivity.INTENT_CHAT_FRIEND_DATA,friend);
								startActivity(intent);
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
	
	private void listenToChatRequests(String uid){
		FirebaseDatabase.getInstance().getReference(Constants.FB_CHAT_REQUEST_BD).child(uid).addChildEventListener(new ChildEventListener() {
			@Override
			public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
				ChatRequest chatRequest = snapshot.getValue(ChatRequest.class);
				if (chatRequest != null ) {
					Log.d(TAG, "Room ID: " + chatRequest.room_id);
					new MessageDialog("Chat Request","You have a new chat request")
							.setCancelButton("Reject")
							.setOkButton("Accept")
							.setCancelButtonClickListener((baseDialog, v) -> {
								snapshot.getRef().child("is_accepted").setValue(ChatRequest.REJECTED);
								snapshot.getRef().child("timestamp").setValue(System.currentTimeMillis());
								return false;
							})
							.setOkButtonClickListener((baseDialog, v) -> {
								snapshot.getRef().child("is_accepted").setValue(ChatRequest.ACCEPTED);
								snapshot.getRef().child("timestamp").setValue(System.currentTimeMillis());
								
								Intent intent = new Intent(selfRef, MessagingActivity.class);
								intent.setAction(MessagingActivity.INTENT_CHAT_ACTION);
								intent.putExtra(MessagingActivity.INTENT_CHAT_REQUEST_DATA,chatRequest);
								startActivity(intent);
								return false;
							})
							.show();
				}
			}
			
			@Override
			public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
			
			}
			
			@Override
			public void onChildRemoved(@NonNull DataSnapshot snapshot) {
			
			}
			
			@Override
			public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
			
			}
			
			@Override
			public void onCancelled(@NonNull DatabaseError error) {
			
			}
		});
	}
	
	//endregion
	
}
