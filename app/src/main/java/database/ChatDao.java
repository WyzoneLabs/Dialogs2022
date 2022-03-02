package database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import models.Chat;
import models.User;

/**
 * Created by Kevine James on 3/2/2022.
 * Copyright (c) 2022 Brimbay. All rights reserved.
 */
@Dao
public interface ChatDao {
	@Query("SELECT * FROM chat")
	List<Chat> getAll();
	
	@Query("SELECT * FROM chat WHERE room_id LIKE :room_id LIMIT 1")
	Chat findByRoomId(String room_id);
	
	@Insert
	void insertAll(Chat... chats);
	
	@Insert(onConflict = OnConflictStrategy.REPLACE)
	void insert(Chat chat);
	
	@Update
	void update(Chat... chats);
	
	@Delete
	void delete(Chat chat);
}
