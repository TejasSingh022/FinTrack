package com.example.sl_project.stats;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.example.expensetracker.R;
import com.example.sl_project.home.HomeActivity;
import com.example.sl_project.profile.ProfileActivity;
import com.example.sl_project.transactions.AddTransactions;
import com.example.sl_project.transactions.TransactionListActivity;
import com.example.sl_project.utils.NavigationUtils;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import java.util.ArrayList;
import java.util.List;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;

public class StatisticsActivity extends AppCompatActivity {

    private TabLayout tabLayout;
    private TextView filterDaily, filterWeekly, filterMonthly, filterYearly;
    private View timeFilterIndicator;
    private DonutChartView donutChart;
    private TextView chartMonth, chartAmount;
    private ImageView backButton;
    private BottomNavigationView bottomNav;
    private BarChart barChart;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.statistics);

        // Initialize UI components
        initViews();
        setupListeners();
        setupData();
        NavigationUtils.setupBottomNavigation(this, findViewById(R.id.bottomNav), R.id.nav_statistics);    }

    private void initViews() {
        backButton = findViewById(R.id.backButton);
        tabLayout = findViewById(R.id.tabLayout);
        filterDaily = findViewById(R.id.filterDaily);
        filterWeekly = findViewById(R.id.filterWeekly);
        filterMonthly = findViewById(R.id.filterMonthly);
        filterYearly = findViewById(R.id.filterYearly);
        timeFilterIndicator = findViewById(R.id.timeFilterIndicator);
        donutChart = findViewById(R.id.donutChart);
        chartMonth = findViewById(R.id.chartMonth);
        chartAmount = findViewById(R.id.chartAmount);
        bottomNav = findViewById(R.id.bottomNav);
        barChart = findViewById(R.id.barChart);
    }

    private void setupListeners() {
        backButton.setOnClickListener(v -> finish());

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                updateChartData(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });

        View.OnClickListener timeFilterClickListener = v -> {
            filterDaily.setTextColor(getResources().getColor(R.color.grey));
            filterWeekly.setTextColor(getResources().getColor(R.color.grey));
            filterMonthly.setTextColor(getResources().getColor(R.color.grey));
            filterYearly.setTextColor(getResources().getColor(R.color.grey));

            int indicatorWidth = timeFilterIndicator.getWidth();

            if (v.getId() == R.id.filterDaily) {
                filterDaily.setTextColor(getResources().getColor(R.color.purple));
                timeFilterIndicator.setTranslationX(filterDaily.getLeft() + filterDaily.getWidth()/2 - indicatorWidth/2);
                updateTimeRange("Daily");
            } else if (v.getId() == R.id.filterWeekly) {
                filterWeekly.setTextColor(getResources().getColor(R.color.purple));
                timeFilterIndicator.setTranslationX(filterWeekly.getLeft() + filterWeekly.getWidth()/2 - indicatorWidth/2);
                updateTimeRange("Weekly");
            } else if (v.getId() == R.id.filterMonthly) {
                filterMonthly.setTextColor(getResources().getColor(R.color.purple));
                timeFilterIndicator.setTranslationX(filterMonthly.getLeft() + filterMonthly.getWidth()/2 - indicatorWidth/2);
                updateTimeRange("Monthly");
            } else if (v.getId() == R.id.filterYearly) {
                filterYearly.setTextColor(getResources().getColor(R.color.purple));
                timeFilterIndicator.setTranslationX(filterYearly.getLeft() + filterYearly.getWidth()/2 - indicatorWidth/2);
                updateTimeRange("Yearly");
            }
        };

        filterDaily.setOnClickListener(timeFilterClickListener);
        filterWeekly.setOnClickListener(timeFilterClickListener);
        filterMonthly.setOnClickListener(timeFilterClickListener);
        filterYearly.setOnClickListener(timeFilterClickListener);

        timeFilterIndicator.post(() -> {
            int indicatorWidth = timeFilterIndicator.getWidth();
            timeFilterIndicator.setTranslationX(filterMonthly.getLeft() + filterMonthly.getWidth()/2 - indicatorWidth/2);

            // Call updateTimeRange to load initial bar chart data
            updateTimeRange("Monthly");
        });
    }

    private void setupData() {
        // Set initial data for the chart
        List<Integer> amounts = new ArrayList<>();
        List<Integer> colors = new ArrayList<>();

        amounts.add(3000);
        colors.add(getResources().getColor(R.color.dark_purple));

        amounts.add(500);
        colors.add(getResources().getColor(R.color.light_purple));

        amounts.add(2000);
        colors.add(getResources().getColor(R.color.medium_purple));

        donutChart.setData(amounts, colors);

        chartMonth.setText("July");
        chartAmount.setText("$ 5500");

        // Initialize with "Monthly" and the default tab (which is 0 for Expense)
        int currentTabPosition = tabLayout.getSelectedTabPosition();
        loadBarChartData("Monthly", currentTabPosition);
    }

    private void updateChartData(int tabPosition) {
        List<Integer> amounts = new ArrayList<>();
        List<Integer> colors = new ArrayList<>();

        String currentTimeRange = "";
        if (filterDaily.getCurrentTextColor() == getResources().getColor(R.color.purple)) {
            currentTimeRange = "Daily";
        } else if (filterWeekly.getCurrentTextColor() == getResources().getColor(R.color.purple)) {
            currentTimeRange = "Weekly";
        } else if (filterMonthly.getCurrentTextColor() == getResources().getColor(R.color.purple)) {
            currentTimeRange = "Monthly";
        } else if (filterYearly.getCurrentTextColor() == getResources().getColor(R.color.purple)) {
            currentTimeRange = "Yearly";
        } else {
            // Default to Monthly if somehow none are selected
            currentTimeRange = "Monthly";
        }

        if (tabPosition == 0) { // Expense
            amounts.add(3000);
            colors.add(getResources().getColor(R.color.dark_purple));

            amounts.add(500);
            colors.add(getResources().getColor(R.color.light_purple));

            amounts.add(2000);
            colors.add(getResources().getColor(R.color.medium_purple));

            chartAmount.setText("$ 5500");
        } else { // Income
            amounts.add(7000);
            colors.add(getResources().getColor(R.color.dark_purple));

            amounts.add(1200);
            colors.add(getResources().getColor(R.color.light_purple));

            amounts.add(800);
            colors.add(getResources().getColor(R.color.medium_purple));

            chartAmount.setText("$ 9000");
        }

        donutChart.setData(amounts, colors);

        loadBarChartData(currentTimeRange, tabPosition);
    }

    private void loadBarChartData(String timeRange, int tabPosition) {
        List<BarEntry> entries = new ArrayList<>();
        List<String> labels = new ArrayList<>();

        // Determine if showing expense or income data
        boolean isExpense = (tabPosition == 0);

        // Dummy values depending on time range and tab
        if (timeRange.equals("Daily")) {
            if (isExpense) {
                entries.add(new BarEntry(0, 120));
                entries.add(new BarEntry(1, 80));
                entries.add(new BarEntry(2, 150));
                entries.add(new BarEntry(3, 100));
            } else {
                entries.add(new BarEntry(0, 180));
                entries.add(new BarEntry(1, 220));
                entries.add(new BarEntry(2, 190));
                entries.add(new BarEntry(3, 240));
            }
            labels.add("Mon"); labels.add("Tue"); labels.add("Wed"); labels.add("Thu");
        } else if (timeRange.equals("Weekly")) {
            if (isExpense) {
                entries.add(new BarEntry(0, 500));
                entries.add(new BarEntry(1, 650));
                entries.add(new BarEntry(2, 400));
                entries.add(new BarEntry(3, 300));
            } else {
                entries.add(new BarEntry(0, 800));
                entries.add(new BarEntry(1, 750));
                entries.add(new BarEntry(2, 900));
                entries.add(new BarEntry(3, 600));
            }
            labels.add("Wk 1"); labels.add("Wk 2"); labels.add("Wk 3"); labels.add("Wk 4");
        } else if (timeRange.equals("Monthly")) {
            if (isExpense) {
                entries.add(new BarEntry(0, 1500));
                entries.add(new BarEntry(1, 1200));
                entries.add(new BarEntry(2, 1800));
                entries.add(new BarEntry(3, 2000));
            } else {
                entries.add(new BarEntry(0, 2200));
                entries.add(new BarEntry(1, 2400));
                entries.add(new BarEntry(2, 2100));
                entries.add(new BarEntry(3, 2300));
            }
            labels.add("Apr"); labels.add("May"); labels.add("Jun"); labels.add("Jul");
        } else if (timeRange.equals("Yearly")) {
            if (isExpense) {
                entries.add(new BarEntry(0, 18000));
                entries.add(new BarEntry(1, 21000));
                entries.add(new BarEntry(2, 15000));
                entries.add(new BarEntry(3, 25000));
            } else {
                entries.add(new BarEntry(0, 24000));
                entries.add(new BarEntry(1, 27000));
                entries.add(new BarEntry(2, 30000));
                entries.add(new BarEntry(3, 28000));
            }
            labels.add("2021"); labels.add("2022"); labels.add("2023"); labels.add("2024");
        }

        BarDataSet dataSet = new BarDataSet(entries, isExpense ? "Expenses" : "Income");

        // Use different colors for expense and income
        dataSet.setColor(getResources().getColor(isExpense ? R.color.purple : R.color.dark_purple));
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

        YAxis leftAxis = barChart.getAxisLeft();
        leftAxis.setDrawGridLines(false);

        YAxis rightAxis = barChart.getAxisRight();
        rightAxis.setEnabled(false);

        barChart.getDescription().setEnabled(false);
        barChart.animateY(800);
        barChart.invalidate();
    }

    private void updateTimeRange(String timeRange) {
        chartMonth.setText(timeRange.equals("Monthly") ? "July" :
                timeRange.equals("Yearly") ? "2024" :
                        timeRange.equals("Weekly") ? "Week 30" : "Today");

        // Get current tab position
        int currentTabPosition = tabLayout.getSelectedTabPosition();

        // Load bar chart data with current time range and tab position
        loadBarChartData(timeRange, currentTabPosition);
    }
}
