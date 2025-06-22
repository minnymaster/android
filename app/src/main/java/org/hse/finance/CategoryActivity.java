package org.hse.finance;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.*;

import java.util.ArrayList;
import java.util.List;

public class CategoryActivity extends AppCompatActivity {

    private DB dbHelper;
    private CategoryAdapter adapter;
    private List<String> categoryList = new ArrayList<>();
    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category);

        dbHelper = new DB(this);
        recyclerView = findViewById(R.id.recycler_view_categories);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new CategoryAdapter(categoryList, categoryName -> {
            deleteCategory(categoryName);
        });

        recyclerView.setAdapter(adapter);

        loadCategories();

        findViewById(R.id.button_add_category).setOnClickListener(v -> {
            showAddDialog();
        });
    }

    private void loadCategories() {
        categoryList.clear();

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT cat_name FROM categories", null);
        if (cursor.moveToFirst()) {
            int index = cursor.getColumnIndex("cat_name");
            do {
                categoryList.add(cursor.getString(index));
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();

        adapter.updateData(categoryList);
    }

    private void deleteCategory(String name) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete("categories", "cat_name=?", new String[]{name});
        db.delete("spendings", "cat_name=?", new String[]{name});
        db.close();
        loadCategories();
    }

    private void showAddDialog() {
        final EditText input = new EditText(this);
        new AlertDialog.Builder(this)
                .setTitle("Новая категория")
                .setView(input)
                .setPositiveButton("Добавить", (dialog, which) -> {
                    String name = input.getText().toString().trim();
                    if (!name.isEmpty()) {
                        addCategory(name);
                    }
                })
                .setNegativeButton("Отмена", null)
                .show();
    }

    private void addCategory(String name) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("cat_name", name);
        db.insert("categories", null, cv);
        db.close();
        loadCategories();
    }
}
