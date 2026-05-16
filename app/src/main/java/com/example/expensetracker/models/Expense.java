package com.example.expensetracker.models;

public class Expense {
    private int id;
    private double amount;
    private String date;
    private int categoryId;
    private String description;
    private String categoryName;
    private String categoryIcon;
    private String categoryColor;

    public Expense(int id, double amount, String date, int categoryId, String description) {
        this.id = id;
        this.amount = amount;
        this.date = date;
        this.categoryId = categoryId;
        this.description = description;
    }

    public int getId() { return id; }
    public double getAmount() { return amount; }
    public String getDate() { return date; }
    public int getCategoryId() { return categoryId; }
    public String getDescription() { return description; }
    public String getCategoryName() { return categoryName; }
    public String getCategoryIcon() { return categoryIcon; }
    public String getCategoryColor() { return categoryColor; }

    public void setAmount(double amount) { this.amount = amount; }
    public void setDate(String date) { this.date = date; }
    public void setCategoryId(int categoryId) { this.categoryId = categoryId; }
    public void setDescription(String description) { this.description = description; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
    public void setCategoryIcon(String categoryIcon) { this.categoryIcon = categoryIcon; }
    public void setCategoryColor(String categoryColor) { this.categoryColor = categoryColor; }
}
