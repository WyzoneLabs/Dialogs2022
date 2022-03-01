package database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import models.Message;
import models.User;
import utils.Constants;

@Database(
		entities = {User.class, Message.class},
		version = 1,
		autoMigrations = {
//				@AutoMigration(from = 1, to = 2)
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
}
