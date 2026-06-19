package com.example.expensetracker.fragments;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.*;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.fragment.app.Fragment;

import com.example.expensetracker.R;
import com.example.expensetracker.database.DatabaseHelper;
import com.example.expensetracker.models.Category;
import com.example.expensetracker.models.Expense;
import com.example.expensetracker.utils.EmailConfig;
import com.example.expensetracker.utils.EmailSender;
import com.example.expensetracker.utils.MonthlyReportBuilder;
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
    private Button btnEmailMonthlyReport;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_reports, container, false);

        db = new DatabaseHelper(requireContext());
        session = new SessionManager(requireContext());

        pieChart = view.findViewById(R.id.pieChart);
        barChart = view.findViewById(R.id.barChart);
        tvTotalReport = view.findViewById(R.id.tvTotalReport);
        btnEmailMonthlyReport = view.findViewById(R.id.btnEmailMonthlyReport);
        btnEmailMonthlyReport.setOnClickListener(v -> sendMonthlyReportEmail());

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

    private void sendMonthlyReportEmail() {
        if (!EmailConfig.isConfigured()) {
            Toast.makeText(requireContext(),
                "Email not set up yet — add your sender Gmail and App Password in EmailConfig.java",
                Toast.LENGTH_LONG).show();
            return;
        }

        int userId = session.getUserId();
        Calendar cal = Calendar.getInstance();
        int month = cal.get(Calendar.MONTH) + 1;
        int year = cal.get(Calendar.YEAR);

        final MonthlyReportBuilder.Report report =
            MonthlyReportBuilder.build(requireContext(), userId, month, year);
        if (report.toEmail == null || report.toEmail.trim().isEmpty()) {
            Toast.makeText(requireContext(), "No email saved for this user", Toast.LENGTH_SHORT).show();
            return;
        }

        Toast.makeText(requireContext(), "Sending report…", Toast.LENGTH_SHORT).show();
        btnEmailMonthlyReport.setEnabled(false);

        // SMTP is a blocking network call, so do it off the UI thread.
        new Thread(() -> {
            String result;
            try {
                EmailSender.send(report.toEmail, report.subject, report.body, report.htmlBody);
                result = "Report sent to " + report.toEmail;
            } catch (Exception e) {
                result = "Failed to send: " + e.getMessage();
            }
            final String message = result;
            mainHandler.post(() -> {
                if (!isAdded()) return; // user may have left the screen
                btnEmailMonthlyReport.setEnabled(true);
                Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show();
            });
        }).start();
    }
}
