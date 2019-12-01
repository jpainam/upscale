package com.copell.upscale;


import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import dmax.dialog.SpotsDialog;

import com.chaos.view.PinView;

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
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.hbb20.CountryCodePicker;
import com.shuhart.stepview.StepView;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;





public class AuthenticationActivity extends AppCompatActivity {

    private int currentStep = 0;
    LinearLayout layout1, layout2;
    StepView stepView;


    private static final String TAG = "AuthenticationActivity";


    private Button sendCodeButton;
    private Button verifyCodeButton;
    ConstraintLayout rootView;
    private EditText phoneNum;
    private PinView verifyCodeET;
    private TextView tvReceivedNot;
    private TextView phonenumberText;
    CountryCodePicker ccp;
    private String mVerificationId;
    private PhoneAuthProvider.ForceResendingToken mResendToken;

    AlertDialog dialog = null;


    private FirebaseAuth mAuth;
    private boolean verificationInProgress = false;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(getSupportActionBar() != null){
            getSupportActionBar().hide();
        }
        setContentView(R.layout.activity_authentication);

        mAuth = FirebaseAuth.getInstance();

        layout1 = findViewById(R.id.layout1);
        layout2 =  findViewById(R.id.layout2);
        sendCodeButton = findViewById(R.id.submit1);
        verifyCodeButton =  findViewById(R.id.submit2);
        phoneNum =  findViewById(R.id.phonenumber);
        verifyCodeET = findViewById(R.id.pinView);
        phonenumberText = findViewById(R.id.phonenumberText);
        rootView = findViewById(R.id.rootView);
        ccp = findViewById(R.id.ccp);
        ccp.registerCarrierNumberEditText(phoneNum);
        stepView = findViewById(R.id.step_view);
        tvReceivedNot = findViewById(R.id.tvReceivedNot);
        stepView.setStepsNumber(3);
        stepView.go(0, true);
        layout1.setVisibility(View.VISIBLE);

        if (dialog == null) {
            dialog = new SpotsDialog.Builder()
                    .setContext(AuthenticationActivity.this).setCancelable(true)
                    .setMessage(getString(R.string.connecting))
                    .build();
        }

        sendCodeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //String phoneNumber = phoneNum.getText().toString();
                String phoneNumber = ccp.getFullNumberWithPlus();
                phonenumberText.setText(phoneNumber);
                if(!Utils.isConnected(AuthenticationActivity.this)){
                    Snackbar.make(layout1, "You are not connected. " +
                            "Check your Internet connection", Snackbar.LENGTH_LONG).show();
                    return;
                }
                if (TextUtils.isEmpty(phoneNumber)) {
                    phoneNum.setError("Enter a Phone Number");
                    phoneNum.requestFocus();
                } else if (phoneNumber.length() < 5) {
                    phoneNum.setError("Please enter a valid phone");
                    phoneNum.requestFocus();
                } else {
                    if (currentStep < stepView.getStepCount() - 1) {
                        currentStep++;
                        stepView.go(currentStep, true);
                    } else {
                        stepView.done(true);
                    }
                    layout1.setVisibility(View.GONE);
                    layout2.setVisibility(View.VISIBLE);
                    PhoneAuthProvider.getInstance().verifyPhoneNumber(
                            phoneNumber,        // Phone number to verify
                            60,                 // Timeout duration
                            TimeUnit.SECONDS,   // Unit of timeout
                            AuthenticationActivity.this,               // Activity (for callback binding)
                            mCallbacks);        // OnVerificationStateChangedCallbacks
                }
            }
        });
        verifyCodeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String verificationCode = verifyCodeET.getText().toString();
                if (verificationCode.isEmpty()) {
                    Toast.makeText(AuthenticationActivity.this, "Enter verification code", Toast.LENGTH_SHORT).show();
                }else if(!Utils.isConnected(AuthenticationActivity.this)){
                    Snackbar.make(layout1, "You are not connected. " +
                            "Check your Internet connection", Snackbar.LENGTH_LONG).show();
                    return;
                } else {
                    dialog.show();
                    verifyVerificationCode(verificationCode);
                }
            }
        });

        tvReceivedNot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!Utils.isConnected(AuthenticationActivity.this)) {
                    Snackbar.make(layout1, "You are not connected. " +
                            "Check your Internet connection", Snackbar.LENGTH_LONG).show();
                    return;
                }
                Toast.makeText(AuthenticationActivity.this, "Retrying... Wait at least 2min", Toast.LENGTH_LONG).show();
                PhoneAuthProvider.getInstance().verifyPhoneNumber(
                        phonenumberText.getText().toString(),        // Phone number to verify
                        60,                 // Timeout duration
                        TimeUnit.SECONDS,   // Unit of timeout
                        AuthenticationActivity.this,               // Activity (for callback binding)
                        mCallbacks);
            }
        });

    }

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        @Override
        public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
            dialog.show();
            Log.d(TAG, "onVerificationCompleted: " + phoneAuthCredential);
            String code = phoneAuthCredential.getSmsCode();
            // Sometimes the code is not detected automatically
            if(code != null){
                verifyCodeET.setText(code);
                verifyVerificationCode(code);
            }else{
                Log.e(TAG, "Should not enter here - Let's try");
            }
            verificationInProgress = false;
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
            verificationInProgress = false;
        }

        @Override
        public void onCodeSent(String verificationId,
                               PhoneAuthProvider.ForceResendingToken token) {
            // Save verification ID and resending token so we can use them later
            mVerificationId = verificationId;
            mResendToken = token;
        }
    };
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        verificationInProgress = savedInstanceState.getBoolean("verificationInProgress");
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("verificationInProgress", verificationInProgress);
    }
    @Override
    protected void onStart() {
        super.onStart();
        if(verificationInProgress){
            PhoneAuthProvider.getInstance().verifyPhoneNumber(
                    phonenumberText.getText().toString(),        // Phone number to verify
                    60,                 // Timeout duration
                    TimeUnit.SECONDS,   // Unit of timeout
                    this,               // Activity (for callback binding)
                    mCallbacks);        // OnVerificationStateChangedCallbacks
        }
    }

    @Override
    public void onBackPressed() {
        if (currentStep > 0) {
            currentStep--;
            stepView.go(currentStep, true);
            layout2.setVisibility(View.GONE);
            layout1.setVisibility(View.VISIBLE);
        }
    }

    private void verifyVerificationCode(String code){
        Log.e(TAG, "Verification Code started " + code);
        try {
            PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, code);
            signInWithPhoneAuthCredential(credential);
        }catch (Exception e){
            Snackbar.make(rootView, "Cannot create PhoneAuthCredential without either verificationProof", Snackbar.LENGTH_LONG).show();
            dialog.dismiss();
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
                    dialog.dismiss();
                    if (currentStep < stepView.getStepCount() - 1) {
                        currentStep++;
                        stepView.go(currentStep, true);
                    } else {
                        stepView.done(true);
                    }
                    layout1.setVisibility(View.GONE);
                    layout2.setVisibility(View.GONE);
                    Log.d(TAG, "signInWithCredential:success");
                    FirebaseUser user = task.getResult().getUser();
                    //registerUserToFirestore(user.getPhoneNumber());
                    Intent mainActivity = new Intent(getApplicationContext(), MainActivity.class);
                    startActivity(mainActivity);
                    finish();
                } else {
                    dialog.dismiss();

                    if(task.getException() instanceof FirebaseAuthInvalidCredentialsException){
                        verifyCodeET.setError("Invalid code");
                        Snackbar.make(rootView, "Invalid code entered", Snackbar.LENGTH_LONG).show();
                    }else{
                        Snackbar.make(rootView, "Something is wrong, we will fix it soon", Snackbar.LENGTH_LONG).show();
                    }
                    Log.w(TAG, "signInWithCredential:failure", task.getException());
                }
            }
        });
    }
    /*private void registerUserToFirestore(String phoneNumber){
        Log.i(TAG, "Register User to Firestore " + phoneNumber);
        SharedPreferences pref = getApplicationContext().getSharedPreferences(
                getString(R.string.shared_preference_file), Context.MODE_PRIVATE);
        String token = pref.getString(getString(R.string.firebase_token), null);
        Log.e(TAG, "Send token to server " + token);
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Map<String, Object> map = new HashMap<>();
        map.put("token", token);
        map.put("phoneNumber", phoneNumber);
        map.put("location", ccp.getSelectedCountryEnglishName());
        db.collection(Constante.USERS_COLLECTION).document(phoneNumber)
                .set(map, SetOptions.merge());
        Utils.sendRegistrationToPhpServer(getApplicationContext(), token, phoneNumber);

    }*/

}
