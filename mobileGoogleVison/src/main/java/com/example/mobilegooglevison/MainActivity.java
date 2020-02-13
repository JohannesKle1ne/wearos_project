package com.example.mobilegooglevison;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Bundle;

import android.util.Log;
import android.util.SparseArray;

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


public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks {

    TextView receivedView;
    TextView resultView;
    ImageView imageView;
    private GoogleApiClient client;
    private List<Node> connectedNode;
    private static final String TAG = "PHONE_MAIN";
    private static final String RESULT_VIEW_DEFAULT = "Result: ";
    private static final String RECEIVED_VIEW_DEFAULT = "Received: ";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();

        setContentView(R.layout.activity_main);
        receivedView = findViewById(R.id.output);
        resultView = findViewById(R.id.recognized);
        imageView = findViewById(R.id.image);



        client = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addApi(Wearable.API)
                .build();
        client.connect();
    }

    public void makeTextFromImage(Bitmap bm){

        //other branch
        imageView.setImageBitmap(bm);

        Bitmap bitmap = bm;

        imageView.setImageBitmap(bitmap);

        TextRecognizer tr = new TextRecognizer.Builder(getApplicationContext()).build();

        if(!tr.isOperational()){
            Toast.makeText(getApplicationContext(), "Could not get the Text",Toast.LENGTH_SHORT).show();
        }else{
            Frame frame = new Frame.Builder().setBitmap(bitmap).build();
            SparseArray<TextBlock> items = tr.detect(frame);
            StringBuilder sb = new StringBuilder();
            for(int i=0; i<items.size(); i++){
                TextBlock myItem = items.valueAt(i);
                sb.append(myItem.getValue());
            }
            String result = sb.toString();
            //used when word is concatinated at phone
            //receivedView.setText(receivedView.getText().toString()+sb.toString());
            resultView.setText(RESULT_VIEW_DEFAULT +sb.toString());

            if(!result.isEmpty()){
                sendMessage(MessageDict.ACK);
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
            case (MessageDict.SPACE):
                receivedView.setText(RECEIVED_VIEW_DEFAULT +"-SPACE-");
                sendMessage(MessageDict.ACK);
                break;
            case (MessageDict.EMPTY):
                receivedView.setText(RECEIVED_VIEW_DEFAULT +"-Empty-");
                imageView.setImageResource(0);
                resultView.setText(RESULT_VIEW_DEFAULT);
                break;
            default:
                receivedView.setText(RECEIVED_VIEW_DEFAULT +"-Bitmap-");
                Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                makeTextFromImage(bmp);
        }
    }

    public void sendMessage(String message) {
        for (int i = 0; i < connectedNode.size(); i++) {
            byte[] bytes = message.getBytes();
            Wearable.MessageApi.sendMessage(client, connectedNode.get(i).getId(), "/meal", bytes);
            Log.i("MessageDict sent", message);
        }
    }
}
