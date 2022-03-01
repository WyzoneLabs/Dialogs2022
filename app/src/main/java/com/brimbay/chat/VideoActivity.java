package com.brimbay.chat;

import static io.agora.rtc.video.VideoEncoderConfiguration.STANDARD_BITRATE;

import android.Manifest;
import android.graphics.PorterDuff;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.brimbay.chat.databinding.ActivityVideoBinding;
import com.brimbay.dialogx.dialogs.MessageDialog;
import com.brimbay.dialogx.util.TextInfo;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Locale;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import io.agora.rtc.Constants;
import io.agora.rtc.IRtcEngineEventHandler;
import io.agora.rtc.RtcEngine;
import io.agora.rtc.mediaio.IVideoSink;
import io.agora.rtc.models.ChannelMediaOptions;
import io.agora.rtc.video.VideoCanvas;
import io.agora.rtc.video.VideoEncoderConfiguration;
import rtc.RtcTokenBuilder;
import utils.GlobalSettings;


public class VideoActivity extends AppCompatActivity {
	private VideoActivity selfRef;
	private static final String TAG = VideoActivity.class.getSimpleName();
	
	private static final long TOKEN_EXPIRATION_TIME_IN_SECS = 15 * 60;
	private static String [] REQUESTED_PERMISSIONS = null;
	
	private ActivityResultLauncher<String[]> launcherResult;
	
	// Firebase
	private FirebaseAuth mAuth;
	
	// region Member Vars
	private ActivityVideoBinding binding;
	private FrameLayout _local_box;
	private RtcEngine mRtcEngine;
	private final IRtcEngineEventHandler mRtcEventHandler = new IRtcEngineEventHandler () {
		
		@Override
		public void onFirstRemoteVideoDecoded(final int uid , int width , int height , int elapsed ) {
			runOnUiThread(() -> {
				setupRemoteVideo(uid);
				setupLocalVideo(uid);
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
			showAlert(String.format(Locale.getDefault(),"onError code %d message %s", err, RtcEngine.getErrorDescription(err)));
		}
	};
	
	protected void showAlert(String message){
		new AlertDialog.Builder(selfRef).setTitle("Tips").setMessage(message)
				.setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
				.show();
	}
	
	protected final void showLongToast(final String msg) {
		new Handler(Looper.getMainLooper()).post(() -> {
			Toast.makeText(selfRef, msg, Toast.LENGTH_LONG).show();
		});
	}
	// endregion
	
	@Override
	protected void onCreate ( @Nullable Bundle savedInstanceState ) {
		super.onCreate (savedInstanceState);
		selfRef = this ;
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
			REQUESTED_PERMISSIONS = new String[]{
					Manifest.permission.RECORD_AUDIO,
					Manifest.permission.CAMERA,
					Manifest.permission.BLUETOOTH_CONNECT
			};
		}else {
			REQUESTED_PERMISSIONS = new String[]{
					Manifest.permission.RECORD_AUDIO ,
					Manifest.permission.CAMERA ,
					Manifest.permission.BLUETOOTH
			};
		}
		launcherResult = createPermissionResultLauncher();
		binding = ActivityVideoBinding.inflate(getLayoutInflater());
		setContentView(binding.getRoot());
		getWindow().addFlags( WindowManager. LayoutParams.FLAG_KEEP_SCREEN_ON );
		_local_box = (FrameLayout) LayoutInflater.from(selfRef).inflate(R.layout.view_local_video,null,
				false);
		// Firebase
		mAuth = FirebaseAuth.getInstance();
		
		launcherResult.launch(REQUESTED_PERMISSIONS);
	}
	
	//region Video Chat
	private ActivityResultLauncher<String[]> createPermissionResultLauncher() {
		return registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
			boolean granted = true;
			for (Map.Entry<String, Boolean> x : result.entrySet()) {
				if (!x.getValue()) granted = false;
			}
			if (granted) {
				if (mAuth != null && mAuth.getUid() != null)initAgoraEngineAndJoinChannel(mAuth.getUid());
			}else {
				MessageDialog messageDialog = new MessageDialog(
						getString(R.string.allow_permissions),
						getString(R.string.permissions_request_info),
						getString(R.string.allow)
				);
				messageDialog.setOkTextInfo(new TextInfo().setBold(true).setFontColor(getResources().getColor(R.color.colorAccent, null)));
				messageDialog.setOkButtonClickListener((baseDialog, v) -> {
					baseDialog.dismiss();
					launcherResult.launch(REQUESTED_PERMISSIONS);
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
	
	private void initAgoraEngineAndJoinChannel(String uid) {
		initializeAgoraEngine();
		setupVideoProfile();
		setupLocalVideo(0);
		setupLocalVideoSec();
		joinChannel(uid);
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
		mRtcEngine.setChannelProfile(Constants.CHANNEL_PROFILE_COMMUNICATION);
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
		CardView container = binding.localVideoViewContainer;
		SurfaceView _local_surface_view = RtcEngine.CreateRendererView(selfRef);
		if(container.getChildCount() > 0) {
			container.removeAllViews();
		}
		if(_local_box.getChildCount() > 0) {
			_local_box.removeAllViews();
		}
		_local_box.addView(_local_surface_view);
		container.addView(_local_box);
		mRtcEngine.setupLocalVideo(new VideoCanvas(_local_surface_view, VideoCanvas.RENDER_MODE_HIDDEN, uid));
	}
	
	private void setupLocalVideoSec() {
		CardView container = findViewById( R.id.local_video_view_container_sec);
		CardView container1 = findViewById( R.id.local_video_view_container);
		if(container.getChildCount() > 0) {
			container.removeAllViews();
		}
		if(container1.getChildCount() > 0) {
			container1.removeAllViews();
		}
			container.addView(_local_box);
	}
	
	private void joinChannel(String uid) {
		String accessToken = "";//getToken(uid);
		if (TextUtils.isEmpty(accessToken) || TextUtils.equals(accessToken, "") || TextUtils.equals(accessToken, "<#YOUR ACCESS TOKEN#>")) {
			accessToken = getString(R.string.agora_access_token);
		}
		ChannelMediaOptions option = new ChannelMediaOptions();
		option.autoSubscribeAudio = false;
		option.autoSubscribeVideo = true;
		mRtcEngine.joinChannel(accessToken, getString(R.string.agora_channel), "Extra Optional Data", 0, option);
	}
	
	private String getToken(String uid){
		RtcTokenBuilder token = new  RtcTokenBuilder();
		int  timestamp  =  (int)(( System.currentTimeMillis()/1000) + TOKEN_EXPIRATION_TIME_IN_SECS);
		return token.buildTokenWithUserAccount(getString(R.string.agora_app_id), getString(R.string.agora_app_cert),
				getString(R.string.agora_channel), uid, RtcTokenBuilder. Role . Role_Publisher , timestamp);
	}
	
	private void setupRemoteVideo ( int uid ) {
		FrameLayout container = findViewById( R.id.remote_video_view_container);
		SurfaceView surfaceView = RtcEngine.CreateRendererView(selfRef);
		if(container.getChildCount() > 0) {
			container.removeAllViews();
		}
		container.addView(surfaceView, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
		mRtcEngine.setupRemoteVideo( new VideoCanvas (surfaceView, VideoCanvas.RENDER_MODE_HIDDEN , uid));
		surfaceView.setTag(uid);
	}
	
	private void onRemoteUserLeft() {
	
	}
	
	private void onRemoteUserVideoMuted( int uid , boolean muted ) {
		FrameLayout container = findViewById( R.id.remote_video_view_container);
		SurfaceView surfaceView = ( SurfaceView ) container.getChildAt( 0 );
		Object tag = surfaceView.getTag();
		if (tag != null && ( Integer ) tag == uid) {
			surfaceView.setVisibility(muted ? View.GONE : View.VISIBLE );
		}
	}
	// endregion
	
	@Override
	public void onBackPressed () {
		super.onBackPressed();
	}
	
	@Override
	protected void onDestroy () {
		super.onDestroy ();
		leaveChannel();
		mRtcEngine.destroy();
		try {
			onBackPressed();
		} catch ( RuntimeException e){
			e.printStackTrace ();
		}
	}
}
