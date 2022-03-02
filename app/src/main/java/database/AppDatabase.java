package database;

import android.content.Context;

import androidx.room.AutoMigration;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import models.Chat;
import models.Friend;
import models.Message;
import models.User;
import utils.Constants;

@Database(
		entities = {User.class, Message.class, Friend.class, Chat.class},
		version = 3,
		autoMigrations = {
				@AutoMigration(from = 2, to = 3)
		}
)
public abstract class AppDatabase  extends RoomDatabase {
	
	public static AppDatabase newInstance(Context context){
		return Room.databaseBuilder(context, AppDatabase.class, Constants.DATABASE_NAME)
				.fallbackToDestructiveMigration()
				.build();
	}
	
	public abstract UserDao userDao();
	public abstract MessageDao messageDao();
	public abstract FriendDao friendDao();
	public abstract ChatDao chatDao();
}
