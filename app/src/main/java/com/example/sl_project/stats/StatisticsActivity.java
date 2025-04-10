package com.example.sl_project.stats;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.expensetracker.R;
import com.example.sl_project.database.DatabaseHelper;
import com.example.sl_project.utils.NavigationUtils;
import com.google.android.material.tabs.TabLayout;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;

import java.text.SimpleDateFormat;
import java.util.*;

public class StatisticsActivity extends AppCompatActivity {

    private TabLayout tabLayout;
    private TextView filterDaily;
    private PieChart pieChart;
    private BarChart barChart;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.statistics);

        dbHelper = new DatabaseHelper(this);
        initViews();
        NavigationUtils.setupBottomNavigation(this, findViewById(R.id.bottomNav), R.id.nav_statistics);
        updateCharts(0);
    }

    private void initViews() {
        tabLayout = findViewById(R.id.tabLayout);
        filterDaily = findViewById(R.id.filterDaily);
        pieChart = findViewById(R.id.expensePieChart);
        barChart = findViewById(R.id.barChart);

        findViewById(R.id.backButton).setOnClickListener(v -> finish());
        filterDaily.setTextColor(ContextCompat.getColor(this, R.color.nav_icon_color));

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                updateCharts(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void updateCharts(int tabPosition) {
        String type = (tabPosition == 0) ? "Expense" : "Income";
        updatePieChart(type);
        updateBarChart(type);
    }

    private void updatePieChart(String type) {
        Map<String, Double> categoryData = new HashMap<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        // Calculate exactly 7 days
        Calendar endCal = Calendar.getInstance();
        // Set end time to the end of today (11:59:59.999 PM)
        endCal.set(Calendar.HOUR_OF_DAY, 23);
        endCal.set(Calendar.MINUTE, 59);
        endCal.set(Calendar.SECOND, 59);
        endCal.set(Calendar.MILLISECOND, 999);
        long endTime = endCal.getTimeInMillis();

        // Set start time to the beginning of 7 days ago (12:00:00.000 AM)
        Calendar startCal = (Calendar) endCal.clone();
        startCal.add(Calendar.DAY_OF_YEAR, -6); // -6 gives 7 days including today
        startCal.set(Calendar.HOUR_OF_DAY, 0);
        startCal.set(Calendar.MINUTE, 0);
        startCal.set(Calendar.SECOND, 0);
        startCal.set(Calendar.MILLISECOND, 0);
        long startTime = startCal.getTimeInMillis();

        try (Cursor cursor = db.rawQuery(
                "SELECT category, SUM(amount) FROM transactions WHERE type = ? AND timestamp BETWEEN ? AND ? GROUP BY category",
                new String[]{type, String.valueOf(startTime), String.valueOf(endTime)})) {

            while (cursor != null && cursor.moveToNext()) {
                categoryData.put(cursor.getString(0), cursor.getDouble(1));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        ArrayList<PieEntry> entries = new ArrayList<>();
        int[] colors = {
                ContextCompat.getColor(this, R.color.red),
                ContextCompat.getColor(this, R.color.blue),
                ContextCompat.getColor(this, R.color.green),
                ContextCompat.getColor(this, R.color.black),
                ContextCompat.getColor(this, R.color.grey)
        };

        for (Map.Entry<String, Double> entry : categoryData.entrySet()) {
            entries.add(new PieEntry(entry.getValue().floatValue(), entry.getKey()));
        }

        if (entries.isEmpty()) {
            entries.add(new PieEntry(1f, "No Data"));
        }

        pieChart.clear();
        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setDrawValues(false);
        pieChart.setRotationEnabled(false);
        dataSet.setColors(colors);
        dataSet.setValueTextColor(Color.WHITE);
        dataSet.setValueTextSize(12f);

        PieData pieData = new PieData(dataSet);
        pieChart.setData(pieData);
        pieChart.getDescription().setEnabled(false);
        pieChart.setDrawEntryLabels(false);
        pieChart.getLegend().setEnabled(true);
        pieChart.invalidate();
    }

    private void updateBarChart(String type) {
        List<BarEntry> entries = new ArrayList<>();
        List<String> labels = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        // Calculate today's date at midnight to use as reference
        Calendar today = Calendar.getInstance();
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        today.set(Calendar.MILLISECOND, 0);

        // We will display 7 days - from 6 days ago to today (inclusive)
        for (int i = 6; i >= 0; i--) {
            // Calculate day start (midnight)
            Calendar dayStart = (Calendar) today.clone();
            dayStart.add(Calendar.DAY_OF_YEAR, -i);
            long startTime = dayStart.getTimeInMillis();

            // Calculate day end (11:59:59.999 PM)
            Calendar dayEnd = (Calendar) dayStart.clone();
            dayEnd.set(Calendar.HOUR_OF_DAY, 23);
            dayEnd.set(Calendar.MINUTE, 59);
            dayEnd.set(Calendar.SECOND, 59);
            dayEnd.set(Calendar.MILLISECOND, 999);
            long endTime = dayEnd.getTimeInMillis();

            // Format the label (oldest day on the left, today on the right)
            String dayLabel = new SimpleDateFormat("EEE dd", Locale.getDefault()).format(dayStart.getTime());
            labels.add(dayLabel);

            // Query for this day's transactions
            float amount = 0;
            try (Cursor cursor = db.rawQuery(
                    "SELECT SUM(amount) FROM transactions WHERE type = ? AND timestamp BETWEEN ? AND ?",
                    new String[]{type, String.valueOf(startTime), String.valueOf(endTime)})) {

                if (cursor != null && cursor.moveToFirst() && !cursor.isNull(0)) {
                    amount = cursor.getFloat(0);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            // Add the entry (oldest day at index 0, today at index 6)
            entries.add(new BarEntry(6-i, amount));
        }

        barChart.clear();
        BarDataSet dataSet = new BarDataSet(entries, type);
        dataSet.setColor(ContextCompat.getColor(this, type.equals("Expense") ? R.color.red : R.color.green));
        dataSet.setValueTextSize(10f);

        BarData barData = new BarData(dataSet);
        barData.setBarWidth(0.6f);

        barChart.setData(barData);
        XAxis xAxis = barChart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);
        xAxis.setLabelRotationAngle(45);

        barChart.setScaleEnabled(false);
        barChart.setPinchZoom(false);
        barChart.setDoubleTapToZoomEnabled(false);
        barChart.setDragEnabled(false);
        barChart.getDescription().setEnabled(false);
        barChart.getAxisRight().setEnabled(false);
        barChart.animateY(500);
        barChart.invalidate();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dbHelper != null) dbHelper.close();
    }
}