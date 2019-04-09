package com.android.instapost;

public class User {

    public String mUsername;
    public String mName;
    public String mEmail;

    public User() {}

    public User(String username, String name, String email) {
        this.mUsername = username;
        this.mName = name;
        this.mEmail = email;

    }

    @Override
    public boolean equals (Object object) {
        boolean result = false;
        if (object == null || object.getClass() != getClass()) {
            result = false;
        } else {
            User user = (User) object;
            if (this.mUsername.equals(user.mUsername)) {
                result = true;
            }
        }
        return result;
    }
}
