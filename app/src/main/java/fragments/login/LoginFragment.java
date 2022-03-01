package fragments.login;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.brimbay.chat.MainActivity;
import com.brimbay.chat.R;
import com.brimbay.chat.StartActivity;
import com.brimbay.chat.databinding.FragmentLoginBinding;
import com.brimbay.dialogx.dialogs.PopTip;
import com.brimbay.dialogx.dialogs.WaitDialog;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.hbb20.CountryCodePicker;

import java.util.concurrent.TimeUnit;

public class LoginFragment extends Fragment {
	private Context selfRef;
	private static final String TAG = LoginFragment.class.getSimpleName();
	
	private static final String KEY_VERIFY_IN_PROGRESS = "com.dialog.key.VerifyProgress";
	
	private static final int STATE_INITIALIZED = 1;
	private static final int STATE_CODE_SENT = 2;
	private static final int STATE_VERIFY_FAILED = 3;
	private static final int STATE_VERIFY_SUCCESS = 4;
	private static final int STATE_SIGNIN_FAILED = 5;
	private static final int STATE_SIGNIN_SUCCESS = 6;
	
	private FirebaseAuth mAuth;
	
	private boolean mVerificationInProgress = false;
	private String mVerificationId;
	private PhoneAuthProvider.ForceResendingToken mResendToken;
	private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;
	private OnLoginInteraction mOnLoginInteraction;
	
	private FragmentLoginBinding binding;
	private WaitDialog waitDialog;
	
	public static LoginFragment newInstance(){
		return new LoginFragment();
	}
	
	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		selfRef = requireContext();
		// Initialize Firebase Auth
		mAuth = FirebaseAuth.getInstance();
	}
	
	public View onCreateView(@NonNull LayoutInflater inflater,
							 ViewGroup container, Bundle savedInstanceState) {
		binding = FragmentLoginBinding.inflate(inflater, container, false);
		
		waitDialog = WaitDialog.setMessage(getString(R.string.please_wait));
		
		return binding.getRoot();
	}
	
	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		if (savedInstanceState != null ) {
			onViewStateRestored (savedInstanceState);
		}
		
		binding.lgNextBtn.setOnClickListener(view1 -> {
			
			if (binding.lgPhoneBox.getVisibility() == View.VISIBLE) {
				//Phone Number
				if (!validatePhoneNumber())return;
				waitDialog.show();
				startPhoneNumberVerification( getPhoneNumber());
			}else{
				//Verification code
				String code = binding.lgCodeEdit.getPin();
				if ( TextUtils.isEmpty(code)) {
					binding.lgCodeErrTxt.setText( getString(R.string.please_enter_the_code_sent));
					return ;
				}
				waitDialog.show();
				verifyPhoneNumberWithCode(mVerificationId, code);
			}
		});
		binding.lgResend.setOnClickListener(v -> {
			if ( ! validatePhoneNumber())return;
			waitDialog.show();
			resendVerificationCode( getPhoneNumber(), mResendToken);
		});
		
		
		// Initialize phone auth callbacks
		mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks () {
			@Override
			public void onVerificationCompleted (@NonNull PhoneAuthCredential credential ) {
				// This callback will be invoked in two situations:
				// 1 - Instant verification. In some cases the phone number can be instantly
				//   verified without needing to send or enter a verification code.
				// 2 - Auto-retrieval. On some devices Google Play services can automatically
				//   detect the incoming verification SMS and perform verification without
				//   user action.
				Log. d( TAG , "onVerificationCompleted: " + credential);
				waitDialog.doDismiss();
				mVerificationInProgress = false ;
				
				// Update the UI and attempt sign in with the phone credential
//				updateUI( STATE_VERIFY_SUCCESS , credential);
				signInWithPhoneAuthCredential(credential);
			}
			
			@Override
			public void onVerificationFailed (@NonNull FirebaseException e ) {
				// This callback is invoked in an invalid request for verification is made,
				// for instance if the the phone number format is not valid.
				Log.w( TAG , "onVerificationFailed ", e);
				waitDialog.doDismiss();
				mVerificationInProgress = false ;
				
				if (e instanceof FirebaseAuthInvalidCredentialsException) {
					// Invalid request
					binding.lgPhoneErrTxt.setText( getString(R.string.please_enter_a_valid_phone_number));
				} else if (e instanceof FirebaseTooManyRequestsException) {
					// The SMS quota for the project has been exceeded
					Toast.makeText(selfRef, getString(R.string.sms_quota_exceeded), Toast.LENGTH_LONG).show();
					binding.lgPhoneErrTxt.setText( getString(R.string.sms_quota_exceeded));
				}
				
				// Show a message and update the UI
//				updateUI( STATE_VERIFY_FAILED );
			}
			
			@Override
			public void onCodeSent ( @NonNull String verificationId , @NonNull PhoneAuthProvider.ForceResendingToken token) {
				// The SMS verification code has been sent to the provided phone number, we
				// now need to ask the user to enter the code and then construct a credential
				// by combining the code with a verification ID.
				Log.d(TAG, "onCodeSent:"+ verificationId);
				
				// Save verification ID and resending token so we can use them later
				mVerificationId = verificationId;
				mResendToken = token;
				
				// Update UI
				binding.lgPhoneBox.setVisibility(View.GONE);
				binding.lgCodeBox.setVisibility(View.VISIBLE);
				binding.lgCodeSubTitle.setText(getString(R.string.enter_code_sent_to_phone_number,getPhoneNumber()));
				if (mOnLoginInteraction != null)mOnLoginInteraction.onLoginPageChanged(STATE_CODE_SENT);
				waitDialog.doDismiss();
//				updateUI(STATE_CODE_SENT);
			}
		};
	}
	
	//region Member Methods
	private void startPhoneNumberVerification(String phoneNumber) {
		PhoneAuthOptions options = PhoneAuthOptions.newBuilder(mAuth)
						.setPhoneNumber(phoneNumber)
						.setTimeout(60L, TimeUnit.SECONDS)
						.setActivity(requireActivity())
						.setCallbacks(mCallbacks)
						.build();
		PhoneAuthProvider.verifyPhoneNumber(options);
		mVerificationInProgress = true;
	}
	
	private void verifyPhoneNumberWithCode(String verificationId, String code) {
		PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);
		signInWithPhoneAuthCredential(credential);
	}
	
	private void resendVerificationCode(String phoneNumber, PhoneAuthProvider.ForceResendingToken token) {
		PhoneAuthOptions options =
				PhoneAuthOptions.newBuilder(mAuth)
						.setPhoneNumber(phoneNumber)
						.setTimeout(60L, TimeUnit.SECONDS)
						.setActivity(requireActivity())
						.setCallbacks(mCallbacks)
						.setForceResendingToken(token)
						.build();
		PhoneAuthProvider.verifyPhoneNumber(options);
	}
	
	private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
		mAuth.signInWithCredential(credential).addOnCompleteListener(requireActivity(), task -> {
			if (task.isSuccessful()) {
				// Sign in success, update UI with the signed-in user's information
				Log.d( TAG , "signInWithCredential:success ");
				
				FirebaseUser user = task.getResult().getUser();
//				updateUI( STATE_SIGNIN_SUCCESS , user);
				Toast.makeText(selfRef, "Successfully logged in", Toast.LENGTH_SHORT).show();
				startActivity(new Intent(selfRef, StartActivity.class));
				requireActivity().finishAffinity();
			} else {
				// Sign in failed, display a message and update the UI
				Log.w( TAG , "signInWithCredential:failure ", task.getException());
				if (task.getException() instanceof FirebaseAuthInvalidCredentialsException ) {
					// The verification code entered was invalid
					binding.lgCodeErrTxt.setText(getString(R.string.invalid_code_entered));
				}
				// Update UI
				binding.lgPhoneBox.setVisibility(View.GONE);
				binding.lgCodeBox.setVisibility(View.VISIBLE);
				binding.lgCodeSubTitle.setText(getString(R.string.enter_code_sent_to_phone_number,getPhoneNumber()));
//				updateUI( STATE_SIGNIN_FAILED );
			}
		});
	}
	
	private void signOut () {
		mAuth.signOut();
		xupdateUI( STATE_INITIALIZED );
	}
	
	private void xupdateUI ( int uiState ) {
		xupdateUI(uiState, mAuth.getCurrentUser(), null );
	}
	
	private void xupdateUI ( FirebaseUser user ) {
		if (user != null ) {
			xupdateUI( STATE_SIGNIN_SUCCESS , user);
		} else {
			xupdateUI( STATE_INITIALIZED );
		}
	}
	
	private void xupdateUI ( int uiState , FirebaseUser user ) {
		xupdateUI(uiState, user, null );
	}
	
	private void xupdateUI ( int uiState , PhoneAuthCredential cred ) {
		xupdateUI(uiState, null , cred);
	}
	
	private void xupdateUI ( int uiState , FirebaseUser user , PhoneAuthCredential cred ) {
		switch (uiState) {
			case STATE_INITIALIZED :
				// Initialized state, show only the phone number field and start button
				binding.lgPhoneBox.setVisibility(View.VISIBLE);
				binding.lgCodeBox.setVisibility(View.GONE);
				break ;
			case STATE_VERIFY_FAILED:
			case STATE_CODE_SENT :
				// Code sent state, show the verification field, the
				binding.lgPhoneBox.setVisibility(View.GONE);
				binding.lgCodeBox.setVisibility(View.VISIBLE);
				binding.lgCodeSubTitle.setText(getString(R.string.enter_code_sent_to_phone_number,getPhoneNumber()));
				if (mOnLoginInteraction != null)mOnLoginInteraction.onLoginPageChanged(uiState);
				break;
			case STATE_VERIFY_SUCCESS:
				// Verification has succeeded, proceed to firebase sign in
				binding.lgPhoneBox.setVisibility(View.GONE);
				binding.lgCodeBox.setVisibility(View.VISIBLE);
				// Set the verification text based on the credential
				if (cred != null ) {
					if (cred.getSmsCode() != null ) {
						binding.lgCodeEdit.setPin(cred.getSmsCode());
					} else {
						binding.lgCodeEdit.setPin("000000");
					}
				}
				break ;
			case STATE_SIGNIN_FAILED :
				// No-op, handled by sign-in check
				Snackbar.make(binding.lgCodeBox,getString(R.string.something_went_wrong),Snackbar.LENGTH_INDEFINITE)
						.setAction(getString(R.string.try_again), view -> {
							//TODO:Go back
						}).show();
				break ;
			case STATE_SIGNIN_SUCCESS :
				// Np-op, handled by sign-in check
				break ;
		}
		
		if (user == null ) {
			// Signed out
			binding.lgPhoneBox.setVisibility(View.VISIBLE);
			binding.lgCodeBox.setVisibility(View.GONE);
		} else {
			// Signed in
			
		}
		
	}
	
	private boolean validatePhoneNumber () {
		String phoneNumber = binding.lgPhoneEdit.getText().toString();
		if (TextUtils.isEmpty(phoneNumber)) {
			binding.lgPhoneErrTxt.setText( getString(R.string.please_enter_a_valid_phone_number));
			return false ;
		}
		
		return true ;
	}
	
	private String getPhoneNumber(){
		CountryCodePicker _country_code = binding.lgCountryCode;
		_country_code.setNumberAutoFormattingEnabled( true );
		_country_code.registerCarrierNumberEditText(binding.lgPhoneEdit);
		return _country_code.getFullNumberWithPlus();
	}
	
	private void enableViews ( View... views ) {
		for ( View v : views) {
			v.setEnabled( true );
		}
	}
	
	private void disableViews ( View... views ) {
		for ( View v : views) {
			v.setEnabled( false );
		}
	}
	
	//endregion
	
	@Override
	public void onStart() {
		super.onStart();
		// Check if user is signed in (non-null) and update UI accordingly.
		FirebaseUser currentUser = mAuth.getCurrentUser();
//		updateUI(currentUser);
		binding.lgPhoneBox.setVisibility(View.VISIBLE);
		binding.lgCodeBox.setVisibility(View.GONE);
		if (mVerificationInProgress && validatePhoneNumber()) {
			startPhoneNumberVerification( getPhoneNumber());
		}
	}
	
	@Override
	public void onSaveInstanceState(@NonNull Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putBoolean(KEY_VERIFY_IN_PROGRESS, mVerificationInProgress);
	}
	
	@Override
	public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
		super.onViewStateRestored(savedInstanceState);
		if (savedInstanceState != null) {
			mVerificationInProgress = savedInstanceState.getBoolean(KEY_VERIFY_IN_PROGRESS);
		}
	}
	
	@Override
	public void onDestroyView() {
		super.onDestroyView();
		binding = null;
	}
	
	public interface OnLoginInteraction{
		void onLoginPageChanged(int page);
	}
	
	public OnLoginInteraction getOnLoginInteraction() {
		return mOnLoginInteraction;
	}
	
	public void setOnLoginInteraction(OnLoginInteraction onLoginInteraction) {
		this.mOnLoginInteraction = onLoginInteraction;
	}
}
