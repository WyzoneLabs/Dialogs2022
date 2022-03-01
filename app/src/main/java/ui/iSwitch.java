package ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.customview.widget.ViewDragHelper;

import com.brimbay.chat.R;
import com.bumptech.glide.Glide;

/**
 * Created by Kevine James on 2/26/2022.
 * Copyright (c) 2022 Brimbay. All rights reserved.
 */
public class iSwitch extends FrameLayout {
	
	//region Views
	private ImageView _start_icon;
	private ImageView _end_icon;
	private FrameLayout _start_box;
	private FrameLayout _end_box;
	private View _root;
	//endregion
	
	private Check mCurrent = Check.START;
	private iSwitchCallback switchCallback;
	public enum Check {
		START {
			@Override
			public Check toggle() {
				return START ;
			}
		},
		END {
			@Override
			public Check toggle() {
				return END ;
			}
		};
		
		public abstract Check toggle();
	}
	
	public iSwitch(Context context) {
		super(context);
		init(context,null);
	}
	
	public iSwitch(Context context, @Nullable AttributeSet attrs) {
		super(context, attrs);
		init(context,attrs);
	}
	
	public iSwitch(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init(context,attrs);
	}
	
	private void init(Context context, AttributeSet attr){
		_root = ((LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.view_i_switch, this, true);
		
		//initUI
		_start_icon = _root.findViewById(R.id.start_icon);
		_end_icon = _root.findViewById(R.id.end_icon);
		_start_box = _root.findViewById(R.id.start_icon_box);
		_end_box = _root.findViewById(R.id.end_icon_box);
		View _parent = _root.findViewById(R.id.parent);
		
//		_start_box.setOnClickListener(view -> {
//			if (switchCallback != null){
//				switchCallback.onSwitchChecked(Check.START);
//				mCurrent = Check.START;
//				updateUI(mCurrent);
//			}
//		});
//
//		_end_box.setOnClickListener(view -> {
//			if (switchCallback != null){
//				switchCallback.onSwitchChecked(Check.END);
//				mCurrent = Check.END;
//				updateUI(mCurrent);
//			}
//		});
		updateUI(mCurrent);
		_parent.setOnClickListener(view -> {
			if (switchCallback != null){
				mCurrent = mCurrent == Check.START?Check.END:Check.START;
				switchCallback.onSwitchChecked(mCurrent);
				updateUI(mCurrent);
			}
		});
	}
	
	private void updateUI(Check check){
		if (check == Check.START){
			selectUI(_start_box,_start_icon,true);
			selectUI(_end_box,_end_icon,false);
			Glide.with(getContext())
					.load(R.drawable.chat_message_96px)
					.into(_start_icon);
			Glide.with(getContext())
					.load(R.drawable.live_video_on_outline_96px)
					.into(_end_icon);
		}else{
			selectUI(_end_box,_end_icon,true);
			selectUI(_start_box,_start_icon,false);
			Glide.with(getContext())
					.load(R.drawable.live_video_on_96px)
					.into(_end_icon);
			Glide.with(getContext())
					.load(R.drawable.chat_bubble_outline_96px)
					.into(_start_icon);
		}
	}
	
	private void selectUI(FrameLayout box, ImageView icon, boolean b){
		icon.setImageTintList(ColorStateList.valueOf(ResourcesCompat.getColor(getResources(),b?R.color.colorAccent:R.color.white, null)));
		box.setBackgroundTintList(ColorStateList.valueOf(ResourcesCompat.getColor(getResources(),b?R.color.white:android.R.color.transparent, null)));
	}
	
	public Check getChecked() {
		return mCurrent;
	}
	
	public void check(Check check) {
		this.mCurrent = check;
		updateUI(check);
	}
	
	public iSwitch.iSwitchCallback getSwitchCallback() {
		return switchCallback;
	}
	
	public void setSwitchCallback(iSwitch.iSwitchCallback iSwitchCallback) {
		this.switchCallback = iSwitchCallback;
	}
	
	public interface iSwitchCallback{
		void onSwitchChecked(Check checked);
	}
	
}
