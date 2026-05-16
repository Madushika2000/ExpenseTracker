package com.example.expensetracker.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.expensetracker.models.Budget;
import com.example.expensetracker.models.Category;
import com.example.expensetracker.models.Expense;
import com.example.expensetracker.models.User;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "ExpenseTracker.db";
    private static final int DATABASE_VERSION = 1;

    // Table Names
    public static final String TABLE_USERS = "users";
    public static final String TABLE_CATEGORIES = "categories";
    public static final String TABLE_EXPENSES = "expenses";
    public static final String TABLE_BUDGETS = "budgets";

    // Common columns
    public static final String COL_ID = "id";

    // Users columns
    public static final String COL_USERNAME = "username";
    public static final String COL_PASSWORD = "password";
    public static final String COL_EMAIL = "email";

    // Categories columns
    public static final String COL_CAT_NAME = "name";
    public static final String COL_CAT_ICON = "icon";
    public static final String COL_CAT_COLOR = "color";
    public static final String COL_CAT_USER_ID = "user_id";

    // Expenses columns
    public static final String COL_EXP_AMOUNT = "amount";
    public static final String COL_EXP_DATE = "date";
    public static final String COL_EXP_CATEGORY_ID = "category_id";
    public static final String COL_EXP_USER_ID = "user_id";
    public static final String COL_EXP_DESCRIPTION = "description";

    // Budgets columns
    public static final String COL_BUD_AMOUNT = "amount";
    public static final String COL_BUD_MONTH = "month";
    public static final String COL_BUD_YEAR = "year";
    public static final String COL_BUD_CATEGORY_ID = "category_id";
    public static final String COL_BUD_USER_ID = "user_id";

    // Create table statements
    private static final String CREATE_TABLE_USERS =
            "CREATE TABLE " + TABLE_USERS + " (" +
            COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COL_USERNAME + " TEXT UNIQUE NOT NULL, " +
            COL_PASSWORD + " TEXT NOT NULL, " +
            COL_EMAIL + " TEXT);";

    private static final String CREATE_TABLE_CATEGORIES =
            "CREATE TABLE " + TABLE_CATEGORIES + " (" +
            COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COL_CAT_NAME + " TEXT NOT NULL, " +
            COL_CAT_ICON + " TEXT, " +
            COL_CAT_COLOR + " TEXT, " +
            COL_CAT_USER_ID + " INTEGER, " +
            "FOREIGN KEY(" + COL_CAT_USER_ID + ") REFERENCES " + TABLE_USERS + "(" + COL_ID + "));";

    private static final String CREATE_TABLE_EXPENSES =
            "CREATE TABLE " + TABLE_EXPENSES + " (" +
            COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COL_EXP_AMOUNT + " REAL NOT NULL, " +
            COL_EXP_DATE + " TEXT NOT NULL, " +
            COL_EXP_CATEGORY_ID + " INTEGER, " +
            COL_EXP_USER_ID + " INTEGER, " +
            COL_EXP_DESCRIPTION + " TEXT, " +
            "FOREIGN KEY(" + COL_EXP_CATEGORY_ID + ") REFERENCES " + TABLE_CATEGORIES + "(" + COL_ID + "), " +
            "FOREIGN KEY(" + COL_EXP_USER_ID + ") REFERENCES " + TABLE_USERS + "(" + COL_ID + "));";

    private static final String CREATE_TABLE_BUDGETS =
            "CREATE TABLE " + TABLE_BUDGETS + " (" +
            COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COL_BUD_AMOUNT + " REAL NOT NULL, " +
            COL_BUD_MONTH + " INTEGER NOT NULL, " +
            COL_BUD_YEAR + " INTEGER NOT NULL, " +
            COL_BUD_CATEGORY_ID + " INTEGER, " +
            COL_BUD_USER_ID + " INTEGER, " +
            "FOREIGN KEY(" + COL_BUD_CATEGORY_ID + ") REFERENCES " + TABLE_CATEGORIES + "(" + COL_ID + "), " +
            "FOREIGN KEY(" + COL_BUD_USER_ID + ") REFERENCES " + TABLE_USERS + "(" + COL_ID + "));";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_USERS);
        db.execSQL(CREATE_TABLE_CATEGORIES);
        db.execSQL(CREATE_TABLE_EXPENSES);
        db.execSQL(CREATE_TABLE_BUDGETS);
        insertDefaultCategories(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_BUDGETS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_EXPENSES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CATEGORIES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        onCreate(db);
    }

    private void insertDefaultCategories(SQLiteDatabase db) {
        String[][] defaults = {
            {"Food & Dining", "🍔", "#FF6B6B"},
            {"Transport", "🚌", "#4ECDC4"},
            {"Education", "📚", "#45B7D1"},
            {"Healthcare", "🏥", "#96CEB4"},
            {"Shopping", "🛍️", "#FFEAA7"},
            {"Entertainment", "🎮", "#DDA0DD"},
            {"Bills & Utilities", "💡", "#98D8C8"},
            {"Other", "💰", "#B0B0B0"}
        };
        for (String[] cat : defaults) {
            ContentValues cv = new ContentValues();
            cv.put(COL_CAT_NAME, cat[0]);
            cv.put(COL_CAT_ICON, cat[1]);
            cv.put(COL_CAT_COLOR, cat[2]);
            cv.put(COL_CAT_USER_ID, 0); // 0 = shared/default
            db.insert(TABLE_CATEGORIES, null, cv);
        }
    }

    // ==================== USER OPERATIONS ====================

    public long registerUser(String username, String password, String email) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_USERNAME, username);
        cv.put(COL_PASSWORD, password);
        cv.put(COL_EMAIL, email);
        long result = db.insert(TABLE_USERS, null, cv);
        db.close();
        return result;
    }

    public User loginUser(String username, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS,
                new String[]{COL_ID, COL_USERNAME, COL_EMAIL},
                COL_USERNAME + "=? AND " + COL_PASSWORD + "=?",
                new String[]{username, password}, null, null, null);
        User user = null;
        if (cursor != null && cursor.moveToFirst()) {
            user = new User(
                cursor.getInt(cursor.getColumnIndexOrThrow(COL_ID)),
                cursor.getString(cursor.getColumnIndexOrThrow(COL_USERNAME)),
                cursor.getString(cursor.getColumnIndexOrThrow(COL_EMAIL))
            );
            cursor.close();
        }
        db.close();
        return user;
    }

    public boolean usernameExists(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS, new String[]{COL_ID},
                COL_USERNAME + "=?", new String[]{username}, null, null, null);
        boolean exists = cursor != null && cursor.getCount() > 0;
        if (cursor != null) cursor.close();
        db.close();
        return exists;
    }

    public String getUserEmail(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS, new String[]{COL_EMAIL},
                COL_ID + "=?", new String[]{String.valueOf(userId)}, null, null, null);
        String email = "";
        if (cursor != null && cursor.moveToFirst()) {
            email = cursor.getString(cursor.getColumnIndexOrThrow(COL_EMAIL));
            cursor.close();
        }
        db.close();
        return email != null ? email : "";
    }

    // ==================== CATEGORY OPERATIONS ====================

    public List<Category> getCategories(int userId) {
        List<Category> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
            "SELECT * FROM " + TABLE_CATEGORIES +
            " WHERE " + COL_CAT_USER_ID + "=0 OR " + COL_CAT_USER_ID + "=?" +
            " ORDER BY " + COL_CAT_NAME,
            new String[]{String.valueOf(userId)});
        if (cursor.moveToFirst()) {
            do {
                list.add(new Category(
                    cursor.getInt(cursor.getColumnIndexOrThrow(COL_ID)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COL_CAT_NAME)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COL_CAT_ICON)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COL_CAT_COLOR))
                ));
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return list;
    }

    public long addCategory(String name, String icon, String color, int userId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_CAT_NAME, name);
        cv.put(COL_CAT_ICON, icon);
        cv.put(COL_CAT_COLOR, color);
        cv.put(COL_CAT_USER_ID, userId);
        long result = db.insert(TABLE_CATEGORIES, null, cv);
        db.close();
        return result;
    }

    // ==================== EXPENSE OPERATIONS ====================

    public long addExpense(double amount, String date, int categoryId, int userId, String description) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_EXP_AMOUNT, amount);
        cv.put(COL_EXP_DATE, date);
        cv.put(COL_EXP_CATEGORY_ID, categoryId);
        cv.put(COL_EXP_USER_ID, userId);
        cv.put(COL_EXP_DESCRIPTION, description);
        long result = db.insert(TABLE_EXPENSES, null, cv);
        db.close();
        return result;
    }

    public boolean updateExpense(int id, double amount, String date, int categoryId, String description) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_EXP_AMOUNT, amount);
        cv.put(COL_EXP_DATE, date);
        cv.put(COL_EXP_CATEGORY_ID, categoryId);
        cv.put(COL_EXP_DESCRIPTION, description);
        int rows = db.update(TABLE_EXPENSES, cv, COL_ID + "=?", new String[]{String.valueOf(id)});
        db.close();
        return rows > 0;
    }

    public boolean deleteExpense(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rows = db.delete(TABLE_EXPENSES, COL_ID + "=?", new String[]{String.valueOf(id)});
        db.close();
        return rows > 0;
    }

    public List<Expense> getExpensesByUser(int userId) {
        List<Expense> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT e.*, c." + COL_CAT_NAME + ", c." + COL_CAT_ICON + ", c." + COL_CAT_COLOR +
                " FROM " + TABLE_EXPENSES + " e" +
                " LEFT JOIN " + TABLE_CATEGORIES + " c ON e." + COL_EXP_CATEGORY_ID + " = c." + COL_ID +
                " WHERE e." + COL_EXP_USER_ID + "=?" +
                " ORDER BY e." + COL_EXP_DATE + " DESC";
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(userId)});
        if (cursor.moveToFirst()) {
            do {
                Expense exp = new Expense(
                    cursor.getInt(cursor.getColumnIndexOrThrow(COL_ID)),
                    cursor.getDouble(cursor.getColumnIndexOrThrow(COL_EXP_AMOUNT)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COL_EXP_DATE)),
                    cursor.getInt(cursor.getColumnIndexOrThrow(COL_EXP_CATEGORY_ID)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COL_EXP_DESCRIPTION))
                );
                exp.setCategoryName(cursor.getString(cursor.getColumnIndexOrThrow(COL_CAT_NAME)));
                exp.setCategoryIcon(cursor.getString(cursor.getColumnIndexOrThrow(COL_CAT_ICON)));
                exp.setCategoryColor(cursor.getString(cursor.getColumnIndexOrThrow(COL_CAT_COLOR)));
                list.add(exp);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return list;
    }

    public List<Expense> getExpensesByMonth(int userId, int month, int year) {
        List<Expense> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String monthStr = String.format("%04d-%02d", year, month);
        String query = "SELECT e.*, c." + COL_CAT_NAME + ", c." + COL_CAT_ICON + ", c." + COL_CAT_COLOR +
                " FROM " + TABLE_EXPENSES + " e" +
                " LEFT JOIN " + TABLE_CATEGORIES + " c ON e." + COL_EXP_CATEGORY_ID + " = c." + COL_ID +
                " WHERE e." + COL_EXP_USER_ID + "=? AND strftime('%Y-%m', e." + COL_EXP_DATE + ")=?" +
                " ORDER BY e." + COL_EXP_DATE + " DESC";
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(userId), monthStr});
        if (cursor.moveToFirst()) {
            do {
                Expense exp = new Expense(
                    cursor.getInt(cursor.getColumnIndexOrThrow(COL_ID)),
                    cursor.getDouble(cursor.getColumnIndexOrThrow(COL_EXP_AMOUNT)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COL_EXP_DATE)),
                    cursor.getInt(cursor.getColumnIndexOrThrow(COL_EXP_CATEGORY_ID)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COL_EXP_DESCRIPTION))
                );
                exp.setCategoryName(cursor.getString(cursor.getColumnIndexOrThrow(COL_CAT_NAME)));
                exp.setCategoryIcon(cursor.getString(cursor.getColumnIndexOrThrow(COL_CAT_ICON)));
                exp.setCategoryColor(cursor.getString(cursor.getColumnIndexOrThrow(COL_CAT_COLOR)));
                list.add(exp);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return list;
    }

    public double getTotalExpenseByMonth(int userId, int month, int year) {
        SQLiteDatabase db = this.getReadableDatabase();
        String monthStr = String.format("%04d-%02d", year, month);
        Cursor cursor = db.rawQuery(
            "SELECT SUM(" + COL_EXP_AMOUNT + ") FROM " + TABLE_EXPENSES +
            " WHERE " + COL_EXP_USER_ID + "=? AND strftime('%Y-%m', " + COL_EXP_DATE + ")=?",
            new String[]{String.valueOf(userId), monthStr});
        double total = 0;
        if (cursor.moveToFirst()) total = cursor.getDouble(0);
        cursor.close();
        db.close();
        return total;
    }

    public double getTotalExpenseByCategory(int userId, int categoryId, int month, int year) {
        SQLiteDatabase db = this.getReadableDatabase();
        String monthStr = String.format("%04d-%02d", year, month);
        Cursor cursor = db.rawQuery(
            "SELECT SUM(" + COL_EXP_AMOUNT + ") FROM " + TABLE_EXPENSES +
            " WHERE " + COL_EXP_USER_ID + "=? AND " + COL_EXP_CATEGORY_ID + "=?" +
            " AND strftime('%Y-%m', " + COL_EXP_DATE + ")=?",
            new String[]{String.valueOf(userId), String.valueOf(categoryId), monthStr});
        double total = 0;
        if (cursor.moveToFirst()) total = cursor.getDouble(0);
        cursor.close();
        db.close();
        return total;
    }

    // ==================== BUDGET OPERATIONS ====================

    public long setBudget(double amount, int month, int year, int categoryId, int userId) {
        SQLiteDatabase db = this.getWritableDatabase();
        // Update if exists, else insert
        Cursor cursor = db.query(TABLE_BUDGETS, new String[]{COL_ID},
            COL_BUD_MONTH + "=? AND " + COL_BUD_YEAR + "=? AND " +
            COL_BUD_CATEGORY_ID + "=? AND " + COL_BUD_USER_ID + "=?",
            new String[]{String.valueOf(month), String.valueOf(year),
                         String.valueOf(categoryId), String.valueOf(userId)},
            null, null, null);
        ContentValues cv = new ContentValues();
        cv.put(COL_BUD_AMOUNT, amount);
        cv.put(COL_BUD_MONTH, month);
        cv.put(COL_BUD_YEAR, year);
        cv.put(COL_BUD_CATEGORY_ID, categoryId);
        cv.put(COL_BUD_USER_ID, userId);
        long result;
        if (cursor != null && cursor.moveToFirst()) {
            int id = cursor.getInt(0);
            result = db.update(TABLE_BUDGETS, cv, COL_ID + "=?", new String[]{String.valueOf(id)});
            cursor.close();
        } else {
            result = db.insert(TABLE_BUDGETS, null, cv);
        }
        db.close();
        return result;
    }

    public List<Budget> getBudgetsByMonth(int userId, int month, int year) {
        List<Budget> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT b.*, c." + COL_CAT_NAME + ", c." + COL_CAT_ICON + ", c." + COL_CAT_COLOR +
                " FROM " + TABLE_BUDGETS + " b" +
                " LEFT JOIN " + TABLE_CATEGORIES + " c ON b." + COL_BUD_CATEGORY_ID + " = c." + COL_ID +
                " WHERE b." + COL_BUD_USER_ID + "=? AND b." + COL_BUD_MONTH + "=? AND b." + COL_BUD_YEAR + "=?";
        Cursor cursor = db.rawQuery(query,
            new String[]{String.valueOf(userId), String.valueOf(month), String.valueOf(year)});
        if (cursor.moveToFirst()) {
            do {
                Budget b = new Budget(
                    cursor.getInt(cursor.getColumnIndexOrThrow(COL_ID)),
                    cursor.getDouble(cursor.getColumnIndexOrThrow(COL_BUD_AMOUNT)),
                    cursor.getInt(cursor.getColumnIndexOrThrow(COL_BUD_MONTH)),
                    cursor.getInt(cursor.getColumnIndexOrThrow(COL_BUD_YEAR)),
                    cursor.getInt(cursor.getColumnIndexOrThrow(COL_BUD_CATEGORY_ID))
                );
                b.setCategoryName(cursor.getString(cursor.getColumnIndexOrThrow(COL_CAT_NAME)));
                b.setCategoryIcon(cursor.getString(cursor.getColumnIndexOrThrow(COL_CAT_ICON)));
                b.setCategoryColor(cursor.getString(cursor.getColumnIndexOrThrow(COL_CAT_COLOR)));
                list.add(b);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return list;
    }
}
