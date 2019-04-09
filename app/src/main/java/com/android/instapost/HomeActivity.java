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


public class HomeActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, UserListFragment.OnUserListFragmentInteractionListener {
    private static final String HOME_TAG = "HomeActivity";
    private static final String EXTRA_NAME = "com.android.instapost.name";
    private static final String EXTRA_USERNAME = "com.android.instapost.username";
    private static final String USER_DB_PATH = "user";
    private static final String TAG_DB_PATH = "tag";
    private static final String USERNAME_DB_PATH = "mUsername";
    private static final int COLUMN_COUNT = 1;

    private DrawerLayout mDrawerLayout;
    private Toolbar mToolbar;
    private NavigationView mNavigationView;
    private ActionBarDrawerToggle mDrawerToggle;

    private TextView mNameView, mUserNameView, mEmailView;
    private AppCompatButton mLogoutButton;
    private String mName, mUserName, mEmail;
    private Uri mPhotoUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
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
                return true;
        }
        return true;
    }

    @Override
    public void onUserListFragmentInteraction(User item) {
        // TODO: handle interaction for UserListFragment
        Toast.makeText(HomeActivity.this, "Item Selected " + item.mUsername,
                Toast.LENGTH_SHORT).show();
    }


    private void getUserInfo() {
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            mName = extras.getString(EXTRA_NAME);
            mUserName = extras.getString(EXTRA_USERNAME);
        }

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            // Name, email address, and profile photo Url
            //mName = user.getDisplayName();
            mEmail = user.getEmail();
            mPhotoUri = user.getPhotoUrl();

            // Check if user's email is verified
            boolean emailVerified = user.isEmailVerified();

            // The user's ID, unique to the Firebase project. Do NOT use this value to
            // authenticate with your backend server, if you have one. Use
            // FirebaseUser.getIdToken() instead.
            String uid = user.getUid();
        }
    }

    private void createPost() {
        Intent intent = new Intent(HomeActivity.this, CreatePostActivity.class);
        startActivity(intent);
    }

    private void logout() {
        FirebaseAuth.getInstance().signOut();
        startActivity(new Intent(HomeActivity.this, LoginActivity.class));
        finish();
    }

    private void retrieveUserList() {
        // TODO: implement query to retrieve all users from db
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference reference = database.getReference(USER_DB_PATH);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                    User user = postSnapshot.getValue(User.class);
                    if (!ContentLists.mUserArrayList.contains(user)) {
                        ContentLists.mUserArrayList.add(user);
                    }
                }
                updateFragment();
            }
            @Override
            public void onCancelled(DatabaseError error) {
                Log.d(HOME_TAG, error.getMessage());
            }
        });

    }

    // get all the hashtags from tag table in firebase, and add them to the recylerview list
    private void retrieveTagList() {
        // TODO: implement query to retrieve all hashtags from db
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference reference = database.getReference(TAG_DB_PATH);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                    String hashtag = postSnapshot.child("mTag").getValue(String.class);
                    if (!ContentLists.mHashtagArrayList.contains(hashtag)) {
                        ContentLists.mHashtagArrayList.add(hashtag);
                    }
                }
                updateFragment();
            }
            @Override
            public void onCancelled(DatabaseError error) {
                Log.d(HOME_TAG, error.getMessage());
            }
        });
    }

    private void updateFragment() {
        FragmentManager manager = getSupportFragmentManager();
        Fragment fragment = UserListFragment.newInstance(COLUMN_COUNT);
        manager.beginTransaction()
            .add(R.id.fragment_container, fragment)
            .commitAllowingStateLoss();
    }

}
