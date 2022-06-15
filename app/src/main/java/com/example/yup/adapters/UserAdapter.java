package com.example.yup.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.yup.R;


public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UsersViewHolder> {

    private Context mContext = null;

    public UserAdapter(Context mContext) {
        this.mContext = mContext;
    }



    @NonNull
    @Override
    public UsersViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_dashboard, parent, false);
        return new UserAdapter.UsersViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UsersViewHolder holder, int position) {


        holder.userName.setText("user name anonymous");
    }

    @Override
    public int getItemCount() {
        return 0;
    }


    public class UsersViewHolder extends RecyclerView.ViewHolder{

        TextView userName;
        public UsersViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }

}
