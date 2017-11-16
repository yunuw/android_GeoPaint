package edu.uw.yw239.geopaint;

import android.Manifest;
import android.app.Activity;
import android.app.IntentService;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Created by yunwu on 11/14/17.
 */

public class MapSavingService extends IntentService{

    private Handler mHandler;

    public static final int WRITE_REQUEST_CODE = 2;

    public MapSavingService(){
        super("MapSavingService");
    }

    @Override
    public void onCreate(){
        mHandler = new Handler();
        super.onCreate();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Bundle extras = intent.getExtras();

        // when the user created a new file
        final String newFileName = extras.getString(MapsActivity.CREATE_NEW_FILE);

        if(newFileName != null){
            File file = new File(getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), newFileName + ".geojson");

            // for test
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    //Toast.makeText(MapSavingService.this, newFileName, Toast.LENGTH_SHORT).show();
                }
            });
        }

        // when the location is changed
        final String[] nameAndContent = extras.getStringArray(MapsActivity.UPDATE_FILE_KEY);

        if(nameAndContent != null){
            String fileName = nameAndContent[0];
            final String content = nameAndContent[1];

            // delete the existed file and create a new file with the same name
            File file = new File(getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), fileName + ".geojson");
            file.delete();
            final File newFile = new File(getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), fileName + ".geojson");

            // check for media availability
            if(isExternalStorageWritable()) {
                int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
                if(permissionCheck == PackageManager.PERMISSION_GRANTED) {
                    //have permission, can go ahead and do stuff

                    //writing
                    try {
                        //saving in public Documents directory
                        PrintWriter out = new PrintWriter(new FileWriter(file, true));
                        out.println(content);
                        out.close();

                        // for test
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                //Toast.makeText(MapSavingService.this, content, Toast.LENGTH_SHORT).show();
                            }
                        });
                    } catch (IOException ioe) {

                    }
                }
                else { //if we're missing permission.
                    // TODO: 11/14/17 ??? finish the permission check
                    Toast.makeText(MapSavingService.this, "permission problem", Toast.LENGTH_SHORT).show();

                    Activity activity = new MapsActivity().thisActivity;
                    ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, WRITE_REQUEST_CODE);
                }
            }
        }

    }

    /* Checks if external storage is available for read and write */
    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    /* Checks if external storage is available to at least read */
    public boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            return true;
        }
        return false;
    }

}
