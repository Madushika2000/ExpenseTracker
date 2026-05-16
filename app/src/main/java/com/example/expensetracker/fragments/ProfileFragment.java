package com.example.expensetracker.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.example.expensetracker.R;
import com.example.expensetracker.activities.LoginActivity;
import com.example.expensetracker.activities.SettingsActivity;
import com.example.expensetracker.database.DatabaseHelper;
import com.example.expensetracker.utils.SessionManager;

import java.util.Calendar;

public class ProfileFragment extends Fragment {

    private DatabaseHelper db;
    private SessionManager session;
    private TextView tvProfileAvatar, tvProfileName, tvProfileEmail;
    private TextView tvProfileExpenses, tvProfileIncome, tvProfileSavings;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        db = new DatabaseHelper(requireContext());
        session = new SessionManager(requireContext());

        tvProfileAvatar = view.findViewById(R.id.tvProfileAvatar);
        tvProfileName = view.findViewById(R.id.tvProfileName);
        tvProfileEmail = view.findViewById(R.id.tvProfileEmail);
        tvProfileExpenses = view.findViewById(R.id.tvProfileExpenses);
        tvProfileIncome = view.findViewById(R.id.tvProfileIncome);
        tvProfileSavings = view.findViewById(R.id.tvProfileSavings);

        view.findViewById(R.id.btnEditProfile).setOnClickListener(v ->
            Toast.makeText(requireContext(), "Edit Profile coming soon", Toast.LENGTH_SHORT).show());
        view.findViewById(R.id.menuPersonalInfo).setOnClickListener(v -> showComingSoon("Personal Information"));
        view.findViewById(R.id.menuSecurity).setOnClickListener(v -> showComingSoon("Security"));
        view.findViewById(R.id.menuNotifications).setOnClickListener(v -> showComingSoon("Notifications"));
        view.findViewById(R.id.menuPaymentMethods).setOnClickListener(v -> showComingSoon("Payment Methods"));
        view.findViewById(R.id.menuSettings).setOnClickListener(v ->
            startActivity(new Intent(requireContext(), SettingsActivity.class)));
        view.findViewById(R.id.menuHelpSupport).setOnClickListener(v -> showComingSoon("Help & Support"));
        view.findViewById(R.id.menuLogout).setOnClickListener(v -> confirmLogout());

        loadProfile();
        return view;
    }

    private void loadProfile() {
        int userId = session.getUserId();
        String username = session.getUsername();
        String email = db.getUserEmail(userId);
        if (email.isEmpty()) {
            email = "No email added";
        }

        tvProfileName.setText(username);
        tvProfileEmail.setText(email);
        tvProfileAvatar.setText(username.isEmpty() ? "U" : username.substring(0, 1).toUpperCase());

        Calendar cal = Calendar.getInstance();
        int month = cal.get(Calendar.MONTH) + 1;
        int year = cal.get(Calendar.YEAR);
        double totalExpense = db.getTotalExpenseByMonth(userId, month, year);
        double totalIncome = session.getMonthlySalary(userId, month, year);
        double savings = totalIncome - totalExpense;

        tvProfileExpenses.setText(String.format("Rs. %.2f", totalExpense));
        tvProfileIncome.setText(String.format("Rs. %.2f", totalIncome));
        tvProfileSavings.setText(String.format("Rs. %.2f", savings));
    }

    private void showComingSoon(String feature) {
        Toast.makeText(requireContext(), feature + " coming soon", Toast.LENGTH_SHORT).show();
    }

    private void confirmLogout() {
        new AlertDialog.Builder(requireContext())
            .setTitle("Logout")
            .setMessage("Do you want to logout of this account?")
            .setPositiveButton("Logout", (dialog, which) -> logout())
            .setNegativeButton("Cancel", null)
            .show();
    }

    private void logout() {
        session.logout();
        Intent intent = new Intent(requireContext(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        requireActivity().finish();
    }
}
