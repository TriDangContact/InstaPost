package com.android.instapost;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

/**
 * {@link RecyclerView.Adapter} that can display a Tag and makes a call to the
 * specified {@link com.android.instapost.HashtagListFragment.OnHashtagListFragmentInteractionListener}.
 * TODO: Replace the implementation with code for your data type.
 */
public class HashtagRecyclerAdapter extends RecyclerView.Adapter<HashtagRecyclerAdapter.ViewHolder> {

    // TODO: replace with your data type
    private final List<String> mValues;
    private final HashtagListFragment.OnHashtagListFragmentInteractionListener mListener;

    // TODO: replace with your data type
    public HashtagRecyclerAdapter(List<String> items,
                                  HashtagListFragment.OnHashtagListFragmentInteractionListener listener) {
        mValues = items;
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_hashtag, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mItem = mValues.get(position);
        holder.mHashtagView.setText(mValues.get(position));
        // TODO: include a way to get the image of the post
        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
                    mListener.onHashtagListFragmentInteraction(holder.mItem);
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
        public final TextView mHashtagView;
        // TODO: replace with your data class object
        public String mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mHashtagView = (TextView) view.findViewById(R.id.fm_hashtag);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mHashtagView.getText() + "'";
        }
    }
}
