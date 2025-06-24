package org.hse.finance;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class ItemEdit extends AppCompatActivity {

    private DB dbHelper;
    private int expenseId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.item_edit);

        dbHelper = new DB(this);
        setupUI();
        setupCloseKeyboardOnTouchOutside();

        // Получаем данные из Intent
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("expense_id")) {
            expenseId = intent.getIntExtra("expense_id", -1);
            loadExpenseData(expenseId);
        }
    }

    private void loadExpenseData(int id) {
        Expense expense = dbHelper.getExpenseById(id);
        if (expense != null) {
            ((TextView) findViewById(R.id.expenseIdTextView)).setText(String.valueOf(expense.getId()));
            ((EditText) findViewById(R.id.expenseNameEditText)).setText(expense.getName());
            ((EditText) findViewById(R.id.expenseAmountEditText)).setText(String.valueOf((int) expense.getAmount()));

            // Получаем AutoCompleteTextView
            AutoCompleteTextView categoryDropdown = findViewById(R.id.categorySpinner);

            // Устанавливаем текущую категорию
            categoryDropdown.setText(expense.getCategory(), false); // false - не фильтровать

            ((EditText) findViewById(R.id.dateEditText)).setText(expense.getDate());
        }
    }

    private void setupUI() {
        // Находим View элементы
        EditText nameInput = findViewById(R.id.expenseNameEditText);
        EditText amountInput = findViewById(R.id.expenseAmountEditText);
        AutoCompleteTextView categoryDropdown = findViewById(R.id.categorySpinner);
        EditText dateInput = findViewById(R.id.dateEditText);
        Button saveButton = findViewById(R.id.saveButton);
        Button deleteButton = findViewById(R.id.deleteButton);

        // Настройка выпадающего списка категорий
        setupCategoryDropdown(categoryDropdown);

        // Настройка DatePicker для поля даты
        dateInput.setOnClickListener(v -> showDatePickerDialog());
        dateInput.setFocusable(false);
        dateInput.setClickable(true);

        saveButton.setOnClickListener(v -> {
            String name = nameInput.getText().toString();
            String amountStr = amountInput.getText().toString();
            String category = categoryDropdown.getText().toString();
            String date = dateInput.getText().toString();

            if (validateInput(name, amountStr, date)) {
                try {
                    int amount = Integer.parseInt(amountStr);
                    boolean success = dbHelper.updateExpense(
                            expenseId,
                            name,
                            amount,
                            category,
                            date
                    );

                    if (success) {
                        showToast("Трата обновлена!");
                        setResult(RESULT_OK);
                        finish();
                    } else {
                        showToast("Ошибка обновления");
                    }
                } catch (NumberFormatException e) {
                    amountInput.setError("Некорректная сумма");
                }
            }
        });

        deleteButton.setOnClickListener(v -> {
            boolean success = dbHelper.deleteExpense(expenseId);
            if (success) {
                showToast("Трата удалена");
                setResult(RESULT_OK);
                finish();
            } else {
                showToast("Ошибка удаления");
            }
        });
    }

    private void setupCategoryDropdown(AutoCompleteTextView dropdown) {
        List<String> categories = dbHelper.getAllCategories();
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                categories
        );
        dropdown.setAdapter(adapter);

        // Убедимся, что dropdown можно редактировать
        dropdown.setFocusable(true);
        dropdown.setFocusableInTouchMode(true);
        dropdown.setClickable(true);
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
        } else if (!isValidDate(date)) {
            ((EditText)findViewById(R.id.dateEditText)).setError("Некорректный формат даты (ГГГГ-ММ-ДД)");
            isValid = false;
        }

        return isValid;
    }

    private boolean isValidDate(String date) {
        if (date == null || !date.matches("\\d{4}-\\d{2}-\\d{2}")) {
            return false;
        }

        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            sdf.setLenient(false);
            sdf.parse(date);
            return true;
        } catch (ParseException e) {
            return false;
        }
    }

    private void showDatePickerDialog() {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    String selectedDate = String.format(Locale.getDefault(),
                            "%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay);
                    ((EditText)findViewById(R.id.dateEditText)).setText(selectedDate);
                },
                year, month, day);
        datePickerDialog.show();
    }

    private void setupCloseKeyboardOnTouchOutside() {
        View rootView = findViewById(android.R.id.content);
        rootView.setOnTouchListener((v, event) -> {
            hideKeyboard();
            return false;
        });
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        View view = getCurrentFocus();
        if (view == null) {
            view = new View(this);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
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