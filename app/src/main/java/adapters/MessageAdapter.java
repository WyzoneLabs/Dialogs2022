package adapters;

import static utils.Util.getInitial;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.brimbay.chat.R;
import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;

import de.hdodenhof.circleimageview.CircleImageView;
import models.Message;
import models.User;
import utils.DateUtils;

/**
 * Created by Kevine James on 2/21/2022.
 * Copyright (c) 2022 Brimbay. All rights reserved.
 */
public class MessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
	private static final int FRIEND_MESSAGE_VIEW = 102;
	private static final int USER_MESSAGE_VIEW = 103;
	private static final int ADMIN_MESSAGE_VIEW = 104;
	private static final int FRIEND_VIDEO_VIEW = 105;
	private static final int USER_VIDEO_VIEW = 106;
	
	public static final int TEXT_LAYOUT = 88;
	public static final int VIDEO_LAYOUT = 89;
	
	private final Context context;
	private final User user;
	private List<Message> mMessages;
	private int viewLayout = TEXT_LAYOUT;
	
	public MessageAdapter(Context context, User user) {
		this.context = context;
		this.user = user;
		mMessages = new ArrayList<>();
	}
	
	public MessageAdapter(Context context, User user, int viewLayout) {
		this.context = context;
		this.user = user;
		this.viewLayout = viewLayout;
		mMessages = new ArrayList<>();
	}
	
	//region Messages
	
	public List<Message> getMessages() {
		return mMessages;
	}
	
	@SuppressLint("NotifyDataSetChanged")
	public void setMessages(List<Message> messages) {
		this.mMessages = messages;
		notifyDataSetChanged();
	}
	
	@SuppressLint("NotifyDataSetChanged")
	public void addMessages(List<Message> messages) {
		this.mMessages.addAll(messages);
		notifyDataSetChanged();
	}
	
	@SuppressLint("NotifyDataSetChanged")
	public void addMessage(Message message) {
		this.mMessages.add(message);
		notifyDataSetChanged();
	}
	
	public int getViewLayout() {
		return viewLayout;
	}
	
	@SuppressLint("NotifyDataSetChanged")
	public void setViewLayout(int viewLayout) {
		this.viewLayout = viewLayout;
		notifyDataSetChanged();
	}
	
	//endregion
	
	@NonNull
	@Override
	public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		switch (viewType){
			case USER_MESSAGE_VIEW:
				View v_user = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_message_user,parent, false);
				return new UserViewHolder(v_user);
			case FRIEND_MESSAGE_VIEW:
				View v_friend = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_message_friend,parent, false);
				return new FriendViewHolder(v_friend);
			case USER_VIDEO_VIEW:
				View v_user1 = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_video_user,parent, false);
				return new UserVideoViewHolder(v_user1);
			case FRIEND_VIDEO_VIEW:
				View v_friend1 = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_video_friend,parent, false);
				return new FriendVideoViewHolder(v_friend1);
			case ADMIN_MESSAGE_VIEW:
				View v_admin = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_message_admin,parent, false);
				return new AdminViewHolder(v_admin);
			default:
				return null;
		}
	}
	
	@Override
	public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
		Message message = mMessages.get(position);
		switch (getItemViewType(position)){
			case USER_MESSAGE_VIEW:
				((UserViewHolder)holder).bind(message);
				break;
			case FRIEND_MESSAGE_VIEW:
				((FriendViewHolder)holder).bind(message);
				break;
			case USER_VIDEO_VIEW:
				((UserVideoViewHolder)holder).bind(message);
				break;
			case FRIEND_VIDEO_VIEW:
				((FriendVideoViewHolder)holder).bind(message);
				break;
			case ADMIN_MESSAGE_VIEW:
			default:
				break;
		}
	}
	
	@Override
	public int getItemCount() {
		return mMessages.size();
	}
	
	@Override
	public int getItemViewType(int position) {
		if (user != null && mMessages.size() > 0){
			if (getViewLayout() == VIDEO_LAYOUT){
				if (Objects.equals(mMessages.get(position).sender_id, user.id)) {
					return FRIEND_VIDEO_VIEW;
				} else if (Objects.equals(mMessages.get(position).receiver_id, user.id)) {
					return USER_VIDEO_VIEW;
				} else {
					return ADMIN_MESSAGE_VIEW;
				}
			}else {
				if (Objects.equals(mMessages.get(position).sender_id, user.id)) {
					return FRIEND_MESSAGE_VIEW;
				} else if (Objects.equals(mMessages.get(position).receiver_id, user.id)) {
					return USER_MESSAGE_VIEW;
				} else {
					return ADMIN_MESSAGE_VIEW;
				}
			}
		}
		return super.getItemViewType(position);
	}
	
	private boolean shouldBubbleAllRound(int position){
		if (position == 0)return false;
		try {
			Message prev_chat = getMessages().get(position - 1);
			Message current_chat = getMessages().get(position);
			int prev_view = getItemViewType(position - 1);
			int current_view = getItemViewType(position);
			
			boolean isNow = ((current_chat.timestamp - prev_chat.timestamp)/1000) <= 60;
			if (prev_view == current_view && DateUtils.hasSameDate(prev_chat.timestamp,current_chat.timestamp) &&
					isNow){
				return true;
			}
		}catch (Exception e){
			e.printStackTrace();
		}
		
		return false;
	}
	
	class UserViewHolder extends RecyclerView.ViewHolder{
		TextView _text, _time;
		View _padding;
		public UserViewHolder(@NonNull View itemView) {
			super(itemView);
			_text = itemView.findViewById(R.id.text);
			_time = itemView.findViewById(R.id.time);
			_padding = itemView.findViewById(R.id.padding);
		}
		
		void bind(Message message){
			_text.setText(message.message);
			_time.setText(DateUtils.formatPassedTimeAndDate(context,message.timestamp));
			if (shouldBubbleAllRound(getBindingAdapterPosition())){
				_text.setBackgroundResource(R.drawable.message_background_user_round);
				_time.setVisibility(View.GONE);
			}else{
				_text.setBackgroundResource(R.drawable.message_background_user_default);
				_time.setVisibility(View.VISIBLE);
			}
			_padding.setVisibility(getItemCount() == getBindingAdapterPosition() + 1?View.VISIBLE:View.GONE);
		}
	}
	
	class UserVideoViewHolder extends RecyclerView.ViewHolder{
		TextView _text,_initial;
		CircleImageView _avatar;
		public UserVideoViewHolder(@NonNull View itemView) {
			super(itemView);
			_text = itemView.findViewById(R.id.text);
			_initial = itemView.findViewById(R.id.avatar_initial);
			_avatar = itemView.findViewById(R.id.avatar);
		}
		
		void bind(Message message){
			_text.setText(message.message);
			
//			if (user.avatar == null){
				_initial.setText("KJ");
				_initial.setBackgroundTintList(ColorStateList.valueOf(ResourcesCompat.getColor(context.getResources(),R.color.colorAccent, null)));
				_initial.setVisibility(View.VISIBLE);
				_avatar.setVisibility(View.GONE);
//			}else{
//				Glide.with(context)
//						.load(user.avatar)
//						.error(R.drawable.user)
//						.into(_avatar);
//
//				_avatar.setVisibility(View.VISIBLE);
//				_initial.setVisibility(View.GONE);
//			}
		}
	}
	
	class FriendViewHolder extends RecyclerView.ViewHolder{
		TextView _text;
		TextView _time;
		View _padding;
		public FriendViewHolder(@NonNull View itemView) {
			super(itemView);
			_text = itemView.findViewById(R.id.text);
			_time = itemView.findViewById(R.id.time);
			_padding = itemView.findViewById(R.id.padding);
		}
		
		void bind(Message message){
			_text.setText(message.message);
			_time.setText(DateUtils.formatPassedTimeAndDate(context,message.timestamp));
//			Glide.with(context)
//					.load(chat.sender_avatar)
//					.error(R.drawable.user)
//					.into(_avata);
			if (shouldBubbleAllRound(getBindingAdapterPosition())){
				_text.setBackgroundResource(R.drawable.message_background_friend_round);
				_time.setVisibility(View.GONE);
			}else{
				_text.setBackgroundResource(R.drawable.message_background_friend_default);
				_time.setVisibility(View.VISIBLE);
			}
			_padding.setVisibility(getItemCount() == getBindingAdapterPosition() + 1?View.VISIBLE:View.GONE);
		}
	}
	
	class FriendVideoViewHolder extends RecyclerView.ViewHolder{
		TextView _text,_initial;
		CircleImageView _avatar;
		public FriendVideoViewHolder(@NonNull View itemView) {
			super(itemView);
			_text = itemView.findViewById(R.id.text);
			_initial = itemView.findViewById(R.id.avatar_initial);
			_avatar = itemView.findViewById(R.id.avatar);
		}
		
		void bind(Message message){
			_text.setText(message.message);
			
			if (user.avatar == null){
				_initial.setText(getInitial(user));
				_initial.setBackgroundTintList(ColorStateList.valueOf(ResourcesCompat.getColor(context.getResources(),android.R.color.holo_orange_dark, null)));
				_initial.setVisibility(View.VISIBLE);
				_avatar.setVisibility(View.GONE);
			}else{
				Glide.with(context)
						.load(user.avatar)
						.error(R.drawable.user)
						.into(_avatar);
				
				_avatar.setVisibility(View.VISIBLE);
				_initial.setVisibility(View.GONE);
			}
		}
	}
	
	static class AdminViewHolder extends RecyclerView.ViewHolder{
		
		public AdminViewHolder(@NonNull View itemView) {
			super(itemView);
		}
	}
}
