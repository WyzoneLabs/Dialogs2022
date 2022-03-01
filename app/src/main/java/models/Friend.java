package models;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.room.Ignore;

import utils.Util;

/**
 * Created by Kevine James on 2/22/2022.
 * Copyright (c) 2022 Brimbay. All rights reserved.
 */
public class Friend extends User implements Parcelable {
	public String room_id;
	
	public Friend() {
		super();
	}
	
	@Ignore
	public String getRoomId(String user_id) {
		this.room_id = Util.getChatRoomId(user_id,this.id);
		return this.room_id;
	}
	
	protected Friend(Parcel in) {
		room_id = in.readString();
	}
	
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(room_id);
	}
	
	@Override
	public int describeContents() {
		return 0;
	}
	
	public static final Creator<Friend> CREATOR = new Creator<Friend>() {
		@Override
		public Friend createFromParcel(Parcel in) {
			return new Friend(in);
		}
		
		@Override
		public Friend[] newArray(int size) {
			return new Friend[size];
		}
	};
}
