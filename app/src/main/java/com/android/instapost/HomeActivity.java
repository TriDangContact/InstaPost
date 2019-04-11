package com.android.instapost;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.util.List;


public class HomeActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, UserListFragment.OnUserListFragmentInteractionListener, PostListFragment.OnPostListFragmentInteractionListener, HashtagListFragment.OnHashtagListFragmentInteractionListener {
    private static final String HOME_TAG = "HomeActivity";
    private static final int REQUEST_CREATE_POST = 0;
    private static final String EXTRA_NAME = "com.android.instapost.name";
    private static final String EXTRA_USERNAME = "com.android.instapost.username";
    private static final String USER_DB_PATH = "user";
    private static final String TAG_DB_PATH = "tag";
    private static final String POST_DB_PATH = "post";
    private static final String USERNAME_DB_ORDER_BY = "mUsername";
    private static final String ID_DB_ORDER_BY = "mId";
    private static final String TAG_DB_ORDER_BY = "mHashtag";
    private static final String DOWNLOAD_DIR = Environment.getExternalStoragePublicDirectory
            (Environment.DIRECTORY_DOWNLOADS).getPath();

    private FirebaseDatabase mDatabase;
    private FirebaseStorage mStorage;
    private DrawerLayout mDrawerLayout;
    private Toolbar mToolbar;
    private NavigationView mNavigationView;
    private ActionBarDrawerToggle mDrawerToggle;
    private TextView mHeaderView;
    private String mName, mUserName, mEmail;

    private int mColumnCount = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        mDatabase = FirebaseDatabase.getInstance();
        mStorage = FirebaseStorage.getInstance();

        mToolbar = (Toolbar) findViewById(R.id.action_bar);
        setSupportActionBar(mToolbar);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, mDrawerLayout, mToolbar, R.string.navigation_drawer_open,
                R.string.navigation_drawer_close);
        mDrawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        mNavigationView = (NavigationView) findViewById(R.id.nav_view);
        mNavigationView.setNavigationItemSelectedListener(this);

        getUserInfo();

        View headerView = mNavigationView.getHeaderView(0);
        mHeaderView = (TextView) headerView.findViewById(R.id.header_text);

        // TODO: call method to retrieve data from db before adding any fragments
        retrieveUserList();

    }

    @Override
    public void onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
            setTitle(getString(R.string.app_name));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_create_post:
                createPost();
                return true;
            case R.id.action_logout:
                logout();
                return true;
            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        switch (item.getItemId()) {
            case R.id.nav_first_fragment:
                Toast.makeText(HomeActivity.this, R.string.drawer_item_1,
                        Toast.LENGTH_SHORT).show();
                mDrawerLayout.closeDrawer(GravityCompat.START);
                retrieveUserList();
                return true;
            case R.id.nav_second_fragment:
                Toast.makeText(HomeActivity.this, R.string.drawer_item_2,
                        Toast.LENGTH_SHORT).show();
                mDrawerLayout.closeDrawer(GravityCompat.START);
                retrieveTagList();
                return true;
            case R.id.nav_third_fragment:
                Toast.makeText(HomeActivity.this, R.string.drawer_item_3,
                        Toast.LENGTH_SHORT).show();
                mDrawerLayout.closeDrawer(GravityCompat.START);
                //changeColumnCount();
                //displayUserFragment();
                return true;
        }
        return true;
    }

    @Override
    public void onUserListFragmentInteraction(User item) {
        getSupportActionBar().setTitle(item.mUsername);
        retrievePostList(item.mUsername, USERNAME_DB_ORDER_BY);
    }

    @Override
    public void onHashtagListFragmentInteraction(String item) {
        getSupportActionBar().setTitle(item);
        retrievePostList(item, TAG_DB_ORDER_BY);
    }

    @Override
    public void onPostListFragmentInteraction(Post item, int selected) {
        // TODO: handle interaction for PostListFragment
        Toast.makeText(HomeActivity.this, "Item Selected " + item.mImagePath + "Option Selected: " + selected,
                Toast.LENGTH_SHORT).show();
        switch (selected) {
            case 1:
                downloadFile(item.mImagePath);
                break;
            case 2:
                deletePost(item.mId, item.mImagePath);
                break;
            default:
                break;
        }

    }

    // get the current authorized user's information and store them
    private void getUserInfo() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            // Name, email address, and profile photo Url
            mEmail = user.getEmail();

            // Get the Username, based on the uid
            String uid = user.getUid();
            DatabaseReference userTable = mDatabase.getReference();
            Query query =
                    userTable.child(USER_DB_PATH).orderByChild(ID_DB_ORDER_BY).equalTo(uid);
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                        User user = postSnapshot.getValue(User.class);
                        mUserName = user.mUsername;
                        mName = user.mName;
                    }
                    // we can set the header once we've retrieved the username
                    setHeader(mUserName);
                }
                @Override
                public void onCancelled(DatabaseError error) {
                    Log.d(HOME_TAG, error.getMessage());
                }
            });
        }
    }

    private void setHeader(String str){
        String header =
                getString(R.string.nav_header_greetings_start) + str + getString(R.string.nav_header_greetings_end);
        mHeaderView.setText(header);
    }

    private void setTitle(String title) {
        getSupportActionBar().setTitle(title);
    }

    private void logout() {
        FirebaseAuth.getInstance().signOut();
        startActivity(new Intent(HomeActivity.this, LoginActivity.class));
        finish();
    }

    private void createPost() {
        Intent intent = new Intent(HomeActivity.this, CreatePostActivity.class);
        intent.putExtra(EXTRA_USERNAME, mUserName);
        startActivityForResult(intent, REQUEST_CREATE_POST);
    }

    // After user is done interacting with CreatePostActivity, we just display the list of users
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CREATE_POST) {
            if (resultCode == RESULT_OK) {
                displayUserFragment();
            }
            else if (requestCode == RESULT_CANCELED) {
                displayUserFragment();
            }
        }
    }

    /*
    -------------------------------------------------------------------------------------------
    START OF: RETRIEVING THE APPROPRIATE LIST DEPENDING ON WHAT USER CLICKED, THEN DISPLAY THEM
    -------------------------------------------------------------------------------------------
    */

    // this gets called when the user selects to view all users
    // get all the users from user table in firebase, and add them to the recyclerview list
    private void retrieveUserList() {
        // TODO: implement query to retrieve all users from db
        DatabaseReference userTable = mDatabase.getReference(USER_DB_PATH);
        userTable.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                ContentLists list = ContentLists.get(getApplicationContext());
                List<User> userList = list.getUsers();
                userList.clear();
                for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                    User user = postSnapshot.getValue(User.class);
                    if (!userList.contains(user)) {
                        userList.add(user);
                    }
                }
                displayUserFragment();
            }
            @Override
            public void onCancelled(DatabaseError error) {
                Log.d(HOME_TAG, error.getMessage());
            }
        });
    }

    // this gets called when the user selects to view all hashtags
    // get all the hashtags from tag table in firebase, and add them to the recylerview list
    private void retrieveTagList() {
        // TODO: implement query to retrieve all hashtags from db
        DatabaseReference hashtagTable = mDatabase.getReference(TAG_DB_PATH);
        hashtagTable.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                ContentLists list = ContentLists.get(getApplicationContext());
                List<String> hashtagList = list.getHashtags();
                hashtagList.clear();
                for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                    for (DataSnapshot snap : postSnapshot.getChildren()) {
                        String hashtag = snap.getValue(String.class);
                        if (!hashtagList.contains(hashtag)) {
                            hashtagList.add(hashtag);
                        }
                    }
                }
                displayTagFragment();
            }
            @Override
            public void onCancelled(DatabaseError error) {
                Log.d(HOME_TAG, error.getMessage());
            }
        });
    }

    // this gets called when the user selects to view all posts
    // get all the posts from firebase, depending on username or tag, and add them to list
    private void retrievePostList(String match, String childPath) {
        DatabaseReference postTable = mDatabase.getReference();
        Query query =
                postTable.child(POST_DB_PATH).orderByChild(childPath).equalTo(match);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                ContentLists list = ContentLists.get(getApplicationContext());
                List<Post> postList = list.getPosts();
                postList.clear();
                for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                    Post post = postSnapshot.getValue(Post.class);
                    if (!postList.contains(post)) {
                        postList.add(post);
                    }
                }
                displayPostFragment();
            }
            @Override
            public void onCancelled(DatabaseError error) {
                Log.d(HOME_TAG, error.getMessage());
            }
        });
    }

    // after we have the list of users, we display them
    private void displayUserFragment() {
        FragmentManager manager = getSupportFragmentManager();
        Fragment fragment = UserListFragment.newInstance(mColumnCount);
        manager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commitAllowingStateLoss();
        mNavigationView.setCheckedItem(R.id.nav_first_fragment);
        setTitle(getString(R.string.app_name));
    }

    // after we have the list of hashtags, we display them
    private void displayTagFragment() {
        FragmentManager manager = getSupportFragmentManager();
        Fragment fragment = HashtagListFragment.newInstance(mColumnCount);
        manager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commitAllowingStateLoss();
        setTitle(getString(R.string.all_tags));
    }

    // after we have the list of posts, we display them
    private void displayPostFragment() {
        FragmentManager manager = getSupportFragmentManager();
        Fragment fragment = PostListFragment.newInstance(mColumnCount);
        manager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commitAllowingStateLoss();
    }

    /*
    -------------------------------------------------------------------------------------------
    END OF: RETRIEVING THE APPROPRIATE LIST DEPENDING ON WHAT USER CLICKED, THEN DISPLAY THEM
    -------------------------------------------------------------------------------------------
    */

    private void deletePost(String uid, String filePath) {
        mDatabase.getReference(POST_DB_PATH).child(uid).removeValue();
        mStorage.getReference().child(filePath).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                displayPostFragment();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(HomeActivity.this,
                        "Delete failed..",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void downloadFile(final String fileName){
        StorageReference storageRef = mStorage.getReference();
        StorageReference downloadRef = storageRef.child(fileName);
        final String path = DOWNLOAD_DIR+"/"+fileName;
        final File fileNameOnDevice = new File(path);

        Log.d(HOME_TAG, "FileName: " +fileName+ ", onDevice: " +fileNameOnDevice);
        downloadRef.getFile(fileNameOnDevice).addOnSuccessListener(
                new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                    Log.d(HOME_TAG, "downloaded the file");
                    Toast.makeText(HomeActivity.this,
                            "Downloaded the file.",
                            Toast.LENGTH_SHORT).show();
                    galleryAddPic(path);
                }
                }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    Log.d(HOME_TAG, "Failed to download the file");
                    Toast.makeText(HomeActivity.this,
                            "Couldn't be downloaded.",
                            Toast.LENGTH_SHORT).show();
                }
        });
    }

    // add the photo to the device's default gallery app
    private void galleryAddPic(String imageUrl) {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File file = new File(imageUrl);
        Uri contentUri = Uri.fromFile(file);
        mediaScanIntent.setData(contentUri);
        this.sendBroadcast(mediaScanIntent);
    }

}
