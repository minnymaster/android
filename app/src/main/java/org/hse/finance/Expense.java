package org.hse.finance;

public class Expense {
    private int id;
    private String name;
    private double amount;
    private String category;
    private String date;

    // Конструктор для создания из списка (без ID)
    public Expense(String name, double amount, String category, String date) {
        this(-1, name, amount, category, date);
    }

    // Полный конструктор (с ID)
    public Expense(int id, String name, double amount, String category, String date) {
        this.id = id;
        this.name = name;
        this.amount = amount;
        this.category = category;
        this.date = date;
    }


    public String getName() { return name; }
    public double getAmount() { return amount; }
    public String getCategory() { return category; }
    public String getDate() { return date; }

    public int getId() { return id; }
}