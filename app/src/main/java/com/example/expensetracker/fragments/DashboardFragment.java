package com.example.expensetracker.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.*;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.expensetracker.R;
import com.example.expensetracker.activities.AddIncomeActivity;
import com.example.expensetracker.activities.MainActivity;
import com.example.expensetracker.adapters.ExpenseAdapter;
import com.example.expensetracker.database.DatabaseHelper;
import com.example.expensetracker.models.Expense;
import com.example.expensetracker.utils.SessionManager;

import java.util.Calendar;
import java.util.List;

public class DashboardFragment extends Fragment {

    private DatabaseHelper db;
    private SessionManager session;
    private TextView tvWelcome, tvDashboardAvatar, tvTotalMonth, tvExpenseCount, tvRemainingSalary;
    private TextView tvIncomeAmount, tvDashboardExpenseAmount;
    private EditText etMonthlySalary;
    private Button btnSaveSalary;
    private ProgressBar progressIncome, progressExpense;
    private RecyclerView rvRecent;
    private boolean lowSalaryAlertShown = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dashboard, container, false);

        db = new DatabaseHelper(requireContext());
        session = new SessionManager(requireContext());

        tvWelcome = view.findViewById(R.id.tvWelcome);
        tvDashboardAvatar = view.findViewById(R.id.tvDashboardAvatar);
        tvTotalMonth = view.findViewById(R.id.tvTotalMonth);
        tvExpenseCount = view.findViewById(R.id.tvExpenseCount);
        tvRemainingSalary = view.findViewById(R.id.tvRemainingSalary);
        tvIncomeAmount = view.findViewById(R.id.tvIncomeAmount);
        tvDashboardExpenseAmount = view.findViewById(R.id.tvDashboardExpenseAmount);
        etMonthlySalary = view.findViewById(R.id.etMonthlySalary);
        btnSaveSalary = view.findViewById(R.id.btnSaveSalary);
        progressIncome = view.findViewById(R.id.progressIncome);
        progressExpense = view.findViewById(R.id.progressExpense);
        rvRecent = view.findViewById(R.id.rvRecentExpenses);

        tvDashboardAvatar.setOnClickListener(v -> {
            if (requireActivity() instanceof MainActivity) {
                ((MainActivity) requireActivity()).openProfile();
            }
        });
        view.findViewById(R.id.btnAddIncome).setOnClickListener(v ->
            startActivity(new Intent(requireContext(), AddIncomeActivity.class)));
        btnSaveSalary.setOnClickListener(v -> saveSalary());

        loadData();
        return view;
    }

    private void loadData() {
        int userId = session.getUserId();
        Calendar cal = Calendar.getInstance();
        int month = cal.get(Calendar.MONTH) + 1;
        int year = cal.get(Calendar.YEAR);

        String username = session.getUsername();
        tvWelcome.setText("Hi, " + username);
        tvDashboardAvatar.setText(getInitial(username));

        double total = db.getTotalExpenseByMonth(userId, month, year);

        double salary = session.getMonthlySalary(userId, month, year);
        double remaining = salary - total;
        if (salary > 0) {
            etMonthlySalary.setText(String.format("%.2f", salary));
        } else {
            etMonthlySalary.setText("");
        }
        tvRemainingSalary.setText(String.format("Rs. %.2f", remaining));
        tvTotalMonth.setText(String.format("Expense this month: Rs. %.2f", total));
        tvIncomeAmount.setText(String.format("Rs. %.2f", salary));
        tvDashboardExpenseAmount.setText(String.format("Rs. %.2f", total));
        updateAnalyticsBars(salary, total);
        showLowSalaryAlertIfNeeded(salary, remaining);

        List<Expense> expenses = db.getExpensesByMonth(userId, month, year);
        tvExpenseCount.setText(expenses.size() + " transactions this month");

        // Show only last 5
        List<Expense> recent = expenses.size() > 5 ? expenses.subList(0, 5) : expenses;
        rvRecent.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvRecent.setAdapter(new ExpenseAdapter(requireContext(), recent, null));
    }

    private void updateAnalyticsBars(double salary, double totalExpense) {
        int expensePercent = 0;
        if (salary > 0) {
            expensePercent = (int) Math.min(100, Math.round((totalExpense / salary) * 100));
        }
        progressExpense.setProgress(expensePercent);
        progressIncome.setProgress(Math.max(0, 100 - expensePercent));
    }

    private String getInitial(String username) {
        if (username == null || username.trim().isEmpty()) {
            return "U";
        }
        return username.trim().substring(0, 1).toUpperCase();
    }

    private void saveSalary() {
        String salaryText = etMonthlySalary.getText().toString().trim();
        if (salaryText.isEmpty()) {
            etMonthlySalary.setError("Enter salary");
            return;
        }

        double salary;
        try {
            salary = Double.parseDouble(salaryText);
            if (salary <= 0) {
                etMonthlySalary.setError("Salary must be > 0");
                return;
            }
        } catch (NumberFormatException e) {
            etMonthlySalary.setError("Invalid salary");
            return;
        }

        Calendar cal = Calendar.getInstance();
        int month = cal.get(Calendar.MONTH) + 1;
        int year = cal.get(Calendar.YEAR);
        session.setMonthlySalary(session.getUserId(), month, year, salary);
        lowSalaryAlertShown = false;
        Toast.makeText(requireContext(), "Salary saved", Toast.LENGTH_SHORT).show();
        loadData();
    }

    private void showLowSalaryAlertIfNeeded(double salary, double remaining) {
        if (salary <= 0 || remaining >= 5000 || lowSalaryAlertShown) {
            return;
        }

        lowSalaryAlertShown = true;
        new AlertDialog.Builder(requireContext())
            .setTitle("Salary Alert")
            .setMessage(String.format("Your remaining salary is Rs. %.2f, which is under Rs. 5000.", remaining))
            .setPositiveButton("OK", null)
            .show();
    }

}
