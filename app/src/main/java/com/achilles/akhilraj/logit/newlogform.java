package com.achilles.akhilraj.logit;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class newlogform extends AppCompatActivity {

    public String[] logtype = {"Beach","Road Trip","Biking","Hiking"};
    public Integer[] imgmap = {R.drawable.imgbeach,R.drawable.imgrdtrip,R.drawable.imgbike,R.drawable.imghike};
    private DBHelper mydb ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_newlogform);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("New Log");
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        final Intent intent = getIntent();

        final Spinner myspinner = (Spinner) findViewById(R.id.spinnernewlogform);
        myspinner.setAdapter(new MyAdapter(this, R.layout.newlogformspinnerlayout, logtype));

        mydb = new DBHelper(this);
        final EditText editText = (EditText) findViewById(R.id.editTextnewlogform);
        Button button = (Button) findViewById(R.id.sumbitbuttonnewlogform);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String name = editText.getText().toString();
                int position = myspinner.getSelectedItemPosition();

                if (name.length() > 0) {
                    if (mydb.insert(name,imgmap[position]))      // making entry in the DB
                    {
                        Toast.makeText(newlogform.this, "log created", Toast.LENGTH_LONG).show();
                        Intent temp = new Intent(getApplicationContext(),MainActivity.class);
                        startActivity(temp);
                       // setResult(Activity.RESULT_OK,intent);
                       // finish();

                    } else {

                        //setResult(Activity.RESULT_CANCELED,intent);
                        //finish();
                    }
                }
                else{
                    Toast.makeText(newlogform.this, "Enter name", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public class MyAdapter extends ArrayAdapter<String> {

        public MyAdapter(Context context, int textViewResourceId,   String[] objects) {
            super(context, textViewResourceId, objects);
        }

        @Override
        public View getDropDownView(int position, View convertView,ViewGroup parent) {
            return getCustomView(position, convertView, parent);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            return getCustomView(position, convertView, parent);
        }

        public View getCustomView(int position, View convertView, ViewGroup parent) {

            LayoutInflater inflater=getLayoutInflater();
            View row=inflater.inflate(R.layout.newlogformspinnerlayout, parent, false);
            TextView label=(TextView)row.findViewById(R.id.textViewnewformlog);
            label.setText(logtype[position]);

            ImageView icon=(ImageView)row.findViewById(R.id.imageViewnewlogform);
            icon.setImageResource(imgmap[position]);

            return row;
        }
    }
}

