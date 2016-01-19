package com.achilles.akhilraj.logit;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class IndivLog extends AppCompatActivity {

    public int VIDEOPERIOD=5;

    private Uri fileUri;
    private static final int CAPTURE_VIDEO_ACTIVITY_REQUEST_CODE = 200;
    public static final int MEDIA_TYPE_IMAGE = 1;
    public static final int MEDIA_TYPE_VIDEO = 2;
    CoordinatorLayout coordinatorLayout;
    private DBHelper mydb ;
    ListView listview;
    String id;
    String log_name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_indiv_log);
        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.indiv_log_content);
        mydb = new DBHelper(this);

        //getting the intent extra details
        String name = getIntent().getStringExtra("name");
        log_name=name;
        String timestamp = getIntent().getStringExtra("timestamp");
        id = getIntent().getStringExtra("id");

        //setting up the log details
        TextView textview = (TextView) findViewById(R.id.textViewStartTimeIndivLog);
        TextView textView2 = (TextView) findViewById(R.id.textViewStartDateIndivLog);
        String temp[] = timestamp.split(" ");
        textview.setText(temp[1]);  //time
        textView2.setText(temp[0]); //date

       //setting up the listview
        listview = (ListView) findViewById(R.id.listViewIndivLog);
        populatelistview();

        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Cursor cursor = (Cursor) parent.getItemAtPosition(position);
                Uri newVideoPath = Uri.parse(cursor.getString(cursor.getColumnIndex(DBHelper.VID_DIR)));
               //playing the video
                Intent intent = new Intent(Intent.ACTION_VIEW, newVideoPath);
                intent.setDataAndType(newVideoPath, "video/*");
                startActivity(intent);
            }
        });

        //finishlog button
        Button finishlog = (Button) findViewById(R.id.buttonfinishlogindivlog);
        finishlog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AlertDialog.Builder dialogbuilder = new AlertDialog.Builder(IndivLog.this);
                dialogbuilder.setTitle("Merge the videos ?")
                                .setMessage("Merged videos are available under completed tab")

                                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        mergelog ml = new mergelog(IndivLog.this);
                                        ml.concatvideos(IndivLog.this.id, log_name);
                                        Toast.makeText(IndivLog.this, "Videos Merged Successfully", Toast.LENGTH_SHORT).show();

                                        Intent returnintent = new Intent(IndivLog.this,MainActivity.class);
                                        startActivity(returnintent);
                                    }
                                })
                                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        finish();
                                    }
                                });
                AlertDialog alertDialog = dialogbuilder.create();
                alertDialog.show();
            }
        });

        //setting up the toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbarindivlog);
        toolbar.setTitle(name);
        setSupportActionBar(toolbar);





        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);

                fileUri = getOutputMediaFileUri(MEDIA_TYPE_VIDEO);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
                intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);
                intent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, VIDEOPERIOD);
                startActivityForResult(intent, CAPTURE_VIDEO_ACTIVITY_REQUEST_CODE);

            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }


    private static Uri getOutputMediaFileUri(int type){
        return Uri.fromFile(getOutputMediaFile(type));
    }
    /** Create a File for saving an image or video */
    private static File getOutputMediaFile(int type){
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.

        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "LogIt");
        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
                Log.d("LogIt", "failed to create directory");
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE){
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "IMG_"+ timeStamp + ".jpg");
        } else if(type == MEDIA_TYPE_VIDEO) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "LOGIT_"+ timeStamp + ".3gp");
        } else {
            return null;
        }

        return mediaFile;
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == CAPTURE_VIDEO_ACTIVITY_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {

                mydb.newvideo(id,fileUri.toString(),VIDEOPERIOD);
                Snackbar.make(coordinatorLayout, "Video saved to:"+fileUri.toString(), Snackbar.LENGTH_LONG).setAction("Action", null).show();
                populatelistview();
            } else if (resultCode == RESULT_CANCELED) {
                Snackbar.make(coordinatorLayout,"Video cancelled",Snackbar.LENGTH_LONG).setAction("Action",null).show();
            } else {
                Snackbar.make(coordinatorLayout,"Video Failed",Snackbar.LENGTH_LONG).setAction("Action",null).show();

            }
        }


    }


    protected  void  populatelistview()
    {

        Cursor cursor = mydb.getVideo(id);

        //mapping the fieldnames
        String[] fromfieldnames = new String[] {DBHelper.VID_TIMESTAMP,DBHelper.VID_DIR,DBHelper.VID_THUMBNAIL_DIR};
        int[] toviewids = new int[] {R.id.textViewtimelistviewindivlog,R.id.imageViewlistviewindivlog,R.id.textViewvideonumberindivlog};

        SimpleCursorAdapter mycursoradapter = new SimpleCursorAdapter(this,R.layout.indivloglistview,cursor,fromfieldnames,toviewids,0);

        mycursoradapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {


            @Override
            public boolean setViewValue(View view, Cursor cursor, int columnIndex) {

                if(view.getId() == R.id.imageViewlistviewindivlog )
                {
                    String dir =cursor.getString(cursor.getColumnIndex(DBHelper.VID_THUMBNAIL_DIR));
                    Log.d("thumbnail-indiv",dir);
                    if(dir.equals("00")) {

                        Log.d("LOGIT_THUMB",cursor.getString(cursor.getColumnIndex(DBHelper.VID_ID)));
                        Uri uri = Uri.parse(cursor.getString(1));
                        File file = new File(uri.getPath());
                        String path = file.getAbsolutePath();
                        Bitmap thumb = ThumbnailUtils.createVideoThumbnail(path, MediaStore.Images.Thumbnails.MINI_KIND);
                        ((ImageView) view).setImageBitmap(thumb);

                        //storing the thumbnails
                        String extr = Environment.getExternalStorageDirectory().toString();
                        File mFolder = new File(extr + "/Logit");

                        if (!mFolder.exists()) {
                            mFolder.mkdir();
                        }

                        String strF = mFolder.getAbsolutePath();
                        File mSubFolder = new File(strF + "/Logit-thumbnails");

                        if (!mSubFolder.exists()) {
                            mSubFolder.mkdir();
                        }

                        String s = cursor.getString(cursor.getColumnIndex(DBHelper.VID_ID))+".png";

                        File f = new File(mSubFolder.getAbsolutePath(),s);

                        String strMyImagePath = f.getAbsolutePath();
                        FileOutputStream fos = null;
                        try {
                            fos = new FileOutputStream(f);
                            thumb.compress(Bitmap.CompressFormat.PNG, 70, fos);

                            fos.flush();
                            fos.close();
                            Log.d("LOGIT_UPDATE", String.valueOf(mydb.updatethumb(strMyImagePath, cursor.getString(cursor.getColumnIndex(DBHelper.VID_ID)))));

                        }catch (FileNotFoundException e) {

                            e.printStackTrace();
                        } catch (Exception e) {

                            e.printStackTrace();
                        }
                    }
                    else
                    {
                            Log.d("LOGIT_THUMB","loading cached thumbnails");
                            String abspath = cursor.getString(cursor.getColumnIndex(DBHelper.VID_THUMBNAIL_DIR));
                            Bitmap cacheBM = BitmapFactory.decodeFile(abspath);
                            ((ImageView) view).setImageBitmap(cacheBM);
                    }
                    return true;
                }

                if(view.getId()== R.id.textViewvideonumberindivlog)
                {
                    ((TextView) view).setText(String.valueOf(cursor.getPosition()+1));

                    return true;
                }



                return false;
            }
        });
        listview.setAdapter(mycursoradapter);
    }
}
