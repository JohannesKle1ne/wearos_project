package com.example.wearos_project;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.support.wearable.activity.WearableActivity;
import android.util.DisplayMetrics;
import android.util.Log;

import android.view.KeyEvent;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.shared.MessageDict;
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


public class MainActivity extends WearableActivity implements
        GoogleApiClient.ConnectionCallbacks{


    private PaintView paintView;
    private GoogleApiClient client;
    private List<Node> connectedNode;
    private CountDownTimer letterTimer;
    private static final String TAG = "WATCH_MAIN";
    private boolean waitingForReset = false;

    private TextBuilder textbuilder;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        paintView = findViewById(R.id.paintView);
        textbuilder = new TextBuilder();

        DisplayMetrics displayMetrics = new DisplayMetrics();

        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

        paintView.initialise(displayMetrics, this);


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
                if (waitingForReset){
                    textbuilder.resetResult();
                    waitingForReset = false;
                    Log.i(TAG, "RESET");
                }else{
                    textbuilder.removeLetter();
                    startResetTimer();
                    Log.i(TAG, "REMOVE");
                }
                return true;
            } else if (keyCode == KeyEvent.KEYCODE_STEM_2) {
                sendBitmap(textbuilder.getResult());
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
                String message = new String(messageEvent.getData());
                Log.i("Received message", message);
                handleMessage(message);
            }
        });

    }

    private void handleMessage(String message) {
        switch(message){
            case (MessageDict.ACK):
                vibrate();
                break;
            default:
                Toast.makeText(this, "received unknown message", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i("Wear", "Google Api Client connection suspended!");

    }

    public void sendSpace(){
        send(MessageDict.SPACE.getBytes());
    }

    public void sendBitmap(Bitmap bitmap){
        if(bitmap==null){
            Log.i(TAG, "send Error! item to send is null");
            send(MessageDict.EMPTY.getBytes());
        }else {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
            byte[] byteArray = stream.toByteArray();
            try {
                stream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            send(byteArray);
        }
    }

    public void send(byte[] byteArray){
        for (int i = 0; i < connectedNode.size(); i++) {
            Wearable.MessageApi.sendMessage(client, connectedNode.get(i).getId(), "/meal", byteArray);
            Log.i("MessageDict sent", "ByteArray");
        }
    }


    public void vibrate(){
        Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        vibrator.vibrate(VibrationEffect.createOneShot(100,VibrationEffect.DEFAULT_AMPLITUDE));
    }


    public void startLetterTimer() {
        if(letterTimer!=null) {
            letterTimer.cancel();
        }
        letterTimer = new CountDownTimer(400, 100) {
            public void onTick(long millisUntilFinished) {
            }
            public void onFinish() {
                Log.i("onFinish","done!");
                Bitmap bitmap = paintView.getBitmap();
                textbuilder.addLetter(bitmap);
                vibrate();
                paintView.clear();
            }
        };
        letterTimer.start();
    }

    public void startResetTimer() {
        if(letterTimer!=null) {
            letterTimer.cancel();
        }
        waitingForReset = true;
        letterTimer = new CountDownTimer(200, 100) {
            public void onTick(long millisUntilFinished) {
            }
            public void onFinish() {
                waitingForReset =false;
                Log.i("onFinish","done!");
            }
        };
        letterTimer.start();
    }


    public void cancelLetterTimer() {
        if(letterTimer!=null)
            letterTimer.cancel();
    }

    public TextBuilder getTextbuilder() {
        return textbuilder;
    }


}
