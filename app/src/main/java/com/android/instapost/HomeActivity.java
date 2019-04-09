package com.android.instapost;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

import java.util.List;


public class HomeActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, UserListFragment.OnUserListFragmentInteractionListener, PostListFragment.OnPostListFragmentInteractionListener {
    private static final String HOME_TAG = "HomeActivity";
    private static final int REQUEST_CREATE_POST = 0;
    private static final String EXTRA_NAME = "com.android.instapost.name";
    private static final String EXTRA_USERNAME = "com.android.instapost.username";
    private static final String USER_DB_PATH = "user";
    private static final String TAG_DB_PATH = "tag";
    private static final String POST_DB_PATH = "post";
    private static final String USERNAME_DB_PATH = "mUsername";
    private static final String ID_DB_PATH = "mId";

    private FirebaseDatabase mDatabase;
    private DrawerLayout mDrawerLayout;
    private Toolbar mToolbar;
    private NavigationView mNavigationView;
    private ActionBarDrawerToggle mDrawerToggle;
    private TextView mHeaderView, mNameView, mUserNameView, mEmailView;
    private AppCompatButton mLogoutButton;
    private String mName, mUserName, mEmail;
    private Uri mPhotoUri;

    private int mColumnCount = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        mDatabase = FirebaseDatabase.getInstance();

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
                Toast.makeText(HomeActivity.this, "Drawer Fragment 1 Selected",
                        Toast.LENGTH_SHORT).show();
                mDrawerLayout.closeDrawer(GravityCompat.START);
                retrieveUserList();
                return true;
            case R.id.nav_second_fragment:
                Toast.makeText(HomeActivity.this, "Drawer Fragment 2 Selected",
                        Toast.LENGTH_SHORT).show();
                mDrawerLayout.closeDrawer(GravityCompat.START);
                retrieveTagList();
                return true;
            case R.id.nav_third_fragment:
                Toast.makeText(HomeActivity.this, "Drawer Fragment 3 Selected",
                        Toast.LENGTH_SHORT).show();
                mDrawerLayout.closeDrawer(GravityCompat.START);
                changeColumnCount();
                displayUserFragment();
                return true;
        }
        return true;
    }

    @Override
    public void onUserListFragmentInteraction(User item) {
        // TODO: handle interaction for UserListFragment
        Toast.makeText(HomeActivity.this, "Item Selected " + item.mUsername,
                Toast.LENGTH_SHORT).show();
        retrievePostList(item.mUsername, USERNAME_DB_PATH);
    }

    @Override
    public void onPostListFragmentInteraction(Post item) {
        // TODO: handle interaction for PostListFragment
        Toast.makeText(HomeActivity.this, "Item Selected " + item.mCaption,
                Toast.LENGTH_SHORT).show();
    }

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

    private void getUserInfo() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            // Name, email address, and profile photo Url
            mEmail = user.getEmail();
            mPhotoUri = user.getPhotoUrl();

            // Check if user's email is verified
            boolean emailVerified = user.isEmailVerified();

            // Get the Username, based on the uid
            String uid = user.getUid();
            Log.d(HOME_TAG, "UID: " +uid);
            DatabaseReference userTable = mDatabase.getReference();
            Query query =
                    userTable.child(USER_DB_PATH).orderByChild(ID_DB_PATH).equalTo(uid);
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

    private void createPost() {
        Intent intent = new Intent(HomeActivity.this, CreatePostActivity.class);
        intent.putExtra(EXTRA_USERNAME, mUserName);
        startActivityForResult(intent, REQUEST_CREATE_POST);
    }

    private void logout() {
        FirebaseAuth.getInstance().signOut();
        startActivity(new Intent(HomeActivity.this, LoginActivity.class));
        finish();
    }

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
                List<String> hashtagList = list.getHastags();
                for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                    String hashtag = postSnapshot.child("mTag").getValue(String.class);
                    if (!hashtagList.contains(hashtag)) {
                        hashtagList.add(hashtag);
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

    private void changeColumnCount(){
        if (mColumnCount == 1) {
            mColumnCount = 2;
        }
        else {
            mColumnCount = 1;
        }
    }

    // after we have the list of users, we display them
    private void displayUserFragment() {
        FragmentManager manager = getSupportFragmentManager();
        Fragment fragment = UserListFragment.newInstance(mColumnCount);
        manager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commitAllowingStateLoss();
    }

    // after we have the list of hashtags, we display them
    private void displayTagFragment() {
        // TODO: implement TagListFragment
    }

    // after we have the list of posts, we display them
    private void displayPostFragment() {
        FragmentManager manager = getSupportFragmentManager();
        Fragment fragment = PostListFragment.newInstance(mColumnCount);
        manager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commitAllowingStateLoss();
    }

}
