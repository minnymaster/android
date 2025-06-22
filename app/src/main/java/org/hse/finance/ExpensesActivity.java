package org.hse.finance;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class ExpensesActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private ExpenseAdapter adapter;
    private List<Expense> expenseList = new ArrayList<>();
    private DB dbHelper;
    private Spinner categorySpinner;
    private Button filterButton;
    private List<String> categories = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expenses);

        dbHelper = new DB(this);
        initViews();
        setupCategorySpinner();
        loadExpenses(null, null, null);
    }

    private void initViews() {
        recyclerView = findViewById(R.id.recyclerViewExpenses);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ExpenseAdapter(expenseList);
        recyclerView.setAdapter(adapter);

        categorySpinner = findViewById(R.id.spinnerCategories);
        filterButton = findViewById(R.id.buttonFilter);

        filterButton.setOnClickListener(v -> {
            String selectedCategory = categorySpinner.getSelectedItemPosition() > 0 ?
                    categories.get(categorySpinner.getSelectedItemPosition() - 1) : null;
            loadExpenses(selectedCategory, null, null);
        });
    }

    private void setupCategorySpinner() {
        categories = dbHelper.getAllCategories();
        List<String> spinnerItems = new ArrayList<>();
        spinnerItems.add("Все категории");
        spinnerItems.addAll(categories);

        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, spinnerItems);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(spinnerAdapter);
    }

    private void loadExpenses(String category, String startDate, String endDate) {
        expenseList.clear();
        expenseList.addAll(dbHelper.getExpenses(category, startDate, endDate));
        adapter.notifyDataSetChanged();
    }
}