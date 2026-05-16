package com.example.expensetracker.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.expensetracker.R;
import com.example.expensetracker.database.DatabaseHelper;
import com.example.expensetracker.utils.SessionManager;

public class SettingsActivity extends AppCompatActivity {

    private static final String PREF_SETTINGS = "ExpenseTrackerSettings";
    private static final String KEY_DARK_MODE = "darkMode";
    private static final String KEY_NOTIFICATIONS = "notifications";
    private static final String KEY_CURRENCY = "currency";
    private static final String KEY_LANGUAGE = "language";

    private SessionManager session;
    private SharedPreferences settings;
    private TextView tvCurrency, tvLanguage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        session = new SessionManager(this);
        settings = getSharedPreferences(PREF_SETTINGS, MODE_PRIVATE);
        DatabaseHelper db = new DatabaseHelper(this);

        TextView tvSettingsAvatar = findViewById(R.id.tvSettingsAvatar);
        TextView tvSettingsName = findViewById(R.id.tvSettingsName);
        TextView tvSettingsEmail = findViewById(R.id.tvSettingsEmail);
        Switch switchDarkMode = findViewById(R.id.switchDarkMode);
        Switch switchNotifications = findViewById(R.id.switchNotifications);
        tvCurrency = findViewById(R.id.tvCurrency);
        tvLanguage = findViewById(R.id.tvLanguage);

        String username = session.getUsername();
        String email = db.getUserEmail(session.getUserId());
        tvSettingsName.setText(username);
        tvSettingsEmail.setText(email.isEmpty() ? "View profile" : email);
        tvSettingsAvatar.setText(username.isEmpty() ? "U" : username.substring(0, 1).toUpperCase());

        switchDarkMode.setChecked(settings.getBoolean(KEY_DARK_MODE, false));
        switchNotifications.setChecked(settings.getBoolean(KEY_NOTIFICATIONS, true));
        tvCurrency.setText(settings.getString(KEY_CURRENCY, "LKR"));
        tvLanguage.setText(settings.getString(KEY_LANGUAGE, "English"));

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        findViewById(R.id.cardProfileShortcut).setOnClickListener(v -> finish());
        switchDarkMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            settings.edit().putBoolean(KEY_DARK_MODE, isChecked).apply();
            Toast.makeText(this, isChecked ? "Dark mode enabled" : "Dark mode disabled", Toast.LENGTH_SHORT).show();
        });
        switchNotifications.setOnCheckedChangeListener((buttonView, isChecked) -> {
            settings.edit().putBoolean(KEY_NOTIFICATIONS, isChecked).apply();
            Toast.makeText(this, isChecked ? "Notifications enabled" : "Notifications disabled", Toast.LENGTH_SHORT).show();
        });

        findViewById(R.id.rowCurrency).setOnClickListener(v -> showCurrencyDialog());
        findViewById(R.id.rowLanguage).setOnClickListener(v -> showLanguageDialog());
        findViewById(R.id.rowBudgetAlerts).setOnClickListener(v -> showComingSoon("Budget alert settings"));
        findViewById(R.id.rowSecurityPrivacy).setOnClickListener(v -> showComingSoon("Security & Privacy"));
        findViewById(R.id.rowBackupRestore).setOnClickListener(v -> showComingSoon("Backup & Restore"));
        findViewById(R.id.cardAbout).setOnClickListener(v ->
            Toast.makeText(this, "Expense Tracker v1.0", Toast.LENGTH_SHORT).show());
        findViewById(R.id.btnLogout).setOnClickListener(v -> confirmLogout());
    }

    private void showCurrencyDialog() {
        String[] currencies = {"LKR", "USD", "EUR", "GBP", "INR"};
        new AlertDialog.Builder(this)
            .setTitle("Select Currency")
            .setItems(currencies, (dialog, which) -> {
                settings.edit().putString(KEY_CURRENCY, currencies[which]).apply();
                tvCurrency.setText(currencies[which]);
            })
            .show();
    }

    private void showLanguageDialog() {
        String[] languages = {"English", "Sinhala", "Tamil"};
        new AlertDialog.Builder(this)
            .setTitle("Select Language")
            .setItems(languages, (dialog, which) -> {
                settings.edit().putString(KEY_LANGUAGE, languages[which]).apply();
                tvLanguage.setText(languages[which]);
            })
            .show();
    }

    private void showComingSoon(String feature) {
        Toast.makeText(this, feature + " coming soon", Toast.LENGTH_SHORT).show();
    }

    private void confirmLogout() {
        new AlertDialog.Builder(this)
            .setTitle("Logout")
            .setMessage("Do you want to logout of this account?")
            .setPositiveButton("Logout", (dialog, which) -> logout())
            .setNegativeButton("Cancel", null)
            .show();
    }

    private void logout() {
        session.logout();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
