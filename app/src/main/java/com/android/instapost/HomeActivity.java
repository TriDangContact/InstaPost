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
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.android.instapost.dummy.DummyContent;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


public class HomeActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, UserListFragment.OnUserListFragmentInteractionListener {
    private static final String EXTRA_NAME = "com.android.instapost.name";
    private static final String EXTRA_USERNAME = "com.android.instapost.username";
    private static final String DISPLAY_USERS = "user";
    private static final String DISPLAY_TAGS = "tags";

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
        addFragment(DISPLAY_USERS);


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
                addFragment(DISPLAY_USERS);
                return true;
            case R.id.nav_second_fragment:
                Toast.makeText(HomeActivity.this, "Drawer Fragment 2 Selected",
                        Toast.LENGTH_SHORT).show();
                mDrawerLayout.closeDrawer(GravityCompat.START);
                addFragment(DISPLAY_TAGS);
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
    public void onUserListFragmentInteraction(DummyContent.DummyItem item) {
        // TODO: handle interaction for UserListFragment
        Toast.makeText(HomeActivity.this, "Item Selected" + item.id,
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

    private void addFragment(String listDisplayed) {
        FragmentManager manager = getSupportFragmentManager();
        Fragment fragment = UserListFragment.newInstance(1, listDisplayed);
        manager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit();
    }

}
