package adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.brimbay.chat.R;
import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import models.Chat;
import utils.DateUtils;

/**
 * Created by Kevine James on 2/22/2022.
 * Copyright (c) 2022 Brimbay. All rights reserved.
 */
public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ViewHolder> {
	private final Context context;
	private List<Chat> mChats;
	private OnChatCallbackListener onChatCallbackListener;
	
	public interface OnChatCallbackListener{
		void onChatClick(Chat chat, int i);
	}
	
	public ChatAdapter(Context context) {
		this.context = context;
		mChats = new ArrayList<>();
	}
	
	//region Getters & Setters
	public OnChatCallbackListener getOnChatCallbackListener() {
		return onChatCallbackListener;
	}
	
	public void setOnChatCallbackListener(OnChatCallbackListener onChatCallbackListener) {
		this.onChatCallbackListener = onChatCallbackListener;
	}
	
	public List<Chat> getChats() {
		return mChats;
	}
	
	public void setChats(List<Chat> mChats) {
		this.mChats = mChats;
	}
	
	//endregion
	
	@NonNull
	@Override
	public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_chat, parent, false);
		return new ViewHolder(view);
	}
	
	@Override
	public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
		Chat chat = mChats.get(position);
		holder._name.setText(context.getString(R.string.friend_name, chat.first_name, chat.last_name));
		holder._time.setText(DateUtils.formatPassedTimeAndDate(context,chat.timestamp));
		holder._text.setText(chat.text);
		Glide.with(context)
				.load(chat.avatar)
				.error(R.drawable.user)
				.into(holder._avatar);
		
		//TODO:Only for test
		holder._unread_box.setVisibility(View.VISIBLE);
		holder._online.setVisibility(View.VISIBLE);
		holder._unread.setText("2");
		
		holder._parent.setOnClickListener(view -> {
			if (onChatCallbackListener != null)onChatCallbackListener.onChatClick(chat,position);
		});
	}
	
	@Override
	public int getItemCount() {
		return mChats.size();
	}
	
	public static class ViewHolder extends RecyclerView.ViewHolder {
		CircleImageView _avatar;
		TextView _name, _time,_unread,_text;
		View _parent,_unread_box,_online;
		public ViewHolder(@NonNull View itemView) {
			super(itemView);
			_avatar = itemView.findViewById(R.id.avatar);
			_name = itemView.findViewById(R.id.name);
			_text = itemView.findViewById(R.id.text);
			_time = itemView.findViewById(R.id.time);
			_unread = itemView.findViewById(R.id.unread_count);
			_parent = itemView.findViewById(R.id.parent);
			_unread_box = itemView.findViewById(R.id.unread_count_box);
			_online = itemView.findViewById(R.id.online);
		}
	}
}
