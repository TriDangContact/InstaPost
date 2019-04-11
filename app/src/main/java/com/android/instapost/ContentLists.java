package com.android.instapost;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

public class ContentLists {

    private static ContentLists sContentLists;
    private static List<Post> mPostArrayList;
    private static List<User> mUserArrayList;
    private static List<String> mHashtagArrayList;

    public static ContentLists get(Context context){
        if (sContentLists == null) {
            sContentLists = new ContentLists(context);
        }
        return sContentLists;
    }


    private ContentLists(Context context){
        mPostArrayList = new ArrayList<Post>();
        mUserArrayList = new ArrayList<User>();
        mHashtagArrayList = new ArrayList<String>();
    }

    public List<Post> getPosts(){
        return mPostArrayList;
    }

    public List<User> getUsers(){
        return mUserArrayList;
    }

    public List<String> getHashtags(){
        return mHashtagArrayList;
    }


}
