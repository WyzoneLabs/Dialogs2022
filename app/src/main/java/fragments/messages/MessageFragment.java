package fragments.messages;

import static io.agora.rtc.video.VideoEncoderConfiguration.STANDARD_BITRATE;
import static utils.Util.getInitial;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.res.ResourcesCompat;
import androidx.lifecycle.ViewModelProvider;

import android.Manifest;
import android.content.Context;
import android.content.res.ColorStateList;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import com.brimbay.chat.R;
import com.brimbay.chat.databinding.FragmentChatBinding;
import com.brimbay.chat.databinding.FragmentMessageBinding;
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

public class MessageFragment extends Fragment {
	private Context selfRef;
	private static final String KEY_FRIEND = "com.brimbay.key.Friend";
	private static final String TAG = MessageFragment.class.getSimpleName();
	private static final long TOKEN_EXPIRATION_TIME_IN_SECS = 15 * 60;
	private static String [] REQUESTED_VIDEO_PERMISSIONS = null;
	
	//region Views
	private RecyclerView _recycler_chats,_recycler_vid_chats;
	private EditText _edit_text;
	private ImageView _send_btn;
	//endregion
	
	//region Vars
	private MessageViewModel mViewModel;
	private FragmentMessageBinding binding;
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
			AppExecutors.getInstance().mainThread().execute(() -> {
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
			AppExecutors.getInstance().mainThread().execute(() -> onRemoteUserLeft());
		}
		
		@Override
		public void onUserMuteVideo(final int uid, final boolean muted) {
			AppExecutors.getInstance().mainThread().execute(() -> onRemoteUserVideoMuted(uid, muted));
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
	
	public static MessageFragment newInstance(Friend friend) {
		MessageFragment fragment = new MessageFragment();
		Bundle bundle = new Bundle();
		bundle.putParcelable(KEY_FRIEND,friend);
		fragment.setArguments(bundle);
		return fragment;
	}
	
	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		selfRef = requireContext();
		
		if (getArguments() != null){
			mFriend = getArguments().getParcelable(KEY_FRIEND);
		}else if (savedInstanceState != null){
			mFriend = savedInstanceState.getParcelable(KEY_FRIEND);
		}
		
		if (mFriend == null){
			Toast.makeText(selfRef, getString(R.string.sorry_something_went_wrong_please_try_again), Toast.LENGTH_SHORT).show();
			requireActivity().finish();
		}
		
		mAuth = FirebaseAuth.getInstance();
		mAppDatabase = AppDatabase.newInstance(selfRef);
		mChatDBReference = FirebaseDatabase.getInstance().getReference(Constants.FB_MESSAGES_DB).child(mFriend.room_id);
		launcherResult = createPermissionResultLauncher();
		mViewModel = new ViewModelProvider(this).get(MessageViewModel.class);
		
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
	}
	
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
							 @Nullable Bundle savedInstanceState) {
		binding = FragmentMessageBinding.inflate(inflater,container, false);
		initUI();
		return binding.getRoot();
	}
	
	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
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
				AppExecutors.getInstance().diskIO().execute(() -> {
					mAppDatabase.messageDao().insert(message);
					mAppDatabase.chatDao().insert(message.getChat(mAuth.getUid()));
				});
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
						mAppDatabase.chatDao().insert(message.getChat(Objects.requireNonNull(mAuth.getUid())));
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
			binding.cpUsername.setTextColor(ResourcesCompat.getColor(getResources(),R.color.white, null));
		}else{
			if (mAdapter != null)mAdapter.setViewLayout(MessageAdapter.TEXT_LAYOUT);
			binding.cpLargeRecyclerBox.setVisibility(View.VISIBLE);
			binding.cpVideoRecyclerBox.setVisibility(View.GONE);
			binding.cpUsername.setTextColor(ResourcesCompat.getColor(getResources(),R.color.colorPrimaryDark, null));
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
				if (mAuth != null && mAuth.getUid() != null)initAgoraEngineAndJoinChannel(mAuth.getUid(),mFriend.room_id);
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
	public void onSaveInstanceState(@NonNull Bundle outState) {
		if (mFriend != null)outState.putParcelable(KEY_FRIEND,mFriend);
		super.onSaveInstanceState(outState);
	}
	
	@Override
	public void onDestroyView() {
		super.onDestroyView();
		binding = null;
		leaveChannel();
		RtcEngine.destroy();
	}
}
