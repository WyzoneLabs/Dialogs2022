package com.brimbay.chat;

import static utils.Util.getInitial;

import android.Manifest;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.brimbay.chat.databinding.ActivityMainBinding;
import com.brimbay.dialogx.dialogs.MessageDialog;
import com.brimbay.dialogx.util.TextInfo;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.hbb20.CountryCodePicker;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;

import contacts.ContactsGetterBuilder;
import contacts.entity.ContactData;
import contacts.entity.PhoneNumber;
import database.AppDatabase;
import fragments.main.HomeFragment;
import models.ChatRequest;
import models.Friend;
import models.User;
import utils.AppExecutors;
import utils.Constants;
import utils.DateUtils;
import utils.Util;

public class MainActivity extends AppCompatActivity {
	private MainActivity selfRef;
	
	private static final String TAG = MainActivity.class.getSimpleName();
	
	//region Views
	private ImageView _avatar;
	private TextView _initial;
	//endregion
	
	private ActivityMainBinding binding;
	private ActivityResultLauncher<String[]> launcherResult;
	private FragmentManager mFragmentManager;
	private FirebaseAuth mAuth;
	
	private DatabaseReference mChatRequestDBParentRef;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		selfRef = this;
		launcherResult = createPermissionResultLauncher();
		mAuth = FirebaseAuth.getInstance();
		
		mChatRequestDBParentRef = FirebaseDatabase.getInstance().getReference(Constants.FB_CHAT_REQUEST_BD);
		
		mFragmentManager = getSupportFragmentManager();
		binding = ActivityMainBinding.inflate(getLayoutInflater());
		setContentView(binding.getRoot());
		initUI();
		listenToChatRequests(mAuth.getUid());
		
		AppExecutors.getInstance().diskIO().execute(() -> {
			List<Friend> friends = AppDatabase.newInstance(selfRef).friendDao().getAll();
			runOnUiThread(() -> {
				if (friends == null || friends.size() == 0){
					launcherResult.launch(new String[]{Manifest.permission.READ_CONTACTS});
				}
			});
		});
		
		HomeFragment fragment = HomeFragment.newInstance();
		addFragment(fragment,null);
	}
	
	//region Member Methods
	private void initUI(){
		_initial = binding.toolbarBox.avatarInitial;
		_avatar = binding.toolbarBox.avatar;
		binding.toolbarBox.avatar.setOnClickListener(view -> {
			
		});
		binding.toolbarBox.title.setText(getString(R.string.app_name));
		setAvatar();
	}
	
	private void setAvatar(){
		AppExecutors.getInstance().diskIO().execute(() -> {
			User user = AppDatabase.newInstance(selfRef).userDao().findByID(mAuth.getUid());
			runOnUiThread(() -> {
				try {
					if (user.avatar == null) {
						_initial.setText(getInitial(user));
						_initial.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorAccent,null)));
						_initial.setVisibility(View.VISIBLE);
						binding.toolbarBox.avatarCnt.setVisibility(View.GONE);
					} else {
						Glide.with(selfRef)
								.load(user.avatar)
								.error(R.drawable.user)
								.into(_avatar);
						
						binding.toolbarBox.avatarCnt.setVisibility(View.VISIBLE);
						_initial.setVisibility(View.GONE);
					}
				}catch (NullPointerException e){
					e.printStackTrace();
				}
			});
		});
	}
	
	private void addFragment(Fragment fragment, @Nullable String tag){
		FragmentTransaction transaction = mFragmentManager.beginTransaction();
		transaction.replace(R.id.mn_body, fragment, tag != null?tag:"current_page").commitNow();
	}
	
	private ActivityResultLauncher<String[]> createPermissionResultLauncher() {
		return registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
			boolean granted = true;
			for (Map.Entry<String, Boolean> x : result.entrySet()) {
				if (!x.getValue()) granted = false;
			}
			if (granted) {
				getContacts();
			}else {
				MessageDialog messageDialog = new MessageDialog(
						getString(R.string.allow_permissions),
						getString(R.string.permissions_request_info),
						getString(R.string.allow)
				);
				messageDialog.setOkTextInfo(new TextInfo().setBold(true).setFontColor(getResources().getColor(R.color.colorAccent, null)));
				messageDialog.setOkButtonClickListener((baseDialog, v) -> {
					baseDialog.dismiss();
					launcherResult.launch(new String[]{Manifest.permission.READ_CONTACTS});
					return false;
				});
				messageDialog.show();
			}
		});
	}
	
	private void getContacts(){
		AppExecutors.getInstance().networkIO().execute(() -> {
			List<ContactData> contactData = new ContactsGetterBuilder(selfRef).allFields().buildList();
			for (ContactData c: contactData) {
				for(PhoneNumber p:c.getPhoneList()){
					String phone = p.getMainData();
					if (phone.length() > 9 && phone.length() < 16) {
						phone = phone.replace(" ","");
						phone = phone.replace("-","");
						phone = phone.substring(phone.length() - 8);
						
						findPhones(phone);
					}
				}
			}
		});
	}
	
	private void findPhones(String phone) {
		FirebaseDatabase.getInstance().getReference().child(Constants.FB_USER_BD)
				.addListenerForSingleValueEvent( new ValueEventListener () {
					@Override
					public void onDataChange ( @NonNull DataSnapshot dataSnapshot ) {
						AppExecutors.getInstance().networkIO().execute(() -> {
							if (dataSnapshot.getValue() != null ) {
								AppDatabase appDatabase = AppDatabase.newInstance(selfRef);
								for ( DataSnapshot d : dataSnapshot.getChildren()) {//385458
									User user = d.getValue( User.class);
									String id = d.getKey();
									
									if (user != null && user.phone != null && mAuth.getUid() != null && !Objects.equals(id, mAuth.getUid()) && user.phone.length() > 9) {
										if (user.phone.substring(user.phone.length() - 8 ).equals(phone)) {
											Friend friend = new Friend();
											friend.first_name = user.first_name;
											friend.last_name = user.last_name;
											friend.phone = user.phone;
											friend.avatar = user.avatar;
											friend.id = user.id;
											friend.room_id = Util.getChatRoomId(mAuth.getUid(),user.id);
											
											appDatabase.friendDao().insert(friend);
										}
										Log.d(TAG, "Confirmed:"+user.phone);
									}
								}
							}
						});
						
					}
					
					@Override
					public void onCancelled ( @NonNull DatabaseError databaseError ) {
					}
				});
	}
	
	//endregion
	
	//region Firebase
	private void sendChatRequest(Friend friend, String uid){
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
								Intent intent = new Intent(selfRef,    ChatActivity.class);
								intent.setAction(   ChatActivity.INTENT_CHAT_ACTION);
								intent.putExtra(   ChatActivity.INTENT_CHAT_FRIEND_DATA,friend.id);
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
								
								Intent intent = new Intent(selfRef,    ChatActivity.class);
								intent.setAction(   ChatActivity.INTENT_CHAT_ACTION);
								intent.putExtra(   ChatActivity.INTENT_CHAT_FRIEND_DATA,uid.equals(chatRequest.receiver_id)?chatRequest.sender_id:chatRequest.receiver_id);
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
