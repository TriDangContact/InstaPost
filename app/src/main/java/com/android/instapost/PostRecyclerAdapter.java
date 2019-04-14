package com.android.instapost;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.List;

/**
 * {@link RecyclerView.Adapter} that can display a {@link Post} and makes a call to the
 * specified {@link com.android.instapost.PostListFragment.OnPostListFragmentInteractionListener}.
 * TODO: Replace the implementation with code for your data type.
 */
public class PostRecyclerAdapter extends RecyclerView.Adapter<PostRecyclerAdapter.ViewHolder> {

    private static final int OPTION_1 = 1;
    private static final int OPTION_2 = 2;
    // TODO: replace with your data type
    private final List<Post> mValues;
    private final PostListFragment.OnPostListFragmentInteractionListener mListener;
    private final Context mContext;

    // TODO: replace with your data type
    public PostRecyclerAdapter(List<Post> items,
                               PostListFragment.OnPostListFragmentInteractionListener listener,
                               Context context) {
        mValues = items;
        mListener = listener;
        mContext = context;
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
        loadImage(holder.mImageView, mValues.get(position).mImagePath, holder.mProgressBar);


        // instead of allowing user to click on the entire post, we only let them click on the
        // post's Option Menu
//        holder.mView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (null != mListener) {
//                    // Notify the active callbacks interface (the activity, if the
//                    // fragment is attached to one) that an item has been selected.
//                    mListener.onPostListFragmentInteraction(holder.mItem);
//                }
//            }
//        });
        holder.mOptionsView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popup = new PopupMenu(mContext, holder.mOptionsView);
                popup.inflate(R.menu.post_options_menu);
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.post_option1:
                                if (null != mListener) {
                                    // Notify the active callbacks interface (the activity, if the
                                    // fragment is attached to one) that an item has been selected.
                                    mListener.onPostListFragmentInteraction(holder.mItem, OPTION_1);
                                }
                                return true;
                            case R.id.post_option2:
                                if (null != mListener) {
                                    // Notify the active callbacks interface (the activity, if the
                                    // fragment is attached to one) that an item has been selected.
                                    mListener.onPostListFragmentInteraction(holder.mItem, OPTION_2);
                                }
                                return true;
                            default:
                                return false;
                        }
                    }
                });
                popup.show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    // get the image from firebase storage and load it using Glide library
    private void loadImage(ImageView imageView, String filePath, ProgressBar progress) {
        final ProgressBar progressBar = progress;
        StorageReference storageReference = FirebaseStorage.getInstance().getReference().child(filePath);
        Glide
            .with(mContext)
            .load(storageReference)
            .centerCrop()
            .listener(new RequestListener<Drawable>() {
                @Override
                public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                    progressBar.setVisibility(View.GONE);
                    return false;
                }
                @Override
                public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                    progressBar.setVisibility(View.GONE);
                    return false;
                }
            })
            .transition(DrawableTransitionOptions.withCrossFade())
            .into(imageView);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView mIdView;
        public final TextView mCaptionView;
        public final TextView mHashtagView;
        public final ImageView mImageView;
        public final ImageView mOptionsView;
        public final ProgressBar mProgressBar;
        // TODO: replace with your data class object
        public Post mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mIdView = (TextView) view.findViewById(R.id.fm_post_username);
            mCaptionView = (TextView) view.findViewById(R.id.fm_post_caption);
            mHashtagView = (TextView) view.findViewById(R.id.fm_post_hashtag);
            mImageView = (ImageView) view.findViewById(R.id.fm_post_image);
            mOptionsView = (ImageView) view.findViewById(R.id.fm_post_option);
            mProgressBar = (ProgressBar) view.findViewById(R.id.fm_post_progress);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mIdView.getText() + "'";
        }
    }
}
