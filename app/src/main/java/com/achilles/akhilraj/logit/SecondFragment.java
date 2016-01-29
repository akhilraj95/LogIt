package com.achilles.akhilraj.logit;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.text.TextPaint;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.github.amlcurran.showcaseview.ShowcaseView;
import com.github.amlcurran.showcaseview.targets.ViewTarget;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;


public class SecondFragment extends Fragment {

    private DBHelper mydb ;
    ListView listview;
    View rootView;
    SimpleCursorAdapter mycursoradapter;
    static final int ADD_MUSIC_REQUEST = 123;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_second, container, false);
        listview = (ListView) rootView.findViewById(R.id.listViewsecondfragment);
        mydb = new DBHelper(getContext());


        populateListView();

        listview.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                Cursor cursor = (Cursor) parent.getItemAtPosition(position);
                final File temp = new File(cursor.getString(cursor.getColumnIndex(DBHelper.COMPLETEDVID_DIR)));
                final Uri newVideoPath = Uri.fromFile(temp);
                final String tempid = cursor.getString(cursor.getColumnIndex(DBHelper.COMPLETEDVID_ID));
                final String name = cursor.getString(cursor.getColumnIndex(DBHelper.COMPLETEDVID_NAME));

                AlertDialog.Builder dialogbuilder = new AlertDialog.Builder(getContext());
                dialogbuilder.setTitle("Choose Action")
                        .setNeutralButton("Add Music", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent addmusicintent = new Intent(getContext(),AddMusic.class);
                                addmusicintent.putExtra("file",temp);
                                addmusicintent.putExtra("name",name);
                                startActivityForResult(addmusicintent, ADD_MUSIC_REQUEST);
                            }
                        })
                        .setPositiveButton("Share", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                Intent sharingIntent = new Intent(Intent.ACTION_SEND);
                                sharingIntent.setType("video/*");
                                sharingIntent.putExtra(Intent.EXTRA_STREAM, newVideoPath);
                                startActivity(Intent.createChooser(sharingIntent, "Share video using"));
                            }
                        })
                        .setNegativeButton("Delete", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                mydb.deletenewcompletedvideo(tempid);
                                boolean deleted = temp.delete();
                                if (deleted) {
                                    Toast.makeText(getContext(), "Video deleted", Toast.LENGTH_SHORT).show();
                                    populateListView();
                                }


                            }
                        });
                AlertDialog alertDialog = dialogbuilder.create();
                alertDialog.show();


                return true;
            }
        });


        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {


                SharedPreferences setpref = PreferenceManager.getDefaultSharedPreferences(getContext());
                SharedPreferences.Editor editor = setpref.edit();
                //showcaseview
                if(setpref.getBoolean("tutsecondfragment",true))
                {
                    AlertDialog.Builder dialogbuilder = new AlertDialog.Builder(getContext());
                    dialogbuilder.setTitle("Usage Guide")
                                    .setMessage("Click on Video to play. Long click on video to share, add music and delete")
                                    .setNeutralButton("Got It", new DialogInterface.OnClickListener() {

                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {

                                        }
                                    });

                    AlertDialog alertDialog = dialogbuilder.create();
                    alertDialog.show();

                    editor.putBoolean("tutsecondfragment", false).commit();
                }

                Cursor cursor = (Cursor) parent.getItemAtPosition(position);
                File temp = new File(cursor.getString(cursor.getColumnIndex(DBHelper.COMPLETEDVID_DIR)));
                Uri newVideoPath = Uri.fromFile(temp);
                //playing the video
                Intent intent = new Intent(Intent.ACTION_VIEW, newVideoPath);
                intent.setDataAndType(newVideoPath, "video/*");
                startActivity(intent);
            }
        });

        return rootView;
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == ADD_MUSIC_REQUEST) {
            if (resultCode == Activity.RESULT_OK) {
                populateListView();
             }
        }
    }
    public void populateListView()
    {
        Cursor cursor = mydb.getCompletedVideo();


        //mapping the fieldnames
        String[] fromfieldnames = new String[] {DBHelper.COMPLETEDVID_NAME,DBHelper.COMPLETEDVID_DIR,DBHelper.COMPLETEDVID_TIMESTAMP};
        int[] toviewids = new int[] {R.id.textViewNameCompletedVideoList,R.id.imageViewCompletedVideoList,R.id.textViewTimeCompletedVideoList};

        mycursoradapter = new SimpleCursorAdapter(getContext(),R.layout.completedvideolist,cursor,fromfieldnames,toviewids,0);

        mycursoradapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {
            @Override
            public boolean setViewValue(View view, Cursor cursor, int columnIndex) {

                if(view.getId()==R.id.textViewNameCompletedVideoList)
                {
                    String temp = cursor.getString(cursor.getColumnIndex(DBHelper.COMPLETEDVID_NAME));
                    String t[] = temp.split("_");
                    ((TextView)view).setText(t[0]);
                    return true;
                }

                if(view.getId() == R.id.imageViewCompletedVideoList)
                {
                    String dircheck = cursor.getString(cursor.getColumnIndex(DBHelper.COMPLETEDVID_THUMBNAIL_DIR));
                    if(dircheck.equals("00")) {
                        Uri uri = Uri.parse(cursor.getString(cursor.getColumnIndex(DBHelper.COMPLETEDVID_DIR)));
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
                        File mSubFolder = new File(strF + "/Logit-completedthumbnails");

                        if (!mSubFolder.exists()) {
                            mSubFolder.mkdir();
                        }

                        String s = cursor.getString(cursor.getColumnIndex(DBHelper.COMPLETEDVID_ID))+".png";

                        File f = new File(mSubFolder.getAbsolutePath(),s);

                        String strMyImagePath = f.getAbsolutePath();
                        FileOutputStream fos = null;
                        try {
                            fos = new FileOutputStream(f);
                            thumb.compress(Bitmap.CompressFormat.PNG, 70, fos);

                            fos.flush();
                            fos.close();
                            Log.d("LOGIT_UPDATE", String.valueOf(mydb.updatethumbcompletedvid(strMyImagePath, cursor.getString(cursor.getColumnIndex(DBHelper.COMPLETEDVID_ID)))));

                        }catch (FileNotFoundException e) {

                            e.printStackTrace();
                        } catch (Exception e) {

                            e.printStackTrace();
                        }
                    }
                    else
                    {
                        Log.d("LOGIT_THUMB","loading cached compltedthumbnails");
                        String abspath = cursor.getString(cursor.getColumnIndex(DBHelper.COMPLETEDVID_THUMBNAIL_DIR));
                        Bitmap cacheBM = BitmapFactory.decodeFile(abspath);
                        ((ImageView) view).setImageBitmap(cacheBM);
                    }

                    return true;
                }
                return false;
            }
        });
        listview.setAdapter(mycursoradapter);

    }

}
