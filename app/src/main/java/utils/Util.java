package utils;

import java.util.Locale;

import models.Friend;
import models.User;

/**
 * Created by Kevine James on 2/21/2022.
 * Copyright (c) 2022 Brimbay. All rights reserved.
 */
public class Util {
	public static String getChatRoomId(String sender_id,String receiver_id){
		int sender = sender_id.hashCode();
		int receiver = receiver_id.hashCode();
		return Math.min(sender,receiver)+""+Math.max(sender,receiver);
	}
	
	public static String getInitial(User friend){
		return String.format(Locale.ROOT,"%s%s",
				friend.first_name.toUpperCase(Locale.ROOT).charAt(0),
				friend.last_name.toUpperCase(Locale.ROOT).charAt(0));
	}
}
