package org.hse.finance;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import java.util.HashMap;
import java.util.Map;

public class DB extends SQLiteOpenHelper {

    // Версия увеличена до 2 (для пересоздания базы с новой колонкой)
    public static final int DB_VERS = 2;
    public static final String DB_NAME = "Finance";

    public static final String T_CAT = "categories";
    public static final String T_SPEND = "spendings";

    public static final String CAT_ID = "_id";
    public static final String CAT_NAME = "cat_name";

    public static final String SPEND_ID = "_id";
    public static final String SPEND_NAME = "spend_name";
    public static final String SPEND_CAT = "cat_name";
    public static final String SPEND_COST = "spend_cost";
    public static final String SPEND_DATE = "spend_date"; // 👈 новое поле

    public DB(@Nullable Context context) {
        super(context, DB_NAME, null, DB_VERS);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Таблица категорий
        String createCatTable = "CREATE TABLE " + T_CAT + "(" +
                CAT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                CAT_NAME + " TEXT UNIQUE NOT NULL)";
        db.execSQL(createCatTable);

        // Таблица трат с датой
        String createSpendTable = "CREATE TABLE " + T_SPEND + "(" +
                SPEND_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                SPEND_NAME + " TEXT NOT NULL," +
                SPEND_CAT + " TEXT NOT NULL," +
                SPEND_COST + " INTEGER NOT NULL," +
                SPEND_DATE + " TEXT NOT NULL," +
                "FOREIGN KEY (" + SPEND_CAT + ") REFERENCES " + T_CAT + "(" + CAT_NAME + "))";
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

    public void addTestData() {
        SQLiteDatabase db = this.getWritableDatabase();

        // Добавляем категории
        db.execSQL("INSERT INTO " + T_CAT + "(" + CAT_NAME + ") VALUES ('Еда')");
        db.execSQL("INSERT INTO " + T_CAT + "(" + CAT_NAME + ") VALUES ('Транспорт')");
        db.execSQL("INSERT INTO " + T_CAT + "(" + CAT_NAME + ") VALUES ('Развлечения')");

        // Добавляем траты с датами (формат YYYY-MM-DD)
        db.execSQL("INSERT INTO " + T_SPEND + "(" + SPEND_NAME + "," + SPEND_CAT + "," + SPEND_COST + "," + SPEND_DATE + ") " +
                "VALUES ('Обед', 1, 500, '2025-06-22')");
        db.execSQL("INSERT INTO " + T_SPEND + "(" + SPEND_NAME + "," + SPEND_CAT + "," + SPEND_COST + "," + SPEND_DATE + ") " +
                "VALUES ('Такси', 2, 300, '2025-06-21')");
        db.execSQL("INSERT INTO " + T_SPEND + "(" + SPEND_NAME + "," + SPEND_CAT + "," + SPEND_COST + "," + SPEND_DATE + ") " +
                "VALUES ('Кино', 3, 700, '2025-06-20')");

        db.close();
    }
}
