package com.example.socialapp;

import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;
import com.example.socialapp.tools.Icons;
import java.util.ArrayList;

import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator;

public class CustomAdapter extends RecyclerView.Adapter<CustomAdapter.ViewHolder> {
    private ArrayList<User> friends;
    public CustomAdapter(ArrayList<User> friends) {
        this.friends = friends;
    }
    public void updateData(ArrayList<User> friends) {
        this.friends.clear(); // Clear current data
        this.friends.addAll(friends); // Add new data
        notifyDataSetChanged(); // Notify adapter about the change
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.friend_v2, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        User friend = friends.get(position);
        holder.bind(friend);
    }

    @Override
    public int getItemCount() {
        return friends.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private User friend;
        private ImageView imageView;
        private TextView username;
        private ImageView callButton;
        private TextView phoneNumber;

        //private TextView kmAway;
        //private TextView lastUpdated;

        public ViewHolder(@NonNull View friendView) {
            super(friendView);
            imageView = friendView.findViewById(R.id.imageViewFriendProfile);
            username = friendView.findViewById(R.id.textViewFriendUsername);
            callButton = friendView.findViewById(R.id.imageViewCallButton);
            phoneNumber = friendView.findViewById(R.id.textViewFriendPhone);

            //kmAway = friendView.findViewById(R.id.textViewFriendKmAway);
            //lastUpdated = friendView.findViewById(R.id.textViewFriendLastUpdated);

            callButton.setOnClickListener(v1 -> {
                if (friend != null) {
                    Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + friend.getPhone()));
                    friendView.getContext().startActivity(intent); // Start activity using the context of the itemView
                }
            });
        }

        public void bind(User friend) {
            this.friend = friend;
            if (friend != null) {
                phoneNumber.setText(friend.getPhone());
                imageView.setImageResource(Icons.getIcons().get(friend.getImage() - 1));
                username.setText(friend.getUsername());
                //kmAway.setText(friend.getMetersAwayMessage());
                //lastUpdated.setText(friend.getUpdateTimeMessage());
            }
        }
    }

    public void enableSwipeToDelete(RecyclerView recyclerView) {
        SwipeToDeleteCallback swipeToDeleteCallback = new SwipeToDeleteCallback() {
            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                friends.remove(position);
                notifyItemRemoved(position);
            }
        };

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(swipeToDeleteCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);
    }

    public abstract class SwipeToDeleteCallback extends ItemTouchHelper.SimpleCallback {
        public SwipeToDeleteCallback() {
            super(0, ItemTouchHelper.LEFT);
        }
        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
            return false;
        }
        @Override
        public abstract void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction);

        @Override
        public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
            new RecyclerViewSwipeDecorator.Builder(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
                    .addSwipeLeftBackgroundColor(Color.rgb(54,31,39))
                    .addSwipeLeftActionIcon(R.drawable.removefriend)
                    .setSwipeLeftLabelColor(Color.WHITE)
                    .create()
                    .decorate();

            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
        }
    }
}
