package com.example.sl_project.profile;

import android.content.SharedPreferences;
import android.content.Context;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.expensetracker.R;

public class PersonalInfoActivity extends AppCompatActivity {

    private EditText etName, etEmail, etLocation;
    private Button btnSave;
    private ImageView backButton;

    private static final String PREF_NAME = "LoginPrefs";
    private static final String KEY_USER_EMAIL = "userEmail";
    private static final String KEY_USER_NAME = "userName";
    private static final String PROFILE_PREF_NAME = "UserProfile";

    private SharedPreferences loginPrefs;
    private SharedPreferences profilePrefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile_personal_info);

        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etLocation = findViewById(R.id.etLocation);
        btnSave = findViewById(R.id.btnSave);
        backButton = findViewById(R.id.backButton);

        loginPrefs = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        profilePrefs = getSharedPreferences(PROFILE_PREF_NAME, MODE_PRIVATE);

        loadUserInfo();

        backButton.setOnClickListener(v -> finish());

        btnSave.setOnClickListener(v -> saveUserInfo());
    }

    private void saveUserInfo() {
        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String location = etLocation.getText().toString().trim();

        if (name.isEmpty() || email.isEmpty() || location.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
        } else {
            SharedPreferences.Editor loginEditor = loginPrefs.edit();
            loginEditor.putString(KEY_USER_NAME, name);
            loginEditor.putString(KEY_USER_EMAIL, email);
            loginEditor.apply();

            SharedPreferences.Editor profileEditor = profilePrefs.edit();
            profileEditor.putString("name", name);
            profileEditor.putString("email", email);
            profileEditor.putString("location", location);
            profileEditor.apply();

            Toast.makeText(this, "Profile Updated!", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadUserInfo() {
        String name = loginPrefs.getString(KEY_USER_NAME, "");
        String email = loginPrefs.getString(KEY_USER_EMAIL, "");

        if (name.isEmpty()) {
            name = profilePrefs.getString("name", "User");
        }

        if (email.isEmpty()) {
            email = profilePrefs.getString("email", "user@example.com");
        }

        String location = profilePrefs.getString("location", "India");

        etName.setText(name);
        etEmail.setText(email);
        etLocation.setText(location);
    }
}


