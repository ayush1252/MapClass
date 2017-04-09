package com.example.ayush.sensorsandpictues;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.net.URI;
import java.security.Permissions;
import java.util.List;
import java.util.jar.Manifest;

public class MainActivity extends AppCompatActivity {
    SensorManager manager;
    Button click, choose;
    ImageView imageview;
    public String photoFileName = "photo.jpg";
    File fil;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        verifyStoragePermissions(MainActivity.this);
/*
         manager= (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        List<Sensor> sensorList = manager.getSensorList(Sensor.TYPE_ALL);
        for(Sensor sensor:sensorList)
            Log.d("Found Sesnor", String.valueOf(sensor));

        Sensor defaultSensor = manager.getDefaultSensor(Sensor.TYPE_LIGHT);
        manager.registerListener(new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {

                Log.d("ValueChanged",event.toString());
                Log.d("Value",event.values[0]+"");

            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {

            }
        }, defaultSensor, SensorManager.SENSOR_DELAY_NORMAL);



    */
    click= (Button) findViewById(R.id.click);
        choose= (Button) findViewById(R.id.choose);
        imageview= (ImageView) findViewById(R.id.imageview);

        click.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, getPhotoFileUri(photoFileName));
                startActivityForResult(intent, 1);
            }
        });

        choose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent=new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                intent.setType("image/");
                startActivityForResult(intent,2);



            }
        });
    }



    private Uri getPhotoFileUri(String filename) {
        File mediaStorageDir = new File(
                getExternalFilesDir(Environment.DIRECTORY_PICTURES), "MyCustomApp");

        if (!mediaStorageDir.exists() && !mediaStorageDir.mkdirs()){
            Log.d("lala", "failed to create directory");
        }

        fil=new File(mediaStorageDir.getPath() + File.separator + filename);
        return FileProvider.getUriForFile(getApplicationContext(), getApplicationContext().getPackageName() + ".provider",fil);
    }

    public static void verifyStoragePermissions(Activity activity) {

        Toast.makeText(activity, "lala", Toast.LENGTH_SHORT).show();
        int permission = ActivityCompat.checkSelfPermission(activity, android.Manifest.permission.READ_EXTERNAL_STORAGE);
        if (permission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    activity,
                    new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE},
                   0
            );
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(requestCode==0)
        {

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode==1)
        {
            if(resultCode==RESULT_OK)
            {
                String filepath=fil.getAbsolutePath();
                Bitmap bitmap = BitmapFactory.decodeFile(filepath);
                imageview.setImageBitmap(bitmap);
            }
        }
        if(requestCode==2)
        {
            if(resultCode==RESULT_OK)
            {
                Uri uri=data.getData();
                Bitmap bitmap=null;
                try {
                     bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
                }catch (Exception e)
                {

                }
                imageview.setImageBitmap(bitmap);
            }
        }
    }
}
