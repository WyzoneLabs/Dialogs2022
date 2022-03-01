package models;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Created by Kevine James on 2/21/2022.
 * Copyright (c) 2022 Brimbay. All rights reserved.
 */
@Keep
@Entity(tableName = "message")
public class Message implements Parcelable {
	@PrimaryKey()
	@NonNull
	public String id;
	public String sender_id;
	public String receiver_id;
	public String message;
	public String room_id;
	public long timestamp;
	
	public Message() {
		id = "";
	}
	
	protected Message(Parcel in) {
		id = in.readString();
		sender_id = in.readString();
		receiver_id = in.readString();
		message = in.readString();
		room_id = in.readString();
		timestamp = in.readLong();
	}
	
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(id);
		dest.writeString(sender_id);
		dest.writeString(receiver_id);
		dest.writeString(message);
		dest.writeString(room_id);
		dest.writeLong(timestamp);
	}
	
	@Override
	public int describeContents() {
		return 0;
	}
	
	public static final Creator<Message> CREATOR = new Creator<Message>() {
		@Override
		public Message createFromParcel(Parcel in) {
			return new Message(in);
		}
		
		@Override
		public Message[] newArray(int size) {
			return new Message[size];
		}
	};
}
