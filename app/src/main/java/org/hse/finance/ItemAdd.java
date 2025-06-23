package org.hse.finance;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class ItemAdd extends AppCompatActivity {

    private DB dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.item_add); // Убедитесь, что используете правильный layout

        dbHelper = new DB(this);
        setupUI();
    }

    private void setupUI() {
        // Находим View элементы
        EditText nameInput = findViewById(R.id.expenseNameEditText);
        EditText amountInput = findViewById(R.id.expenseAmountEditText);
        AutoCompleteTextView categoryDropdown = findViewById(R.id.categorySpinner); // Изменено на AutoCompleteTextView
        EditText dateInput = findViewById(R.id.dateEditText);
        Button saveButton = findViewById(R.id.saveButton);

        // Настройка выпадающего списка категорий
        setupCategoryDropdown(categoryDropdown);

        saveButton.setOnClickListener(v -> {
            String name = nameInput.getText().toString();
            String amountStr = amountInput.getText().toString();
            String category = categoryDropdown.getText().toString();
            String date = dateInput.getText().toString();

            if (validateInput(name, amountStr, date)) {
                try {
                    int amount = Integer.parseInt(amountStr);
                    boolean success = dbHelper.addExpense(name, amount, category, date);

                    if (success) {
                        showToast("Трата сохранена!");
                        clearInputs();
                        setResult(RESULT_OK); // Указываем, что операция успешна
                        finish(); // Закрываем активность
                    } else {
                        showToast("Ошибка сохранения");
                    }
                } catch (NumberFormatException e) {
                    amountInput.setError("Некорректная сумма");
                }
            }
        });
    }

    private void setupCategoryDropdown(AutoCompleteTextView dropdown) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                dbHelper.getAllCategories()
        );
        dropdown.setAdapter(adapter);
    }

    private boolean validateInput(String name, String amount, String date) {
        boolean isValid = true;

        if (name.isEmpty()) {
            ((EditText)findViewById(R.id.expenseNameEditText)).setError("Введите название");
            isValid = false;
        }

        if (amount.isEmpty()) {
            ((EditText)findViewById(R.id.expenseAmountEditText)).setError("Введите сумму");
            isValid = false;
        }

        if (date.isEmpty()) {
            ((EditText)findViewById(R.id.dateEditText)).setError("Введите дату");
            isValid = false;
        }

        return isValid;
    }

    private void clearInputs() {
        ((EditText)findViewById(R.id.expenseNameEditText)).setText("");
        ((EditText)findViewById(R.id.expenseAmountEditText)).setText("");
        ((EditText)findViewById(R.id.dateEditText)).setText("");
        ((AutoCompleteTextView)findViewById(R.id.categorySpinner)).setText("");
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        dbHelper.close();
        super.onDestroy();
    }
}