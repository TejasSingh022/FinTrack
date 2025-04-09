package com.example.sl_project.stats;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.example.expensetracker.R;
import com.example.sl_project.database.DatabaseHelper;
import com.example.sl_project.home.HomeActivity;
import com.example.sl_project.profile.ProfileActivity;
import com.example.sl_project.transactions.AddTransactions;
import com.example.sl_project.transactions.TransactionListActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.tabs.TabLayout;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.PercentFormatter;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StatisticsActivity extends AppCompatActivity {

    private TabLayout tabLayout;
    private TextView filterDaily, filterWeekly, filterMonthly, filterYearly;
    private View timeFilterIndicator;
    private PieChart pieChart;
    private ImageView backButton;
    private BottomNavigationView bottomNav;
    private BarChart barChart;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.statistics);

        dbHelper = new DatabaseHelper(this);
        initViews();
        setupListeners();
        setupData();
        setupBottomNavigation();
    }

    private void initViews() {
        backButton = findViewById(R.id.backButton);
        tabLayout = findViewById(R.id.tabLayout);
        filterDaily = findViewById(R.id.filterDaily);
        filterWeekly = findViewById(R.id.filterWeekly);
        filterMonthly = findViewById(R.id.filterMonthly);
        filterYearly = findViewById(R.id.filterYearly);
        timeFilterIndicator = findViewById(R.id.timeFilterIndicator);
        pieChart = findViewById(R.id.expensePieChart);
        bottomNav = findViewById(R.id.bottomNav);
        barChart = findViewById(R.id.barChart);
    }

    private void setupListeners() {
        backButton.setOnClickListener(v -> finish());

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override public void onTabSelected(TabLayout.Tab tab) { updateChartData(tab.getPosition()); }
            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });

        View.OnClickListener timeFilterClickListener = v -> {
            filterDaily.setTextColor(getResources().getColor(R.color.grey));
            filterWeekly.setTextColor(getResources().getColor(R.color.grey));
            filterMonthly.setTextColor(getResources().getColor(R.color.grey));
            filterYearly.setTextColor(getResources().getColor(R.color.grey));

            TextView selected = (TextView) v;
            selected.setTextColor(getResources().getColor(R.color.nav_icon_color));
            int indicatorWidth = timeFilterIndicator.getWidth();
            timeFilterIndicator.setTranslationX(selected.getLeft() + selected.getWidth()/2 - indicatorWidth/2);

            updateTimeRange(selected.getText().toString());
        };

        filterDaily.setOnClickListener(timeFilterClickListener);
        filterWeekly.setOnClickListener(timeFilterClickListener);
        filterMonthly.setOnClickListener(timeFilterClickListener);
        filterYearly.setOnClickListener(timeFilterClickListener);

        timeFilterIndicator.post(() -> {
            int indicatorWidth = timeFilterIndicator.getWidth();
            timeFilterIndicator.setTranslationX(filterMonthly.getLeft() + filterMonthly.getWidth()/2 - indicatorWidth/2);
            updateTimeRange("Monthly");
        });
    }

    private void setupData() {
        updateChartData(tabLayout.getSelectedTabPosition());
    }

    private void updateChartData(int tabPosition) {
        String transactionType = (tabPosition == 0) ? "Expense" : "Income";
        Map<String, Double> categoryData = fetchCategoryData(transactionType);
        updatePieChartWithData(categoryData);
        loadBarChartData(getCurrentTimeRange(), tabPosition);
    }

    private String getCurrentTimeRange() {
        int highlightColor = getResources().getColor(R.color.nav_icon_color);
        if (filterDaily.getCurrentTextColor() == highlightColor) return "Daily";
        else if (filterWeekly.getCurrentTextColor() == highlightColor) return "Weekly";
        else if (filterMonthly.getCurrentTextColor() == highlightColor) return "Monthly";
        else if (filterYearly.getCurrentTextColor() == highlightColor) return "Yearly";
        else return "Monthly";
    }

    private Map<String, Double> fetchCategoryData(String transactionType) {
        Map<String, Double> categoryAmounts = new HashMap<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT category, SUM(amount) FROM transactions WHERE type = ? GROUP BY category", new String[]{transactionType});
        if (cursor.moveToFirst()) {
            do {
                categoryAmounts.put(cursor.getString(0), cursor.getDouble(1));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return categoryAmounts;
    }

    private void updatePieChartWithData(Map<String, Double> categoryData) {
        ArrayList<PieEntry> entries = new ArrayList<>();
        List<Integer> colors = new ArrayList<>();
        int[] colorResources = { R.color.red, R.color.blue, R.color.green, R.color.black };
        int colorIndex = 0;

        for (Map.Entry<String, Double> entry : categoryData.entrySet()) {
            entries.add(new PieEntry(entry.getValue().floatValue(), entry.getKey()));
            colors.add(getResources().getColor(colorResources[colorIndex % colorResources.length]));
            colorIndex++;
        }

        if (entries.isEmpty()) {
            entries.add(new PieEntry(1f, "No Data"));
            colors.add(getResources().getColor(R.color.grey));
        }

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(colors);
        dataSet.setDrawValues(false);
        dataSet.setSliceSpace(3f);

        PieData data = new PieData(dataSet);
        data.setValueFormatter(new PercentFormatter(pieChart));
        data.setValueTextSize(11f);
        data.setValueTextColor(Color.WHITE);

        pieChart.setData(data);
        pieChart.getDescription().setEnabled(false);
        pieChart.setUsePercentValues(false);
        pieChart.animateY(1000);
        pieChart.setDrawEntryLabels(false);
        pieChart.getLegend().setEnabled(true);
        pieChart.invalidate();
    }

    private void loadBarChartData(String timeRange, int tabPosition) {
        List<BarEntry> entries = new ArrayList<>();
        List<String> labels = new ArrayList<>();

        boolean isExpense = (tabPosition == 0);
        String transactionType = isExpense ? "Expense" : "Income";
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        long currentTime = System.currentTimeMillis();
        long dayMillis = 24 * 60 * 60 * 1000L;
        long weekMillis = 7 * dayMillis;
        long monthMillis = 30 * dayMillis;
        long yearMillis = 365 * dayMillis;

        for (int i = 0; i < 4; i++) {
            long start = 0, end = 0;
            String label = "";

            switch (timeRange) {
                case "Daily":
                    end = currentTime - (i * dayMillis);
                    start = end - dayMillis;
                    label = "Day " + (4 - i);
                    break;
                case "Weekly":
                    end = currentTime - (i * weekMillis);
                    start = end - weekMillis;
                    label = "Wk " + (4 - i);
                    break;
                case "Monthly":
                    end = currentTime - (i * monthMillis);
                    start = end - monthMillis;
                    label = new java.text.SimpleDateFormat("MMM").format(end);
                    break;
                case "Yearly":
                    Calendar cal = Calendar.getInstance();
                    cal.add(Calendar.YEAR, -i);
                    int year = cal.get(Calendar.YEAR);
                    label = String.valueOf(year);
                    cal.set(Calendar.DAY_OF_YEAR, 1);
                    start = cal.getTimeInMillis();
                    cal.add(Calendar.YEAR, 1);
                    end = cal.getTimeInMillis();
                    break;
            }

            Cursor cursor = db.rawQuery("SELECT SUM(amount) FROM transactions WHERE type = ? AND timestamp BETWEEN ? AND ?",
                    new String[]{transactionType, String.valueOf(start), String.valueOf(end)});
            float amount = 0;
            if (cursor.moveToFirst() && !cursor.isNull(0)) {
                amount = cursor.getFloat(0);
            }
            cursor.close();

            entries.add(new BarEntry(3 - i, amount));
            labels.add(label);
        }

        BarDataSet dataSet = new BarDataSet(entries, isExpense ? "Expenses" : "Income");
        dataSet.setColor(getResources().getColor(R.color.nav_icon_color));
        dataSet.setValueTextSize(12f);

        BarData barData = new BarData(dataSet);
        barData.setBarWidth(0.6f);

        barChart.setData(barData);
        barChart.setFitBars(true);

        XAxis xAxis = barChart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);
        xAxis.setLabelCount(labels.size());

        barChart.getAxisLeft().setDrawGridLines(false);
        barChart.getAxisRight().setEnabled(false);
        barChart.setScaleEnabled(false);
        barChart.setPinchZoom(false);
        barChart.setDoubleTapToZoomEnabled(false);
        barChart.setDragEnabled(false);
        barChart.setTouchEnabled(false);
        barChart.getDescription().setEnabled(false);
        barChart.animateY(800);
        barChart.invalidate();
    }

    private void updateTimeRange(String timeRange) {
        updateChartData(tabLayout.getSelectedTabPosition());
    }

    private void setupBottomNavigation() {
        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            Intent home = new Intent(this, HomeActivity.class);
            Intent allTransactions = new Intent(this, TransactionListActivity.class);
            Intent addTransaction = new Intent(this, AddTransactions.class);
            Intent stats = new Intent(this, StatisticsActivity.class);
            Intent profile = new Intent(this, ProfileActivity.class);

            if (itemId == R.id.nav_home) {
                startActivity(home);
                return true;
            }else if (itemId == R.id.nav_add) {
                startActivity(addTransaction);
                return true;
            } else if (itemId == R.id.nav_transactions) {
                startActivity(allTransactions);
                return true;
            } else if (itemId == R.id.nav_statistics) {
                startActivity(stats);
                return true;
            } else if (itemId == R.id.nav_profile) {
                startActivity(profile);
                return true;
            } else {
                return false;
            }
        });
    }
}
