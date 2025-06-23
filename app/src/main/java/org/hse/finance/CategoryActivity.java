package org.hse.finance;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

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
    private boolean dataChanged = false; // Флаг изменения данных

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category);

        dbHelper = new DB(this);
        recyclerView = findViewById(R.id.recycler_view_categories);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new CategoryAdapter(categoryList, categoryName -> {
            boolean success = dbHelper.deleteCategory(categoryName);
            if (success) {
                Toast.makeText(this, "Категория удалена", Toast.LENGTH_SHORT).show();
                loadCategories();
                dataChanged = true; // Устанавливаем флаг изменения
            } else {
                Toast.makeText(this, "Ошибка удаления", Toast.LENGTH_SHORT).show();
            }
        });

        recyclerView.setAdapter(adapter);

        loadCategories();

        findViewById(R.id.button_add_category).setOnClickListener(v -> showAddDialog());
    }

    private void loadCategories() {
        categoryList.clear();
        categoryList.addAll(dbHelper.getAllCategories());
        adapter.updateData(categoryList);
    }

    private void showAddDialog() {
        final EditText input = new EditText(this);
        new AlertDialog.Builder(this)
                .setTitle("Новая категория")
                .setView(input)
                .setPositiveButton("Добавить", (dialog, which) -> {
                    String name = input.getText().toString().trim();
                    if (!name.isEmpty()) {
                        boolean success = dbHelper.addCategory(name);
                        if (success) {
                            Toast.makeText(this, "Категория добавлена", Toast.LENGTH_SHORT).show();
                            loadCategories();
                        } else {
                            Toast.makeText(this, "Такая категория уже есть", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .setNegativeButton("Отмена", null)
                .show();
    }

    @Override
    public void onBackPressed() {
        // Устанавливаем результат только если были изменения
        if (dataChanged) {
            setResult(RESULT_OK);
        }
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        // Аналогично для других способов закрытия активности
        if (dataChanged && isFinishing()) {
            setResult(RESULT_OK);
        }
        super.onDestroy();
    }
}