package com.example.itog.ui.statistics;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.itog.R;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.RadarChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.RadarData;
import com.github.mikephil.charting.data.RadarDataSet;
import com.github.mikephil.charting.data.RadarEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import java.util.ArrayList;
import java.util.List;

public class StatisticsFragment extends Fragment {

    private BarChart barChart, barChart2;
    private RadarChart radarChart;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_statistics, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        barChart = view.findViewById(R.id.barChart);
        barChart2 = view.findViewById(R.id.barChart2);
        radarChart = view.findViewById(R.id.radarChart);

        setupBarChart(barChart);
        loadData(barChart);

        setupBarChart(barChart2);
        loadData2(barChart2);

        setupRadarChart(radarChart);
        loadRadarData(radarChart);
    }

    private void setupBarChart(BarChart chart) {
        chart.getDescription().setEnabled(false);
        chart.setDrawGridBackground(false);
        chart.setFitBars(true);

        chart.setTouchEnabled(false);
        chart.setDragEnabled(false);
        chart.setScaleEnabled(false);
        chart.setPinchZoom(false);

        chart.setExtraOffsets(10f, 0f, 10f, 0f);

        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);
        xAxis.setLabelCount(7);
        xAxis.setValueFormatter(new DayAxisFormatter());

        YAxis leftAxis = chart.getAxisLeft();
        leftAxis.setDrawGridLines(true);
        leftAxis.setAxisMinimum(0f);

        chart.getAxisRight().setEnabled(false);
        chart.getLegend().setEnabled(false);
    }

    private void loadData(BarChart chart) {
        List<BarEntry> entries = new ArrayList<>();
        entries.add(new BarEntry(0, 45));
        entries.add(new BarEntry(1, 67));
        entries.add(new BarEntry(2, 30));
        entries.add(new BarEntry(3, 82));
        entries.add(new BarEntry(4, 56));
        entries.add(new BarEntry(5, 90));
        entries.add(new BarEntry(6, 40));

        BarDataSet dataSet = new BarDataSet(entries, "Дни");
        dataSet.setColor(Color.parseColor("#6200EE"));
        dataSet.setValueTextSize(12f);

        BarData barData = new BarData(dataSet);
        barData.setBarWidth(0.3f);
        chart.setData(barData);
        chart.invalidate();
    }

    private void loadData2(BarChart chart) {
        List<BarEntry> entries = new ArrayList<>();
        entries.add(new BarEntry(0, 50));
        entries.add(new BarEntry(1, 20));
        entries.add(new BarEntry(2, 30));
        entries.add(new BarEntry(3, 10));
        entries.add(new BarEntry(4, 5));
        entries.add(new BarEntry(5, 9));
        entries.add(new BarEntry(6, 60));

        BarDataSet dataSet = new BarDataSet(entries, "Дни");
        dataSet.setColor(Color.parseColor("#FF6F00"));
        dataSet.setValueTextSize(12f);

        BarData barData = new BarData(dataSet);
        barData.setBarWidth(0.3f);
        chart.setData(barData);
        chart.invalidate();
    }

    private static class DayAxisFormatter extends ValueFormatter {
        private final String[] days = {"Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс"};

        @Override
        public String getAxisLabel(float value, AxisBase axis) {
            int index = (int) value;
            if (index >= 0 && index < days.length) {
                return days[index];
            }
            return "";
        }
    }

    private void setupRadarChart(RadarChart chart) {

        chart.getDescription().setEnabled(false);
        chart.setWebLineWidth(0.5f);
        chart.setWebColor(Color.LTGRAY);
        chart.setWebLineWidthInner(0.5f);
        chart.setWebColorInner(Color.LTGRAY);
        chart.setWebAlpha(100);

        chart.setRotationEnabled(false);

        XAxis xAxis = chart.getXAxis();
        xAxis.setTextSize(12f);
        xAxis.setValueFormatter(new PartyAxisFormatter());

        YAxis yAxis = chart.getYAxis();
        yAxis.setAxisMinimum(0f);
        yAxis.setTextSize(8f);
        yAxis.setDrawLabels(false);

        chart.getLegend().setEnabled(false);
    }

    private void loadRadarData(RadarChart chart) {
        List<RadarEntry> entries = new ArrayList<>();
        entries.add(new RadarEntry(200)); // Ладонь
        entries.add(new RadarEntry(160)); // Кулак
        entries.add(new RadarEntry(120)); // Рога
        entries.add(new RadarEntry(80));  // Ножницы
        entries.add(new RadarEntry(40));  // Указательный палец

        RadarDataSet dataSet = new RadarDataSet(entries, "Жесты");
        dataSet.setColor(Color.parseColor("#7209B7"));
        dataSet.setFillColor(Color.parseColor("#560BAD"));
        dataSet.setDrawFilled(true);
        dataSet.setFillAlpha(100);
        dataSet.setLineWidth(1.5f);
        dataSet.setDrawValues(false);

        RadarData radarData = new RadarData(dataSet);
        radarData.setValueTextSize(10f);
        chart.setData(radarData);
        chart.invalidate();
    }

    private static class PartyAxisFormatter extends ValueFormatter {
        private final String[] parties = {"Ладонь", "Кулак", "Рога", "Ножницы", "Палец"};

        @Override
        public String getAxisLabel(float value, AxisBase axis) {
            int index = (int) value;
            return (index >= 0 && index < parties.length) ? parties[index] : "";
        }
    }
}