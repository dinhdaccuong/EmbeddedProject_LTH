package com.cuongdd150481.hust.esp_luongtronghai;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.LegendEntry;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.formatter.IValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.utils.ViewPortHandler;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class MainActivity extends AppCompatActivity {
    //Spinner
    ArrayList<String> listSpinner;
    ArrayAdapter arrayAdapterSpinner;
    Spinner spinnerr;
    // Button
    Button buttonMeasure;
    Button buttonRelay1;
    Button buttonRelay2;

    //Line chart
    ArrayList<Entry> dataValTemperature;
    ArrayList<Entry> dataValHumidity;
    LineDataSet lineDataSetTemperature;
    LineDataSet lineDataSetHumidity;
    ArrayList<ILineDataSet> lineDataSets;
    LineData lineData;
    LineChart mpLineChart;

    //Process attribute
    boolean measuring;
    int iPeriod;
    int[] listPeriod;
    int orderReceiveData;
    ButtonProject relay1;
    ButtonProject relay2;
    ButtonProject measure;
    DatabaseReference myRef;
    Date dateTime;
    long currentTime = 0;
    ////////////////////////////////////////////////////////
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        paramInit();
        viewInit();
        dateTime = new Date();
        currentTime = dateTime.getTime();
        dataValTemperature = new ArrayList<Entry>();
        dataValHumidity = new ArrayList<Entry>();
        lineDataSetTemperature = new LineDataSet(null, null);
        lineDataSetHumidity = new LineDataSet(null, null);
        lineDataSets = new ArrayList<ILineDataSet>();
        mpLineChart.setNoDataText("Measurement is not performed");
        mpLineChart.setNoDataTextColor(Color.RED);
        Description description = new Description();
        description.setText("Time");
        mpLineChart.setDescription(description);

        // custom X - Axis
        XAxis xAxis = mpLineChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setDrawLabels(true);
        xAxis.setValueFormatter(new MyIAxisValueFormatter());

        //custom Y - Axis
        YAxis yAxisLeft = mpLineChart.getAxisLeft();
        yAxisLeft.setAxisMinimum(0);
        yAxisLeft.setAxisMaximum(100);
        YAxis yAxisRight = mpLineChart.getAxisRight();
        yAxisRight.setEnabled(false);
        // custom line chart
        lineDataSetTemperature.setColors(Color.RED);
        lineDataSetTemperature.setCircleColor(Color.RED);
        lineDataSetTemperature.setValueFormatter(new IValueFormatter() {
            @Override
            public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {
                return value + "°C";
            }
        });

        lineDataSetHumidity.setValueFormatter(new IValueFormatter() {
            @Override
            public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {
                return value + "%";
            }
        });

        // custom legend
        Legend legend = mpLineChart.getLegend();
        legend.setEnabled(true);
        legend.setForm(Legend.LegendForm.LINE);

        final LegendEntry[] listLegendEntry = new LegendEntry[2];
        LegendEntry legendEntryTemp = new LegendEntry();
        legendEntryTemp.formColor = Color.RED;
        legendEntryTemp.label = "Temperature";
        listLegendEntry[0] = legendEntryTemp;
        LegendEntry legendEntryHum = new LegendEntry();
        legendEntryHum.formColor = lineDataSetHumidity.getColor();
        legendEntryHum.label = "Humidity";
        listLegendEntry[1] = legendEntryHum;
        legend.setCustom(listLegendEntry);
        firebaseInit();

        myRef.child("Data").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                String temp = dataSnapshot.child("Temp").getValue().toString();
                String humi = dataSnapshot.child("Humi").getValue().toString();
                float ftemp = Float.parseFloat(temp);
                float fhumi = Float.parseFloat(humi);
                orderReceiveData = orderReceiveData + listPeriod[iPeriod];
                Entry entryTemp = new Entry(orderReceiveData, ftemp);
                Entry entryHumi = new Entry(orderReceiveData, fhumi);
                dataValTemperature.add(entryTemp);
                dataValHumidity.add(entryHumi);
                showChart(dataValTemperature, dataValHumidity);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        myRef.child("Measure_response").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String data = dataSnapshot.getValue().toString();
                if (data.equals("ON")) {
                    measure.setStatus("ON");
                    myRef.child("Measure").setValue("ON");
                    buttonMeasure.setBackgroundColor(Color.GREEN);
                } else {
                    measure.setStatus("OFF");
                    myRef.child("Measure").setValue("OFF");
                    buttonMeasure.setBackgroundColor(Color.rgb(0xD0, 0xF0, 0xA0));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        myRef.child("Relay1_response").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String data = dataSnapshot.getValue().toString();
                if (data.equals("ON")) {
                    myRef.child("Relay1").setValue("ON");
                    buttonRelay1.setBackgroundColor(Color.GREEN);
                } else {
                    myRef.child("Relay1").setValue("OFF");
                    buttonRelay1.setBackgroundColor(Color.rgb(0xD0, 0xF0, 0xA0));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        myRef.child("Relay2_response").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String data = dataSnapshot.getValue().toString();
                if (data.equals("ON")) {
                    myRef.child("Relay2").setValue("ON");
                    buttonRelay2.setBackgroundColor(Color.GREEN);
                } else {
                    myRef.child("Relay2").setValue("OFF");
                    buttonRelay2.setBackgroundColor(Color.rgb(0xD0, 0xF0, 0xA0));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        spinnerr.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                iPeriod = position;
                if(measure.getStatus().equals("ON")){
                    myRef.child("Period").setValue(listPeriod[iPeriod]);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        buttonMeasure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (measure.getStatus().equals("ON")) {
                    myRef.child("Measure").setValue("OFF");
                    measure.setStatus("OFF");
                } else {
                    myRef.child("Measure").setValue("ON");
                    measure.setStatus("ON");
                    myRef.child("Period").setValue(listPeriod[iPeriod]);
                }
            }
        });
        //Button Relay1
        buttonRelay1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (relay1.getStatus().equals("ON")) {
                    myRef.child("Relay1").setValue("OFF");
                    relay1.setStatus("OFF");
                } else {
                    myRef.child("Relay1").setValue("ON");
                    relay1.setStatus("ON");
                }

            }
        });

        //Button Relay2
        buttonRelay2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (relay2.getStatus().equals("ON")) {
                    myRef.child("Relay2").setValue("OFF");
                    relay2.setStatus("OFF");
                } else {
                    myRef.child("Relay2").setValue("ON");
                    relay2.setStatus("ON");
                }
            }
        });
    }

    private class MyIAxisValueFormatter implements IAxisValueFormatter{
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        @Override

        public String getFormattedValue(float value, AxisBase axis) {
            Date dateeee = new Date();
            long timeeee = dateeee.getTime() + (long)value*1000;
            String time = sdf.format(timeeee).toString();
            Log.d("debug_cuong", ""+ time);
            return time;
        }
    }
    private void showChart(ArrayList<Entry> datavalTemp, ArrayList<Entry> datavalHum) {
        lineDataSetTemperature.setValues(datavalTemp);
        lineDataSetTemperature.setLabel("Temperature");
        lineDataSetHumidity.setValues(datavalHum);
        lineDataSetHumidity.setLabel("Humidity");
        lineDataSets.clear();
        lineDataSets.add(lineDataSetTemperature);
        lineDataSets.add(lineDataSetHumidity);
        lineData = new LineData(lineDataSets);
        mpLineChart.setData(lineData);
        mpLineChart.invalidate();
    }

    private void paramInit() {
        relay1 = new ButtonProject("OFF");
        relay2 = new ButtonProject("OFF");
        measure = new ButtonProject("OFF");
        iPeriod = 0;
        orderReceiveData = 0;
        listPeriod = new int[]{5, 30, 60, 300};
    }

    private void firebaseInit() {
        myRef = FirebaseDatabase.getInstance().getReference();
        myRef.child("Data").removeValue();
        myRef.child("Data").setValue("");
    }

    private void viewInit() {
        mpLineChart = (LineChart) findViewById(R.id.line_chart);
        buttonMeasure = (Button) findViewById(R.id.button_measure);
        buttonRelay1 = (Button) findViewById(R.id.button_relay1);
        buttonRelay2 = (Button) findViewById(R.id.button_relay2);
        //view spinner init
        spinnerr = (Spinner) findViewById(R.id.spinner);
        listSpinner = new ArrayList<String>();
        listSpinner.add("5s");
        listSpinner.add("30s");
        listSpinner.add("1min");
        listSpinner.add("5min");
        arrayAdapterSpinner = new ArrayAdapter(this, android.R.layout.simple_spinner_item, listSpinner);
        arrayAdapterSpinner.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerr.setAdapter(arrayAdapterSpinner);
    }

    private void lineChartInit() {
    }
}
