package com.example.expensetracker.fragments;

import android.graphics.Color;
import android.os.Bundle;
import android.view.*;
import android.widget.TextView;
import androidx.fragment.app.Fragment;

import com.example.expensetracker.R;
import com.example.expensetracker.database.DatabaseHelper;
import com.example.expensetracker.models.Category;
import com.example.expensetracker.models.Expense;
import com.example.expensetracker.utils.SessionManager;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.*;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.*;

public class ReportsFragment extends Fragment {

    private DatabaseHelper db;
    private SessionManager session;
    private PieChart pieChart;
    private BarChart barChart;
    private TextView tvTotalReport;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_reports, container, false);

        db = new DatabaseHelper(requireContext());
        session = new SessionManager(requireContext());

        pieChart = view.findViewById(R.id.pieChart);
        barChart = view.findViewById(R.id.barChart);
        tvTotalReport = view.findViewById(R.id.tvTotalReport);

        loadReports();
        return view;
    }

    private void loadReports() {
        int userId = session.getUserId();
        Calendar cal = Calendar.getInstance();
        int month = cal.get(Calendar.MONTH) + 1;
        int year = cal.get(Calendar.YEAR);

        List<Expense> expenses = db.getExpensesByMonth(userId, month, year);
        double total = 0;
        for (Expense e : expenses) total += e.getAmount();
        tvTotalReport.setText(String.format("Total Spent: Rs. %.2f", total));

        // Pie Chart by Category
        Map<String, Double> catMap = new LinkedHashMap<>();
        for (Expense e : expenses) {
            String name = e.getCategoryName() != null ? e.getCategoryName() : "Other";
            catMap.put(name, catMap.getOrDefault(name, 0.0) + e.getAmount());
        }

        List<PieEntry> pieEntries = new ArrayList<>();
        for (Map.Entry<String, Double> entry : catMap.entrySet()) {
            pieEntries.add(new PieEntry(entry.getValue().floatValue(), entry.getKey()));
        }

        PieDataSet pieDataSet = new PieDataSet(pieEntries, "Expenses by Category");
        pieDataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        pieDataSet.setValueTextSize(12f);
        PieData pieData = new PieData(pieDataSet);

        pieChart.setData(pieData);
        pieChart.setUsePercentValues(true);
        pieChart.getDescription().setText("This Month");
        pieChart.setHoleRadius(40f);
        pieChart.setCenterText("Spending");
        pieChart.animateY(1000);
        pieChart.invalidate();

        // Bar Chart - last 6 months
        List<BarEntry> barEntries = new ArrayList<>();
        String[] monthNames = {"","Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec"};
        List<String> labels = new ArrayList<>();
        for (int i = 5; i >= 0; i--) {
            Calendar c = Calendar.getInstance();
            c.add(Calendar.MONTH, -i);
            int m = c.get(Calendar.MONTH) + 1;
            int y = c.get(Calendar.YEAR);
            double t = db.getTotalExpenseByMonth(userId, m, y);
            barEntries.add(new BarEntry(5 - i, (float) t));
            labels.add(monthNames[m]);
        }

        BarDataSet barDataSet = new BarDataSet(barEntries, "Monthly Expenses");
        barDataSet.setColors(ColorTemplate.COLORFUL_COLORS);
        barDataSet.setValueTextSize(10f);
        BarData barData = new BarData(barDataSet);

        barChart.setData(barData);
        barChart.getDescription().setText("Last 6 Months");
        barChart.animateY(1000);
        barChart.invalidate();
    }
}
