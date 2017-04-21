package com.example.sreer.sdsuchat;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

/**
 * Created by sreer on 07-04-2017.
 */

public class UserAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<User> userList;
    private Context context;
    private String mCurrentUserEmail;
    private long mCurrentUserCreatedAt;
    private String mCurrentUserId;

    private final int VIEW_ITEM = 1;
    private final int VIEW_PROG = 0;
    private int visibleThreshold = 5;
    private int lastVisibleItem, totalItemCount;
    private boolean loading;
    private OnLoadMoreListener onLoadMoreListener;

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        public TextView nickname, city, year;

        public MyViewHolder(View view) {
            super(view);
            nickname = (TextView) view.findViewById(R.id.row_nickname);
            city = (TextView) view.findViewById(R.id.row_city);
            year = (TextView) view.findViewById(R.id.row_year);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {

            User user = userList.get(getLayoutPosition());
            FirebaseDatabase.getInstance().getReference().child("meta-data")
                    .child(user.getNickname()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.getValue() != null) {
                        User user = dataSnapshot.getValue(User.class);
                        if (TextUtils.equals(user.getFirebaseID(), mCurrentUserId))
                            Toast.makeText(context, "That is You !", Toast.LENGTH_SHORT).show();
                        else {
                            String chatRef = user.createUniqueChatRef(mCurrentUserCreatedAt, mCurrentUserEmail);
                            Intent chatIntent = new Intent(context, ChatActivity.class);
                            chatIntent.putExtra(ExtraIntent.EXTRA_CURRENT_USER_ID, mCurrentUserId);
                            chatIntent.putExtra(ExtraIntent.EXTRA_RECIPIENT_ID, user.getFirebaseID());
                            chatIntent.putExtra(ExtraIntent.EXTRA_CHAT_REF, chatRef);
                            chatIntent.putExtra(ExtraIntent.EXTRA_RECIPIENT_NAME,user.getNickname());
                            //Start new activity
                            context.startActivity(chatIntent);
                        }
                    } else {
                        Toast.makeText(context, "User not registered in firebase", Toast.LENGTH_SHORT).show();

                    }

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                    Toast.makeText(context, "Error:Data fetch failed from firebase", Toast.LENGTH_SHORT).show();

                }
            });
        }
    }


    public UserAdapter(List<User> userList, Context context,String userUid, String email, long createdAt ) {
        this.userList = userList;
        this.context = context;
        mCurrentUserId = userUid;
        mCurrentUserEmail = email;
        mCurrentUserCreatedAt = createdAt;
        Log.i("mCurrentUserId",mCurrentUserId );
        Log.i("mCurrentUserEmail",mCurrentUserEmail );
        Log.i("mCurrentUserCreatedAt",String.valueOf(mCurrentUserCreatedAt));
    }

    public UserAdapter(List<User> userList, Context context, String userUid, String email, long createdAt, RecyclerView recyclerView) {
        this.userList = userList;
        this.context = context;
        mCurrentUserId = userUid;
        mCurrentUserEmail = email;
        mCurrentUserCreatedAt = createdAt;
        if (recyclerView.getLayoutManager() instanceof LinearLayoutManager) {

            final LinearLayoutManager linearLayoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();

            Log.i("test", "It is instance");


            recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(RecyclerView recyclerView,int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);

                    totalItemCount = linearLayoutManager.getItemCount();
                    lastVisibleItem = linearLayoutManager.findLastVisibleItemPosition();
                    if (!loading
                            && totalItemCount <= (lastVisibleItem + visibleThreshold)) {
                        // End has been reached
                        // Do something
                        if (onLoadMoreListener != null) {
                            onLoadMoreListener.onLoadMore();
                        }
                    }
                }
            });
        }

        Log.i("test", "It is not instance");

    }

    public List<User> getUsersList(){
        return this.userList;
    }

    public void updateAdapter(List<User> filterData){
        userList.clear();
        userList.addAll(filterData);
        notifyDataSetChanged();
    }


    public User getUser(int position) {
        return userList.get(position);
    }


    @Override
    public int getItemViewType(int position) {
        return userList.get(position) != null ? VIEW_ITEM : VIEW_PROG;
    }

    public void setLoaded() {
        this. loading = false;
    }

    public void setLoading() {
        this. loading = true;
    }

    public void setOnLoadMoreListener(OnLoadMoreListener onLoadMoreListener) {
        this.onLoadMoreListener = onLoadMoreListener;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder viewHolder;
        if (viewType == VIEW_ITEM) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.user_list_row, parent, false);
            viewHolder = new MyViewHolder(itemView);
        }
        else {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.progressbar_item, parent, false);
            viewHolder = new ProgressViewHolder(itemView);
        }
        return viewHolder;
    }


    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof MyViewHolder){
            MyViewHolder userHolder = (MyViewHolder)holder;
            User user = userList.get(position);
            userHolder.nickname.setText(user.getNickname());
            userHolder.city.setText(user.getCity() + " | " + user.getState() + " | " + user.getCountry());
            userHolder.year.setText(String.valueOf(user.getYear()));
        }
        else
            ((ProgressViewHolder) holder).progressBar.setIndeterminate(true);

    }


    @Override
    public int getItemCount() {
        return userList.size();
    }

    public static class ProgressViewHolder extends RecyclerView.ViewHolder {
        public ProgressBar progressBar;
        public ProgressViewHolder(View v) {
            super(v);
            progressBar = (ProgressBar) v.findViewById(R.id.progressBar1);
        }
    }

    public void add(User user) {
        userList.add(user);
        notifyItemInserted(userList.size() - 1);
    }

    public void clear(){
        userList.clear();
    }

    public void remove(){
        userList.remove(userList.size() - 1);
        notifyItemRemoved(userList.size());
    }

    public boolean contains(User user){
        if(userList.contains(user))
            return true;
        return false;

    }

}

