package database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import models.Message;

/**
 * Created by Kevine James on 2/5/2022.
 * Copyright (c) 2022 Brimbay. All rights reserved.
 */
@Dao
public interface MessageDao {
	@Query("SELECT * FROM message")
	List<Message> getAll();
	
	@Query("SELECT * FROM message WHERE room_id = :roomId")
	List<Message> getByRoomID(String roomId);
	
	@Insert
	void insertAll(Message... messages);
	
	@Insert(onConflict = OnConflictStrategy.REPLACE)
	void insert(Message message);
	
	@Update
	void update(Message... messages);
	
	@Delete
	void delete(Message message);
}
