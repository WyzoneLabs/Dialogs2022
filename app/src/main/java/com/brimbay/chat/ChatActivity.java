package com.brimbay.chat;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.brimbay.chat.databinding.ActivityChatBinding;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Objects;

import database.AppDatabase;
import fragments.messages.MessageFragment;
import models.Friend;
import utils.AppExecutors;

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
	
	private ActivityChatBinding binding;
	private FragmentManager mFragmentManager;
	private String mFriendId;
	
	private FirebaseAuth mAuth;
	
	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		selfRef = this;
		getWindow().addFlags( WindowManager. LayoutParams.FLAG_KEEP_SCREEN_ON );
		mAuth = FirebaseAuth.getInstance();
		mFragmentManager = getSupportFragmentManager();
		handleData(savedInstanceState);
		
		binding = ActivityChatBinding.inflate(getLayoutInflater());
		setContentView(binding.getRoot());
	}
	
	//region Member Methods
	private void handleData(Bundle bundle){
		Intent intent = getIntent();
		if (intent != null && intent.getAction() != null && Objects.equals(intent.getAction(), INTENT_CHAT_ACTION)){
			mFriendId = intent.getStringExtra(INTENT_CHAT_FRIEND_DATA);
		}else if (bundle != null){
			mFriendId = bundle.getString(KEY_FRIEND);
		}
		
		AppExecutors.getInstance().diskIO().execute(() -> {
			if (mFriendId != null){
				Friend friend = AppDatabase.newInstance(selfRef).friendDao().findByID(mFriendId);
				runOnUiThread(()->{
					if (friend != null && mAuth.getUid() != null) {
						MessageFragment messageFragment = MessageFragment.newInstance(friend);
						addFragment(messageFragment, null);
					}else {
						Log.e(TAG,"Null data");
						Toast.makeText(selfRef, getString(R.string.sorry_something_went_wrong_please_try_again), Toast.LENGTH_SHORT).show();
						finish();
					}
				});
				return;
			}
			runOnUiThread(() -> {
				Log.e(TAG,"Null data 1");
				Toast.makeText(selfRef, getString(R.string.sorry_something_went_wrong_please_try_again), Toast.LENGTH_SHORT).show();
				finish();
			});
		});
	}
	
	private void addFragment(Fragment fragment, @Nullable String tag){
		FragmentTransaction transaction = mFragmentManager.beginTransaction();
		transaction.replace(R.id.chat_fragment, fragment, tag != null?tag:"current_page").commitNow();
	}
	//endregion
	
	@Override
	public void onBackPressed() {
		super.onBackPressed();
	}
	
	@Override
	protected void onSaveInstanceState(@NonNull Bundle outState) {
		if (mFriendId != null)outState.putString(KEY_FRIEND,mFriendId);
		super.onSaveInstanceState(outState);
	}
}
