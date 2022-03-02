package fragments.main.pages;

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
import com.brimbay.chat.databinding.FragmentChatBinding;
import com.google.firebase.auth.FirebaseAuth;

import adapters.ChatAdapter;
import database.AppDatabase;
import models.Friend;
import utils.AppExecutors;
import utils.ChatUtils;

public class ChatFragment extends Fragment {
	private Context selfRef;
	
	private RecyclerView _recycler_chats;
	
	private ChatViewModel mViewModel;
	private ChatAdapter mChatAdapter;
	private FragmentChatBinding binding;
	private ChatsCallbackListener chatsCallbackListener;
	private FirebaseAuth mAuth;
	
	public static ChatFragment newInstance() {
		return new ChatFragment();
	}
	
	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		selfRef = requireContext();
		mAuth = FirebaseAuth.getInstance();
		mViewModel = new ViewModelProvider(this).get(ChatViewModel.class);
		mViewModel.init(selfRef);
	}
	
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
							 @Nullable Bundle savedInstanceState) {
		binding = FragmentChatBinding.inflate(inflater,container, false);
		initUI();
		return binding.getRoot();
	}
	
	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		handleChats();
	}
	
	//region Member Methods
	private void initUI(){
		_recycler_chats = binding.ctRecyclerChats;
	}
	
	private void handleChats(){
		mChatAdapter = new ChatAdapter(selfRef);
		_recycler_chats.setLayoutManager(new LinearLayoutManager(selfRef, RecyclerView.VERTICAL, false));
		_recycler_chats.setAdapter(mChatAdapter);
		mChatAdapter.setOnChatCallbackListener((chat, i) -> {
			AppExecutors.getInstance().diskIO().execute(() -> {
				Friend friend = AppDatabase.newInstance(selfRef).friendDao().findByID(chat.friend_id);
				AppExecutors.getInstance().mainThread().execute(() -> {
					if (friend != null ){
						ChatUtils chatUtils = new ChatUtils(selfRef);
						chatUtils.sendChatRequest(friend, mAuth.getUid());
					}
				});
			});
		
		});
		
		mViewModel.getChats().observe(getViewLifecycleOwner(), chats -> mChatAdapter.setChats(chats));
	}
	//endregion
	
	@Override
	public void onResume() {
		super.onResume();
		mViewModel.init(selfRef);
	}
	
	public interface ChatsCallbackListener{
		void onChatClicked(Friend friend);
	}
	
	public ChatsCallbackListener getChatsCallbackListener() {
		return chatsCallbackListener;
	}
	
	public void setChatsCallbackListener(ChatsCallbackListener chatsCallbackListener) {
		this.chatsCallbackListener = chatsCallbackListener;
	}
}
