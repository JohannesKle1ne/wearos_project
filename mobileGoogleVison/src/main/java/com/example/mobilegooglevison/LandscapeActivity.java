package com.example.mobilegooglevison;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

public class LandscapeActivity extends AppCompatActivity {


    BitmapView bitmapView;
    Button loadBitmaps;
    Button liveBitmaps;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_landscape);
        bitmapView = findViewById(R.id.bitmapView);
        loadBitmaps = findViewById(R.id.loadBitmaps);
        loadBitmaps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fetch(1);
            }
        });
        liveBitmaps = findViewById(R.id.liveBitmaps);
        liveBitmaps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bitmapView.showDynamic();
            }
        });
    }

    //weiter mit welche filenamen und speichern der filenamen
    // in view einfach ne id aneben und die Bitmaps angezeigt bekommen
    public void fetch(int userId){


    }
}
