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

import com.example.expensetracker.R;
import com.example.sl_project.home.HomeActivity;
import com.example.sl_project.login.LoginActivity;
import com.example.sl_project.stats.StatisticsActivity;
import com.example.sl_project.transactions.AddTransactions;
import com.example.sl_project.transactions.TransactionListActivity;
import com.example.sl_project.utils.NavigationUtils;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class ProfileActivity extends AppCompatActivity {

    private ImageView backButton;
    private TextView username, email;
    private LinearLayout personalInfoLayout, changePasswordLayout, inviteLayout;
    private LinearLayout contactUsLayout, logoutLayout;
    private BottomNavigationView bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile);

        initViews();
        setupToolbar();
        loadUserData();
        setupClickListeners();
        NavigationUtils.setupBottomNavigation(this, findViewById(R.id.bottomNav), R.id.nav_profile);
    }

    private void initViews() {
        backButton = findViewById(R.id.backButton);
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
        SharedPreferences loginPrefs = getSharedPreferences("LoginPrefs", Context.MODE_PRIVATE);
        String userEmail = loginPrefs.getString("userEmail", "email@example.com");
        String userName = loginPrefs.getString("userName", "User");

        username.setText(userName);
        email.setText(userEmail);
    }

    private void setupClickListeners() {
        personalInfoLayout.setOnClickListener(v -> startActivity(new Intent(ProfileActivity.this, PersonalInfoActivity.class)));
        changePasswordLayout.setOnClickListener(v -> showChangePasswordDialog());
        inviteLayout.setOnClickListener(v -> shareInvite());
        contactUsLayout.setOnClickListener(v -> contactUs());
        logoutLayout.setOnClickListener(v -> logout());
    }

    private void showChangePasswordDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.profile_change_password, null);
        new MaterialAlertDialogBuilder(this)
                .setTitle("Change Password")
                .setView(dialogView)
                .setPositiveButton("Change", (dialog, which) ->
                    Toast.makeText(ProfileActivity.this, "Password changed successfully", Toast.LENGTH_SHORT).show())
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadUserData();
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
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    LoginActivity.clearLoginState(this);
                    Toast.makeText(ProfileActivity.this, "Logged out successfully", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("No", null)
                .show();
    }
}
