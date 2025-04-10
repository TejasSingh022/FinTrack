package com.example.sl_project.profile;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Context;
import android.content.SharedPreferences;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.expensetracker.R;
import com.example.sl_project.home.HomeActivity;
import com.example.sl_project.login.LoginActivity;
import com.example.sl_project.profile.PersonalInfoActivity;
import com.example.sl_project.stats.StatisticsActivity;
import com.example.sl_project.transactions.AddTransactions;
import com.example.sl_project.transactions.TransactionListActivity;
import com.example.sl_project.utils.NavigationUtils;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import de.hdodenhof.circleimageview.CircleImageView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class ProfileActivity extends AppCompatActivity {

    private ImageView backButton;
    private CircleImageView profileImage;
    private TextView username, email;
    private LinearLayout personalInfoLayout, changePasswordLayout, inviteLayout;
    private LinearLayout contactUsLayout, logoutLayout;
    private BottomNavigationView bottomNav;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile);

        // Initialize UI components
        initViews();
        setupToolbar();
        loadUserData();
        setupClickListeners();
        NavigationUtils.setupBottomNavigation(this, findViewById(R.id.bottomNav), R.id.nav_profile);
//        bottomNav.setSelectedItemId(2131231097);
    }

    private void initViews() {
        backButton = findViewById(R.id.backButton);
        profileImage = findViewById(R.id.profileImage);
        username = findViewById(R.id.username);
        email = findViewById(R.id.email);
        personalInfoLayout = findViewById(R.id.personalInfoItem);
        changePasswordLayout = findViewById(R.id.changePasswordItem);
        inviteLayout = findViewById(R.id.inviteFriendsItem);
        contactUsLayout = findViewById(R.id.contactUsItem);
        logoutLayout = findViewById(R.id.logOutItem);
        bottomNav = findViewById(R.id.bottomNav);
    }

    private void setupToolbar() {
        backButton.setOnClickListener(v -> onBackPressed());
    }

    private void loadUserData() {
        // Get SharedPreferences for login data
        SharedPreferences loginPrefs = getSharedPreferences("LoginPrefs", Context.MODE_PRIVATE);

        // Get SharedPreferences for profile-specific data
        SharedPreferences profilePrefs = getSharedPreferences("UserProfile", Context.MODE_PRIVATE);

        // Get user data from SharedPreferences
        String userEmail = loginPrefs.getString("userEmail", "email@example.com");
        String userName = loginPrefs.getString("userName", "User");

        // Get profile image URI from profile preferences
        String profileImageUri = profilePrefs.getString("profileImageUri", null);

        // Load profile image from URI if available, otherwise use placeholder
        if (profileImageUri != null) {
            try {
                // Try to load from URI using Glide
                Glide.with(this)
                        .load(Uri.parse(profileImageUri))
                        .apply(new RequestOptions()
                                .placeholder(R.drawable.profile_placeholder)
                                .error(R.drawable.profile_placeholder))
                        .into(profileImage);
            } catch (Exception e) {
                // Fallback to placeholder if URI is invalid
                profileImage.setImageResource(R.drawable.profile_placeholder);
            }
        } else {
            // No saved image URI, use placeholder
            Glide.with(this)
                    .load(R.drawable.profile_placeholder)
                    .into(profileImage);
        }

        // Set username and email from SharedPreferences
        username.setText(userName);
        email.setText(userEmail);
    }

    private void setupClickListeners() {
        // Personal Information
        personalInfoLayout.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, PersonalInfoActivity.class);
            startActivity(intent);
        });

        // Change password
        changePasswordLayout.setOnClickListener(v -> showChangePasswordDialog());

        // Invite Friends
        inviteLayout.setOnClickListener(v -> shareInvite());

        // Contact Us
        contactUsLayout.setOnClickListener(v -> contactUs());

        // Logout
        logoutLayout.setOnClickListener(v -> logout());
    }

    private void showChangePasswordDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.profile_change_password, null);

        new MaterialAlertDialogBuilder(this)
                .setTitle("Change Password")
                .setView(dialogView)
                .setPositiveButton("Change", (dialog, which) -> {
                    // Process password change
                    // In a real app, validate inputs and submit to backend
                    Toast.makeText(ProfileActivity.this, "Password changed successfully", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void shareInvite() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name));

        String shareMessage = "Hey! Check out this awesome app: ";

        shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage);
        startActivity(Intent.createChooser(shareIntent, "Invite via"));
    }

    private void contactUs() {
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:"));
        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{"support@example.com"});

        try {
            startActivity(Intent.createChooser(intent, "Contact Us via Email"));
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(this, "No email client found.", Toast.LENGTH_SHORT).show();
        }
    }

    private void logout() {
        // Show confirmation dialog
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    LoginActivity.clearLoginState(this);

                    Toast.makeText(ProfileActivity.this, "Logged out successfully", Toast.LENGTH_SHORT).show();

                    // Redirect to login
                    Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("No", null)
                .show();
    }
}