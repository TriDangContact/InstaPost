package com.android.instapost;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

/**
 * {@link RecyclerView.Adapter} that can display a {@link User} and makes a call to the
 * specified {@link com.android.instapost.UserListFragment.OnUserListFragmentInteractionListener}.
 * TODO: Replace the implementation with code for your data type.
 */
public class UserRecyclerAdapter extends RecyclerView.Adapter<UserRecyclerAdapter.ViewHolder> {

    // TODO: replace with your data type
    private final List<User> mValues;
    private final UserListFragment.OnUserListFragmentInteractionListener mListener;

    // TODO: replace with your data type
    public UserRecyclerAdapter(List<User> items,
                               UserListFragment.OnUserListFragmentInteractionListener listener) {
        mValues = items;
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_user, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mItem = mValues.get(position);
        holder.mIdView.setText(mValues.get(position).mUsername);
        holder.mCaptionView.setText(mValues.get(position).mName);
        holder.mHashtagView.setText(mValues.get(position).mEmail);
        // TODO: include a way to get the image of the post

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
                    mListener.onUserListFragmentInteraction(holder.mItem);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView mIdView;
        public final TextView mCaptionView;
        public final TextView mHashtagView;
        public final ImageView mImageView;
        // TODO: replace with your data class object
        public User mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mIdView = (TextView) view.findViewById(R.id.fm_user_username);
            mCaptionView = (TextView) view.findViewById(R.id.fm_user_name);
            mHashtagView = (TextView) view.findViewById(R.id.fm_user_email);
            mImageView = (ImageView) view.findViewById(R.id.fm_user_image);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mIdView.getText() + "'";
        }
    }
}
