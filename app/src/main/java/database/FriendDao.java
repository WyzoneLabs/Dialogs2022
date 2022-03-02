package database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import models.Friend;
import models.User;

/**
 * Created by Kevine James on 3/2/2022.
 * Copyright (c) 2022 Brimbay. All rights reserved.
 */
@Dao
public interface FriendDao {
	@Query("SELECT * FROM friend")
	List<Friend> getAll();
	
	@Query("SELECT * FROM friend WHERE id IN (:ids)")
	List<Friend> loadAllByIds(int[] ids);
	
	@Query("SELECT * FROM friend WHERE id LIKE :id LIMIT 1")
	Friend findByID(String id);
	
	@Insert
	void insertAll(Friend... friends);
	
	@Insert(onConflict = OnConflictStrategy.REPLACE)
	void insert(Friend friend);
	
	@Update
	void update(Friend... friends);
	
	@Delete
	void delete(Friend friend);
}
