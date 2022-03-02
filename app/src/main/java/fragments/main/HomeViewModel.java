package fragments.main;

import android.content.Context;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;

import database.AppDatabase;
import models.Chat;
import models.Friend;
import models.Message;
import utils.AppExecutors;

public class HomeViewModel extends ViewModel {
	private MutableLiveData<List<Friend>> mFriends;
	
	public HomeViewModel() {
		mFriends = new MutableLiveData<>();
	}
	
	public void init(Context context){
		fetchFriends(context);
	}
	
	private void fetchFriends(Context context){
		AppExecutors.getInstance().diskIO().execute(() -> {
			List<Friend> friends = AppDatabase.newInstance(context).friendDao().getAll();
			AppExecutors.getInstance().mainThread().execute(()->mFriends.setValue(friends));
		});
	}
	public MutableLiveData<List<Friend>> getFriends() {
		return mFriends;
	}
}
