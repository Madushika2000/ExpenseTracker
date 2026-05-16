package com.example.expensetracker.activities;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.*;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.expensetracker.R;
import com.example.expensetracker.database.DatabaseHelper;
import com.example.expensetracker.models.Category;
import com.example.expensetracker.utils.SessionManager;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class AddExpenseActivity extends AppCompatActivity {

    private EditText etAmount, etDescription, etDate;
    private Spinner spinnerCategory, spinnerPaymentMethod;
    private Button btnSave, btnUploadReceipt;
    private ImageButton btnBack;

    private DatabaseHelper db;
    private SessionManager session;
    private List<Category> categories;
    private Calendar selectedDate = Calendar.getInstance();
    private int editExpenseId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_expense);

        db = new DatabaseHelper(this);
        session = new SessionManager(this);

        etAmount = findViewById(R.id.etAmount);
        etDescription = findViewById(R.id.etDescription);
        etDate = findViewById(R.id.etDate);
        spinnerCategory = findViewById(R.id.spinnerCategory);
        spinnerPaymentMethod = findViewById(R.id.spinnerPaymentMethod);
        btnSave = findViewById(R.id.btnSave);
        btnUploadReceipt = findViewById(R.id.btnUploadReceipt);
        btnBack = findViewById(R.id.btnBack);

        // Set today's date
        updateDateField();

        // Load categories
        categories = db.getCategories(session.getUserId());
        ArrayAdapter<Category> adapter = new ArrayAdapter<>(this,
            android.R.layout.simple_spinner_item, categories);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(adapter);

        String[] paymentMethods = {"Cash", "Debit Card", "Credit Card", "Bank Transfer", "Mobile Wallet"};
        ArrayAdapter<String> paymentAdapter = new ArrayAdapter<>(this,
            android.R.layout.simple_spinner_item, paymentMethods);
        paymentAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerPaymentMethod.setAdapter(paymentAdapter);

        // Date picker
        etDate.setOnClickListener(v -> showDatePicker());

        // Check if editing
        editExpenseId = getIntent().getIntExtra("expense_id", -1);
        if (editExpenseId != -1) {
            etAmount.setText(String.valueOf(getIntent().getDoubleExtra("expense_amount", 0)));
            etDescription.setText(getIntent().getStringExtra("expense_description"));
            etDate.setText(getIntent().getStringExtra("expense_date"));
            int catId = getIntent().getIntExtra("expense_category_id", 0);
            for (int i = 0; i < categories.size(); i++) {
                if (categories.get(i).getId() == catId) {
                    spinnerCategory.setSelection(i);
                    break;
                }
            }
            btnSave.setText("Update Expense");
        }

        btnBack.setOnClickListener(v -> finish());
        btnUploadReceipt.setOnClickListener(v ->
            Toast.makeText(this, "Receipt upload coming soon", Toast.LENGTH_SHORT).show());
        btnSave.setOnClickListener(v -> saveExpense());
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
        etDate.setText(new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            .format(selectedDate.getTime()));
    }

    private void saveExpense() {
        String amountStr = etAmount.getText().toString().trim();
        String description = etDescription.getText().toString().trim();
        String date = etDate.getText().toString().trim();

        if (TextUtils.isEmpty(amountStr)) { etAmount.setError("Enter amount"); return; }

        double amount;
        try {
            amount = Double.parseDouble(amountStr);
            if (amount <= 0) { etAmount.setError("Amount must be > 0"); return; }
        } catch (NumberFormatException e) {
            etAmount.setError("Invalid amount");
            return;
        }

        Category selectedCategory = categories.get(spinnerCategory.getSelectedItemPosition());

        if (editExpenseId != -1) {
            boolean updated = db.updateExpense(editExpenseId, amount, date, selectedCategory.getId(), description);
            if (updated) {
                Toast.makeText(this, "Expense updated!", Toast.LENGTH_SHORT).show();
                finishAfterSalaryCheck(date);
            }
        } else {
            long result = db.addExpense(amount, date, selectedCategory.getId(), session.getUserId(), description);
            if (result != -1) {
                Toast.makeText(this, "Expense added!", Toast.LENGTH_SHORT).show();
                finishAfterSalaryCheck(date);
            }
        }
    }

    private void finishAfterSalaryCheck(String expenseDate) {
        int year;
        int month;
        try {
            year = Integer.parseInt(expenseDate.substring(0, 4));
            month = Integer.parseInt(expenseDate.substring(5, 7));
        } catch (RuntimeException e) {
            finish();
            return;
        }

        double salary = session.getMonthlySalary(session.getUserId(), month, year);
        if (salary <= 0) {
            finish();
            return;
        }

        double totalExpense = db.getTotalExpenseByMonth(session.getUserId(), month, year);
        double remaining = salary - totalExpense;
        if (remaining >= 5000) {
            finish();
            return;
        }

        new AlertDialog.Builder(this)
            .setTitle("Salary Alert")
            .setMessage(String.format("Your remaining salary is Rs. %.2f, which is under Rs. 5000.", remaining))
            .setPositiveButton("OK", (dialog, which) -> finish())
            .setOnCancelListener(dialog -> finish())
            .show();
    }
}
