package com.android.instapost;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

/**
 * {@link RecyclerView.Adapter} that can display a {@link Post} and makes a call to the
 * specified {@link com.android.instapost.PostListFragment.OnPostListFragmentInteractionListener}.
 * TODO: Replace the implementation with code for your data type.
 */
public class PostRecyclerAdapter extends RecyclerView.Adapter<PostRecyclerAdapter.ViewHolder> {

    // TODO: replace with your data type
    private final List<Post> mValues;
    private final PostListFragment.OnPostListFragmentInteractionListener mListener;

    // TODO: replace with your data type
    public PostRecyclerAdapter(List<Post> items,
                               PostListFragment.OnPostListFragmentInteractionListener listener) {
        mValues = items;
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_post, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mItem = mValues.get(position);
        holder.mIdView.setText(mValues.get(position).mUsername);
        holder.mCaptionView.setText(mValues.get(position).mCaption);
        holder.mHashtagView.setText(mValues.get(position).mHashtag);
        // TODO: include a way to get the image of the post
        //holder.mImageView.setImageDrawable(mValues.get(position).image);

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
                    mListener.onPostListFragmentInteraction(holder.mItem);
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
        public Post mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mIdView = (TextView) view.findViewById(R.id.fm_post_username);
            mCaptionView = (TextView) view.findViewById(R.id.fm_post_caption);
            mHashtagView = (TextView) view.findViewById(R.id.fm_post_hashtag);
            mImageView = (ImageView) view.findViewById(R.id.fm_post_image);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mIdView.getText() + "'";
        }
    }
}
