package com.achilles.akhilraj.logit;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.text.TextPaint;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.github.amlcurran.showcaseview.ShowcaseView;
import com.github.amlcurran.showcaseview.targets.ActionItemTarget;
import com.github.amlcurran.showcaseview.targets.ActionViewTarget;
import com.github.amlcurran.showcaseview.targets.ViewTarget;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;


public class FirstFragment extends Fragment {

    private DBHelper mydb ;
    private CoordinatorLayout coordinatorLayout;
    ListView listview;
    View rootView;
    SharedPreferences setpref;
    SharedPreferences.Editor editor;

    static final int ADD_LOG_REQ=777;
    public static final int OPEN_LOG_REQ= 456;

    public FirstFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_first, container, false);
        listview = (ListView) rootView.findViewById(R.id.firstfragmentlistview1);
        mydb = new DBHelper(getContext());


        setpref = PreferenceManager.getDefaultSharedPreferences(getContext());
        editor= setpref.edit();
        if(setpref.getBoolean("tutaddlogbtn",true)) {
            //tutorial
            android.app.AlertDialog.Builder dialogbuilder = new android.app.AlertDialog.Builder(getContext());
            dialogbuilder.setTitle("Learn how to use the app?")
                    .setMessage("Hello! Notice that there are two tabs in the home screen! \n\nLogs tab - the tab lets you organize your videos into logs \n Merged Tab - the tab lists the Movies (merged videos)\nWatch the video walkthrough for a better experiance.")
                    .setPositiveButton("No", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            editor.putBoolean("tutaddlogbtn", false).commit();

                        }
                    })
                    .setNeutralButton("Skip for now", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    })
                    .setNegativeButton("Watch a Tutorial", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/watch?v=H-30B0cqh88")));
                            editor.putBoolean("tutaddlogbtn", false).commit();
                        }
                    });

            android.app.AlertDialog alertDialog = dialogbuilder.create();
            alertDialog.show();

        }


        Button button = (Button) rootView.findViewById(R.id.addnewlogbtnfragment1);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(getActivity(), newlogform.class);
                startActivityForResult(intent, ADD_LOG_REQ);


            }
        });


        //populating the loglist list view
        populateListView();

        listview.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

                Cursor cursor = (Cursor) parent.getItemAtPosition(position);
                final String tempid = cursor.getString(cursor.getColumnIndex(DBHelper.KEY_ID));

                android.app.AlertDialog.Builder dialogbuilder = new android.app.AlertDialog.Builder(getContext());
                dialogbuilder.setTitle("Choose Action")
                        .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                //deleting all corresponding videos
                                Cursor deletecursor = mydb.getVideo(tempid);
                                while (deletecursor.moveToNext()) {
                                    String temp = deletecursor.getString(deletecursor.getColumnIndex(DBHelper.VID_DIR));
                                    Uri uri = Uri.parse(temp);
                                    File file = new File(uri.getPath());
                                    file.delete();
                                }
                                //deleting all the video records
                                mydb.deletevideo(tempid);
                                boolean deleted =mydb.delete(tempid);
                                if (deleted) {
                                    Toast.makeText(getContext(), "Log deleted", Toast.LENGTH_SHORT).show();
                                    populateListView();
                                }
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {

                            }
                        });
                android.app.AlertDialog alertDialog = dialogbuilder.create();
                alertDialog.show();
                return true;
            }
        });

        //Listener for the item click on the listView
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Intent intent = new Intent(getActivity(), IndivLog.class);
                Cursor cursor = (Cursor) parent.getItemAtPosition(position);
                intent.putExtra("id",cursor.getString(0));
                intent.putExtra("name", cursor.getString(1));
                intent.putExtra("timestamp", cursor.getString(2));
                getActivity().startActivityForResult(intent, OPEN_LOG_REQ);
            }
        });

        return rootView;
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if(requestCode==ADD_LOG_REQ)
        {
            if(resultCode==Activity.RESULT_OK)
            {

                populateListView();
                Snackbar.make(rootView," log created", Snackbar.LENGTH_SHORT).setAction("Action", null).show();

            }
        }


    }

    


    private void populateListView()
    {
        Cursor cursor = mydb.getData();

        final Integer[] imgmap = {R.drawable.imgbeach,R.drawable.imgrdtrip,R.drawable.imgbike,R.drawable.imghike};

        //mapping the fieldnames
            String[] fromfieldnames = new String[] {DBHelper.KEY_NAME,DBHelper.KEY_TIMESTAMP,DBHelper.KEY_LOGTYPE};
            int[] toviewids = new int[] {R.id.logname,R.id.logdate,R.id.loglisticon};

        SimpleCursorAdapter mycursoradapter = new SimpleCursorAdapter(getContext(),R.layout.loglist,cursor,fromfieldnames,toviewids,0);

        mycursoradapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {
            @Override
            public boolean setViewValue(View view, Cursor cursor, int columnIndex) {

                if(view.getId() == R.id.loglisticon)
                {
                    ((ImageView)view).setImageDrawable(getResources().getDrawable(cursor.getInt(columnIndex)));
                    return true;
                }

                if(view.getId() == R.id.logdate)
                {
                    ((TextView) view).setText(parseDateToddMMyyyy(cursor.getString(cursor.getColumnIndex(DBHelper.KEY_TIMESTAMP))));
                    return true;
                }

                return false;
            }
        });

        listview.setAdapter(mycursoradapter);


    }

    public String parseDateToddMMyyyy(String time) {
        String inputPattern = "yyyy-MM-dd HH:mm:ss";
        String outputPattern = "dd MMM  h:mm a";
        SimpleDateFormat inputFormat = new SimpleDateFormat(inputPattern);
        SimpleDateFormat outputFormat = new SimpleDateFormat(outputPattern);

        Date date = null;
        String str = null;

        try {
            date = inputFormat.parse(time);
            str = outputFormat.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return str;
    }




}
