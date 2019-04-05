package com.android.instapost;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatButton;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class LoginActivity extends AppCompatActivity {
    private static final String LOGIN_TAG = "LoginActivity";
    private static final String EXTRA_NAME = "com.android.instapost.name";
    private static final String EXTRA_USERNAME = "com.android.instapost.username";
    private static final String EXTRA_EMAIL = "com.android.instapost.email";
    private static final String USER_DB_PATH = "user";

    private static final int REQUEST_REGISTER = 0;

    private FirebaseAuth mAuth;
    private FirebaseDatabase mDatabase;

    private EditText mEmailText;
    private EditText mPasswordText;
    private AppCompatButton mLoginButton;
    private TextView mRegisterLink;

    private String mName;
    private String mUserName;
    private String mEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance();
        mEmailText = (EditText) findViewById(R.id.login_input_email);
        mPasswordText = (EditText) findViewById(R.id.login_input_password);
        mLoginButton = (AppCompatButton) findViewById(R.id.login_btn_login);
        mRegisterLink = (TextView) findViewById(R.id.login_link_signup);

        mLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signIn(mEmailText.getText().toString(), mPasswordText.getText().toString());
            }
        });

        mRegisterLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), RegisterActivity.class);
                startActivityForResult(intent, REQUEST_REGISTER);
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser!=null) {
            goToHomeActivity(currentUser);
        }
    }

    //this is called when a user registration is successful
    //need to grab their account info and store it
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_REGISTER) {
            if (resultCode == RESULT_OK) {

                // TODO: Implement successful signup logic here
                FirebaseUser user = mAuth.getCurrentUser();
                Bundle extras = data.getExtras();
                mName = extras.getString(EXTRA_NAME);
                mUserName = extras.getString(EXTRA_USERNAME);
                mEmail = extras.getString(EXTRA_EMAIL);

                writeNewUser(user.getUid(), mUserName, mName, mEmail);
                Toast.makeText(LoginActivity.this, R.string.registration_success,
                        Toast.LENGTH_SHORT).show();

                goToHomeActivity(user);
                // By default we just finish the Activity and log them in automatically
                //this.finish();
            }
        }
    }

    //if signed in, go to home activity
    private void goToHomeActivity(FirebaseUser user) {
        Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
        finish();
        startActivity(intent);
    }

    private void signIn(String email, String password) {
        if (validate(email, password)) {
            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                // Sign in success, update UI with the signed-in user's information
                                Log.d(LOGIN_TAG, "signInWithEmail:success");
                                FirebaseUser user = mAuth.getCurrentUser();
                                goToHomeActivity(user);
                            } else {
                                // If sign in fails, display a message to the user.
                                Log.w(LOGIN_TAG, "signInWithEmail:failure", task.getException());
                                Toast.makeText(LoginActivity.this, R.string.login_failed,
                                        Toast.LENGTH_SHORT).show();
                            }
                            // ...
                        }
                    });
        }
        else {
            Toast.makeText(LoginActivity.this, R.string.login_failed,
                    Toast.LENGTH_SHORT).show();
        }
    }

    private boolean validate(String email, String password) {
        boolean valid = true;

        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            mEmailText.setError(getString(R.string.invalid_email));
            valid = false;
        } else {
            mEmailText.setError(null);
        }

        if (password.isEmpty()) {
            mPasswordText.setError(getString(R.string.invalid_password));
            valid = false;
        } else {
            mPasswordText.setError(null);
        }

        return valid;
    }


    private void writeNewUser(String uid, String username, String name, String email) {
        DatabaseReference userTable = mDatabase.getReference(USER_DB_PATH);
        User user = new User(username, name, email);
        userTable.child(uid).setValue(user);
    }

}
