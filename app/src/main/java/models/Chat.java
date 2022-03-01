package models;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Kevine James on 2/22/2022.
 * Copyright (c) 2022 Brimbay. All rights reserved.
 */
public class Chat extends Friend implements Parcelable {
	public String text;
	public long timestamp;
	public int unread_count;
	
	public Chat() {
	}
	
	protected Chat(Parcel in) {
		text = in.readString();
		timestamp = in.readLong();
		unread_count = in.readInt();
	}
	
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(text);
		dest.writeLong(timestamp);
		dest.writeInt(unread_count);
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
