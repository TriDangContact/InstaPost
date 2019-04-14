package com.android.instapost;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatButton;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CreatePostActivity extends AppCompatActivity {
    private static final String EXTRA_USERNAME = "com.android.instapost.username";
    private static final String CREATE_POST_TAG = "CreatePostActivity";
    private static final String TAG_DB_PATH = "tag";
    private static final String POST_DB_PATH = "post";
    private static final String TAG_DB_ORDER_BY = "mHashtag";
    private static final int REQUEST_CAMERA = 0;
    private static final int REQUEST_GALLERY = 1;
    private static final String DOWNLOAD_DIR = Environment.getExternalStoragePublicDirectory
            (Environment.DIRECTORY_DOWNLOADS).getPath();

    private FirebaseDatabase mDatabase;
    private ImageView mImageView;
    private ImageButton mCaptureButton;
    private EditText mCaptionText;
    private EditText mHashtagText;
    private AppCompatButton mCreatePostButton;
    private TextView mCancelLink;
    private String mCurrentPhotoPath = "";
    private Uri mPhotoUri;
    private ProgressBar mProgressBar;

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
        mProgressBar = (ProgressBar) findViewById(R.id.createpost_loading);

        mCaptureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPictureDialog();
                //launchCamera();
            }
        });

        mCreatePostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validate()) {
                    try {
                        downsizeImage(mPhotoUri);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    addTagToDB(mHashtagText.getText().toString());
                    addPhotoToDB(mPhotoUri);
                }
                else {
                    Toast.makeText(CreatePostActivity.this, getString(R.string.no_photo),
                            Toast.LENGTH_SHORT).show();
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

    /*
    -------------------------------------------------------------------------------
    START OF: ASK USER TO TAKE A PHOTO PUT IT INTO THE THUMBNAIL
    -------------------------------------------------------------------------------
    */

    private void showPictureDialog(){
        AlertDialog.Builder pictureDialog = new AlertDialog.Builder(this);
        pictureDialog.setTitle(R.string.image_dialog_title);
        String[] pictureDialogItems = {
                getString(R.string.image_dialog_gallery),
                getString(R.string.image_dialog_camera) };
        pictureDialog.setItems(pictureDialogItems,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0:
                                choosePhotoFromGallery();
                                break;
                            case 1:
                                launchCamera();
                                break;
                        }
                    }
                });
        pictureDialog.show();
    }

    public void choosePhotoFromGallery() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_PICK);
        startActivityForResult(Intent.createChooser(intent,
                getString(R.string.image_chooser_title)),
                REQUEST_GALLERY);
    }

    // use function from Android Document to take a photo
    public void launchCamera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
                Log.d(CREATE_POST_TAG, ex.getMessage());
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.android.instapost.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_CAMERA);
            }
        } else {
            Toast.makeText(CreatePostActivity.this, getString(R.string.no_camera),
                    Toast.LENGTH_SHORT).show();
        }
    }

    // use function from Android Document to generate an image file
    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat(getString(R.string.date_format)).format(new Date());
        String pathStart = getString(R.string.create_image_path_start);
        String pathEnd = getString(R.string.create_image_path_end);
        String imageSuffix = getString(R.string.create_image_suffix);

        String imageFileName = pathStart + timeStamp + pathEnd;
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                imageSuffix,         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        mPhotoUri = Uri.fromFile(image);
        return image;
    }

    // once the user is done taking a photo, we load it into the thumbnail
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CAMERA) {
            if (resultCode == RESULT_OK) {
                loadPhotoToThumbnail(mPhotoUri);
            }
            else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(CreatePostActivity.this, getString(R.string.cancelled_photo),
                        Toast.LENGTH_SHORT).show();
            }
        }
        else if (requestCode == REQUEST_GALLERY) {
            if (resultCode == RESULT_OK) {
                mPhotoUri = data.getData();
                loadPhotoToThumbnail(mPhotoUri);
            }
            else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(CreatePostActivity.this, getString(R.string.cancelled_gallery),
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void loadPhotoToThumbnail(Uri uri) {
        Glide
            .with(this)
            .load(uri)
            .override(mImageView.getWidth(), mImageView.getHeight())
            .into(mImageView);
    }

    // three step process to convert URI to bitmap, then back to URI
    private Uri downsizeImage(Uri uri) throws IOException{
        Bitmap image = createBitmap(uri);
        return getImageUri(image);
    }

    private Bitmap createBitmap(Uri uri) throws IOException{
        return MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
    }

    private Uri getImageUri(Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 50, bytes);
        String path = MediaStore.Images.Media.insertImage(this.getContentResolver(), inImage,
                "Title", null);
        return Uri.parse(path);
    }

    /*
    -------------------------------------------------------------------------------
    END OF: ASK USER TO TAKE A PHOTO PUT IT INTO THE THUMBNAIL
    -------------------------------------------------------------------------------
    */

    // we validate user inputs
    private boolean validate() {
        boolean valid = true;
        // TODO: implement method to validate the post before posting it
        String hashtag = mHashtagText.getText().toString().trim();

        if (mCurrentPhotoPath.isEmpty() && mPhotoUri == null) {
            valid = false;
        }

        if (hashtag.isEmpty() || !isValidHashtag(hashtag)) {
            mHashtagText.setError(getString(R.string.invalid_hashtag));
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

    /*
    -------------------------------------------------------------------------------
    START OF: UPLOADING THE POST TO DATABASE
    -------------------------------------------------------------------------------
    */

    // 1. before we can create a post, we need to upload the photo to db and get db path so that we
    // can store it in the Post
    private void addPhotoToDB(Uri picUri) {
        String uid = UUID.randomUUID().toString();
        final String cloudFilePath = getCurrentUserName() + uid;

        StorageReference storageRef = FirebaseStorage.getInstance().getReference();
        StorageReference uploadeRef = storageRef.child(cloudFilePath);
        StorageMetadata metadata = new StorageMetadata.Builder()
                .setContentType("image/jpg")
                .build();

        // upload to cloud and show progress while doing it
        showProgressBar();
        uploadeRef.putFile(picUri, metadata).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>(){
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot){
                // finish the progress bar and
                addPostToDB(createPost(cloudFilePath));
                mProgressBar.setVisibility(ProgressBar.INVISIBLE);
                // allow user interaction again
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
            }
        }).addOnFailureListener(new OnFailureListener(){
            public void onFailure(@NonNull Exception exception){
                Toast.makeText(CreatePostActivity.this, getString(R.string.failed_upload),
                        Toast.LENGTH_SHORT).show();
            }
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                // set the progressbar status
                double progress = (100.0*taskSnapshot.getBytesTransferred()/taskSnapshot
                        .getTotalByteCount());
                int progressInt = (int) progress;
                mProgressBar.setProgress(progressInt);
                // prevent user from touching anything while uploading
                getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
            }
        });
    }

    // 2. once we have the db path of the photo, we can create our Post
    private Post createPost(String image){
        String username = getCurrentUserName();
        String caption = mCaptionText.getText().toString().trim();
        String hashtag = mHashtagText.getText().toString().trim();
        return new Post(username, caption, hashtag, image);
    }

    // get the username that was passed in when this activity was started
    private String getCurrentUserName(){
        Bundle bundle = getIntent().getExtras();
        return bundle.getString(EXTRA_USERNAME);
    }

    private void addPostToDB(Post post) {
        DatabaseReference postTable = mDatabase.getReference(POST_DB_PATH);
        // get a uid based on time of upload
        String uid = postTable.push().getKey();
        post.mId = uid;
        postTable.child(uid).setValue(post);
        Toast.makeText(CreatePostActivity.this, getString(R.string.create_post_successful),
                Toast.LENGTH_SHORT).show();
        setResult(RESULT_OK);
        finish();
    }

    // check if a hashtag already exists in firebase; if not, add it
    private void addTagToDB(String hashtag) {
        final String tag = hashtag.trim();
        final DatabaseReference hashtagTable = mDatabase.getReference();
        Query query = hashtagTable.child(TAG_DB_PATH).orderByChild(TAG_DB_ORDER_BY).equalTo(hashtag);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    String uid = UUID.randomUUID().toString();
                    hashtagTable.child(TAG_DB_PATH).child(uid).child(TAG_DB_ORDER_BY).setValue(tag);
                }
            }
            @Override
            public void onCancelled(DatabaseError error) {
                Log.d(CREATE_POST_TAG, error.getMessage());
            }
        });
    }

    /*
    -------------------------------------------------------------------------------
    END OF: UPLOADING THE POST TO DATABASE
    -------------------------------------------------------------------------------
    */

    private void showProgressBar() {
        mProgressBar.setVisibility(ProgressBar.VISIBLE);
        mProgressBar.setProgress(0);
        mProgressBar.setMax(100);
    }
}
