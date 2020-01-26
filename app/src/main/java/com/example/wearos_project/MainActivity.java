package com.example.wearos_project;

import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;

import java.util.*;

public class MainActivity extends WearableActivity {

    private TextView resultText;
    private PaintView paintView;
    private Button sendButton;
    private Button convertButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sendButton = findViewById(R.id.sendButton);
        convertButton = findViewById(R.id.convertButton);
        paintView = findViewById(R.id.paintView);
        resultText = findViewById(R.id.resultText);



        DisplayMetrics displayMetrics = new DisplayMetrics();


        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

        paintView.initialise(displayMetrics);

        convertButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                getStringFromBitmap();
            }
        });

    }

    public void getStringFromBitmap(){
        TextRecognizer tr = new TextRecognizer.Builder(getApplicationContext()).build();

        if(!tr.isOperational()){
            Log.i("TR",String.valueOf(tr.isOperational()));
            Toast.makeText(getApplicationContext(), "Could not get the Text",Toast.LENGTH_SHORT).show();
        }else{
            Frame frame = new Frame.Builder().setBitmap(paintView.getBitmap()).build();
            SparseArray<TextBlock> items = tr.detect(frame);
            StringBuilder sb = new StringBuilder();
            for(int i=0; i<items.size(); i++){
                TextBlock myItem = items.valueAt(i);
                sb.append(myItem.getValue());
                sb.append("\n");
                Log.i("ITEM", myItem.getValue());
            }
            resultText.setText(sb.toString());

        }
        paintView.clear();
    }
}
