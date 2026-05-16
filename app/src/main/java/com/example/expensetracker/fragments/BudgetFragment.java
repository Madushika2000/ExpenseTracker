package com.example.expensetracker.fragments;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.*;
import android.widget.*;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.expensetracker.R;
import com.example.expensetracker.adapters.BudgetAdapter;
import com.example.expensetracker.database.DatabaseHelper;
import com.example.expensetracker.models.Budget;
import com.example.expensetracker.models.Category;
import com.example.expensetracker.utils.SessionManager;

import java.util.Calendar;
import java.util.List;

public class BudgetFragment extends Fragment {

    private DatabaseHelper db;
    private SessionManager session;
    private RecyclerView rvBudgets;
    private Button btnAddBudget;
    private TextView tvMonthYear, tvTotalBudget, tvBudgetSpent, tvBudgetRemaining;

    private int currentMonth, currentYear;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_budget, container, false);

        db = new DatabaseHelper(requireContext());
        session = new SessionManager(requireContext());

        rvBudgets = view.findViewById(R.id.rvBudgets);
        btnAddBudget = view.findViewById(R.id.btnAddBudget);
        tvMonthYear = view.findViewById(R.id.tvMonthYear);
        tvTotalBudget = view.findViewById(R.id.tvTotalBudget);
        tvBudgetSpent = view.findViewById(R.id.tvBudgetSpent);
        tvBudgetRemaining = view.findViewById(R.id.tvBudgetRemaining);

        Calendar cal = Calendar.getInstance();
        currentMonth = cal.get(Calendar.MONTH) + 1;
        currentYear = cal.get(Calendar.YEAR);

        String[] months = {"Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec"};
        tvMonthYear.setText(months[currentMonth - 1] + " " + currentYear);

        btnAddBudget.setOnClickListener(v -> showAddBudgetDialog());
        loadBudgets();
        return view;
    }

    private void loadBudgets() {
        int userId = session.getUserId();
        List<Budget> budgets = db.getBudgetsByMonth(userId, currentMonth, currentYear);
        double totalBudget = 0;
        double totalSpent = 0;
        for (Budget b : budgets) {
            double spent = db.getTotalExpenseByCategory(userId, b.getCategoryId(), currentMonth, currentYear);
            b.setSpent(spent);
            totalBudget += b.getAmount();
            totalSpent += spent;
        }
        tvTotalBudget.setText(String.format("Budget: Rs. %.2f", totalBudget));
        tvBudgetSpent.setText(String.format("Spent: Rs. %.2f", totalSpent));
        tvBudgetRemaining.setText(String.format("Remaining budget: Rs. %.2f", totalBudget - totalSpent));

        rvBudgets.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvBudgets.setAdapter(new BudgetAdapter(requireContext(), budgets));
    }

    private void showAddBudgetDialog() {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_budget, null);
        Spinner spinnerCat = dialogView.findViewById(R.id.spinnerCategory);
        EditText etAmount = dialogView.findViewById(R.id.etBudgetAmount);

        List<Category> categories = db.getCategories(session.getUserId());
        ArrayAdapter<Category> adapter = new ArrayAdapter<>(requireContext(),
            android.R.layout.simple_spinner_item, categories);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCat.setAdapter(adapter);

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
            .setTitle("Create Monthly Budget")
            .setView(dialogView)
            .setPositiveButton("Save", null)
            .setNegativeButton("Cancel", null)
            .create();

        dialog.setOnShowListener(d -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String amtStr = etAmount.getText().toString().trim();
            if (amtStr.isEmpty()) {
                etAmount.setError("Enter budget amount");
                return;
            }

            double amount;
            try {
                amount = Double.parseDouble(amtStr);
                if (amount <= 0) {
                    etAmount.setError("Budget must be > 0");
                    return;
                }
            } catch (NumberFormatException e) {
                etAmount.setError("Invalid amount");
                return;
            }

            Category cat = categories.get(spinnerCat.getSelectedItemPosition());
            db.setBudget(amount, currentMonth, currentYear, cat.getId(), session.getUserId());
            Toast.makeText(requireContext(), "Budget created", Toast.LENGTH_SHORT).show();
            loadBudgets();
            dialog.dismiss();
        }));
        dialog.show();
    }
}
