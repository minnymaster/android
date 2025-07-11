package org.hse.finance;

import android.content.ContentValues;
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

    public static final int DB_VERS = 2;
    public static final String DB_NAME = "Finance";
    public static final String T_CAT = "categories";
    public static final String T_SPEND = "spendings";
    public static final String CAT_ID = "_id";
    public static final String CAT_NAME = "cat_name";
    public static final String SPEND_ID = "_id";
    public static final String SPEND_NAME = "spend_name";
    public static final String SPEND_CAT = "cat_id";
    public static final String SPEND_COST = "spend_cost";
    public static final String SPEND_DATE = "spend_date";
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
                SPEND_CAT + " INTEGER NOT NULL," +              // INTEGER, а не TEXT
                SPEND_COST + " INTEGER NOT NULL," +
                SPEND_DATE + " TEXT," +
                "FOREIGN KEY (" + SPEND_CAT + ") REFERENCES " + T_CAT + "(" + CAT_ID + "))";
        db.execSQL(createSpendTable);

        // Добавляем категории
        db.execSQL("INSERT OR IGNORE INTO " + T_CAT + "(" + CAT_NAME + ") VALUES ('Еда')");
        db.execSQL("INSERT OR IGNORE INTO " + T_CAT + "(" + CAT_NAME + ") VALUES ('Транспорт')");
        db.execSQL("INSERT OR IGNORE INTO " + T_CAT + "(" + CAT_NAME + ") VALUES ('Развлечения')");

        int foodCatId = getCategoryIdByName(db, "Еда");
        int transportCatId = getCategoryIdByName(db, "Транспорт");
        int funCatId = getCategoryIdByName(db, "Развлечения");

        // Добавляем траты
        db.execSQL("INSERT OR IGNORE INTO " + T_SPEND + "(" + SPEND_NAME + "," + SPEND_CAT + "," + SPEND_COST + "," + SPEND_DATE + ") " +
                "VALUES ('Обед', " + foodCatId + ", 500, '2025-06-18')");
        db.execSQL("INSERT OR IGNORE INTO " + T_SPEND + "(" + SPEND_NAME + "," + SPEND_CAT + "," + SPEND_COST + "," + SPEND_DATE + ") " +
                "VALUES ('Такси'," + transportCatId + " , 300, '2025-06-19')");
        db.execSQL("INSERT OR IGNORE INTO " + T_SPEND + "(" + SPEND_NAME + "," + SPEND_CAT + "," + SPEND_COST + "," + SPEND_DATE + ") " +
                "VALUES ('Кино', " + funCatId + ", 700, '2025-06-20')");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + T_SPEND);
        db.execSQL("DROP TABLE IF EXISTS " + T_CAT);
        onCreate(db);
    }

    private int getCategoryIdByName(SQLiteDatabase db, String catName) {
        int id = -1;
        Cursor cursor = db.rawQuery("SELECT " + CAT_ID + " FROM " + T_CAT + " WHERE " + CAT_NAME + "=?", new String[]{catName});
        if (cursor.moveToFirst()) {
            id = cursor.getInt(cursor.getColumnIndexOrThrow(CAT_ID));
        }
        cursor.close();
        return id;
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

            Log.d("DEBUG", "Категории расходов: найдено строк -> " + cursor.getCount());
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

    public boolean deleteCategory(String name) {
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            // Сначала удаляем траты с этой категорией (по cat_id)
            int catId = getCategoryIdByName(db, name);
            if (catId != -1) {
                db.delete(T_SPEND, SPEND_CAT + "=?", new String[]{String.valueOf(catId)});
            }
            // Затем удаляем саму категорию
            db.delete(T_CAT, CAT_NAME + "=?", new String[]{name});
            return true;
        } catch (Exception e) {
            Log.e("DB_ERROR", "Ошибка при удалении категории: " + name, e);
            return false;
        } finally {
            db.close();
        }
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

    public List<Expense> getExpenses(String categoryNameFilter, String startDate, String endDate) {
        List<Expense> expenses = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT s." + SPEND_ID + ", s." + SPEND_NAME + ", s." + SPEND_COST + ", " +
                "s." + SPEND_DATE + ", c." + CAT_NAME + " " +
                "FROM " + T_SPEND + " s " +
                "INNER JOIN " + T_CAT + " c ON s." + SPEND_CAT + " = c." + CAT_ID + " " +
                "WHERE 1=1";

        List<String> args = new ArrayList<>();

        if (categoryNameFilter != null) {
            query += " AND c." + CAT_NAME + "=?";
            args.add(categoryNameFilter);
        }

        if (startDate != null) {
            query += " AND s." + SPEND_DATE + ">=?";
            args.add(startDate);
        }

        if (endDate != null) {
            query += " AND s." + SPEND_DATE + "<=?";
            args.add(endDate);
        }

        Cursor cursor = db.rawQuery(query, args.toArray(new String[0]));

        if (cursor.moveToFirst()) {
            do {
                expenses.add(new Expense(
                        cursor.getInt(cursor.getColumnIndexOrThrow(SPEND_ID)),
                        cursor.getString(cursor.getColumnIndexOrThrow(SPEND_NAME)),
                        cursor.getDouble(cursor.getColumnIndexOrThrow(SPEND_COST)),
                        cursor.getString(cursor.getColumnIndexOrThrow(CAT_NAME)),
                        cursor.getString(cursor.getColumnIndexOrThrow(SPEND_DATE))
                ));
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return expenses;
    }

    public boolean addExpense(String name, int cost, String categoryName, String date) {
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            // Получаем ID категории по имени
            int catId = getCategoryIdByName(db, categoryName);
            if (catId == -1) {
                Log.e("DB_ERROR", "Категория не найдена: " + categoryName);
                return false;
            }

            // Вставляем новую трату
            ContentValues values = new ContentValues();
            values.put(SPEND_NAME, name);
            values.put(SPEND_CAT, catId);
            values.put(SPEND_COST, cost);
            values.put(SPEND_DATE, date);

            long result = db.insert(T_SPEND, null, values);
            return result != -1;  // true, если запись успешно добавлена
        } catch (Exception e) {
            Log.e("DB_ERROR", "Ошибка при добавлении траты", e);
            return false;
        } finally {
            db.close();
        }
    }

    public Expense getExpenseById(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Expense expense = null;

        String query = "SELECT s." + SPEND_ID + ", s." + SPEND_NAME + ", s." + SPEND_COST + ", " +
                "s." + SPEND_DATE + ", c." + CAT_NAME + " " +
                "FROM " + T_SPEND + " s " +
                "INNER JOIN " + T_CAT + " c ON s." + SPEND_CAT + " = c." + CAT_ID + " " +
                "WHERE s." + SPEND_ID + " = ?";

        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(id)});

        if (cursor.moveToFirst()) {
            expense = new Expense(
                    cursor.getInt(cursor.getColumnIndexOrThrow(SPEND_ID)),
                    cursor.getString(cursor.getColumnIndexOrThrow(SPEND_NAME)),
                    cursor.getDouble(cursor.getColumnIndexOrThrow(SPEND_COST)),
                    cursor.getString(cursor.getColumnIndexOrThrow(CAT_NAME)),
                    cursor.getString(cursor.getColumnIndexOrThrow(SPEND_DATE))
            );
        }

        cursor.close();
        db.close();
        return expense;
    }

    public boolean updateExpense(int id, String name, int cost, String categoryName, String date) {
        SQLiteDatabase db = this.getWritableDatabase();

        try {
            // Получаем ID категории по имени
            int catId = getCategoryIdByName(db, categoryName);
            if (catId == -1) {
                Log.e("DB_ERROR", "Категория не найдена: " + categoryName);
                return false;
            }

            ContentValues values = new ContentValues();
            values.put(SPEND_NAME, name);
            values.put(SPEND_CAT, catId);
            values.put(SPEND_COST, cost);
            values.put(SPEND_DATE, date);

            int rowsAffected = db.update(
                    T_SPEND,
                    values,
                    SPEND_ID + " = ?",
                    new String[]{String.valueOf(id)});

            return rowsAffected > 0;
        } catch (Exception e) {
            Log.e("DB_ERROR", "Ошибка при обновлении траты", e);
            return false;
        } finally {
            db.close();
        }
    }

    public boolean deleteExpense(int id) {
        SQLiteDatabase db = this.getWritableDatabase();

        try {
            int rowsAffected = db.delete(
                    T_SPEND,
                    SPEND_ID + " = ?",
                    new String[]{String.valueOf(id)});

            return rowsAffected > 0;
        } catch (Exception e) {
            Log.e("DB_ERROR", "Ошибка при удалении траты", e);
            return false;
        } finally {
            db.close();
        }
    }
}
