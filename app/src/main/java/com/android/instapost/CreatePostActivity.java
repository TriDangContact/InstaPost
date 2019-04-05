package com.android.instapost;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatButton;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

public class CreatePostActivity extends AppCompatActivity {

    private ImageView mImageView;
    private ImageButton mCaptureButton;
    private EditText mCaption;
    private EditText mHashtag;
    private AppCompatButton mCreatePostButton;
    private TextView mCancelLink;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_post);

        mImageView = (ImageView) findViewById(R.id.createpost_image);
        mCaptureButton = (ImageButton) findViewById(R.id.createpost_capture);
        mCaption = (EditText) findViewById(R.id.createpost_input_caption);
        mHashtag = (EditText) findViewById(R.id.createpost_input_hashtag);
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
            }
        });

        mCancelLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });




    }
}
