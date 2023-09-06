package com.example.socialapp;

import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.helper.widget.Flow;
import androidx.constraintlayout.widget.ConstraintLayout;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Objects;

public class EditProfile extends AppCompatActivity {
    private Bundle bundle;
    private String username;
    private Integer image;

    private Flow flowLayout;
    private ConstraintLayout flowConstraintLayout;

    private View selectedView;
    private Integer choice;
    private Integer views;

    private EditText editText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        try {
            bundle = getIntent().getExtras();
            String phone = bundle.getString("phone");
            username = bundle.getString("username");
            image = bundle.getInt("image");
            choice = image;

            TextView cancel = findViewById(R.id.textViewCancel);
            TextView done = findViewById(R.id.textViewDone);

            editText = findViewById(R.id.editTextChangeUsername);
            editText.setText(username);

            flowLayout = findViewById(R.id.flowLayout);
            flowConstraintLayout = findViewById(R.id.flowConstraintLayout);

            editText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View view, boolean hasFocus) {
                    if (!hasFocus) {
                        hideKeyboard(view);
                    }
                }
            });

            for (int i=0;i<Icons.getIcons().size();i++){
                addView(i);
            }

            cancel.setOnClickListener(v -> goToFeed());

            done.setOnClickListener(v -> {
                String tempUsername = username;
                int tempImage = image;
                new Thread(() -> {
                    if (!Objects.equals(tempImage, choice)) { //If image changed
                        if (ServerSQL.setImage(phone, choice)) { //If set image executed
                            System.out.println("Image changed successfully!");
                        }
                    }
                }).start();

                if (!tempUsername.equals(editText.getText().toString())) { //If username changed
                    if (editText.getText().toString().isEmpty()){
                        System.out.println("Username is empty!");
                        Toast.makeText(EditProfile.this, "Username is empty!", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    boolean success = ServerSQL.setUsername(phone, editText.getText().toString());
                    if (success) { //If set username executed
                        System.out.println("Username changed successfully!");

                        username = editText.getText().toString();
                        image = choice;

                        SharedPreferences.Editor prefEditor = PreferenceManager.getDefaultSharedPreferences(EditProfile.this).edit();
                        prefEditor.putString("username", username);
                        prefEditor.putInt("image", image);
                        prefEditor.apply();

                        goToFeed();

                    }else{
                        System.out.println("Username already exists!");
                        Toast.makeText(EditProfile.this, "Username already exists!", Toast.LENGTH_SHORT).show();
                    }
                }else{
                    image = choice;
                    SharedPreferences.Editor prefEditor = PreferenceManager.getDefaultSharedPreferences(EditProfile.this).edit();
                    prefEditor.putInt("image", image);
                    prefEditor.apply();

                    goToFeed();
                }
            });
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void hideKeyboard(View view) {
        InputMethodManager inputMethodManager =(InputMethodManager)getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    private void goToFeed()
    {
        Intent intent = new Intent(this, Feed.class);
        bundle.putString("username",username);
        bundle.putInt("image",image);
        intent.putExtras(bundle);
        startActivity(intent);
        overridePendingTransition(R.anim.fadein, R.anim.fadeout);
        finish();
    }

    private void addView(int i){
        try {
            @SuppressLint("InflateParams")
            View imageViewChoice = getLayoutInflater().inflate(R.layout.image, null, false);
            imageViewChoice.setLayoutParams(new ConstraintLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT));
            imageViewChoice.setId(View.generateViewId());

            ImageView imageSelected = imageViewChoice.findViewById(R.id.imageViewImage);
            ImageView imageSelectedCheck = imageViewChoice.findViewById(R.id.imageViewCheck);

            if (flowConstraintLayout.getChildCount()==1)
            {
                views = imageViewChoice.getId();
                views--;
            }

            imageSelected.setImageResource(Icons.getIcons().get(i));

            if (imageViewChoice.getId() - views == image){
                selectedView = imageViewChoice;
                imageSelectedCheck.bringToFront();
            }

            imageSelected.setOnClickListener(view -> {
                editText.clearFocus();
                imageSelectedCheck.bringToFront();

                if (selectedView!=view){
                    ImageView imageSelectedPrev = selectedView.findViewById(R.id.imageViewImage);
                    imageSelectedPrev.bringToFront();
                }

                choice = imageViewChoice.getId();
                choice -= views;
                selectedView = view;
            });

            flowConstraintLayout.addView(imageViewChoice);
            flowLayout.addView(imageViewChoice);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void onBackPressed() {}
}