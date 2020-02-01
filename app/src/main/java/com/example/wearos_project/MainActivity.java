package com.example.wearos_project;

import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;
import com.google.android.gms.wearable.MessageClient;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Wearable;


public class MainActivity extends WearableActivity implements GoogleApiClient.ConnectionCallbacks{

    private TextView resultText;
    private PaintView paintView;
    private Button sendButton;
    private Button convertButton;
    private GoogleApiClient client;
    private String currentString;

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

        client = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addApi(Wearable.API)
                .build();
        client.connect();

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

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.i("connected!", "");
        Wearable.MessageApi.addListener(client, new MessageClient.OnMessageReceivedListener() {
            @Override
            public void onMessageReceived(@NonNull MessageEvent messageEvent) {
                currentString = new String(messageEvent.getData());
                Log.i("RECEIVED!", currentString);
            }
        });

    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i("Wear", "Google Api Client connection suspended!");

    }

}
