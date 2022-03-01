package com.brimbay.chat;

import static io.agora.rtc.video.VideoEncoderConfiguration.STANDARD_BITRATE;
import static utils.Util.getInitial;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.PorterDuff;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import com.brimbay.chat.databinding.ActivityMessagingBinding;
import com.brimbay.dialogx.dialogs.MessageDialog;
import com.brimbay.dialogx.util.TextInfo;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Random;

import adapters.MessageAdapter;
import database.AppDatabase;
import io.agora.rtc.IRtcEngineEventHandler;
import io.agora.rtc.RtcEngine;
import io.agora.rtc.models.ChannelMediaOptions;
import io.agora.rtc.video.VideoCanvas;
import io.agora.rtc.video.VideoEncoderConfiguration;
import models.Friend;
import models.Message;
import rtc.RtcTokenBuilder;
import ui.iSwitch;
import utils.AppExecutors;
import utils.Constants;
import utils.GlobalSettings;

public class MessagingActivity extends AppCompatActivity {
	private MessagingActivity selfRef;
	private static final String TAG = MessagingActivity.class.getSimpleName();
	private static final long TOKEN_EXPIRATION_TIME_IN_SECS = 15 * 60;
	private static String [] REQUESTED_VIDEO_PERMISSIONS = null;
	
	public static final String INTENT_CHAT_ACTION = "com.dialog.chat.intent.Action";
	public static final String INTENT_CHAT_FRIEND_DATA = "com.dialog.chat.intent.data.Friend";
	public static final String INTENT_CHAT_REQUEST_DATA = "com.dialog.chat.intent.data.ChatRequest";
	private static final String KEY_FRIEND = "com.dialog.key.Friend";
	
	//region Views
	private RecyclerView _recycler_chats,_recycler_vid_chats;
	private EditText _edit_text;
	private ImageView _send_btn;
	//endregion
	
	//region Vars
	private ActivityMessagingBinding binding;
	private Friend mFriend;
	private MessageAdapter mAdapter;
	private LinearLayoutManager mLinearLayoutManager,mVideoLinearLayoutManager;
	private AppDatabase mAppDatabase;
	//endregion
	
	// region Video
	private RtcEngine mRtcEngine;
	private final IRtcEngineEventHandler mRtcEventHandler = new IRtcEngineEventHandler () {
		
		@Override
		public void onFirstRemoteVideoDecoded(final int uid , int width , int height , int elapsed ) {
			runOnUiThread(() -> {
				setupRemoteVideo(uid);
				setupLocalVideo(uid);
				changeChatUI(iSwitch.Check.END);
			});
		}
		
		@Override
		public void onFirstLocalVideoFrame(int width, int height, int elapsed) {
			super.onFirstLocalVideoFrame(width, height, elapsed);
		}
		
		@Override
		public void onUserOffline(int uid, int reason) {
			runOnUiThread(() -> onRemoteUserLeft());
		}
		
		@Override
		public void onUserMuteVideo(final int uid, final boolean muted) {
			runOnUiThread(() -> onRemoteUserVideoMuted(uid, muted));
		}
		
		@Override
		public void onWarning(int warn) {
			super.onWarning(warn);
			Log.w(TAG, String.format("onWarning code %d message %s", warn, RtcEngine.getErrorDescription(warn)));
		}
		
		@Override
		public void onError(int err) {
			super.onError(err);
			Log.e(TAG, String.format("onError code %d message %s", err, RtcEngine.getErrorDescription(err)));
			Toast.makeText(selfRef, String.format(Locale.getDefault(),"onError code %d message %s", err, RtcEngine.getErrorDescription(err)), Toast.LENGTH_SHORT).show();
		}
	};
	private ActivityResultLauncher<String[]> launcherResult;
	// endregion
	
	//region Firebase
	private FirebaseAuth mAuth;
	private DatabaseReference mChatDBReference;
	//endregion
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		selfRef = this;
		getWindow().addFlags( WindowManager. LayoutParams.FLAG_KEEP_SCREEN_ON );
		
		handleData(savedInstanceState);
		
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
			REQUESTED_VIDEO_PERMISSIONS = new String[]{
					Manifest.permission.RECORD_AUDIO,
					Manifest.permission.CAMERA,
					Manifest.permission.BLUETOOTH_CONNECT
			};
		}else {
			REQUESTED_VIDEO_PERMISSIONS = new String[]{
					Manifest.permission.RECORD_AUDIO ,
					Manifest.permission.CAMERA ,
					Manifest.permission.BLUETOOTH
			};
		}
		mAppDatabase = AppDatabase.newInstance(selfRef);
		
		if (mFriend == null || mFriend.id.equals("")){
//			Toast.makeText(selfRef, getString(R.string.sorry_something_went_wrong_please_try_again), Toast.LENGTH_SHORT).show();
//			onBackPressed();
			//TODO::Test data
			mFriend = new Friend();
			mFriend.first_name = "Test";
			mFriend.last_name = "One";
			mFriend.phone = "+6736767840033";
		}
		
		//Firebase
		mAuth = FirebaseAuth.getInstance();
		try {
			mFriend.id = Objects.equals(mAuth.getUid(), "wCiuoykn8VVAl9TPrzSj3uu2kKk1") ?"qkNDAviF2QV1AIralt6hpyTGVac2":"wCiuoykn8VVAl9TPrzSj3uu2kKk1";
			mFriend.room_id = mFriend.getRoomId(mAuth.getUid());
		}catch (NullPointerException e){
			e.printStackTrace();
		}
		mChatDBReference = FirebaseDatabase.getInstance().getReference(Constants.FB_MESSAGES_DB).child(mFriend.room_id);
		
		launcherResult = createPermissionResultLauncher();
		
		binding = ActivityMessagingBinding.inflate(getLayoutInflater());
		setContentView(binding.getRoot());
		initUI();
		
		handleMessages(mFriend);
		receiveMessage();
	}
	
	//region Member Methods
	private void initUI(){
		_recycler_chats = binding.messagesRecycler;
		_recycler_vid_chats = binding.messagesRecyclerVideo;
		_edit_text = binding.editText;
		_send_btn = binding.editSend;
		
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
		binding.cpSwitch.setSwitchCallback(checked -> {
			if(checked == iSwitch.Check.END){
				launcherResult.launch(REQUESTED_VIDEO_PERMISSIONS);
			}else{
				leaveChannel();
			}
		});

		handleFriendData(mFriend);
	}
	
	private void handleFriendData(Friend friend){
		int[] colors = getResources().getIntArray(R.array.color_initial_bgs);
		Random random = new Random();
		if (friend.avatar == null){
			binding.cpAvatarInitial.setText(getInitial(friend));
			binding.cpAvatarInitial.setBackgroundTintList(ColorStateList.valueOf(colors[random.nextInt(colors.length)]));
			binding.cpAvatarInitial.setVisibility(View.VISIBLE);
			binding.cpAvatar.setVisibility(View.GONE);
		}else{
			Glide.with(selfRef)
					.load(friend.avatar)
					.error(R.drawable.user)
					.into(binding.cpAvatar);
			
			binding.cpAvatar.setVisibility(View.VISIBLE);
			binding.cpAvatarInitial.setVisibility(View.GONE);
		}
		
		binding.cpUsername.setText(String.format(Locale.ROOT,"%s %s",friend.first_name,friend.last_name));
	}
	
	private void handleData(Bundle bundle){
		Intent intent = getIntent();
		if (intent != null && intent.getAction() != null && Objects.equals(intent.getAction(), INTENT_CHAT_ACTION)){
			mFriend = intent.getParcelableExtra(INTENT_CHAT_FRIEND_DATA);
		}else if (bundle != null){
			mFriend = bundle.getParcelable(KEY_FRIEND);
		}
	}
	
	private void handleMessages(Friend friend){
		mAdapter = new MessageAdapter(selfRef,friend,MessageAdapter.TEXT_LAYOUT);
		mLinearLayoutManager = new LinearLayoutManager(selfRef, RecyclerView.VERTICAL, false);
		_recycler_chats.setLayoutManager(mLinearLayoutManager);
		_recycler_chats.setAdapter(mAdapter);
		
		handleVidChats(friend);
	}
	
	private void handleVidChats(Friend friend){
		mVideoLinearLayoutManager = new LinearLayoutManager(selfRef, RecyclerView.VERTICAL, false);
		_recycler_vid_chats.setLayoutManager(mVideoLinearLayoutManager);
		_recycler_vid_chats.setAdapter(mAdapter);
		
		AppExecutors.getInstance().diskIO().execute(() -> {
			List<Message> chats = mAppDatabase.messageDao().getByRoomID(friend.room_id);
			AppExecutors.getInstance().mainThread().execute(() -> {
				if (chats != null && chats.size() > 0) {
					mAdapter.setMessages(chats);
					mLinearLayoutManager.scrollToPosition(mAdapter.getItemCount() - 1);
					mVideoLinearLayoutManager.scrollToPosition(mAdapter.getItemCount() - 1);
				}
			});
		});
	}
	
	private void sendMessage(Friend friend,String s){
		String key = mChatDBReference.push().getKey();
		if (key != null) {
			Message message = new Message();
			message.id = key;
			message.receiver_id = friend.id;
			message.sender_id = mAuth.getUid();
			message.message = s;
			message.room_id = friend.room_id;
			message.timestamp = System.currentTimeMillis();
			
			mAdapter.addMessage(message);
			_edit_text.setText("");
			mLinearLayoutManager.scrollToPosition(mAdapter.getItemCount() - 1);
			mVideoLinearLayoutManager.scrollToPosition(mAdapter.getItemCount() - 1);
			
			mChatDBReference.child(key).setValue(message).addOnSuccessListener(unused -> {
				AppExecutors.getInstance().diskIO().execute(() -> mAppDatabase.messageDao().insert(message));
			}).addOnCanceledListener(()-> Toast.makeText(selfRef, getString(R.string.sorry_something_went_wrong_please_try_again), Toast.LENGTH_SHORT).show());
		}
	}
	
	private void receiveMessage(){
		mChatDBReference.addChildEventListener(new ChildEventListener() {
			@Override
			public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
				Message message = snapshot.getValue(Message.class);
				if (message != null && !Objects.equals(message.sender_id, mAuth.getUid())) {
					mAdapter.addMessage(message);
					mLinearLayoutManager.scrollToPosition(mAdapter.getItemCount() - 1);
					mVideoLinearLayoutManager.scrollToPosition(mAdapter.getItemCount() - 1);
					AppExecutors.getInstance().diskIO().execute(() -> {
						mAppDatabase.messageDao().insert(message);
						snapshot.getRef().removeValue();
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
	}
	
	private void changeChatUI(iSwitch.Check check){
		if (check == iSwitch.Check.END){
			if (mAdapter != null)mAdapter.setViewLayout(MessageAdapter.VIDEO_LAYOUT);
			binding.cpLargeRecyclerBox.setVisibility(View.GONE);
			binding.cpVideoRecyclerBox.setVisibility(View.VISIBLE);
		}else{
			if (mAdapter != null)mAdapter.setViewLayout(MessageAdapter.TEXT_LAYOUT);
			binding.cpLargeRecyclerBox.setVisibility(View.VISIBLE);
			binding.cpVideoRecyclerBox.setVisibility(View.GONE);
		}
	}
	
	//endregion
	
	//region Video Chat
	private ActivityResultLauncher<String[]> createPermissionResultLauncher() {
		return registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
			boolean granted = true;
			for (Map.Entry<String, Boolean> x : result.entrySet()) {
				if (!x.getValue()) granted = false;
			}
			if (granted) {
				if (mAuth != null && mAuth.getUid() != null)initAgoraEngineAndJoinChannel(mAuth.getUid(),mFriend.getRoomId(mAuth.getUid()));
			}else {
				MessageDialog messageDialog = new MessageDialog(
						getString(R.string.allow_permissions),
						getString(R.string.permissions_request_info),
						getString(R.string.allow)
				);
				messageDialog.setOkTextInfo(new TextInfo().setBold(true).setFontColor(getResources().getColor(R.color.colorAccent, null)));
				messageDialog.setOkButtonClickListener((baseDialog, v) -> {
					baseDialog.dismiss();
					launcherResult.launch(REQUESTED_VIDEO_PERMISSIONS);
					return false;
				});
				messageDialog.show();
			}
		});
	}
	
	public void onLocalVideoMuteClicked(View view){
		ImageView iv = ( ImageView ) view;
		if (iv.isSelected()) {
			iv.setSelected( false );
			iv.clearColorFilter();
		} else {
			iv.setSelected( true );
			iv.setColorFilter(getResources().getColor( R.color.colorPrimary, null ), PorterDuff. Mode.MULTIPLY );
		}
		
		mRtcEngine.muteLocalVideoStream(iv.isSelected());
		
		FrameLayout container = findViewById( R.id.local_video_view_container);
		SurfaceView surfaceView = ( SurfaceView ) container.getChildAt( 0 );
		surfaceView.setZOrderMediaOverlay( ! iv.isSelected());
		surfaceView.setVisibility(iv.isSelected() ? View.GONE : View.VISIBLE );
	}
	
	public void onLocalAudioMuteClicked(View view){
		ImageView iv = ( ImageView ) view;
		if (iv.isSelected()) {
			iv.setSelected( false );
			iv.clearColorFilter();
		} else {
			iv.setSelected( true );
			iv.setColorFilter(getResources().getColor( R.color.colorPrimary), PorterDuff.Mode.MULTIPLY );
		}
		
		mRtcEngine.muteLocalAudioStream(iv.isSelected());
	}
	
	public void onSwitchCameraClicked(View vie){
		mRtcEngine.switchCamera();
	}
	
	private void initAgoraEngineAndJoinChannel(String uid,String room_id) {
		initializeAgoraEngine();
		setupVideoProfile();
		setupLocalVideo(0);
		joinChannel(uid,room_id);
	}
	
	private void leaveChannel () {
		try {
			mRtcEngine.leaveChannel();
			onRemoteUserLeft();
		} catch ( NullPointerException e) {
			e.printStackTrace ();
		}
	}
	
	private void initializeAgoraEngine () {
		try {
			mRtcEngine = RtcEngine.create(selfRef, getString( R.string.agora_app_id), mRtcEventHandler);
		} catch ( Exception e) {
			Log.e( TAG , Log.getStackTraceString(e));
			throw new RuntimeException ( "NEED TO check rtc sdk init fatal error \n" + Log.getStackTraceString(e));
		}
	}
	
	private void setupVideoProfile () {
		mRtcEngine.setChannelProfile(io.agora.rtc.Constants.CHANNEL_PROFILE_COMMUNICATION);
		mRtcEngine.setClientRole(IRtcEngineEventHandler.ClientRole.CLIENT_ROLE_AUDIENCE);
		mRtcEngine.enableVideo();
		mRtcEngine.disableAudio();
		mRtcEngine.enableDualStreamMode( false );
		
		GlobalSettings globalSettings = new GlobalSettings();
		mRtcEngine.setVideoEncoderConfiguration(new VideoEncoderConfiguration(
				globalSettings.getVideoEncodingDimensionObject(),
				VideoEncoderConfiguration.FRAME_RATE.valueOf(globalSettings.getVideoEncodingFrameRate()),
				STANDARD_BITRATE,
				VideoEncoderConfiguration.ORIENTATION_MODE.valueOf(globalSettings.getVideoEncodingOrientation())
		));
	}
	
	private void setupLocalVideo (int uid) {
		FrameLayout container = binding.ctLocalVideoBox;
		SurfaceView _local_surface_view = RtcEngine.CreateRendererView(selfRef);
		_local_surface_view.setZOrderOnTop(false);
		_local_surface_view.setZOrderMediaOverlay(false);
		if(container.getChildCount() > 0) {
			container.removeAllViews();
		}
		container.addView(_local_surface_view);
		mRtcEngine.setupLocalVideo(new VideoCanvas(_local_surface_view, VideoCanvas.RENDER_MODE_HIDDEN, uid));
	}
	
	private void joinChannel(String uid,String room_id) {
		String accessToken = getToken(uid,room_id);
		Log.d(TAG,"Token:"+accessToken);
		if (TextUtils.isEmpty(accessToken) || TextUtils.equals(accessToken, "") || TextUtils.equals(accessToken, "<#YOUR ACCESS TOKEN#>")) {
			accessToken = getString(R.string.agora_access_token);
		}
		ChannelMediaOptions option = new ChannelMediaOptions();
		option.autoSubscribeAudio = false;
		option.autoSubscribeVideo = true;
		mRtcEngine.joinChannel(null, room_id, "Extra Optional Data", 0, option);
	}
	
	private String getToken(String uid, String room_id){
		RtcTokenBuilder token = new  RtcTokenBuilder();
		int  timestamp  =  (int)(( System.currentTimeMillis()/1000) + TOKEN_EXPIRATION_TIME_IN_SECS);
		return token.buildTokenWithUid(getString(R.string.agora_app_id), getString(R.string.agora_app_cert),
				"CHAN_"+room_id, uid.hashCode(), RtcTokenBuilder.Role.Role_Publisher , timestamp);
	}
	
	private void setupRemoteVideo ( int uid ) {
		FrameLayout container = binding.ctGuestVideoBox;
		SurfaceView surfaceView = RtcEngine.CreateRendererView(selfRef);
		if(container.getChildCount() > 0) {
			container.removeAllViews();
		}
		container.addView(surfaceView, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
		mRtcEngine.setupRemoteVideo( new VideoCanvas (surfaceView, VideoCanvas.RENDER_MODE_HIDDEN , uid));
		surfaceView.setTag(uid);
	}
	
	private void onRemoteUserLeft() {
		binding.ctGuestVideoBox.removeAllViews();
		binding.ctLocalVideoBox.removeAllViews();
		binding.cpSwitch.check(iSwitch.Check.START);
		changeChatUI(iSwitch.Check.START);
	}
	
	private void onRemoteUserVideoMuted( int uid , boolean muted ) {
		FrameLayout container = binding.ctGuestVideoBox;
		SurfaceView surfaceView = ( SurfaceView ) container.getChildAt( 0 );
		Object tag = surfaceView.getTag();
		if (tag != null && ( Integer ) tag == uid) {
			surfaceView.setVisibility(muted ? View.GONE : View.VISIBLE );
		}
	}
	// endregion
	
	@Override
	public void onBackPressed() {
		super.onBackPressed();
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		leaveChannel();
		RtcEngine.destroy();
	}
	
	@Override
	protected void onSaveInstanceState(@NonNull Bundle outState) {
		if (mFriend != null)outState.putParcelable(KEY_FRIEND,mFriend);
		super.onSaveInstanceState(outState);
	}
	
}
