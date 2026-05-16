package com.example.expensetracker.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {
    private static final String PREF_NAME = "ExpenseTrackerSession";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";
    private static final String KEY_USER_ID = "userId";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_MONTHLY_SALARY_PREFIX = "monthlySalary_";

    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;

    public SessionManager(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = prefs.edit();
    }

    public void createLoginSession(int userId, String username) {
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.putInt(KEY_USER_ID, userId);
        editor.putString(KEY_USERNAME, username);
        editor.apply();
    }

    public boolean isLoggedIn() {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    public int getUserId() {
        return prefs.getInt(KEY_USER_ID, -1);
    }

    public String getUsername() {
        return prefs.getString(KEY_USERNAME, "");
    }

    public void setMonthlySalary(int userId, int month, int year, double salary) {
        editor.putFloat(getMonthlySalaryKey(userId, month, year), (float) salary);
        editor.apply();
    }

    public double getMonthlySalary(int userId, int month, int year) {
        return prefs.getFloat(getMonthlySalaryKey(userId, month, year), 0f);
    }

    private String getMonthlySalaryKey(int userId, int month, int year) {
        return KEY_MONTHLY_SALARY_PREFIX + userId + "_" + year + "_" + month;
    }

    public void logout() {
        editor.remove(KEY_IS_LOGGED_IN);
        editor.remove(KEY_USER_ID);
        editor.remove(KEY_USERNAME);
        editor.commit();
    }
}
