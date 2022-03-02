package models;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import database.AppDatabase;
import utils.AppExecutors;

/**
 * Created by Kevine James on 2/22/2022.
 * Copyright (c) 2022 Brimbay. All rights reserved.
 */
@Keep
@Entity(tableName = "chat")
public class Chat implements Parcelable {
	@PrimaryKey
	@NonNull
	public String room_id;
	public String friend_id;
	public String text;
	public long timestamp;
	@Ignore
	public Friend friend;
	
	public Chat() {
		this.room_id = "";
	}
	
	@Ignore
	public Chat(@NonNull String room_id, String friend_id, String text, long timestamp) {
		this.room_id = room_id;
		this.friend_id = friend_id;
		this.text = text;
		this.timestamp = timestamp;
	}
	
	protected Chat(Parcel in) {
		room_id = in.readString();
		friend_id = in.readString();
		text = in.readString();
		timestamp = in.readLong();
		friend = in.readParcelable(Friend.class.getClassLoader());
	}
	
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(room_id);
		dest.writeString(friend_id);
		dest.writeString(text);
		dest.writeLong(timestamp);
		dest.writeParcelable(friend, flags);
	}
	
	@Override
	public int describeContents() {
		return 0;
	}
	
	public static final Creator<Chat> CREATOR = new Creator<Chat>() {
		@Override
		public Chat createFromParcel(Parcel in) {
			return new Chat(in);
		}
		
		@Override
		public Chat[] newArray(int size) {
			return new Chat[size];
		}
	};
}
