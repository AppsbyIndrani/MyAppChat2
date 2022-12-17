package com.example.myappchat;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;

public class ImagefromCameraActivity extends AppCompatActivity {

    ImageView cameraim1;
    Button camerabut1;
    private int REQUEST_CAMERA=100;
    private Uri myimage;
    ImageButton cameraimgbut;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_imagefrom_camera);
        cameraim1=findViewById(R.id.imagefromcamera);
        camerabut1=findViewById(R.id.camerabutton);
        cameraimgbut=findViewById(R.id.sendcamerapicbutton);

        camerabut1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA) !=
                        PackageManager.PERMISSION_GRANTED)
                {
                    ActivityCompat.requestPermissions(ImagefromCameraActivity.this, new String[]{Manifest.permission.CAMERA},
                            REQUEST_CAMERA);
                } else {
                    TakePictureFromCamera();
                }
            }
        });
    }

    private void TakePictureFromCamera()
    {
        Intent intent=new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent,100);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==100 && data!=null)
        {

            Bitmap bitmap= (Bitmap) data.getExtras().get("data");
            cameraim1.setImageBitmap(bitmap);

            SaveImageToDatabase(bitmap);
        }
    }

    private void SaveImageToDatabase(Bitmap bitmap)
    {

        ByteArrayOutputStream baos=new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG,100,baos);

        String imageencoded= Base64.encodeToString(baos.toByteArray(),Base64.DEFAULT);
        Toast.makeText(ImagefromCameraActivity.this,imageencoded,Toast.LENGTH_SHORT).show();


    }
}
