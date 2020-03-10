package com.example.mobilegooglevison;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;

public class LandscapeActivity extends AppCompatActivity {


    BitmapView bitmapView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_landscape);
        bitmapView = findViewById(R.id.bitmapView);
    }


    public void closeActivity(View v){
        finish();
    }
}
