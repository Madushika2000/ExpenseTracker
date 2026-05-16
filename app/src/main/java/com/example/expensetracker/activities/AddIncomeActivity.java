package com.example.expensetracker.activities;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.expensetracker.R;
import com.example.expensetracker.utils.SessionManager;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class AddIncomeActivity extends AppCompatActivity {

    private EditText etIncomeAmount, etIncomeDate, etIncomeNotes;
    private Spinner spinnerIncomeSource;
    private SessionManager session;
    private Calendar selectedDate = Calendar.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_income);

        session = new SessionManager(this);

        ImageButton btnBack = findViewById(R.id.btnBack);
        Button btnSaveIncome = findViewById(R.id.btnSaveIncome);
        etIncomeAmount = findViewById(R.id.etIncomeAmount);
        etIncomeDate = findViewById(R.id.etIncomeDate);
        etIncomeNotes = findViewById(R.id.etIncomeNotes);
        spinnerIncomeSource = findViewById(R.id.spinnerIncomeSource);

        String[] sources = {"Salary", "Business", "Bonus", "Freelance", "Investment", "Other"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
            android.R.layout.simple_spinner_item, sources);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerIncomeSource.setAdapter(adapter);

        updateDateField();
        etIncomeDate.setOnClickListener(v -> showDatePicker());
        btnBack.setOnClickListener(v -> finish());
        btnSaveIncome.setOnClickListener(v -> saveIncome());
    }

    private void showDatePicker() {
        new DatePickerDialog(this,
            (view, year, month, day) -> {
                selectedDate.set(year, month, day);
                updateDateField();
            },
            selectedDate.get(Calendar.YEAR),
            selectedDate.get(Calendar.MONTH),
            selectedDate.get(Calendar.DAY_OF_MONTH)
        ).show();
    }

    private void updateDateField() {
        etIncomeDate.setText(new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            .format(selectedDate.getTime()));
    }

    private void saveIncome() {
        String amountText = etIncomeAmount.getText().toString().trim();
        if (TextUtils.isEmpty(amountText)) {
            etIncomeAmount.setError("Enter income amount");
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(amountText);
            if (amount <= 0) {
                etIncomeAmount.setError("Income must be > 0");
                return;
            }
        } catch (NumberFormatException e) {
            etIncomeAmount.setError("Invalid income amount");
            return;
        }

        int month = selectedDate.get(Calendar.MONTH) + 1;
        int year = selectedDate.get(Calendar.YEAR);
        int userId = session.getUserId();
        double currentIncome = session.getMonthlySalary(userId, month, year);
        session.setMonthlySalary(userId, month, year, currentIncome + amount);

        String source = spinnerIncomeSource.getSelectedItem().toString();
        String notes = etIncomeNotes.getText().toString().trim();
        String message = notes.isEmpty()
            ? source + " income saved"
            : source + " income saved with notes";
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        finish();
    }
}
