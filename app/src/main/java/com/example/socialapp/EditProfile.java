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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Spinner;
import com.example.socialapp.tools.Icons;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class EditProfile extends AppCompatActivity {
    private Bundle bundle;
    private User user;
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
            assert bundle != null;
            user = bundle.getParcelable("user");
            assert user != null;
            choice = user.getImage();

            TextView cancel = findViewById(R.id.textViewCancel);
            TextView done = findViewById(R.id.textViewDone);

            editText = findViewById(R.id.editTextChangeUsername);
            editText.setText(user.getUsername());

            flowLayout = findViewById(R.id.flowLayout);
            flowConstraintLayout = findViewById(R.id.flowConstraintLayout);

            Spinner spinner = findViewById(R.id.spinner_k);
            List<String> spinnerItems = new ArrayList<String>();
            spinnerItems.add("5");
            spinnerItems.add("10");
            spinnerItems.add("25");
            spinnerItems.add("100");

            ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, spinnerItems);
            dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinner.setAdapter(dataAdapter);

            int position = spinnerItems.indexOf(String.valueOf(MainActivity.k));
            spinner.setSelection(position);

            spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                    MainActivity.k = Integer.parseInt(spinnerItems.get(position));
                }

                @Override
                public void onNothingSelected(AdapterView<?> parentView) {
                    // Do nothing here
                }
            });

            editText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View view, boolean hasFocus) {
                    if (!hasFocus) {
                        hideKeyboard(view);
                    }
                }
            });

            for (int i = 0; i< Icons.getIcons().size(); i++){
                addView(i);
            }

            cancel.setOnClickListener(v -> goToFeed());

            done.setOnClickListener(v -> {
                String tempUsername = user.getUsername();
                int tempImage = user.getImage();

                if (!Objects.equals(tempImage, choice)) { // If image changed
                    Thread thread = new Thread(() -> {
                        if (HttpHelper.setImage(user.getPhone(), choice)) { // If set image executed
                            System.out.println("Image changed successfully!");
                            user.setImage(choice);
                            SharedPreferences.Editor prefEditor = PreferenceManager.getDefaultSharedPreferences(EditProfile.this).edit();
                            prefEditor.putInt("image", user.getImage());
                            prefEditor.apply();
                        }
                    });

                    try {
                        thread.start();
                        thread.join();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }

                if (!tempUsername.equals(editText.getText().toString())) { //If username changed
                    if (editText.getText().toString().isEmpty()){
                        Toast.makeText(EditProfile.this, "Username is empty!", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    Thread thread = new Thread(() -> {
                        if (HttpHelper.setUsername(user.getPhone(), editText.getText().toString())) { //If set image executed
                            System.out.println("Username changed successfully!");
                            user.setUsername(editText.getText().toString());
                            SharedPreferences.Editor prefEditor = PreferenceManager.getDefaultSharedPreferences(EditProfile.this).edit();
                            prefEditor.putString("username", user.getUsername());
                            prefEditor.apply();
                        }
                    });

                    try {
                        thread.start();
                        thread.join();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }

                goToFeed();
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
        bundle.putParcelable("user", user);
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

            if (imageViewChoice.getId() - views == user.getImage()){
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

            imageViewChoice.setPadding(0 ,20,20,0);
            flowConstraintLayout.addView(imageViewChoice);
            flowLayout.addView(imageViewChoice);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void onBackPressed() {}
}