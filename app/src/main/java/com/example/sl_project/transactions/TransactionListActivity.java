package com.example.sl_project.transactions;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.expensetracker.R;
import com.example.sl_project.database.DatabaseHelper;
import com.example.sl_project.home.HomeActivity;
import com.example.sl_project.profile.ProfileActivity;
import com.example.sl_project.stats.StatisticsActivity;
import com.example.sl_project.utils.NavigationUtils;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;

public class TransactionListActivity extends AppCompatActivity implements TransactionAdapter.OnTransactionClickListener {

    private RecyclerView recyclerTransactions;
    private TransactionAdapter adapter;
    private List<Transaction> transactionList;
    private Button btnShowAll, btnShowIncome, btnShowExpense;
    private AppCompatButton btnAddNew;
    private DatabaseHelper dbHelper;
    private BottomNavigationView bottomNav;

    private String currentFilter = "all";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.transaction_list);

        dbHelper = new DatabaseHelper(this);

        recyclerTransactions = findViewById(R.id.recyclerTransactions);
        btnShowAll = findViewById(R.id.btnShowAll);
        btnShowIncome = findViewById(R.id.btnShowIncome);
        btnShowExpense = findViewById(R.id.btnShowExpense);
        btnAddNew = findViewById(R.id.btnAddNew);
        bottomNav = findViewById(R.id.bottomNav);

        transactionList = new ArrayList<>();
        adapter = new TransactionAdapter(this, transactionList, this);
        recyclerTransactions.setLayoutManager(new LinearLayoutManager(this));
        recyclerTransactions.setAdapter(adapter);

        loadTransactions(currentFilter);
        setupListeners();
        NavigationUtils.setupBottomNavigation(this, findViewById(R.id.bottomNav), R.id.nav_transactions);    }

    @Override
    protected void onResume() {
        super.onResume();
        loadTransactions(currentFilter);
    }

    private void setupListeners() {
        btnShowAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentFilter = "all";
                updateButtonStyles(btnShowAll);
                loadTransactions(currentFilter);
            }
        });

        btnShowIncome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentFilter = "Income";
                updateButtonStyles(btnShowIncome);
                loadTransactions(currentFilter);
            }
        });

        btnShowExpense.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentFilter = "Expense";
                updateButtonStyles(btnShowExpense);
                loadTransactions(currentFilter);
            }
        });

        btnAddNew.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TransactionListActivity.this, AddTransactions.class);
                startActivity(intent);
            }
        });
    }

    private void updateButtonStyles(Button selectedButton) {
        btnShowAll.setBackgroundResource(android.R.color.transparent);
        btnShowAll.setTextColor(getResources().getColor(android.R.color.darker_gray));

        btnShowIncome.setBackgroundResource(android.R.color.transparent);
        btnShowIncome.setTextColor(getResources().getColor(android.R.color.darker_gray));

        btnShowExpense.setBackgroundResource(android.R.color.transparent);
        btnShowExpense.setTextColor(getResources().getColor(android.R.color.darker_gray));

        selectedButton.setBackgroundResource(R.drawable.blue_btn);
        selectedButton.setTextColor(getResources().getColor(android.R.color.white));
    }

    private void loadTransactions(String filter) {
        transactionList.clear();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String query;
        String[] selectionArgs = null;

        if (filter.equals("Income")) {
            query = "SELECT * FROM " + DatabaseHelper.TABLE_TRANSACTIONS +
                    " WHERE " + DatabaseHelper.KEY_TRANSACTION_TYPE + "=? ORDER BY " +
                    DatabaseHelper.KEY_TRANSACTION_TIMESTAMP + " DESC";
            selectionArgs = new String[]{"Income"};
        } else if (filter.equals("Expense")) {
            query = "SELECT * FROM " + DatabaseHelper.TABLE_TRANSACTIONS +
                    " WHERE " + DatabaseHelper.KEY_TRANSACTION_TYPE + "=? ORDER BY " +
                    DatabaseHelper.KEY_TRANSACTION_TIMESTAMP + " DESC";
            selectionArgs = new String[]{"Expense"};
        } else {
            query = "SELECT * FROM " + DatabaseHelper.TABLE_TRANSACTIONS +
                    " ORDER BY " + DatabaseHelper.KEY_TRANSACTION_TIMESTAMP + " DESC";
        }

        Cursor cursor = db.rawQuery(query, selectionArgs);

        if (cursor.moveToFirst()) {
            do {
                long id = cursor.getLong(cursor.getColumnIndex(DatabaseHelper.KEY_TRANSACTION_ID));
                double amount = cursor.getDouble(cursor.getColumnIndex(DatabaseHelper.KEY_TRANSACTION_AMOUNT));
                String category = cursor.getString(cursor.getColumnIndex(DatabaseHelper.KEY_TRANSACTION_CATEGORY));
                String paymentMethod = cursor.getString(cursor.getColumnIndex(DatabaseHelper.KEY_TRANSACTION_PAYMENT_METHOD));
                String description = cursor.getString(cursor.getColumnIndex(DatabaseHelper.KEY_TRANSACTION_DESCRIPTION));
                long timestamp = cursor.getLong(cursor.getColumnIndex(DatabaseHelper.KEY_TRANSACTION_TIMESTAMP));
                String type = cursor.getString(cursor.getColumnIndex(DatabaseHelper.KEY_TRANSACTION_TYPE));

                Transaction transaction = new Transaction(id, amount, category, paymentMethod, description, timestamp, type);
                transactionList.add(transaction);
            } while (cursor.moveToNext());
        }

        cursor.close();
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onTransactionClick(final Transaction transaction) {
        // Show options in dialog
        String[] options = {"Edit", "Delete", "Cancel"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Transaction Options");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0: // Edit
                        editTransaction(transaction);
                        break;
                    case 1: // Delete
                        deleteTransaction(transaction);
                        break;
                    case 2: // Cancel
                        dialog.dismiss();
                        break;
                }
            }
        });
        builder.show();
    }

    private void editTransaction(Transaction transaction) {
        // Create intent to edit transaction
        Intent intent = new Intent(this, AddTransactions.class);
        intent.putExtra("TRANSACTION_ID", transaction.getId());
        intent.putExtra("TRANSACTION_AMOUNT", transaction.getAmount());
        intent.putExtra("TRANSACTION_CATEGORY", transaction.getCategory());
        intent.putExtra("TRANSACTION_PAYMENT_METHOD", transaction.getPaymentMethod());
        intent.putExtra("TRANSACTION_DESCRIPTION", transaction.getDescription());
        intent.putExtra("TRANSACTION_TIMESTAMP", transaction.getTimestamp());
        intent.putExtra("TRANSACTION_TYPE", transaction.getType());
        startActivity(intent);
    }

    private void deleteTransaction(final Transaction transaction) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Confirm Delete");
        builder.setMessage("Are you sure you want to delete this transaction?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                SQLiteDatabase db = dbHelper.getWritableDatabase();
                int rowsDeleted = db.delete(
                        DatabaseHelper.TABLE_TRANSACTIONS,
                        DatabaseHelper.KEY_TRANSACTION_ID + "=?",
                        new String[]{String.valueOf(transaction.getId())}
                );

                if (rowsDeleted > 0) {
                    Toast.makeText(TransactionListActivity.this, "Transaction deleted", Toast.LENGTH_SHORT).show();
                    loadTransactions(currentFilter);
                } else {
                    Toast.makeText(TransactionListActivity.this, "Error deleting transaction", Toast.LENGTH_SHORT).show();
                }
            }
        });
        builder.setNegativeButton("No", null);
        builder.show();
    }

    @Override
    protected void onDestroy() {
        dbHelper.close();
        super.onDestroy();
    }
}