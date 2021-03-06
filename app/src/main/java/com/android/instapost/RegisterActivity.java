package com.android.instapost;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegisterActivity extends AppCompatActivity {

    private static final String REGISTER_TAG = "RegisterActivity";
    private static final String EXTRA_NAME = "com.android.instapost.name";
    private static final String EXTRA_USERNAME = "com.android.instapost.username";
    private static final String EXTRA_EMAIL = "com.android.instapost.email";
    private static final String USER_DB_PATH = "user";
    private static final String USERNAME_DB_PATH = "mUsername";

    private FirebaseAuth mAuth;
    private FirebaseDatabase mDatabase;
    private EditText mNameText;
    private EditText mUserNameText;
    private EditText mEmailText;
    private EditText mPasswordText;
    private AppCompatButton mCreateAccountButton;
    private TextView mLoginLink;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance();

        mNameText = (EditText) findViewById(R.id.register_input_name);
        mUserNameText = (EditText) findViewById(R.id.register_input_username);
        mEmailText = (EditText) findViewById(R.id.register_input_email);
        mPasswordText = (EditText) findViewById(R.id.register_input_password);
        mCreateAccountButton = (AppCompatButton) findViewById(R.id.register_btn_signup);
        mLoginLink = (TextView) findViewById(R.id.register_link_login);

        mCreateAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validate()) {
                    checkUserNameExist(mUserNameText.getText().toString());
                }
            }
        });

        mLoginLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private boolean validate() {
        boolean valid = true;

        String name = mNameText.getText().toString().trim();
        String username = mUserNameText.getText().toString().trim();
        String email = mEmailText.getText().toString().trim();
        String password = mPasswordText.getText().toString().trim();

        if (name.isEmpty()) {
            mNameText.setError(getString(R.string.invalid_name));
            valid = false;
        } else {
            mNameText.setError(null);
        }
        if (username.isEmpty()) {
            mUserNameText.setError(getString(R.string.invalid_username));
            valid = false;
        } else {
            mUserNameText.setError(null);
        }
        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            mEmailText.setError(getString(R.string.invalid_email));
            valid = false;
        } else {
            mEmailText.setError(null);
        }

        if (password.isEmpty() || !isValidPassword(password)) {
            mPasswordText.setError(getString(R.string.invalid_password));
            valid = false;
        } else {
            mPasswordText.setError(null);
        }

        return valid;
    }

    private void checkUserNameExist(String username) {
        DatabaseReference usernameTable = mDatabase.getReference();
        Query query = usernameTable.child(USER_DB_PATH).orderByChild(USERNAME_DB_PATH).equalTo(username);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    createAccount();
                }
                else {
                    Toast.makeText(RegisterActivity.this, R.string.username_exists,
                            Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onCancelled(DatabaseError error) {
                Log.d(REGISTER_TAG, error.getMessage());
            }
        });
    }


    private void createAccount() {
        final String email = mEmailText.getText().toString().trim();
        final String password = mPasswordText.getText().toString().trim();
        final String name = mNameText.getText().toString().trim();
        final String username = mUserNameText.getText().toString().trim();

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Registration successful, return to login screen
                            Intent intent = new Intent(getApplicationContext(), RegisterActivity.class);
                            intent.putExtra(EXTRA_NAME, name);
                            intent.putExtra(EXTRA_USERNAME, username);
                            intent.putExtra(EXTRA_EMAIL, email);
                            setResult(RESULT_OK, intent);
                            finish();
                        } else {
                            // If registration fails, display a message to the user.
                            Log.w(REGISTER_TAG, "createUserWithEmail:failure", task.getException());
                            Toast.makeText(RegisterActivity.this, R.string.registration_failed,
                                    Toast.LENGTH_SHORT).show();
                        }
                        // ...
                    }
                });
    }

    public boolean isValidPassword(final String password) {
        Pattern pattern;
        Matcher matcher;
        //At least 1 digit, 1 uppercase, no white spaces in between, at least 6 characters
        final String PASSWORD_PATTERN = "^(?=.*[0-9])(?=.*[A-Z])(?=\\S+$).{6,}$";

        pattern = Pattern.compile(PASSWORD_PATTERN);
        matcher = pattern.matcher(password);

        return matcher.matches();
    }

}
