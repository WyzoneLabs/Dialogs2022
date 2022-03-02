package com.brimbay.chat;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentManagerKt;
import androidx.fragment.app.FragmentTransaction;

import android.os.Bundle;

import com.brimbay.chat.databinding.ActivityAccountBinding;

import fragments.login.LoginFragment;
import fragments.register.RegisterFragment;

public class AccountActivity extends AppCompatActivity {
    private AccountActivity selfRef;

    private FragmentManager mFragmentManager;
    private ActivityAccountBinding binding;
    private boolean codeSent = false;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        selfRef = this;
        binding = ActivityAccountBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        initUI();
        mFragmentManager = getSupportFragmentManager();
    
        LoginFragment loginFragment = LoginFragment.newInstance();
        loginFragment.setOnLoginInteraction(new LoginFragment.OnLoginInteraction() {
            @Override
            public void onLoginPageChanged(int page) {
                codeSent = true;
                binding.toolbarBox.title.setText(page == LoginFragment.STATE_CODE_SENT?getString(R.string.code_verification):getString(R.string.phone_number));
            }
    
            @Override
            public void onStartRegistration() {
                binding.toolbarBox.title.setText(getString(R.string.register));
                RegisterFragment registerFragment = RegisterFragment.newInstance();
                addFragment(registerFragment,"register");
            }
        });
        addFragment(loginFragment,null);
    }
    
    //region Member Methods
    private void initUI(){
        binding.toolbarBox.back.setOnClickListener(view -> onBackPressed());
        binding.toolbarBox.title.setText(codeSent?getString(R.string.code_verification):getString(R.string.phone_number));
    }
    
    private void addFragment(Fragment fragment, @Nullable String tag){
        FragmentTransaction transaction = mFragmentManager.beginTransaction();
        transaction.replace(R.id.ac_body, fragment, tag != null?tag:"current_page").commitNow();
    }
    //endregion
    
    @Override
    public void onBackPressed() {
        if (codeSent){
        
        }else {
            super.onBackPressed();
        }
    }
}
