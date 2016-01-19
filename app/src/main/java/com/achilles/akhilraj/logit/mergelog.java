package com.achilles.akhilraj.logit;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.widget.Toast;

import com.googlecode.mp4parser.BasicContainer;
import com.googlecode.mp4parser.DataSource;
import com.googlecode.mp4parser.authoring.Movie;
import com.googlecode.mp4parser.authoring.Track;
import com.googlecode.mp4parser.authoring.builder.DefaultMp4Builder;
import com.googlecode.mp4parser.authoring.container.mp4.MovieCreator;
import com.googlecode.mp4parser.authoring.tracks.AppendTrack;

import java.io.File;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by akhil on 1/17/2016.
 */
public class mergelog {

    private Context context;
    private DBHelper mydb ;


    public  mergelog(Context context){
        this.context = context;
    }

    public void concatvideos(String log_id,String log_name)
    {
        mydb = new DBHelper(context);

        //The details for the video name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String newfilename = log_name+"_"+timeStamp;

        Log.d("Log_id",log_id);
        //getting all the video files
        String Logvideolistpath[] = fromCursorToStringArray(mydb.getVideo(log_id));

        //merging the videos
        MovieCreator mc = new MovieCreator();
        Movie  Movielist[] = new Movie[Logvideolistpath.length];

        Log.d("Noofvid",String.valueOf(Logvideolistpath.length));

        try {
            for (int i = 0; i < Logvideolistpath.length; i++) {
                Log.d("abs_path",Logvideolistpath[i]);
                Movielist[i] = mc.build(Logvideolistpath[i]);

            }
            List<Track> videoTracks = new LinkedList<Track>();
            List<Track> audioTracks = new LinkedList<Track>();


            for (Movie m : Movielist) {
                for (Track t : m.getTracks()) {
                    if (t.getHandler().equals("soun")) {
                        audioTracks.add(t);
                    }
                    if (t.getHandler().equals("vide")) {
                        videoTracks.add(t);
                    }
                }
            }

            Movie result = new Movie();

            if (audioTracks.size() > 0) {
                result.addTrack(new AppendTrack(audioTracks.toArray(new Track[audioTracks.size()])));
            }
            if (videoTracks.size() > 0) {
                result.addTrack(new AppendTrack(videoTracks.toArray(new Track[videoTracks.size()])));
            }

            BasicContainer out = (BasicContainer) new DefaultMp4Builder().build(result);

            String completefiledir = String.format(Environment.getExternalStorageDirectory() + "/Logit/"+newfilename+".mp4");

            @SuppressWarnings("resource")
            FileChannel fc = new RandomAccessFile(completefiledir, "rw").getChannel();
            out.writeContainer(fc);
            fc.close();


            mydb.newcompletedvideo(newfilename,completefiledir);

            //Toast.makeText(context, "Videos Merged "+completefiledir, Toast.LENGTH_SHORT).show();
        }catch (Exception e)
        {
            e.printStackTrace();
        }


    }

    private String[] fromCursorToStringArray(Cursor c){
        String[] result = new String[c.getCount()];
        c.moveToFirst();
        Log.d("cursorCount",String.valueOf(c.getCount()));
        for(int i = 0; i < c.getCount(); i++){
            String row = c.getString(c.getColumnIndex(DBHelper.VID_DIR));

            Uri uri = Uri.parse(row);
            File file = new File(uri.getPath());
            row = file.getAbsolutePath();

            result[i] = row;
            c.moveToNext();
        }
        return result;
    }
}
