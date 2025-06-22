package org.hse.finance;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DB extends SQLiteOpenHelper {

    public static final int DB_VERS = 1;
    public static final String DB_NAME = "Finance";
    public static final String T_CAT = "categories";
    public static final String T_SPEND = "spendings";
    public static final String CAT_ID = "_id";
    public static final String CAT_NAME = "cat_name";
    public static final String SPEND_ID = "_id";
    public static final String SPEND_NAME = "spend_name";
    public static final String SPEND_CAT = "cat_name";
    public static final String SPEND_COST = "spend_cost";


    public DB(@Nullable Context context) {
        super(context, DB_NAME, null, DB_VERS);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Создаем таблицу категорий
        String createCatTable = "CREATE TABLE " + T_CAT + "(" +
                CAT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                CAT_NAME + " TEXT UNIQUE NOT NULL)";
        db.execSQL(createCatTable);

        // Создаем таблицу трат
        String createSpendTable = "CREATE TABLE " + T_SPEND + "(" +
                SPEND_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                SPEND_NAME + " TEXT NOT NULL," +
                SPEND_CAT + " TEXT NOT NULL," +
                SPEND_COST + " INTEGER NOT NULL," +
                "FOREIGN KEY (" + SPEND_CAT + ") REFERENCES " + T_CAT + "(" + CAT_NAME + "))";

        // сразу вставляем тестовые данные
        db.execSQL("INSERT OR IGNORE INTO " + T_CAT + "(" + CAT_NAME + ") VALUES ('Еда')");
        db.execSQL("INSERT OR IGNORE INTO " + T_CAT + "(" + CAT_NAME + ") VALUES ('Транспорт')");
        db.execSQL("INSERT OR IGNORE INTO " + T_CAT + "(" + CAT_NAME + ") VALUES ('Развлечения')");

        db.execSQL(createSpendTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + T_SPEND);
        db.execSQL("DROP TABLE IF EXISTS " + T_CAT);
        onCreate(db);
    }

    public Map<String, Integer> getCategorySums() {
        Map<String, Integer> result = new HashMap<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;

        try {
            // Модифицированный запрос с JOIN к таблице категорий
            String query = "SELECT " + T_CAT + "." + CAT_NAME + ", " +
                    "SUM(" + T_SPEND + "." + SPEND_COST + ") as total " +
                    "FROM " + T_SPEND + " " +
                    "INNER JOIN " + T_CAT + " ON " +
                    T_SPEND + "." + SPEND_CAT + " = " + T_CAT + "." + CAT_ID + " " +
                    "GROUP BY " + T_CAT + "." + CAT_NAME;

            cursor = db.rawQuery(query, null);

            if (cursor != null && cursor.moveToFirst()) {
                int catNameIndex = cursor.getColumnIndex(CAT_NAME);
                int totalIndex = cursor.getColumnIndex("total");

                do {
                    String categoryName = cursor.getString(catNameIndex);
                    int total = cursor.getInt(totalIndex);
                    result.put(categoryName, total);
                } while (cursor.moveToNext());
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            db.close();
        }

        return result;
    }

    public boolean addCategory(String categoryName) {
        SQLiteDatabase db = this.getWritableDatabase();

        try {
            db.execSQL("INSERT INTO " + T_CAT + " (" + CAT_NAME + ") VALUES (?)", new Object[]{categoryName});
            return true;
        } catch (Exception e) {
            Log.e("DB_ERROR", "Ошибка при добавлении категории: " + categoryName, e);
            return false;
        } finally {
            db.close();
        }
    }

    public List<String> getAllCategories() {
        List<String> categories = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;

        try {
            cursor = db.rawQuery("SELECT " + CAT_NAME + " FROM " + T_CAT + " ORDER BY " + CAT_NAME, null);
            if (cursor.moveToFirst()) {
                int nameIndex = cursor.getColumnIndex(CAT_NAME);
                do {
                    categories.add(cursor.getString(nameIndex));
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e("DB_ERROR", "Ошибка при получении категорий", e);
        } finally {
            if (cursor != null) cursor.close();
            db.close();
        }

        return categories;
    }
    public List<String> getAllCategories() {
        List<String> categories = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT " + CAT_NAME + " FROM " + T_CAT, null);

        if (cursor.moveToFirst()) {
            do {
                categories.add(cursor.getString(0));
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return categories;
    }

    public List<Expense> getExpenses(String category, String startDate, String endDate) {
        List<Expense> expenses = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT * FROM " + T_SPEND + " WHERE 1=1";
        List<String> args = new ArrayList<>();

        if (category != null) {
            query += " AND " + SPEND_CAT + "=?";
            args.add(category);
        }

        if (startDate != null) {
            query += " AND " + SPEND_DATE + ">=?";
            args.add(startDate);
        }

        if (endDate != null) {
            query += " AND " + SPEND_DATE + "<=?";
            args.add(endDate);
        }

        Cursor cursor = db.rawQuery(query, args.toArray(new String[0]));

        if (cursor.moveToFirst()) {
            do {
                expenses.add(new Expense(
                        cursor.getString(cursor.getColumnIndexOrThrow(SPEND_NAME)),
                        cursor.getDouble(cursor.getColumnIndexOrThrow(SPEND_COST)),
                        cursor.getString(cursor.getColumnIndexOrThrow(SPEND_CAT)),
                        cursor.getString(cursor.getColumnIndexOrThrow(SPEND_DATE))
                ));
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return expenses;
    }

    public List<String> getAllCategories() {
        List<String> categories = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT " + CAT_NAME + " FROM " + T_CAT, null);

        if (cursor.moveToFirst()) {
            do {
                categories.add(cursor.getString(0));
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return categories;
    }

    public List<Expense> getExpenses(String category, String startDate, String endDate) {
        List<Expense> expenses = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT * FROM " + T_SPEND + " WHERE 1=1";
        List<String> args = new ArrayList<>();

        if (category != null) {
            query += " AND " + SPEND_CAT + "=?";
            args.add(category);
        }

        if (startDate != null) {
            query += " AND " + SPEND_DATE + ">=?";
            args.add(startDate);
        }

        if (endDate != null) {
            query += " AND " + SPEND_DATE + "<=?";
            args.add(endDate);
        }

        Cursor cursor = db.rawQuery(query, args.toArray(new String[0]));

        if (cursor.moveToFirst()) {
            do {
                expenses.add(new Expense(
                        cursor.getString(cursor.getColumnIndexOrThrow(SPEND_NAME)),
                        cursor.getDouble(cursor.getColumnIndexOrThrow(SPEND_COST)),
                        cursor.getString(cursor.getColumnIndexOrThrow(SPEND_CAT)),
                        cursor.getString(cursor.getColumnIndexOrThrow(SPEND_DATE))
                ));
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return expenses;
    }
}
