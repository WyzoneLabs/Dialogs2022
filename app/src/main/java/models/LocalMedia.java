package models;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.Keep;

@Keep
public class LocalMedia implements Parcelable {
	public String path;
	public String type;
	
	public LocalMedia() {
	}
	
	public LocalMedia(String path, String type) {
		this.path = path;
		this.type = type;
	}
	
	protected LocalMedia(Parcel in) {
		path = in.readString();
		type = in.readString();
	}
	
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(path);
		dest.writeString(type);
	}
	
	@Override
	public int describeContents() {
		return 0;
	}
	
	public static final Creator<LocalMedia> CREATOR = new Creator<LocalMedia>() {
		@Override
		public LocalMedia createFromParcel(Parcel in) {
			return new LocalMedia(in);
		}
		
		@Override
		public LocalMedia[] newArray(int size) {
			return new LocalMedia[size];
		}
	};
}
