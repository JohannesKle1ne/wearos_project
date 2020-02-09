package com.example.mobilegooglevison;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Message;
import android.util.Log;
import android.util.SparseArray;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.shared.MessageDict;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.MessageClient;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;


import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

import java.util.List;


public class MainActivity extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks,
        GestureDetector.OnGestureListener,
        GestureDetector.OnDoubleTapListener{

    TextView outputView;
    ImageView imageView;
    private GoogleApiClient client;
    private List<Node> connectedNode;
    private String word;
    private static final String TAG = "PHONE_MAIN";
    private GestureDetector gestureDetector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        word = "";

        setContentView(R.layout.activity_main);
        outputView = findViewById(R.id.output);
        imageView = findViewById(R.id.image);



        client = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addApi(Wearable.API)
                .build();
        client.connect();

        gestureDetector = new GestureDetector(this, this);

    }

    public void getTextFromImage(Bitmap bm){

        Bitmap bitmap = addBlackBorder(bm, 1000);

        imageView.setImageBitmap(bitmap);

        TextRecognizer tr = new TextRecognizer.Builder(getApplicationContext()).build();

        if(!tr.isOperational()){
            Toast.makeText(getApplicationContext(), "Could not get the Text",Toast.LENGTH_SHORT).show();
        }else{
            Log.i("Bitmap",bitmap.toString());
            Frame frame = new Frame.Builder().setBitmap(bitmap).build();
            SparseArray<TextBlock> items = tr.detect(frame);
            StringBuilder sb = new StringBuilder();
            for(int i=0; i<items.size(); i++){
                TextBlock myItem = items.valueAt(i);
                sb.append(myItem.getValue());
                Log.i("ITEM", myItem.getValue());
            }
            String result = sb.toString();
            word = word+result;
            outputView.setText(outputView.getText().toString()+sb.toString());
            Log.i("outputString1", word);

            if(!result.isEmpty()){
                sendMessage(MessageDict.ack);
            }
        }
    }
    private Bitmap addBlackBorder(Bitmap bmp, int borderSize) {
        Bitmap bmpWithBorder = Bitmap.createBitmap(bmp.getWidth() + borderSize * 2, bmp.getHeight() + borderSize * 2, bmp.getConfig());
        Canvas canvas = new Canvas(bmpWithBorder);
        canvas.drawColor(Color.BLACK);
        canvas.drawBitmap(bmp, borderSize, borderSize, null);
        return bmpWithBorder;
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Wearable.NodeApi.getConnectedNodes(client).setResultCallback(new ResultCallback<NodeApi.GetConnectedNodesResult>() {
            @Override
            public void onResult(NodeApi.GetConnectedNodesResult getConnectedNodesResult) {
                connectedNode = getConnectedNodesResult.getNodes();
            }
        });

        Wearable.MessageApi.addListener(client, new MessageClient.OnMessageReceivedListener() {
            @Override
            public void onMessageReceived(@NonNull MessageEvent messageEvent) {
                byte[] bytes = messageEvent.getData();

                handleMessage(bytes);
                //Log.i("Received message", new String(bytes));
            }
        });
        Log.i("Phone client connected", String.valueOf(client.isConnected()));
    }

    @Override
    public void onConnectionSuspended(int i) {
        connectedNode = null;
    }

    private void handleMessage(byte[] bytes) {
        String message = new String(bytes);
        switch(message){
            case (MessageDict.space):
                outputView.setText(outputView.getText().toString()+" ");
                Toast.makeText(this, "received space", Toast.LENGTH_SHORT).show();
                break;
            default:
                Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                getTextFromImage(bmp);
        }
    }

    public void sendMessage(String message) {
        for (int i = 0; i < connectedNode.size(); i++) {
            byte[] bytes = message.getBytes();
            Wearable.MessageApi.sendMessage(client, connectedNode.get(i).getId(), "/meal", bytes);
            Log.i("MessageDict sent", message);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        gestureDetector.onTouchEvent(event);

        /*switch (event.getAction()) {

            case MotionEvent.ACTION_DOWN:
                Log.i(TAG, "ACTION_DONW");
                break;
            case MotionEvent.ACTION_UP:
                Log.i(TAG, "ACTION_UP");
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                Log.i(TAG, "ACTION_POINTER_DOWN");
                break;
            case MotionEvent.ACTION_POINTER_UP:
                Log.i(TAG, "ACTION_POINTER_UP");
                break;
            case MotionEvent.ACTION_MOVE:
                //Log.i(TAG, "ACTION_MOVE");
                break;
        }*/
        return super.onTouchEvent(event);
    }








    @Override
    public boolean onDown(MotionEvent e) {
        return false;
    }

    @Override
    public void onShowPress(MotionEvent e) {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {

    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        return false;
    }

    @Override
    public boolean onSingleTapConfirmed(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onDoubleTap(MotionEvent e) {
        Log.i(TAG, "double tap");
        return false;
    }

    @Override
    public boolean onDoubleTapEvent(MotionEvent e) {
        return false;
    }
}
