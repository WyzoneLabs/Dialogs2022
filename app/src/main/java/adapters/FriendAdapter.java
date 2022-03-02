package adapters;

import static utils.Util.getInitial;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.brimbay.chat.R;
import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import de.hdodenhof.circleimageview.CircleImageView;
import models.Friend;

/**
 * Created by Kevine James on 2/22/2022.
 * Copyright (c) 2022 Brimbay. All rights reserved.
 */
public class FriendAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
	private static final int VIEW_FRIEND = 199;
	private static final int VIEW_ADD_FRIEND = 200;
	
	private final Context context;
	private List<Friend> mFriends;
	private final int[] colors;
	private OnFriendCallbackListener onFriendCallbackListener;
	
	public interface OnFriendCallbackListener{
		void onAddFriendClickListener();
		void onFriendClickListener(Friend friend, int i);
	}
	
	public FriendAdapter(Context context) {
		this.context = context;
		mFriends = new ArrayList<>();
		colors = context.getResources().getIntArray(R.array.color_initial_bgs);
	}
	
	//region Getters & Setters
	public List<Friend> getFriends() {
		return mFriends;
	}
	
	@SuppressLint("NotifyDataSetChanged")
	public void setFriends(List<Friend> friends) {
		this.mFriends = friends;
		notifyDataSetChanged();
	}
	
	@SuppressLint("NotifyDataSetChanged")
	public void addFriends(List<Friend> friends) {
		this.mFriends.addAll(friends);
		notifyDataSetChanged();
	}
	
	@SuppressLint("NotifyDataSetChanged")
	public void addFriend(Friend friend) {
		this.mFriends.add(friend);
		notifyDataSetChanged();
	}
	
	public OnFriendCallbackListener getOnFriendCallbackListener() {
		return onFriendCallbackListener;
	}
	
	public void setOnFriendCallbackListener(OnFriendCallbackListener onFriendCallbackListener) {
		this.onFriendCallbackListener = onFriendCallbackListener;
	}
	//endregion
	
	@NonNull
	@Override
	public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		if (viewType == VIEW_FRIEND){
			View v1 = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_friend,parent,false);
			return new FriendViewHolder(v1);
		}else if (viewType == VIEW_ADD_FRIEND){
			View v2 = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_add_friend,parent,false);
			return new AddFriendViewHolder(v2);
		}
		return null;
	}
	
	@Override
	public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
		if (getItemViewType(position) == VIEW_FRIEND){
			Friend friend = mFriends.get(position);
			((FriendViewHolder)holder).bind(friend);
		}else if (getItemViewType(position) == VIEW_ADD_FRIEND){
			((AddFriendViewHolder)holder).bind();
		}
	}
	
	@Override
	public int getItemCount() {
		return mFriends.size();
	}
	
	@Override
	public int getItemViewType(int position) {
		if (mFriends != null) {
//			if (position == 0) {
//				return VIEW_ADD_FRIEND;
//			} else {
				return VIEW_FRIEND;
//			}
		}
		return super.getItemViewType(position);
	}
	
	class FriendViewHolder extends RecyclerView.ViewHolder{
		ImageView _avatar;
		TextView _name,_initial;
		View _view,_avatar_cnt,_padding;
		public FriendViewHolder(@NonNull View itemView) {
			super(itemView);
			_view = itemView.findViewById(R.id.parent);
			_avatar = itemView.findViewById(R.id.avatar);
			_padding = itemView.findViewById(R.id.padding);
			_avatar_cnt = itemView.findViewById(R.id.avatar_cnt);
			_initial = itemView.findViewById(R.id.avatar_initial);
			_name = itemView.findViewById(R.id.name);
		}
		
		void bind(Friend friend){
			_name.setText(friend.first_name);
			_padding.setVisibility(getBindingAdapterPosition() == 0?View.VISIBLE:View.GONE);
			Random random = new Random();
			if (friend.avatar == null){
				_initial.setText(getInitial(friend));
				_initial.setBackgroundTintList(ColorStateList.valueOf(colors[random.nextInt(colors.length)]));
				_initial.setVisibility(View.VISIBLE);
				_avatar_cnt.setVisibility(View.GONE);
			}else{
				Glide.with(context)
						.load(friend.avatar)
						.error(R.drawable.user)
						.into(_avatar);
				
				_avatar_cnt.setVisibility(View.VISIBLE);
				_initial.setVisibility(View.GONE);
			}
			
			_view.setOnClickListener(view -> {
				if (onFriendCallbackListener != null)onFriendCallbackListener.onFriendClickListener(friend,getBindingAdapterPosition());
			});
		}
	}
	
	class AddFriendViewHolder extends RecyclerView.ViewHolder{
		View _view;
		public AddFriendViewHolder(@NonNull View itemView) {
			super(itemView);
			_view = itemView.findViewById(R.id.parent);
		}
		
		void bind(){
			_view.setOnClickListener(view -> {
				if (onFriendCallbackListener != null)onFriendCallbackListener.onAddFriendClickListener();
			});
		}
	}
}
