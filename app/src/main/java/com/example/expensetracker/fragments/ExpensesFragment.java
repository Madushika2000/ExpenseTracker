package com.example.expensetracker.fragments;

import android.os.Bundle;
import android.view.*;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.expensetracker.R;
import com.example.expensetracker.adapters.ExpenseAdapter;
import com.example.expensetracker.database.DatabaseHelper;
import com.example.expensetracker.models.Expense;
import com.example.expensetracker.utils.SessionManager;

import java.util.List;

public class ExpensesFragment extends Fragment implements ExpenseAdapter.OnExpenseActionListener {

    private DatabaseHelper db;
    private SessionManager session;
    private RecyclerView rvExpenses;
    private ExpenseAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_expenses, container, false);

        db = new DatabaseHelper(requireContext());
        session = new SessionManager(requireContext());

        rvExpenses = view.findViewById(R.id.rvExpenses);
        rvExpenses.setLayoutManager(new LinearLayoutManager(requireContext()));

        loadExpenses();
        return view;
    }

    private void loadExpenses() {
        List<Expense> expenses = db.getExpensesByUser(session.getUserId());
        adapter = new ExpenseAdapter(requireContext(), expenses, this);
        rvExpenses.setAdapter(adapter);
    }

    @Override
    public void onExpenseDeleted() {
        loadExpenses();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadExpenses();
    }
}
