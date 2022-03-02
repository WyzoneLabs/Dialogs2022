package fragments.main.pages;

import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.brimbay.chat.R;

public class ChatRequestFragment extends Fragment {
	
	private ChatRequestViewModel mViewModel;
	
	public static ChatRequestFragment newInstance() {
		return new ChatRequestFragment();
	}
	
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
							 @Nullable Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_chat_request, container, false);
	}
	
	@Override
	public void onActivityCreated(@Nullable Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		mViewModel = new ViewModelProvider(this).get(ChatRequestViewModel.class);
		// TODO: Use the ViewModel
	}
	
}
