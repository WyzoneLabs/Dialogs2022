package com.brimbay.chat;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.brimbay.chat.databinding.ActivityChatBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Objects;

import adapters.MessageAdapter;
import models.Friend;
import models.Message;
import utils.AppExecutors;
import utils.Constants;

/**
 * Created by Kevine James on 2/5/2022.
 * Copyright (c) 2022 dialog. All rights reserved.
 */
public class ChatActivity extends AppCompatActivity {
	public static final String INTENT_CHAT_ACTION = "com.dialog.chat.intent.Action";
	public static final String INTENT_CHAT_FRIEND_DATA = "com.dialog.chat.intent.data.Friend";
	
	private static final String TAG = ChatActivity.class.getSimpleName();
	private static final String KEY_FRIEND = "com.dialog.key.Friend";
	
	private ChatActivity selfRef;
	
	//region Views
	private RecyclerView _recycler_chats;
	private EditText _edit_text;
	private ImageView _send_btn;
	//endregion
	
	private ActivityChatBinding binding;
	private Friend mFriend;
	private MessageAdapter mAdapter;
	private LinearLayoutManager mLinearLayoutManager;
//	private AppDatabase mAppDatabase;
	
	private FirebaseAuth mAuth;
	private DatabaseReference mDatabaseReference;
	
	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		selfRef = this;
//		mAppDatabase = AppDatabase.newInstance(selfRef);
		
		mAuth = FirebaseAuth.getInstance();
		
		Intent intent = getIntent();
		if (intent != null && intent.getAction() != null && Objects.equals(intent.getAction(), INTENT_CHAT_ACTION)){
			mFriend = intent.getParcelableExtra(INTENT_CHAT_FRIEND_DATA);
		}else if (savedInstanceState != null){
			mFriend = savedInstanceState.getParcelable(KEY_FRIEND);
		}
		
		binding = ActivityChatBinding.inflate(getLayoutInflater());
		setContentView(binding.getRoot());
		
		if (mFriend == null || mAuth == null){
			Toast.makeText(selfRef, getString(R.string.sorry_something_went_wrong_please_try_again), Toast.LENGTH_SHORT).show();
			finish();
		}
		mFriend = new Friend();
		mFriend.id = "yut78HGkfEmVCxzQE239BcIgvm";
		mFriend.first_name = "Test";
		mFriend.last_name = "One";
		mFriend.phone = "+6736767840033";
		mFriend.room_id = mFriend.getRoomId(mAuth.getUid());
		
		mDatabaseReference = FirebaseDatabase.getInstance().getReference(Constants.FB_MESSAGES_DB).child(mFriend.room_id);
		
		initUI();
		handleMessages(mFriend);
		receiveMessage();
	}
	
	//region Member Methods
	private void initUI(){
		_recycler_chats = binding.chatBox.messagesRecycler;
		_edit_text = binding.chatBox.editText;
		_send_btn = binding.chatBox.editSend;
		
		binding.ctTitle.setText(getString(R.string.friend_name,mFriend.first_name, mFriend.last_name));
		binding.back.setOnClickListener(v -> onBackPressed());
		_edit_text.setOnEditorActionListener((v, actionId, event) -> {
			if (actionId == EditorInfo.IME_ACTION_SEND) {
				String txt = _edit_text.getText().toString();
				if (!TextUtils.isEmpty(txt)) sendMessage(mFriend,txt);
				return true;
			}
			return false;
		});
		_edit_text.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			
			}
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				_send_btn.setImageTintList(ColorStateList.valueOf(
						ResourcesCompat.getColor(getResources(),(s.length() > 0)? R.color.colorPrimaryDark: R.color.colorGray, null)));
				_send_btn.setEnabled(s.length() > 0);
			}
			
			@Override
			public void afterTextChanged(Editable s) {
			
			}
		});
		_send_btn.setOnClickListener(v -> {
			String txt = _edit_text.getText().toString();
			if (!TextUtils.isEmpty(txt)) {
				sendMessage(mFriend,txt);
			}
		});
	}
	
	private void handleMessages(Friend friend){
		mAdapter = new MessageAdapter(selfRef,friend);
		mLinearLayoutManager = new LinearLayoutManager(selfRef, RecyclerView.VERTICAL, false);
		_recycler_chats.setLayoutManager(mLinearLayoutManager);
		_recycler_chats.setAdapter(mAdapter);
		
		AppExecutors.getInstance().diskIO().execute(() -> {
//			List<Message> chats = mAppDatabase.messageDao().getByRoomID(friend.room_id);
//			AppExecutors.getInstance().mainThread().execute(() -> {
//				if (chats != null && chats.size() > 0) {
//					mAdapter.setMessages(chats);
//					mLinearLayoutManager.scrollToPosition(mAdapter.getItemCount() - 1);
//				}
//			});
		});
	}
	
	private void sendMessage(Friend friend,String s){
		AppExecutors.getInstance().diskIO().execute(() -> {
			String key = mDatabaseReference.push().getKey();
			if (friend != null && key != null) {
				Message message = new Message();
				message.id = key;
				if (s.startsWith("rv ") ){//TODO:Change in production
					message.sender_id = friend.id;
					message.receiver_id = mAuth.getUid();
					message.message = s.substring(2);
				}else {
					message.receiver_id = friend.id;
					message.sender_id = mAuth.getUid();
					message.message = s;
				}
				message.room_id = friend.room_id;
				message.timestamp = System.currentTimeMillis();
				
				AppExecutors.getInstance().mainThread().execute(() -> {
					mAdapter.addMessage(message);
					_edit_text.setText("");
					mLinearLayoutManager.scrollToPosition(mAdapter.getItemCount() - 1);
				});
				mDatabaseReference.child(key).setValue(message).addOnSuccessListener(unused -> {
//					mAppDatabase.messageDao().insert(message);
				}).addOnCanceledListener(()-> {
					Toast.makeText(selfRef, getString(R.string.sorry_something_went_wrong_please_try_again), Toast.LENGTH_SHORT).show();
				});
			}
		});
	}
	
	private void receiveMessage(){
		AppExecutors.getInstance().networkIO().execute(() -> {
			mDatabaseReference.addChildEventListener(new ChildEventListener() {
				@Override
				public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
					Message message = snapshot.getValue(Message.class);
					if (message != null && !Objects.equals(message.sender_id, mAuth.getUid())) {
						mAdapter.addMessage(message);
						mLinearLayoutManager.scrollToPosition(mAdapter.getItemCount() - 1);
						AppExecutors.getInstance().diskIO().execute(() -> {
//							mAppDatabase.messageDao().insert(message);
						});
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
					Log.e(TAG, error.getMessage());
				}
			});
		});
	}
	//endregion
	
	//region Video Chat
	//endregion
	
	@Override
	public void onBackPressed() {
		super.onBackPressed();
	}
	
	@Override
	protected void onSaveInstanceState(@NonNull Bundle outState) {
		if (mFriend != null)outState.putParcelable(KEY_FRIEND,mFriend);
		super.onSaveInstanceState(outState);
	}
}
