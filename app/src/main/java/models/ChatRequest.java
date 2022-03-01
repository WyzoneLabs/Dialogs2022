package models;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.Keep;

import rtc.Packable;

/**
 * Created by Kevine James on 2/25/2022.
 * Copyright (c) 2022 Brimbay. All rights reserved.
 */
@Keep
public class ChatRequest implements Parcelable {
	public static final int PENDING = 0;
	public static final int REJECTED = -1;
	public static final int ACCEPTED = 1;
	
	public String id;
	public String sender_id;
	public String receiver_id;
	public String room_id;
	public long timestamp;
	public int is_accepted;
	
	public ChatRequest() {
	}
	
	public ChatRequest(String id, String sender_id, String receiver_id, String room_id, long timestamp, int is_accepted) {
		this.id = id;
		this.sender_id = sender_id;
		this.receiver_id = receiver_id;
		this.room_id = room_id;
		this.timestamp = timestamp;
		this.is_accepted = is_accepted;
	}
	
	protected ChatRequest(Parcel in) {
		id = in.readString();
		sender_id = in.readString();
		receiver_id = in.readString();
		room_id = in.readString();
		timestamp = in.readLong();
		is_accepted = in.readInt();
	}
	
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(id);
		dest.writeString(sender_id);
		dest.writeString(receiver_id);
		dest.writeString(room_id);
		dest.writeLong(timestamp);
		dest.writeInt(is_accepted);
	}
	
	@Override
	public int describeContents() {
		return 0;
	}
	
	public static final Creator<ChatRequest> CREATOR = new Creator<ChatRequest>() {
		@Override
		public ChatRequest createFromParcel(Parcel in) {
			return new ChatRequest(in);
		}
		
		@Override
		public ChatRequest[] newArray(int size) {
			return new ChatRequest[size];
		}
	};
}
