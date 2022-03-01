package database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import models.User;

@Dao
public interface UserDao {
	@Query("SELECT * FROM user")
	List<User> getAll();
	
	@Query("SELECT * FROM user WHERE id IN (:userIds)")
	List<User> loadAllByIds(int[] userIds);
	
	@Query("SELECT * FROM user WHERE phone LIKE :phone LIMIT 1")
	User findByPhone(String phone);
	
	@Query("SELECT * FROM user LIMIT 1")
	User user();
	
	@Insert
	void insertAll(User... users);
	
	@Insert(onConflict = OnConflictStrategy.REPLACE)
	void insert(User user);
	
	@Update
	void update(User... users);
	
	@Delete
	void delete(User user);
}
