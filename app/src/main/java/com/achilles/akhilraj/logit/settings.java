package com.achilles.akhilraj.logit;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.Spinner;

import java.util.ArrayList;
import java.util.List;

public class settings extends AppCompatActivity {


    SharedPreferences setpref;
    SharedPreferences.Editor editor;
    Button b;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        final Spinner spinner = (Spinner) findViewById(R.id.spinnervideoqualitysettings);
        List<String> list = new ArrayList<String>();
        list.add("Low Quality");
        list.add("High Quality");
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, R.layout.settingspinner,list);
        spinner.setAdapter(dataAdapter);


        Button done = (Button) findViewById(R.id.buttondone);
        done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        setpref = PreferenceManager.getDefaultSharedPreferences(settings.this);
        editor = setpref.edit();

        b = (Button) findViewById(R.id.buttonsettings);

        b.setText(String.valueOf(setpref.getInt("maxtime", 5)) + " sec");

        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                show();
            }
        });

       spinner.setSelection(setpref.getInt("quality",1));
       spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
           @Override
           public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

               if(spinner.getSelectedItem().equals("High Quality"))
                    editor.putInt("quality", 1).commit();
               else
                    editor.putInt("quality",0).commit();
           }

           @Override
           public void onNothingSelected(AdapterView<?> parent) {

           }
       });


    }

    public void show()
    {


        final NumberPicker np = new NumberPicker(settings.this);
        String[] nums = {"3","4","5","6","7","8","9","10","11","12","13","14","15"};

        np.setMinValue(3);
        np.setMaxValue(15);
        np.setWrapSelectorWheel(false);
        np.setDisplayedValues(nums);
        np.setValue(5);

        AlertDialog.Builder dialogbuilder = new AlertDialog.Builder(settings.this);
        dialogbuilder.setTitle("Delete Video ?")
                .setView(np)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        editor.putInt("maxtime",np.getValue()).commit();
                        b.setText(String.valueOf(np.getValue())+" sec");
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
        AlertDialog alertDialog = dialogbuilder.create();
        alertDialog.show();


    }
}

