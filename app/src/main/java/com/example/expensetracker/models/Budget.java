package com.example.expensetracker.models;

public class Budget {
    private int id;
    private double amount;
    private int month;
    private int year;
    private int categoryId;
    private String categoryName;
    private String categoryIcon;
    private String categoryColor;
    private double spent;

    public Budget(int id, double amount, int month, int year, int categoryId) {
        this.id = id;
        this.amount = amount;
        this.month = month;
        this.year = year;
        this.categoryId = categoryId;
    }

    public int getId() { return id; }
    public double getAmount() { return amount; }
    public int getMonth() { return month; }
    public int getYear() { return year; }
    public int getCategoryId() { return categoryId; }
    public String getCategoryName() { return categoryName; }
    public String getCategoryIcon() { return categoryIcon; }
    public String getCategoryColor() { return categoryColor; }
    public double getSpent() { return spent; }

    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
    public void setCategoryIcon(String categoryIcon) { this.categoryIcon = categoryIcon; }
    public void setCategoryColor(String categoryColor) { this.categoryColor = categoryColor; }
    public void setSpent(double spent) { this.spent = spent; }

    public double getPercentage() {
        if (amount == 0) return 0;
        return (spent / amount) * 100;
    }
}
