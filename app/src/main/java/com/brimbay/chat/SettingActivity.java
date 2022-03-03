package com.brimbay.chat;

import static utils.Util.getInitial;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.brimbay.chat.databinding.ActivitySettingBinding;
import com.brimbay.dialogx.dialogs.WaitDialog;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Locale;

import database.AppDatabase;
import models.User;
import utils.AppExecutors;

public class SettingActivity extends AppCompatActivity {
	private SettingActivity selfRef;
	
	//region Views
	private ImageView _avatar;
	private TextView _initial;
	//endregion
	
	private ActivitySettingBinding binding;
	private FirebaseAuth mAuth;
	private WaitDialog waitDialog;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		selfRef = this;
		mAuth = FirebaseAuth.getInstance();
		binding = ActivitySettingBinding.inflate(getLayoutInflater());
		setContentView(binding.getRoot());
		initUI();
	}
	
	//region Member Methods
	private void initUI(){
		_initial = binding.avatarInitial;
		_avatar = binding.avatar;
		waitDialog = WaitDialog.setMessage(getString(R.string.please_wait));
		binding.back.setOnClickListener(view -> onBackPressed());
		binding.stSignoutBox.setOnClickListener(this::signOut);
		handleUserData();
	}
	
	private void handleUserData(){
		AppExecutors.getInstance().diskIO().execute(() -> {
			User user = AppDatabase.newInstance(selfRef).userDao().findByID(mAuth.getUid());
			runOnUiThread(() -> {
				try {
					if (user.avatar == null) {
						_initial.setText(getInitial(user));
						_initial.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorAccent,null)));
						_initial.setVisibility(View.VISIBLE);
						binding.avatarCnt.setVisibility(View.GONE);
					} else {
						Glide.with(selfRef)
								.load(user.avatar)
								.error(R.drawable.user)
								.into(_avatar);
						
						binding.avatarCnt.setVisibility(View.VISIBLE);
						_initial.setVisibility(View.GONE);
					}
					
					binding.stUserName.setText(String.format(Locale.ROOT,"%s %s",user.first_name, user.last_name));
					binding.stUserPhone.setText(user.phone);
				}catch (NullPointerException e){
					e.printStackTrace();
				}
			});
		});
	}
	
	private void signOut(View v){
		waitDialog.show();
		AppExecutors.getInstance().networkIO().execute(() -> {
			AppDatabase.newInstance(selfRef).clearAllTables();
			runOnUiThread(() -> {
				if (mAuth != null){
					mAuth.signOut();
				}
				waitDialog.doDismiss();
				startActivity(new Intent(selfRef, StartActivity.class));
				finishAffinity();
			});
		});
	}
	//endregion
	
	@Override
	public void onBackPressed() {
		super.onBackPressed();
	}
}
