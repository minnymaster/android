package org.hse.finance;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

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
        ImageButton homeBtn = findViewById(R.id.imageButton);
        ImageButton optionsBtn = findViewById(R.id.imageButton2);
        ImageButton spendingBtn = findViewById(R.id.imageButton3);

        // Добавьте обработчики кликов по кнопкам при необходимости
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
    }

    private void loadChartData() {
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
    protected void onDestroy() {
        dbHelper.close();
        super.onDestroy();
    }
}