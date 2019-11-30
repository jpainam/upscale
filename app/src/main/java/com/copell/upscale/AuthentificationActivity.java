package com.copell.upscale;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import butterknife.BindView;
import butterknife.ButterKnife;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.copell.upscale.utils.Utils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class AuthentificationActivity extends AppCompatActivity {
    public static final String TAG = AuthentificationActivity.class.getSimpleName();

    @BindView(R.id.editTextPhone)
    EditText txtPhone;

    @BindView(R.id.buttonGetVerificationCode)
    Button btnVerificationCode;

    @BindView(R.id.editTextCode) EditText txtCode;

    @BindView(R.id.buttonSignIn) Button btnSignIn;
    private FirebaseAuth mAuth;

    @BindView(R.id.rootView) RelativeLayout rootView;

    private PhoneAuthProvider.ForceResendingToken mResendToken;


    private String mVerificationId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_authentification);
        ButterKnife.bind(this);
        mAuth = FirebaseAuth.getInstance();

        btnVerificationCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String phoneNumber = txtPhone.getText().toString();

                if (phoneNumber.length() == 0) {
                    txtPhone.setError("Enter a Phone Number");
                    txtPhone.requestFocus();
                } else if (phoneNumber.length() < 5) {
                    txtPhone.setError("Please enter a valid phone");
                    txtPhone.requestFocus();
                } else {

                    PhoneAuthProvider.getInstance().verifyPhoneNumber(
                            phoneNumber,        // Phone number to verify
                            60,                 // Timeout duration
                            TimeUnit.SECONDS,   // Unit of timeout
                            AuthentificationActivity.this,               // Activity (for callback binding)
                            mCallbacks);        // OnVerificationStateChangedCallbacks
                }

            }
        });

        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String verificationCode = txtCode.getText().toString();
                if (verificationCode.isEmpty()) {
                    Toast.makeText(AuthentificationActivity.this, "Enter verification code", Toast.LENGTH_SHORT).show();
                }else if(!Utils.isConnected(AuthentificationActivity.this)){
                    Snackbar.make(rootView, "You are not connected. " +
                            "Check your Internet connection", Snackbar.LENGTH_LONG).show();
                    return;
                } else {
                    //dialog.show();
                    verifyVerificationCode(verificationCode);
                }
            }
        });


    }

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks = new
            PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        @Override
        public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
            // dialog.show();
            Log.d(TAG, "onVerificationCompleted: " + phoneAuthCredential);
            String code = phoneAuthCredential.getSmsCode();
            // Sometimes the code is not detected automatically
            if(code != null){
                txtCode.setText(code);
                verifyVerificationCode(code);
            }else{
                Log.e(TAG, "Should not enter here - Let's try");
            }
            //verificationInProgress = false;
        }

        @Override
        public void onVerificationFailed(FirebaseException e) {
            if (e instanceof FirebaseAuthInvalidCredentialsException) {
                Log.e(TAG, "Invalid request", e);
                Snackbar.make(rootView, "Invalid request", Snackbar.LENGTH_LONG).show();
            } else if (e instanceof FirebaseTooManyRequestsException) {
                Snackbar.make(rootView, "The SMS quota for the project has been exceeded", Snackbar.LENGTH_LONG).show();
                Log.e(TAG, "The SMS quota for the project has been exceeded", e);
            }else if(e instanceof FirebaseNetworkException){
                Snackbar.make(rootView, "A Network eror has occured", Snackbar.LENGTH_LONG).show();
                Log.e(TAG, "A Network error", e);
            }else if(e instanceof FirebaseAuthException){
                Snackbar.make(rootView, "This app is not authorized to use Firebase Authentication", Snackbar.LENGTH_LONG).show();
                Log.e(TAG, "This app is not authorized to use Firebase Authentication", e);

            }else{
                Snackbar.make(rootView, "Something is wrong, we will fix it soon", Snackbar.LENGTH_LONG).show();
                Log.e(TAG, "Something is wrong", e);
            }
            //verificationInProgress = false;
        }

        @Override
        public void onCodeSent(String verificationId,
                               PhoneAuthProvider.ForceResendingToken token) {
            // Save verification ID and resending token so we can use them later
            mVerificationId = verificationId;
            mResendToken = token;
        }
    };


    private void verifyVerificationCode(String code){
        Log.e(TAG, "Verification Code started " + code);
        try {
            PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, code);
            signInWithPhoneAuthCredential(credential);
        }catch (Exception e){
            Snackbar.make(rootView, "Cannot create PhoneAuthCredential without either verificationProof", Snackbar.LENGTH_LONG).show();
            //dialog.dismiss();
            Log.e(TAG, "Error", e);
        }
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "signInWithCredential:success");
                    //dialog.dismiss();

                    Log.d(TAG, "signInWithCredential:success");
                    FirebaseUser user = task.getResult().getUser();
                   //registerUserToFirestore(user.getPhoneNumber());
                    Intent mainActivity = new Intent(getApplicationContext(), MainActivity.class);
                    startActivity(mainActivity);
                    finish();
                } else {
                    //dialog.dismiss();

                    if(task.getException() instanceof FirebaseAuthInvalidCredentialsException){
                        txtCode.setError("Invalid code");
                        Snackbar.make(rootView, "Invalid code entered", Snackbar.LENGTH_LONG).show();
                    }else{
                        Snackbar.make(rootView, "Something is wrong, we will fix it soon", Snackbar.LENGTH_LONG).show();
                    }
                    Log.w(TAG, "signInWithCredential:failure", task.getException());
                }
            }
        });
    }
}
