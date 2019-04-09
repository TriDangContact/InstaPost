package com.android.instapost;

import java.util.ArrayList;
import java.util.List;

public class Post {

    public String mUsername;
    public String mCaption;
    public String mHashtag;
    public String mImagePath;

    public Post() {
    }

    public Post(String username, String caption, String hashtag, String imagepath) {
        this.mUsername = username;
        this.mCaption = caption;
        this.mHashtag = hashtag;
        this.mImagePath = imagepath;
    }

    @Override
    public String toString() {
        return "Username " + mUsername + ", caption: " + mCaption + ", hashtag: " + mHashtag + "," +
                " imagepath: " + mImagePath;
    }


}
