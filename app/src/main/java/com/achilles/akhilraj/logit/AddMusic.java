package com.achilles.akhilraj.logit;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Toast;

import com.coremedia.iso.IsoFile;
import com.googlecode.mp4parser.BasicContainer;
import com.googlecode.mp4parser.FileDataSourceImpl;
import com.googlecode.mp4parser.authoring.Movie;
import com.googlecode.mp4parser.authoring.Track;
import com.googlecode.mp4parser.authoring.builder.DefaultMp4Builder;
import com.googlecode.mp4parser.authoring.container.mp4.MovieCreator;
import com.googlecode.mp4parser.authoring.tracks.AppendTrack;
import com.googlecode.mp4parser.authoring.tracks.CroppedTrack;
import com.googlecode.mp4parser.authoring.tracks.MP3TrackImpl;
import com.mpatric.mp3agic.InvalidDataException;
import com.mpatric.mp3agic.Mp3File;
import com.mpatric.mp3agic.NotSupportedException;
import com.mpatric.mp3agic.UnsupportedTagException;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.URI;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

public class AddMusic extends AppCompatActivity {

    static final int REQ_CODE_PICK_SOUNDFILE = 707;
    Uri audioFileUri;
    private DBHelper mydb ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_music);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        String vidname = getIntent().getStringExtra("name");
        final File vidfile = (File) getIntent().getExtras().get("file");
        String namecontent[] = vidname.split("_");

        if(namecontent[0].contains("(audioEdit)"))
        {
            String t[] = namecontent[0].split("\\(audioE");
            namecontent[0]= t[0];
        }

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        final String newfilename = namecontent[0]+"(audioEdit)"+"_"+timeStamp;

        mydb = new DBHelper(AddMusic.this);

        Button button = (Button) findViewById(R.id.buttoncustommusicaddmusic);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent;
                intent = new Intent();
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.setType("audio/mpeg");
                startActivityForResult(Intent.createChooser(intent,"Select Audio"), REQ_CODE_PICK_SOUNDFILE);
            }
        });

        final CheckBox checkbox = (CheckBox) findViewById(R.id.checkBoxcustommusicaddmusic);

        Button buttonmerge = (Button) findViewById(R.id.buttonmergeaudioaddmusic);
        buttonmerge.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //using custommusic
                    if(checkbox.isChecked())
                    {
                        MovieCreator mc = new MovieCreator();
                        Movie movie = new Movie();
                        try {
                                 movie = mc.build(vidfile.getAbsolutePath());

                            IsoFile isoFile = new IsoFile(vidfile.getAbsolutePath());
                            double lengthInSeconds = (double) isoFile.getMovieBox().getMovieHeaderBox().getDuration() / isoFile.getMovieBox().getMovieHeaderBox().getTimescale();


                                List<Track> videoTracks = new LinkedList<Track>();

                                for (Track t : movie.getTracks()) {
                                    if (t.getHandler().equals("vide")) {
                                        videoTracks.add(t);
                                    }
                                }
                            Movie result = new Movie();
                                //removing headers
                                Mp3File mp3file = new Mp3File(getRealPathFromURI(AddMusic.this,audioFileUri));
                                if (mp3file.hasId3v1Tag()) {
                                    mp3file.removeId3v1Tag();
                                    Log.d("MP3agic","removeId3v1Tag");
                                }
                                if (mp3file.hasId3v2Tag()) {
                                    mp3file.removeId3v2Tag();
                                    Log.d("MP3agic", "removeId3v2Tag");
                                }
                                if (mp3file.hasCustomTag()) {
                                    mp3file.removeCustomTag();
                                    Log.d("MP3agic","removeCustomTag");
                                }

                            String tempdir = String.format(Environment.getExternalStorageDirectory() + "/Logit/temp.mp3");
                            File file = new File(tempdir);
                            if(file.exists())
                                file.delete();
                            mp3file.save(tempdir);
                            MP3TrackImpl aacTrack = new MP3TrackImpl(new FileDataSourceImpl(tempdir));
                            CroppedTrack croppedaacTrack = new CroppedTrack(aacTrack,0,(long)((lengthInSeconds*1000)/26));
                            result.addTrack(croppedaacTrack);


                            if (videoTracks.size() > 0) {
                                result.addTrack(new AppendTrack(videoTracks.toArray(new Track[videoTracks.size()])));
                            }

                            BasicContainer out = (BasicContainer) new DefaultMp4Builder().build(result);

                            String completefiledir = String.format(Environment.getExternalStorageDirectory() + "/Logit/"+newfilename+".mp4");

                            @SuppressWarnings("resource")
                            FileChannel fc = new RandomAccessFile(completefiledir, "rw").getChannel();
                            out.writeContainer(fc);
                            fc.close();


                            mydb.newcompletedvideo(newfilename, completefiledir);

                            Toast.makeText(AddMusic.this, String.valueOf(lengthInSeconds)+"Music Added", Toast.LENGTH_SHORT).show();

                            Intent tint = new Intent(AddMusic.this,MainActivity.class);
                            startActivity(tint);

                        } catch (IOException e) {
                            e.printStackTrace();
                            Toast.makeText(AddMusic.this, "Failed", Toast.LENGTH_SHORT).show();
                        } catch (UnsupportedTagException e) {
                            e.printStackTrace();
                            Toast.makeText(AddMusic.this, "Failed", Toast.LENGTH_SHORT).show();
                        } catch (InvalidDataException e) {
                            e.printStackTrace();
                            Toast.makeText(AddMusic.this, "Failed", Toast.LENGTH_SHORT).show();
                        } catch (NotSupportedException e) {
                            e.printStackTrace();
                            Toast.makeText(AddMusic.this, "Failed", Toast.LENGTH_SHORT).show();
                        }
                    }
            }
        });
    }

    public String getRealPathFromURI(Context context, Uri contentUri) {
        Cursor cursor = null;
        try {
            String[] proj = { MediaStore.Images.Media.DATA };
            cursor = context.getContentResolver().query(contentUri,  proj, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ_CODE_PICK_SOUNDFILE && resultCode == Activity.RESULT_OK){
            if ((data != null) && (data.getData() != null)){
                audioFileUri = data.getData();
                Toast.makeText(AddMusic.this, "audio selected", Toast.LENGTH_SHORT).show();
                // Now you can use that Uri to get the file path, or upload it, ...
            }
        }
    }

}
