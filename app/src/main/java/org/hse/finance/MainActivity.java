package org.hse.finance;

import android.os.Bundle;
import android.view.View;
import android.content.Intent;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    private DB dbHelper;
    private PieChart pieChart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Инициализация базы данных
        dbHelper = new DB(this);

        // Инициализация диаграммы
        pieChart = findViewById(R.id.pieChart);
        setupPieChart();

        // Загрузка данных в диаграмму
        loadChartData();

        // Обработчики кнопок (если нужно)
        LinearLayout qrScanerBtn = findViewById(R.id.buttonQrScan);
        LinearLayout addSpendingBtn = findViewById(R.id.buttonAddExpense);
        LinearLayout spendingBtn = findViewById(R.id.buttonExpenses);
        LinearLayout categoryBtn = findViewById(R.id.buttonCategories);

        categoryBtn.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, CategoryActivity.class);
            startActivityForResult(intent, 1);
        });

        qrScanerBtn.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, QrScannerActivity.class);
            startActivityForResult(intent, 100);
        });

        spendingBtn.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ExpensesActivity.class);
            startActivityForResult(intent, 1);
        });

        addSpendingBtn.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ItemAdd.class);
            startActivityForResult(intent, 1); // Используем startActivityForResult с requestCode
        });


    }





    private void setupPieChart() {
        pieChart.setUsePercentValues(true);
        pieChart.getDescription().setEnabled(false);
        pieChart.setExtraOffsets(5, 10, 5, 5);
        pieChart.setDragDecelerationFrictionCoef(0.95f);
        pieChart.setDrawHoleEnabled(true);
        pieChart.setHoleColor(android.R.color.transparent);
        pieChart.setTransparentCircleRadius(61f);
        pieChart.animateY(1000);
        pieChart.getLegend().setEnabled(false);
    }

    public void loadChartData() {
        // Получаем данные из БД
        Map<String, Integer> categorySums = dbHelper.getCategorySums();

        // Подготавливаем данные для диаграммы
        List<PieEntry> entries = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : categorySums.entrySet()) {
            entries.add(new PieEntry(entry.getValue(), entry.getKey()));
        }

        // Настраиваем набор данных
        PieDataSet dataSet = new PieDataSet(entries, "Категории расходов");
        dataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        dataSet.setSliceSpace(3f);
        dataSet.setSelectionShift(5f);

        // Создаем объект данных и устанавливаем его в диаграмму
        PieData data = new PieData(dataSet);
        data.setValueTextSize(10f);
        pieChart.setData(data);

        // Обновляем диаграмму
        pieChart.invalidate();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 100 && resultCode == RESULT_OK) {
            String amount = data.getStringExtra("qr_amount");
            String date = data.getStringExtra("qr_date");
            if (amount == null || amount.isEmpty()) {
                // Если сумма не распознана
                Toast.makeText(this, "Не удалось прочитать данные QR-кода", Toast.LENGTH_LONG).show();
            } else {
                // Форматируем дату из формата чека (t=20240101T1200) в yyyy-MM-dd
                String formattedDate = formatDateFromQR(date);

                // Запускаем ItemAdd с предзаполненными данными
                Intent addItemIntent = new Intent(this, ItemAdd.class);
                addItemIntent.putExtra("prefilled_amount", amount);
                addItemIntent.putExtra("prefilled_date", formattedDate);
                startActivityForResult(addItemIntent, 1);
            }
        } else if (requestCode == 1 && resultCode == RESULT_OK) {
            loadChartData();
            Toast.makeText(this, "Данные обновлены", Toast.LENGTH_SHORT).show();
        }
    }

    // Преобразуем дату из формата 20240101T1200 в 2024-01-01
    private String formatDateFromQR(String qrDate) {
        if (qrDate == null || qrDate.length() < 8) return "";
        try {
            String year = qrDate.substring(0, 4);
            String month = qrDate.substring(4, 6);
            String day = qrDate.substring(6, 8);
            return year + "-" + month + "-" + day;
        } catch (Exception e) {
            return "";
        }
    }

    @Override
    protected void onDestroy() {
        dbHelper.close();
        super.onDestroy();
    }
}