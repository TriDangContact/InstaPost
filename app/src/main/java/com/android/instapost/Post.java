package com.android.instapost;

public class Post {

    public String mId;
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
    public boolean equals (Object object) {
        boolean result = false;
        if (object == null || object.getClass() != getClass()) {
            result = false;
        } else {
            Post post = (Post) object;
            if (this.mId.equals(post.mId)) {
                result = true;
            }
        }
        return result;
    }

    @Override
    public String toString() {
        return "Username " + mUsername + ", caption: " + mCaption + ", hashtag: " + mHashtag + "," +
                " imagepath: " + mImagePath;
    }


}
