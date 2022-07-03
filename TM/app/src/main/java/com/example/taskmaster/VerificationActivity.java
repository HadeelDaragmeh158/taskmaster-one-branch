package com.example.taskmaster;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.amplifyframework.core.Amplify;

public class VerificationActivity extends AppCompatActivity {

    private static final String TAG = "VerificationActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verification);

        Intent intent = getIntent();
        String username = intent.getStringExtra(SignUpActivity.USERNAME);
        String email = intent.getStringExtra(SignUpActivity.EMAIL);

        EditText  verifyText = findViewById(R.id.verify_code);
        Button verifyBtn = findViewById(R.id.verify_btn);
        verifyBtn.setOnClickListener(view->{
            String verifyCode = verifyText.getText().toString();
            Log.i(TAG, "onCreate: "+username+" "+email+" "+verifyCode);

            verifyByUsername(username, verifyCode);
//            verifyByEmail(email, verifyCode);
            Intent intentToLogin = new Intent(VerificationActivity.this, LoginActivity.class);
            startActivity(intentToLogin);
            finish();

        });

    }
    private void verifyByUsername(String username , String code ){
        Amplify.Auth.confirmSignUp(
                username ,
                code,
                result -> {
                    Log.i(TAG, result.isSignUpComplete() ? "Confirm signUp succeeded" : "Confirm sign up not complete");
                    startActivity(new Intent(VerificationActivity.this , LoginActivity.class));
                    finish();
                },
                fail -> {
                    Log.i(TAG, "verify: failed to be verified");

                }
        );

    }

    private void verifyByEmail(String email , String code ){
        Amplify.Auth.confirmSignUp(
                email ,
                code,
                result -> {
                    Log.i(TAG, result.isSignUpComplete() ? "Confirm signUp succeeded" : "Confirm sign up not complete");
                    startActivity(new Intent(VerificationActivity.this , LoginActivity.class));
                    finish();
                },
                fail -> {
                    Log.i(TAG, "verify: failed to be verified");

                }
        );

    }
}