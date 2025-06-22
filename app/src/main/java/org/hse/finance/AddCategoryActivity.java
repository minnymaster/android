package org.hse.finance;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import org.hse.finance.DB;

public class AddCategoryActivity extends AppCompatActivity {

    private EditText editTextCategoryName;
    private Button buttonAddCategory;
    private DB db;

    private ListView categoryListView;
    private ArrayAdapter<String> categoryAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_category);

        editTextCategoryName = findViewById(R.id.editTextCategoryName);
        buttonAddCategory = findViewById(R.id.buttonAddCategory);
        categoryListView = findViewById(R.id.categoryListView);
        db = new DB(this);

        // Подключаем адаптер
        categoryAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, db.getAllCategories());
        categoryListView.setAdapter(categoryAdapter);

        buttonAddCategory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String categoryName = editTextCategoryName.getText().toString().trim();

                if (categoryName.isEmpty()) {
                    Toast.makeText(AddCategoryActivity.this, "Введите название категории", Toast.LENGTH_SHORT).show();
                    return;
                }

                boolean success = db.addCategory(categoryName);
                if (success) {
                    Toast.makeText(AddCategoryActivity.this, "Категория добавлена", Toast.LENGTH_SHORT).show();
                    editTextCategoryName.setText("");
                    updateCategoryList();  // Обновим список
                } else {
                    Toast.makeText(AddCategoryActivity.this, "Категория уже существует", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void updateCategoryList() {
        categoryAdapter.clear();
        categoryAdapter.addAll(db.getAllCategories());
        categoryAdapter.notifyDataSetChanged();
    }
}