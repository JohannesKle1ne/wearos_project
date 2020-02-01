package com.example.mobilegooglevison;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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

    TextView outputView;
    ImageView imageView;
    Button button;
    private GoogleApiClient client;
    private List<Node> connectedNode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        outputView = findViewById(R.id.output);
        imageView = findViewById(R.id.image);
        button = findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //sendMessage();
            }
        });



        client = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addApi(Wearable.API)
                .build();
        client.connect();

    }

    public void getTextFromImage(Bitmap bitmap){

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
                sb.append("\n");
                Log.i("ITEM", myItem.getValue());
            }
            outputView.setText(sb.toString());
        }
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
                Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                getTextFromImage(bmp);
                Log.i("Received message", new String(bytes));
                sendMessage();
            }
        });
        Log.i("Phone client connected", String.valueOf(client.isConnected()));
    }

    @Override
    public void onConnectionSuspended(int i) {
        connectedNode = null;
    }

    public void sendMessage() {

        for (int i = 0; i < connectedNode.size(); i++) {
            String message = "messageFromPhone";
            byte[] bytes = message.getBytes();
            Wearable.MessageApi.sendMessage(client, connectedNode.get(i).getId(), "/meal", bytes);
            Log.i("Message sent", message);
        }
    }


}
