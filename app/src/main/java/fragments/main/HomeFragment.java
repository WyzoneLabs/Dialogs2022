package fragments.main;

import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.brimbay.chat.   ChatActivity;
import com.brimbay.chat.R;
import com.brimbay.chat.databinding.FragmentHomeBinding;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

import adapters.ChatAdapter;
import adapters.FriendAdapter;
import adapters.ViewPagerFragmentAdapter;
import models.Chat;
import models.ChatRequest;
import models.Friend;
import utils.ChatUtils;
import utils.Constants;
import utils.DateUtils;

public class HomeFragment extends Fragment {
	private Context selfRef;
	
	//region Views
	private RecyclerView _recycler_friends;
	private ViewPager2 _view_pager;
	private TabLayout _tab_layout;
	//endregion
	
	private HomeViewModel mViewModel;
	private ViewPagerFragmentAdapter pagerFragmentAdapter;
	private FragmentHomeBinding binding;
	private FriendAdapter mFriendAdapter;
	private FirebaseAuth mAuth;
	
	public static HomeFragment newInstance() {
		return new HomeFragment();
	}
	
	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		selfRef = requireContext();
		mAuth = FirebaseAuth.getInstance();
		mViewModel = new ViewModelProvider(this).get(HomeViewModel.class);
		mViewModel.init(selfRef);
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
	}
	
	//region Member Methods
	private void initUI(){
		_view_pager = binding.hPager;
		_tab_layout = binding.hTabLayout;
		_recycler_friends = binding.hFriendsRecycler;
		handlePages();
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
				ChatUtils chatUtils = new ChatUtils(selfRef);
				chatUtils.sendChatRequest(friend, mAuth.getUid());
			}
		});
		mViewModel.getFriends().observe(getViewLifecycleOwner(), friends -> mFriendAdapter.addFriends(friends));
	}
	
	private void handlePages(){
		String[] tabs = {
				getString(R.string.chats),getString(R.string.requests),getString(R.string.friends),
		};
		
		pagerFragmentAdapter = new ViewPagerFragmentAdapter(this);
		_view_pager.setOrientation(ViewPager2.ORIENTATION_VERTICAL);
		
		_view_pager.setAdapter(pagerFragmentAdapter);
		_view_pager.setUserInputEnabled(false);
		new TabLayoutMediator(_tab_layout, _view_pager,
				(tab, position) -> tab.setText(tabs[position])
		).attach();
	}
	
	//endregion
	
}
