package models;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Created by Kevine James on 2/22/2022.
 * Copyright (c) 2022 Brimbay. All rights reserved.
 */
@Keep
@Entity(tableName = "user")
public class User implements Parcelable {
	@PrimaryKey
	@NonNull
	public String id;
	public String first_name;
	public String last_name;
	public String avatar;
	public String phone;
	
	public User() {
		id = "";
	}
	
	protected User(Parcel in) {
		id = in.readString();
		first_name = in.readString();
		last_name = in.readString();
		avatar = in.readString();
		phone = in.readString();
	}
	
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(id);
		dest.writeString(first_name);
		dest.writeString(last_name);
		dest.writeString(avatar);
		dest.writeString(phone);
	}
	
	@Override
	public int describeContents() {
		return 0;
	}
	
	public static final Creator<User> CREATOR = new Creator<User>() {
		@Override
		public User createFromParcel(Parcel in) {
			return new User(in);
		}
		
		@Override
		public User[] newArray(int size) {
			return new User[size];
		}
	};
}
