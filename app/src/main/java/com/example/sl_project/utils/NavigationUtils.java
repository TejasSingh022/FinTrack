package com.example.sl_project.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import com.example.sl_project.home.HomeActivity;
import com.example.sl_project.profile.ProfileActivity;
import com.example.sl_project.stats.StatisticsActivity;
import com.example.sl_project.transactions.AddTransactions;
import com.example.sl_project.transactions.TransactionListActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.example.expensetracker.R;

public class NavigationUtils {

    public static void setupBottomNavigation(Context context, BottomNavigationView bottomNav, int currentNavId) {
        bottomNav.setSelectedItemId(currentNavId);

        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == currentNavId) {
                return true; // Already on this tab
            }

            Intent intent = null;

            if (itemId == R.id.nav_home) {
                intent = new Intent(context, HomeActivity.class);
            } else if (itemId == R.id.nav_add) {
                intent = new Intent(context, AddTransactions.class);
            } else if (itemId == R.id.nav_transactions) {
                intent = new Intent(context, TransactionListActivity.class);
            } else if (itemId == R.id.nav_statistics) {
                intent = new Intent(context, StatisticsActivity.class);
            } else if (itemId == R.id.nav_profile) {
                intent = new Intent(context, ProfileActivity.class);
            }

            if (intent != null) {
                intent.putExtra("SELECTED_NAV_ID", itemId);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                context.startActivity(intent);
                if (context instanceof Activity) {
                    ((Activity) context).finish();
                }
                return true;
            }

            return false;
        });
    }
}

