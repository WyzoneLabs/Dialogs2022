package adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import fragments.main.pages.ChatFragment;
import fragments.main.pages.ChatRequestFragment;
import fragments.main.pages.FriendFragment;

public class ViewPagerFragmentAdapter extends FragmentStateAdapter {
    
    public ViewPagerFragmentAdapter(@NonNull Fragment fragment) {
        super(fragment);
    }
    
    
    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return ChatFragment.newInstance();
            case 1:
                return ChatRequestFragment.newInstance();
            case 2:
                return FriendFragment.newInstance();
        }
        return null;
    }
    
    @Override
    public int getItemCount() {
        return 3;
    }
    
}
