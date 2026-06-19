package com.example.expensetracker.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.expensetracker.R;
import com.example.expensetracker.fragments.DashboardFragment;
import com.example.expensetracker.fragments.ExpensesFragment;
import com.example.expensetracker.fragments.BudgetFragment;
import com.example.expensetracker.fragments.ProfileFragment;
import com.example.expensetracker.fragments.ReportsFragment;
import com.example.expensetracker.utils.ReportScheduler;
import com.example.expensetracker.utils.SessionManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class MainActivity extends AppCompatActivity {

    private SessionManager session;
    private BottomNavigationView bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        session = new SessionManager(this);
        if (!session.isLoggedIn()) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_main);

        // Make sure the automatic monthly report email is scheduled.
        ReportScheduler.scheduleMonthlyReport(this);

        bottomNav = findViewById(R.id.bottomNav);
        FloatingActionButton fabAdd = findViewById(R.id.fabAdd);

        // Load default fragment
        loadFragment(new DashboardFragment());

        bottomNav.setOnItemSelectedListener(item -> {
            Fragment fragment;
            int itemId = item.getItemId();
            if (itemId == R.id.nav_dashboard) {
                fragment = new DashboardFragment();
            } else if (itemId == R.id.nav_expenses) {
                fragment = new ExpensesFragment();
            } else if (itemId == R.id.nav_budget) {
                fragment = new BudgetFragment();
            } else if (itemId == R.id.nav_reports) {
                fragment = new ReportsFragment();
            } else if (itemId == R.id.nav_profile) {
                fragment = new ProfileFragment();
            } else {
                return false;
            }
            loadFragment(fragment);
            return true;
        });

        fabAdd.setOnClickListener(v ->
            startActivity(new Intent(this, AddExpenseActivity.class)));
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
            .beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit();
    }

    public void openProfile() {
        bottomNav.setSelectedItemId(R.id.nav_profile);
    }
}
