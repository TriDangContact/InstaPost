package com.android.instapost;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the
 * {@link OnPostListFragmentInteractionListener}
 * interface.
 */
public class PostListFragment extends Fragment {

    // TODO: Customize parameter argument names
    private static final String ARG_COLUMN_COUNT = "column-count";

    // TODO: Customize parameters
    private RecyclerView mRecyclerView;
    private int mColumnCount;
    private OnPostListFragmentInteractionListener mListener;


    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public PostListFragment() {
    }

    // TODO: Customize parameter initialization
    @SuppressWarnings("unused")
    public static PostListFragment newInstance(int columnCount) {
        PostListFragment fragment = new PostListFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_COLUMN_COUNT, columnCount);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ContentLists list = ContentLists.get(getActivity());
        List<Post> postList = list.getPosts();
        View view;
        if (postList.size() == 0) {
            view = inflater.inflate(R.layout.fragment_empty, container, false);
            TextView emptyText = (TextView) view.findViewById(R.id.no_list_text);
            emptyText.setText(R.string.no_posts);
        }
        else {
            view = inflater.inflate(R.layout.fragment_post_list, container, false);

            // Set the adapter
            if (view instanceof RecyclerView) {
                Context context = view.getContext();
                mRecyclerView = (RecyclerView) view;
                if (mColumnCount <= 1) {
                    mRecyclerView.setLayoutManager(new LinearLayoutManager(context));
                } else {
                    mRecyclerView.setLayoutManager(new GridLayoutManager(context, mColumnCount));
                }
                // TODO: pass in the list of items you want to display
                mRecyclerView.setAdapter(new PostRecyclerAdapter(postList, mListener,
                        getContext()));
            }
        }
        return view;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnPostListFragmentInteractionListener) {
            mListener = (OnPostListFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnListFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnPostListFragmentInteractionListener {
        // TODO: Update argument type and name
        void onPostListFragmentInteraction(Post item, int selected);
    }
}
