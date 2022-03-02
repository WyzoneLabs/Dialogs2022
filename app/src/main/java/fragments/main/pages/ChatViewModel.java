package fragments.main.pages;

import android.content.Context;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;

import database.AppDatabase;
import models.Chat;
import models.Friend;
import utils.AppExecutors;

public class ChatViewModel extends ViewModel {
	private MutableLiveData<List<Chat>> mChats;
	
	public ChatViewModel() {
		mChats = new MutableLiveData<>();
	}
	
	public void init(Context context){
		fetchChats(context);
	}
	
	private void fetchChats(Context context){
		AppExecutors.getInstance().diskIO().execute(() -> {
			List<Chat> chats = AppDatabase.newInstance(context).chatDao().getAll();
			AppExecutors.getInstance().mainThread().execute(()->mChats.setValue(chats));
		});
	}
	
	public MutableLiveData<List<Chat>> getChats() {
		return mChats;
	}
	
}
