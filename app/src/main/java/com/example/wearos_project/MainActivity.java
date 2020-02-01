package com.example.wearos_project;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;
import android.util.DisplayMetrics;
import android.util.Log;

import android.view.KeyEvent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;

import com.google.android.gms.wearable.MessageClient;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import java.util.List;


public class MainActivity extends WearableActivity implements GoogleApiClient.ConnectionCallbacks{


    private PaintView paintView;
    private GoogleApiClient client;
    private String currentString;
    private List<Node> connectedNode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        paintView = findViewById(R.id.paintView);

        DisplayMetrics displayMetrics = new DisplayMetrics();

        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

        paintView.initialise(displayMetrics);


        client = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addApi(Wearable.API)
                .build();
        client.connect();

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (event.getRepeatCount() == 0) {
            if (keyCode == KeyEvent.KEYCODE_STEM_1) {
                paintView.undo();
                return true;
            } else if (keyCode == KeyEvent.KEYCODE_STEM_2) {
                sendMessage();
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.i("connected!", "");
        Wearable.NodeApi.getConnectedNodes(client).setResultCallback(new ResultCallback<NodeApi.GetConnectedNodesResult>() {
            @Override
            public void onResult(NodeApi.GetConnectedNodesResult getConnectedNodesResult) {
                connectedNode = getConnectedNodesResult.getNodes();
            }
        });

        Wearable.MessageApi.addListener(client, new MessageClient.OnMessageReceivedListener() {
            @Override
            public void onMessageReceived(@NonNull MessageEvent messageEvent) {
                currentString = new String(messageEvent.getData());
                Log.i("Received message", currentString);
            }
        });

    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i("Wear", "Google Api Client connection suspended!");

    }
    

    public void sendMessage() {
        Bitmap bitmap = paintView.getBitmap(); //auslagern, an methode übergeben

        //convert Bitmap to Byte
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] byteArray = stream.toByteArray();
        try {
            stream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        for (int i = 0; i < connectedNode.size(); i++) {
            Wearable.MessageApi.sendMessage(client, connectedNode.get(i).getId(), "/meal", byteArray);
            Log.i("Message sent", new String(byteArray));
        }
        paintView.clear();
    }

}
