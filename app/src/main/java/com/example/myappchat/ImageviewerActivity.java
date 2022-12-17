package com.example.myappchat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Date;

public class ImageviewerActivity extends AppCompatActivity {

    private ImageView imageView;
    private String imageUrl;
    private int REQUEST_STORAGE=102;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_imageviewer);

        imageView=(ImageView)findViewById(R.id.image_viewer);
        imageUrl=getIntent().getStringExtra("url");
        Picasso.get().load(imageUrl).into(imageView);



    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
         super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.save_to_gallery,menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
         super.onOptionsItemSelected(item);

         if (item.getItemId() == R.id.save_to_gallery_option)
         {
             if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) !=
                     PackageManager.PERMISSION_GRANTED)
             {
                 ActivityCompat.requestPermissions(ImageviewerActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                         REQUEST_STORAGE);
             } else
                 {
                     saveImageToGallery();

             }
         }
         return true;
    }

    private void saveImageToGallery()
    {

      DownloadTask downloadTask=new DownloadTask();
      downloadTask.execute(imageUrl);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        if (requestCode==REQUEST_STORAGE)
        {
            if (grantResults.length>0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                saveImageToGallery();
            }else
            {
                Toast.makeText(ImageviewerActivity.this,"permission denied",Toast.LENGTH_SHORT).show();
            }
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);


    }

    class DownloadTask extends AsyncTask<String,Integer,String>
    {
        ProgressDialog progressDialog;
        @Override
        protected void onPreExecute() {
            progressDialog=new ProgressDialog(ImageviewerActivity.this);
            progressDialog.setTitle("saving...");
            progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            progressDialog.setMax(100);
            progressDialog.setProgress(0);
            progressDialog.show();
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... param)
        {
            String path=param[0];
            int fl=0;
            try {
                URL url = new URL(path);
                URLConnection urlConnection=url.openConnection();
                urlConnection.connect();
                fl=urlConnection.getContentLength();
                File newfolder=new File(Environment.getExternalStorageDirectory(),"myappchat");
                if (!newfolder.exists())
                {
                    newfolder.mkdirs();
                }

                File inputfile=new File(newfolder,System.currentTimeMillis() + "downloadimg.jpg");
                InputStream inputStream=new BufferedInputStream(url.openStream(),8192);
                byte[] data=new byte[1024];
                int total=0;
                int count=0;
                OutputStream outputStream=new FileOutputStream(inputfile);
                while ((count=inputStream.read(data))!=-1)
                {
                    total+=count;
                    outputStream.write(data,0,count);
                    int progress=(int)total*100/fl;
                    publishProgress(progress);
                }
                inputStream.close();
                outputStream.close();

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return "Download completed.....";
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            progressDialog.setProgress(values[0]);
        }

        @Override
        protected void onPostExecute(String result)
        {
            progressDialog.hide();
            Toast.makeText(ImageviewerActivity.this,result,Toast.LENGTH_SHORT).show();
            String path="Internal storage/Canva/System.currentTimeMillis() + \"downloadimg.jpg\"";
        }
    }
}
