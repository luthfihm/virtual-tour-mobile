package com.luthfihm.virtualtour.activity;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.luthfihm.virtualtour.R;
import com.luthfihm.virtualtour.apiclient.ObjectModelAPI;
import com.luthfihm.virtualtour.model.ObjectModel;

import java.io.InputStream;
import java.net.URL;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ObjectDetailsActivity extends AppCompatActivity implements Callback<ObjectModel> {

    private Gson gson;
    private Retrofit retrofit;
    private ObjectModelAPI objectModelAPI;

    private ObjectModel objectModel;

    private ImageView objectImage;
    private TextView objectLocation;
    private TextView objectDescription;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_object_details);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        objectImage = (ImageView) findViewById(R.id.objectImage);
        objectDescription = (TextView) findViewById(R.id.objectDescription);
        objectLocation = (TextView) findViewById(R.id.objectLocation);

        gson = new GsonBuilder()
                .setDateFormat("yyyy-MM-dd'T'HH:mm:ssZ")
                .create();

        retrofit = new Retrofit.Builder()
                .baseUrl(ObjectModelAPI.ENDPOINT)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();

        objectModelAPI = retrofit.create(ObjectModelAPI.class);

        Log.d("objectId", getIntent().getStringExtra("objectId"));

        Call<ObjectModel> call = objectModelAPI.getObject(getIntent().getStringExtra("objectId"));

        call.enqueue(ObjectDetailsActivity.this);
        ObjectDetailsActivity.this.setTitle(getIntent().getStringExtra("objectTitle"));
    }

    @Override
    public void onResponse(Call<ObjectModel> call, Response<ObjectModel> response) {
        int code = response.code();
        if (code == 200) {
            objectModel = response.body();

            new DownLoadImageTask(objectImage).execute(objectModel.getImageUrl());
            objectLocation.setText(objectModel.getLocation());
            objectDescription.setText(objectModel.getDescription());
        } else {
            Toast.makeText(this, "Did not work: " + String.valueOf(code), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onFailure(Call<ObjectModel> call, Throwable t) {
        Toast.makeText(this, "Failed", Toast.LENGTH_LONG).show();
    }

    private class DownLoadImageTask extends AsyncTask<String,Void,Bitmap> {
        ImageView imageView;

        public DownLoadImageTask(ImageView imageView){
            this.imageView = imageView;
        }

        /*
            doInBackground(Params... params)
                Override this method to perform a computation on a background thread.
         */
        protected Bitmap doInBackground(String...urls){
            String urlOfImage = urls[0];
            Bitmap bitmap = null;
            try{
                InputStream is = new URL(urlOfImage).openStream();
                /*
                    decodeStream(InputStream is)
                        Decode an input stream into a bitmap.
                 */
                bitmap = BitmapFactory.decodeStream(is);
            }catch(Exception e){ // Catch the download exception
                e.printStackTrace();
            }
            return bitmap;
        }

        /*
            onPostExecute(Result result)
                Runs on the UI thread after doInBackground(Params...).
         */
        protected void onPostExecute(Bitmap result){
            imageView.setImageBitmap(result);
        }
    }
}
