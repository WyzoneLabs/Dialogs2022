package fragments.register;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.location.Location;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.anilokcun.uwmediapicker.UwMediaPicker;
import com.brimbay.chat.R;
import com.brimbay.chat.StartActivity;
import com.brimbay.chat.databinding.FragmentRegisterBinding;
import com.brimbay.dialogx.dialogs.MessageDialog;
import com.brimbay.dialogx.dialogs.PopTip;
import com.brimbay.dialogx.dialogs.WaitDialog;
import com.brimbay.dialogx.util.TextInfo;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import database.AppDatabase;
import fragments.login.LoginFragment;
import models.LocalMedia;
import models.User;
import utils.AppExecutors;
import utils.Constants;

public class RegisterFragment extends Fragment {
	private Context selfRef;

	private static final String TAG = RegisterFragment.class.getSimpleName();
	private static final String KEY_MEDIA = "com.dialog.accounts.key.Media";
	
	private static final int VALIDATE_FIRST_NAME = 11;
	private static final int VALIDATE_LAST_NAME = 12;
	private static final int VALIDATE_LOCATION = 14;
	
	private FragmentRegisterBinding binding;
	private WaitDialog waitDialog;
	private LocalMedia mSelectedMedia;
	private FirebaseAuth mAuth;
	private ActivityResultLauncher<String[]> pictureResultLauncher;
	
	public static RegisterFragment newInstance(){
		return new RegisterFragment();
	}
	
	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		selfRef = requireContext();
		if (savedInstanceState != null){
			if (savedInstanceState.containsKey(KEY_MEDIA))mSelectedMedia = savedInstanceState.getParcelable(KEY_MEDIA);
		}
		mAuth  = FirebaseAuth.getInstance();
		
		if (mAuth.getUid() == null || mAuth.getCurrentUser() == null){
			startActivity(new Intent(selfRef, StartActivity.class));
			requireActivity().finishAffinity();
		}
		pictureResultLauncher = createPictureResultLauncher();
	}
	
	public View onCreateView(@NonNull LayoutInflater inflater,
							 ViewGroup container, Bundle savedInstanceState) {
		binding = FragmentRegisterBinding.inflate(inflater, container, false);
		initUI();
		return binding.getRoot();
	}
	
	//region Member Methods
	private void initUI(){
		waitDialog = WaitDialog.setMessage(getString(R.string.please_wait))
				.setOnBackPressedListener(() -> {
					PopTip.show(getString(R.string.would_you_like_to_cancel), getString(R.string.cancel)).setButton((baseDialog, v) -> {
						WaitDialog.dismiss();
						return false;
					});
					return false;
				});
		binding.addProfile.setOnClickListener(this::launchPicSelect);
		binding.addProfileLink.setOnClickListener(this::launchPicSelect);
		binding.avatar.setOnClickListener(this::launchPicSelect);
		binding.rgNextBtn.setOnClickListener(v-> {
			handleAllErrors(true);
			String first_name = binding.rgFirstName.getText().toString().trim();
			String last_name = binding.rgLastName.getText().toString().trim();
			
			if (!isValidName(first_name) || !isValidName(last_name) )return;
			
			User user = new User();
			user.id = Objects.requireNonNull(mAuth.getUid());
			user.last_name = last_name;
			user.first_name = first_name;
			user.phone = Objects.requireNonNull(mAuth.getCurrentUser()).getPhoneNumber();
			
			handleUser(user,mAuth.getUid());
		});
		handleAllErrors(false);
	}
	
	private void handleUser(User user, String id){
		waitDialog.show();
		FirebaseDatabase.getInstance().getReference(Constants.FB_USER_BD).child(id).addListenerForSingleValueEvent(new ValueEventListener() {
			@Override
			public void onDataChange(@NonNull DataSnapshot snapshot) {
				if (snapshot.exists()){
					User usr = snapshot.getValue(User.class);
					if (usr != null){
						saveUserData(usr);
						return;
					}
				}
				snapshot.getRef().setValue(user).addOnSuccessListener(unused -> saveUserData(user));
			}
			
			@Override
			public void onCancelled(@NonNull DatabaseError error) {
				waitDialog.doDismiss();
				mAuth.signOut();
				startActivity(new Intent(selfRef, StartActivity.class));
				requireActivity().finishAffinity();
			}
		});
	}
	
	private void saveUserData(User user){
		Toast.makeText(selfRef, "Successfully logged in", Toast.LENGTH_SHORT).show();
		waitDialog.doDismiss();
		AppExecutors.getInstance().diskIO().execute(() -> {
			AppDatabase.newInstance(selfRef).userDao().insert(user);
			startActivity(new Intent(selfRef, StartActivity.class));
			requireActivity().finishAffinity();
		});
	}
	
	private void launchPicSelect(View v){
		pictureResultLauncher.launch(new String[]{Manifest.permission.CAMERA,Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE});
	}
	
	private void  handleAllErrors(boolean click){
		if (click) {
			showAllErrors(binding.rgFirstNameBox, binding.rgFirstName, VALIDATE_FIRST_NAME);
			showAllErrors(binding.rgLastNameBox, binding.rgLastName, VALIDATE_LAST_NAME);
		} else {
			showOrRemoveError(binding.rgFirstNameBox, binding.rgFirstName, VALIDATE_FIRST_NAME);
			showOrRemoveError(binding.rgLastNameBox, binding.rgLastName, VALIDATE_LAST_NAME);
		}
	}
	
	private void showOrRemoveError(TextInputLayout layout, TextInputEditText edit, int validationType){
		layout.setErrorTextColor(ColorStateList.valueOf(getResources().getColor(R.color.colorAccent, null)));
		layout.setEndIconVisible(false);
		
		edit.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				showAllErrors(layout, edit, validationType);
			}
			
			@Override
			public void afterTextChanged(Editable s) {
			
			}
		});
	}
	
	private boolean showAllErrors(TextInputLayout layout, TextInputEditText edit, int validationType){
		boolean valid = false;
		String text = edit.getText().toString().trim().toLowerCase(Locale.ROOT);
		if (TextUtils.isEmpty(text)){
			layout.setError(getString(R.string.this_field_is_required));
		}
		
		switch (validationType){
			case VALIDATE_FIRST_NAME:
				if (!isValidName(text)){
					layout.setError(getString(R.string.not_valid_first_name));
					layout.setErrorEnabled(true);
				}else {
					layout.setErrorEnabled(false);
					valid = true;
				}
				break;
			case VALIDATE_LAST_NAME:
				if (!isValidName(text)){
					layout.setError(getString(R.string.not_valid_last_name));
					layout.setErrorEnabled(true);
				}else {
					layout.setErrorEnabled(false);
					valid = true;
				}
				break;
		}
		return valid;
	}
	
	public static boolean isValidName(String name){
		return !TextUtils.isEmpty(name) && !TextUtils.isDigitsOnly(name) && TextUtils.getTrimmedLength(name) >= 3;
	}
	
	private void setAvatar(String path){
		Glide.with(selfRef)
				.load(path)
				.centerCrop()
				.placeholder(R.drawable.user)
				.diskCacheStrategy(DiskCacheStrategy.ALL)
				.into(binding.avatar);
	}
	
	public void selectPicture(){
		UwMediaPicker.Companion
				.with(this)
				.setGalleryMode(UwMediaPicker.GalleryMode.ImageGallery)
				.setGridColumnCount(4)
				.setMaxSelectableMediaCount(1)
				.setLightStatusBar(true)
				.enableImageCompression(false)
				.setCompressionMaxWidth(1280F)
				.setCompressionMaxHeight(720F)
				.setCompressFormat(Bitmap.CompressFormat.PNG)
				.setCompressionQuality(85)
				.setCancelCallback(() -> null)
				.launch(uwMediaPickerMediaModels -> {
					if (uwMediaPickerMediaModels.size() > 0 && uwMediaPickerMediaModels.get(0) != null){
						if (mSelectedMedia == null){
							mSelectedMedia = new LocalMedia();
						}
						mSelectedMedia.path = uwMediaPickerMediaModels.get(0).getMediaPath();
						mSelectedMedia.type = uwMediaPickerMediaModels.get(0).getMediaType().name();
						setAvatar(uwMediaPickerMediaModels.get(0).getMediaPath());
					}
					return null;
				});
	}
	
	private ActivityResultLauncher<String[]> createPictureResultLauncher() {
		return registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
			boolean granted = true;
			for (Map.Entry<String, Boolean> x : result.entrySet()) {
				if (!x.getValue()) granted = false;
			}
			if (granted) {
				selectPicture();
			}else {
				MessageDialog messageDialog = new MessageDialog(
						getString(R.string.allow_permissions),
						getString(R.string.permissions_request_info),
						getString(R.string.confirm)
				);
				messageDialog.setOkTextInfo(new TextInfo().setBold(true).setFontColor(getResources().getColor(R.color.colorAccent, null)));
				messageDialog.setOkButtonClickListener((baseDialog, v) -> {
					baseDialog.dismiss();
					pictureResultLauncher.launch(new String[]{Manifest.permission.CAMERA,Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE});
					return false;
				});
				messageDialog.show();
				
			}
		});
	}
	//endregion
	
	@Override
	public void onSaveInstanceState(@NonNull Bundle outState) {
		if (mSelectedMedia != null)outState.putParcelable(KEY_MEDIA,mSelectedMedia);
		super.onSaveInstanceState(outState);
	}
	
	@Override
	public void onDestroyView() {
		super.onDestroyView();
		binding = null;
	}
}
