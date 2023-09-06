package com.example.socialapp;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

@SuppressWarnings("ALL")
public class SignUp extends AppCompatActivity {

    private EditText phone;
    private EditText username;
    private EditText password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        try {
            phone = findViewById(R.id.editTextSignUpPhone);
            username = findViewById(R.id.editTextSignUpUsername);
            password = findViewById(R.id.editTextSignUpPassword);

            phone.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View view, boolean hasFocus) {
                    if (!hasFocus) {
                        hideKeyboard(view);
                    }
                }
            });

            username.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View view, boolean hasFocus) {
                    if (!hasFocus) {
                        hideKeyboard(view);
                    }
                }
            });

            password.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View view, boolean hasFocus) {
                    if (!hasFocus) {
                        hideKeyboard(view);
                    }
                }
            });

            TextView signIn = findViewById(R.id.textViewSignIn);
            signIn.setPaintFlags(signIn.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
            signIn.setOnClickListener(view -> goToSignIn());

            Button signUp = findViewById(R.id.buttonSignUp);
            signUp.setOnClickListener(view -> {
                phone.clearFocus();
                username.clearFocus();
                password.clearFocus();
                AsyncTaskSignUp SingUp = new AsyncTaskSignUp();
                SingUp.execute();
            });
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void hideKeyboard(View view) {
        InputMethodManager inputMethodManager =(InputMethodManager)getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    private void goToSignIn()
    {
        Intent intent = new Intent(this, SignIn.class);
        startActivity(intent);
        overridePendingTransition(R.anim.fadein, R.anim.fadeout);
        finish();
    }

    private void goToFeed()
    {
        String username = ServerSQL.getUsername(phone.getText().toString());
        String joinDate = ServerSQL.getJoinDate(phone.getText().toString());
        SharedPreferences.Editor prefEditor = PreferenceManager.getDefaultSharedPreferences(SignUp.this).edit();
        prefEditor.putString("phone",phone.getText().toString());
        prefEditor.putString("username",username);
        prefEditor.putString("joinDate",joinDate);
        prefEditor.putInt("image",1);
        prefEditor.apply();

        Intent intent = new Intent(this, Feed.class);
        Bundle bundle = new Bundle();
        bundle.putString("phone",phone.getText().toString());
        bundle.putString("username",username);
        bundle.putString("joinDate",joinDate);
        bundle.putInt("image",1);
        intent.putExtras(bundle);
        startActivity(intent);
        overridePendingTransition(R.anim.fadein, R.anim.fadeout);
        finish();
    }

    @SuppressWarnings("deprecation")
    @SuppressLint("StaticFieldLeak")
    private class AsyncTaskSignUp extends AsyncTask<String,String,String> {
        @Override
        protected String doInBackground(String... strings) {
            try {
                if (ServerSQL.signUp(phone.getText().toString(),username.getText().toString(),password.getText().toString())){
                    goToFeed();
                }else{
                    runOnUiThread(() -> Toast.makeText(SignUp.this,"Phone already exists or a field has less than 4 characters!",Toast.LENGTH_SHORT).show());
                }
            }catch (Exception e){
                e.printStackTrace();
            }
            return null;
        }
    }

    @Override
    public void onBackPressed() {}
}