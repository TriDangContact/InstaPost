package com.android.instapost;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatButton;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CreatePostActivity extends AppCompatActivity {
    private static final String EXTRA_USERNAME = "com.android.instapost.username";
    private static final String CREATE_POST_TAG = "CreatePostActivity";
    private static final String TAG_DB_PATH = "tag";
    private static final String POST_DB_PATH = "post";

    private FirebaseDatabase mDatabase;
    private ImageView mImageView;
    private ImageButton mCaptureButton;
    private EditText mCaptionText;
    private EditText mHashtagText;
    private AppCompatButton mCreatePostButton;
    private TextView mCancelLink;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_post);

        mDatabase = FirebaseDatabase.getInstance();
        mImageView = (ImageView) findViewById(R.id.createpost_image);
        mCaptureButton = (ImageButton) findViewById(R.id.createpost_capture);
        mCaptionText = (EditText) findViewById(R.id.createpost_input_caption);
        mHashtagText = (EditText) findViewById(R.id.createpost_input_hashtag);
        mCreatePostButton = (AppCompatButton) findViewById(R.id.createpost_btn_post);
        mCancelLink = (TextView) findViewById(R.id.createpost_link_cancel);

        mCaptureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: create implicit intent to get a photo
            }
        });

        mCreatePostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: put the post into database
                if (validate()) {
                    addPostToDB(createPost());
                    addTagToDB(mHashtagText.getText().toString());
                    setResult(RESULT_OK);
                    finish();
                }
            }
        });

        mCancelLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(RESULT_CANCELED);
                finish();
            }
        });
    }

    private boolean validate() {
        boolean valid = true;
        // TODO: implement method to validate the post before posting it
        String hashtag = mHashtagText.getText().toString().trim();

        if (hashtag.isEmpty() || !isValidHashtag(hashtag)) {
            mHashtagText.setError("At least 1 hashtag required.\nHashtag can only contain alphabetic" +
                    " " +
                    "letters.");
            valid = false;
        }
        else {
            mHashtagText.setError(null);
        }


        return valid;
    }

    private boolean isValidHashtag(String hashtag) {
        Pattern pattern;
        Matcher matcher;
        // only alphabet letters
        final String PASSWORD_PATTERN = "[a-zA-Z]+";

        pattern = Pattern.compile(PASSWORD_PATTERN);
        matcher = pattern.matcher(hashtag);

        return matcher.matches();
    }

    private Post createPost(){
        String username = getCurrentUserName();
        String caption = mCaptionText.getText().toString().trim();
        String hashtag = mHashtagText.getText().toString().trim();
        // TODO: implement way to store the image and able to retrieve it
        String image = "current image";
        return new Post(username, caption, hashtag, image);
    }

    private String getCurrentUserName(){
        Bundle bundle = getIntent().getExtras();
        return bundle.getString(EXTRA_USERNAME);
        // TODO: get the uid of the current user, then use it to look in db and get their username
    }

    private void addPostToDB(Post post) {
        DatabaseReference postTable = mDatabase.getReference(POST_DB_PATH);
        String uid = UUID.randomUUID().toString();
        post.mId = uid;
        postTable.child(uid).setValue(post);
        Toast.makeText(CreatePostActivity.this, "Post created",
                Toast.LENGTH_SHORT).show();
    }

    // check if a hashtag already exists in firebase; if not, add it
    private void addTagToDB(String hashtag) {
        final String tag = hashtag.trim();
        final DatabaseReference hashtagTable = mDatabase.getReference();
        Query query = hashtagTable.child(TAG_DB_PATH).orderByChild("mTag").equalTo(hashtag);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    String uid = UUID.randomUUID().toString();
                    hashtagTable.child(TAG_DB_PATH).child(uid).child("mTag").setValue(tag);
                }
            }
            @Override
            public void onCancelled(DatabaseError error) {
                Log.d(CREATE_POST_TAG, error.getMessage());
            }
        });
    }
}
