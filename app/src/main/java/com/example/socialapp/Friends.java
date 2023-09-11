package com.example.socialapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

@SuppressWarnings("ALL")
public class Friends extends AppCompatActivity {

    Bundle bundle;
    String phone;
    String friendUsername = "";

    LinearLayout linearLayout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends);
        try {
            bundle = getIntent().getExtras();
            phone = bundle.getString("phone");

            EditText username = findViewById(R.id.editTextUsernameToAdd);

            username.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View view, boolean hasFocus) {
                    if (!hasFocus) {
                        hideKeyboard(view);
                    }
                }
            });

            ImageView backButton = findViewById(R.id.imageViewFriendsBack);
            backButton.setOnClickListener(v -> goToFeed());

            Button sendRequest = findViewById(R.id.buttonSendRequest);
            sendRequest.setOnClickListener(v -> {
                username.clearFocus();
                String temp = username.getText().toString();
                if (!temp.isEmpty()){
                    friendUsername = username.getText().toString();
                    AsyncTaskSendRequest taskSendRequest = new AsyncTaskSendRequest();
                    taskSendRequest.execute();
                    username.setText("");

                }else{
                    Toast.makeText(Friends.this, "Something went wrong.", Toast.LENGTH_SHORT).show();
                }
            });


            linearLayout = findViewById(R.id.linearLayoutRequests);
            linearLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    username.clearFocus();
                }
            });

            AsyncTaskCreateList createList = new AsyncTaskCreateList();
            createList.execute();

            SwipeRefreshLayout swipeRefreshLayout = findViewById(R.id.swipeToRefreshRequests);
            swipeRefreshLayout.setOnRefreshListener(() -> {
                AsyncTaskCreateList createList1 = new AsyncTaskCreateList();
                createList1.execute();
                swipeRefreshLayout.setRefreshing(false);
                System.out.println("List refreshed!");
            });

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @SuppressLint("SetTextI18n")
    private void addView(User friendUser){
        try {
            @SuppressLint("InflateParams")
            View requestView = getLayoutInflater().inflate(R.layout.request, null, false);
            requestView.setId(View.generateViewId());

            ImageView image = requestView.findViewById(R.id.imageViewRequestProfile);
            image.setImageResource(Icons.getIcons().get(friendUser.getImage()-1));

            TextView username = requestView.findViewById(R.id.textViewRequestUsername);
            username.setText("Friend request by " + friendUser.getName());

            ImageView accept = requestView.findViewById(R.id.imageViewAccept);
            ImageView reject = requestView.findViewById(R.id.imageViewReject);

            TextView message = findViewById(R.id.textViewFriendsMessage);

            accept.setOnClickListener(v -> {
                linearLayout.removeView(requestView);
                if (linearLayout.getChildCount()==0){
                    message.setVisibility(View.VISIBLE);
                }
                new Thread(() -> ServerSQL.acceptFriendRequest(phone,friendUser.getName())).start();
            });

            reject.setOnClickListener(v -> {
                linearLayout.removeView(requestView);
                if (linearLayout.getChildCount()==0){
                    message.setVisibility(View.VISIBLE);
                }
                new Thread(() -> ServerSQL.removeFriendOrRequest(phone,friendUser.getName())).start();
            });

            linearLayout.addView(requestView);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void hideKeyboard(View view) {
        InputMethodManager inputMethodManager =(InputMethodManager)getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    @SuppressLint("StaticFieldLeak")
    private class AsyncTaskCreateList extends AsyncTask<String,String,String> {
        @Override
        protected String doInBackground(String... strings) {
            try {
                ArrayList<User> friendRequests = ServerSQL.getFriendRequests(phone);
                runOnUiThread(() -> {
                    linearLayout.removeAllViews();
                    for (int i=0;i<friendRequests.size();i++){
                        addView(friendRequests.get(i));
                    }
                    TextView message = findViewById(R.id.textViewFriendsMessage);
                    if (friendRequests.size()==0){
                        message.setVisibility(View.VISIBLE);
                    }else{
                        message.setVisibility(View.INVISIBLE);
                    }
                });
            }catch (Exception e){
                e.printStackTrace();
            }
            return null;
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class AsyncTaskSendRequest extends AsyncTask<String,String,String> {
        @Override
        protected String doInBackground(String... strings) {
            try {
                if (ServerSQL.sendFriendRequest(phone,friendUsername)) {
                    runOnUiThread(() -> Toast.makeText(Friends.this, "Request has been sent.", Toast.LENGTH_SHORT).show());
                }else{
                    runOnUiThread(() -> Toast.makeText(Friends.this, "Something went wrong.", Toast.LENGTH_SHORT).show());
                }
            }catch (Exception e){
                e.printStackTrace();
            }
            return null;
        }
    }

    private void goToFeed()
    {
        Intent intent = new Intent(this, Feed.class);
        intent.putExtras(bundle);
        startActivity(intent);
        overridePendingTransition(R.anim.fadein, R.anim.fadeout);
        finish();
    }

    @Override
    public void onBackPressed() {}
}