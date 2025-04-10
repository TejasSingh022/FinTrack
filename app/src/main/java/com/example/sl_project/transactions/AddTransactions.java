package com.example.sl_project.transactions;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.example.expensetracker.R;
import com.example.sl_project.database.DatabaseHelper;
import com.example.sl_project.home.HomeActivity;
import com.example.sl_project.profile.ProfileActivity;
import com.example.sl_project.stats.StatisticsActivity;
import com.example.sl_project.utils.NavigationUtils;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class AddTransactions extends AppCompatActivity {

    // UI components
    private Button btnIncome, btnExpense;
    private EditText editAmount, editDescription;
    private Spinner spinnerCategory, spinnerAccount;
    private AppCompatButton btnDateTime, btnSave;
    private BottomNavigationView bottomNav;

    // Transaction data
    private String transactionType = "Expense";
    private Calendar selectedDateTime = Calendar.getInstance();

    // Category arrays
    private String[] incomeCategories = {"Salary", "Freelance", "Gift", "Other"};
    private String[] expenseCategories = {"Food", "Travel", "Rent", "Shopping", "Other"};
    private String[] paymentMethods = {"Cash", "Debit Card", "Credit Card", "UPI", "Other"};

    // Database helper
    private DatabaseHelper dbHelper;

    // Edit mode
    private boolean isEditMode = false;
    private long editTransactionId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.transaction_add);

        // Initialize database helper
        dbHelper = new DatabaseHelper(this);

        // Initialize views
        btnIncome = findViewById(R.id.btnIncome);
        btnExpense = findViewById(R.id.btnExpense);
        editAmount = findViewById(R.id.editAmount);
        editDescription = findViewById(R.id.editDescription);
        spinnerCategory = findViewById(R.id.spinnerCategory);
        spinnerAccount = findViewById(R.id.spinnerAccount);
        btnDateTime = findViewById(R.id.btnDateTime);
        btnSave = findViewById(R.id.btnSave);
        bottomNav = findViewById(R.id.bottomNav);

        // Set up spinners
        setupSpinners();

        // Set click listeners
        setupListeners();

        // Check if we're editing an existing transaction
        handleEditIntent();
        NavigationUtils.setupBottomNavigation(this, findViewById(R.id.bottomNav), R.id.nav_add);
    }

    private void handleEditIntent() {
        Intent intent = getIntent();
        if (intent.hasExtra("TRANSACTION_ID")) {
            isEditMode = true;
            editTransactionId = intent.getLongExtra("TRANSACTION_ID", -1);

            // Set transaction type
            transactionType = intent.getStringExtra("TRANSACTION_TYPE");
            if (transactionType.equals("Income")) {
                btnIncome.performClick();
            } else {
                btnExpense.performClick();
            }

            // Fill form fields
            editAmount.setText(String.valueOf(intent.getDoubleExtra("TRANSACTION_AMOUNT", 0)));
            editDescription.setText(intent.getStringExtra("TRANSACTION_DESCRIPTION"));

            // Set date time
            long timestamp = intent.getLongExtra("TRANSACTION_TIMESTAMP", System.currentTimeMillis());
            selectedDateTime.setTimeInMillis(timestamp);
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault());
            btnDateTime.setText(dateFormat.format(selectedDateTime.getTime()));

            // Set category and payment method
            String category = intent.getStringExtra("TRANSACTION_CATEGORY");
            String paymentMethod = intent.getStringExtra("TRANSACTION_PAYMENT_METHOD");

            spinnerAccount.post(new Runnable() {
                @Override
                public void run() {
                    // Set payment method selection
                    for (int i = 0; i < paymentMethods.length; i++) {
                        if (paymentMethods[i].equals(paymentMethod)) {
                            spinnerAccount.setSelection(i);
                            break;
                        }
                    }

                    // Set category selection
                    String[] categories = transactionType.equals("Income") ? incomeCategories : expenseCategories;
                    for (int i = 0; i < categories.length; i++) {
                        if (categories[i].equals(category)) {
                            spinnerCategory.setSelection(i);
                            break;
                        }
                    }
                }
            });

            btnSave.setText("Update Transaction");
        }
    }

    private void setupSpinners() {
        // Set up expense categories by default
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                expenseCategories);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(categoryAdapter);

        // Set up payment methods
        ArrayAdapter<String> paymentAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                paymentMethods);
        paymentAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerAccount.setAdapter(paymentAdapter);
    }

    private void setupListeners() {
        // Income button
        btnIncome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                transactionType = "Income";
                btnIncome.setTextColor(getResources().getColor(R.color.green));
                btnExpense.setTextColor(getResources().getColor(android.R.color.darker_gray));

                // Update categories
                ArrayAdapter<String> adapter = new ArrayAdapter<>(
                        AddTransactions.this,
                        android.R.layout.simple_spinner_item,
                        incomeCategories);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerCategory.setAdapter(adapter);
            }
        });

        // Expense button
        btnExpense.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                transactionType = "Expense";
                btnExpense.setTextColor(getResources().getColor(R.color.red));
                btnIncome.setTextColor(getResources().getColor(android.R.color.darker_gray));

                // Update categories
                ArrayAdapter<String> adapter = new ArrayAdapter<>(
                        AddTransactions.this,
                        android.R.layout.simple_spinner_item,
                        expenseCategories);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerCategory.setAdapter(adapter);
            }
        });

        // Date & Time button
        btnDateTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDateTimePicker();
            }
        });

        // Save button
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveTransaction();
            }
        });
    }

    private void showDateTimePicker() {
        final Calendar currentDate = Calendar.getInstance();

        // Show date picker
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(android.widget.DatePicker view, int year, int month, int dayOfMonth) {
                        selectedDateTime.set(Calendar.YEAR, year);
                        selectedDateTime.set(Calendar.MONTH, month);
                        selectedDateTime.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                        // After date is set, show time picker
                        TimePickerDialog timePickerDialog = new TimePickerDialog(
                                AddTransactions.this,
                                new TimePickerDialog.OnTimeSetListener() {
                                    @Override
                                    public void onTimeSet(android.widget.TimePicker view, int hourOfDay, int minute) {
                                        selectedDateTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                                        selectedDateTime.set(Calendar.MINUTE, minute);

                                        // Update button text
                                        SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault());
                                        btnDateTime.setText(dateFormat.format(selectedDateTime.getTime()));
                                    }
                                },
                                currentDate.get(Calendar.HOUR_OF_DAY),
                                currentDate.get(Calendar.MINUTE),
                                false
                        );
                        timePickerDialog.show();
                    }
                },
                currentDate.get(Calendar.YEAR),
                currentDate.get(Calendar.MONTH),
                currentDate.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    private void saveTransaction() {
        // Validate input
        if (editAmount.getText().toString().isEmpty()) {
            editAmount.setError("Please enter an amount");
            return;
        }

        // Get form data
        String amount = editAmount.getText().toString();
        String category = spinnerCategory.getSelectedItem().toString();
        String paymentMethod = spinnerAccount.getSelectedItem().toString();
        String description = editDescription.getText().toString();

        // Create ContentValues
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.KEY_TRANSACTION_AMOUNT, Double.parseDouble(amount));
        values.put(DatabaseHelper.KEY_TRANSACTION_CATEGORY, category);
        values.put(DatabaseHelper.KEY_TRANSACTION_PAYMENT_METHOD, paymentMethod);
        values.put(DatabaseHelper.KEY_TRANSACTION_DESCRIPTION, description);
        values.put(DatabaseHelper.KEY_TRANSACTION_TIMESTAMP, selectedDateTime.getTimeInMillis());
        values.put(DatabaseHelper.KEY_TRANSACTION_TYPE, transactionType);

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        long result = -1;

        if (isEditMode) {
            // Update existing transaction
            result = db.update(
                    DatabaseHelper.TABLE_TRANSACTIONS,
                    values,
                    DatabaseHelper.KEY_TRANSACTION_ID + "=?",
                    new String[]{String.valueOf(editTransactionId)}
            );

            if (result > 0) {
                Toast.makeText(this, "Transaction updated successfully", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Error updating transaction", Toast.LENGTH_SHORT).show();
            }
        } else {
            // Insert new transaction
            result = db.insert(DatabaseHelper.TABLE_TRANSACTIONS, null, values);

            if (result != -1) {
                Toast.makeText(this, "Saved " + transactionType + ": ₹" + amount, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Error saving transaction", Toast.LENGTH_SHORT).show();
            }
        }

        db.close();

        if (result != -1) {
            // Reset form
            if (!isEditMode) {
                editAmount.setText("");
                editDescription.setText("");
                btnDateTime.setText("Select Date and Time");
            }

            // Launch TransactionListActivity to show all transactions
            Intent intent = new Intent(AddTransactions.this, TransactionListActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        dbHelper.close();
        super.onDestroy();
    }
}