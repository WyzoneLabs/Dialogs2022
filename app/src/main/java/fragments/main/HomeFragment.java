package fragments.main;

import androidx.lifecycle.ViewModelProvider;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.brimbay.chat.R;
import com.brimbay.chat.databinding.FragmentHomeBinding;

import adapters.ChatAdapter;
import adapters.FriendAdapter;
import models.Chat;
import models.Friend;

public class HomeFragment extends Fragment {
	private Context selfRef;
	
	//region Views
	private RecyclerView _recycler_friends,_recycler_chats;
	//endregion
	
	private HomeViewModel mViewModel;
	private FragmentHomeBinding binding;
	private FriendAdapter mFriendAdapter;
	private ChatAdapter mChatAdapter;
	
	public static HomeFragment newInstance() {
		return new HomeFragment();
	}
	
	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		selfRef = requireContext();
		mViewModel = new ViewModelProvider(this).get(HomeViewModel.class);
	}
	
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
							 @Nullable Bundle savedInstanceState) {
		binding = FragmentHomeBinding.inflate(inflater,container, false);
		initUI();
		return binding.getRoot();
	}
	
	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		handleFriends();
		handleChats();
	}
	
	//region Member Methods
	private void initUI(){
		_recycler_chats = binding.hChatsRecycler;
		_recycler_friends = binding.hFriendsRecycler;
	}
	
	private void handleFriends(){
		mFriendAdapter = new FriendAdapter(selfRef);
		_recycler_friends.setLayoutManager(new LinearLayoutManager(selfRef, RecyclerView.HORIZONTAL, false));
		_recycler_friends.setAdapter(mFriendAdapter);
		mFriendAdapter.setOnFriendCallbackListener(new FriendAdapter.OnFriendCallbackListener() {
			@Override
			public void onAddFriendClickListener() {
			
			}
			
			@Override
			public void onFriendClickListener(Friend friend, int i) {
			
			}
		});
	}
	
	private void handleChats(){
		mChatAdapter = new ChatAdapter(selfRef);
		_recycler_chats.setLayoutManager(new LinearLayoutManager(selfRef, RecyclerView.VERTICAL, false));
		_recycler_chats.setAdapter(mChatAdapter);
		mChatAdapter.setOnChatCallbackListener((chat, i) -> {
		
		});
	}
	//endregion
}
