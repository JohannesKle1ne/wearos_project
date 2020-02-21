package com.example.mobilegooglevison;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;


import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Bundle;

import android.os.Environment;
import android.text.method.ScrollingMovementMethod;
import android.util.Base64;
import android.util.Log;
import android.util.SparseArray;

import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;


public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks {

    TextView receivedView;
    TextView resultView;
    TextView userId;
    LogView logView;
    ImageView imageView;
    Button startUserSession;
    EditText idInput;

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
        logView = findViewById(R.id.log);
        logView.setMovementMethod(new ScrollingMovementMethod());
        userId = findViewById(R.id.userId);
        idInput = findViewById(R.id.idInput);
        startUserSession = findViewById(R.id.button);
        receivedView = findViewById(R.id.output);
        resultView = findViewById(R.id.recognized);
        receivedView.setVisibility(View.GONE);
        resultView.setVisibility(View.GONE);
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
                try {
                    handleMessage(bytes);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
        Log.i("Phone client connected", String.valueOf(client.isConnected()));
    }

    @Override
    public void onConnectionSuspended(int i) {
        connectedNode = null;
    }

    private void handleMessage(byte[] bytes) throws JSONException {
        String message = new String(bytes);

        switch(message){
            case (MessageDict.SPACE):
                //receivedView.setText(RECEIVED_VIEW_DEFAULT +"-SPACE-");
                logView.addLine(RECEIVED_VIEW_DEFAULT +"-SPACE-");
                sendMessage(MessageDict.ACK);
                break;
            case (MessageDict.EMPTY):
                //receivedView.setText(RECEIVED_VIEW_DEFAULT +"-Empty-");
                logView.addLine(RECEIVED_VIEW_DEFAULT+"-EMPTY-");
                imageView.setImageResource(0);
                resultView.setText(RESULT_VIEW_DEFAULT);
                break;
            default:
                String type = new JSONObject(message).getString(MessageDict.MESSAGE_TYPE);
                if(type.equals(MessageDict.LOG)){
                    String log = new JSONObject(message).getJSONObject(MessageDict.MESSAGE)
                            .getString(MessageDict.LOG);

                    if (!log.isEmpty()) {

                        if(checkPersmission(Manifest.permission.WRITE_EXTERNAL_STORAGE)){
                            Log.i(TAG,"permission granted!");
                            saveLog(log);

                        }else {

                            File textFile = new File(Environment.
                                    getExternalStoragePublicDirectory(Environment
                                            .DIRECTORY_DOWNLOADS), "name");
                            FileOutputStream fos = null;
                            try {
                                fos = new FileOutputStream(textFile);
                                fos.write(log.getBytes());
                                Log.i(TAG, Environment.
                                        getExternalStoragePublicDirectory(Environment
                                                .DIRECTORY_DOWNLOADS).toString());
                            } catch (FileNotFoundException e) {
                                e.printStackTrace();
                            } catch (IOException e) {
                                e.printStackTrace();
                            } finally {
                                if (fos != null) {
                                    try {
                                        fos.close();
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        }

                    }
                } else {
                    //receivedView.setText(RECEIVED_VIEW_DEFAULT +"-Bitmap-");
                    String bitmap = new JSONObject(message).getJSONObject(MessageDict.MESSAGE)
                            .getString(MessageDict.BITMAP);
                    byte [] encodeByte= Base64.decode(bitmap,Base64.DEFAULT);
                    logView.addLine(RECEIVED_VIEW_DEFAULT + "-BITMAP-");
                    Bitmap bmp = BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.length);
                    makeTextFromImage(bmp);
                }
        }
    }

    private void saveLog(String log) {
       /* int count = 0;

        File sdDirectory = Environment.getExternalStorageDirectory();
        File subDirectory = new File(sdDirectory.toString() + "/Pictures/Paint");

        if (subDirectory.exists()) {

            File[] existing = subDirectory.listFiles();

            for (File file : existing) {

                if (file.getName().endsWith(".jpg") || file.getName().endsWith(".png")) {

                    count++;
                }
            }
        } else {

            subDirectory.mkdir();

        }

        if (subDirectory.exists()) {

            File image = new File(subDirectory, "/drawing_" + (count + 1) + ".png");
            FileOutputStream fileOutputStream;

            try {

                fileOutputStream = new FileOutputStream(image);

                mBitmap.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream);

                fileOutputStream.flush();
                fileOutputStream.close();

                Toast.makeText(getContext(), "saved", Toast.LENGTH_LONG).show();

            } catch (FileNotFoundException e) {


            } catch (IOException e) {


            }

        }
*/

    }

    public boolean checkPersmission(String permission){
        int check = ContextCompat.checkSelfPermission(this, permission);
        return (check == PackageManager.PERMISSION_GRANTED);
    }

    public void sendUserInformation(View v){
        String id = idInput.getText().toString();
        if(id!= null && !id.isEmpty()) {
            try {
                String jsonString = new JSONObject()
                        .put(MessageDict.MESSAGE_TYPE,MessageDict.USER)
                        .put(MessageDict.MESSAGE,new JSONObject()
                                .put(MessageDict.USER,id))
                        .toString();
                sendMessage(jsonString);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            userId.setText(idInput.getText());
            idInput.getText().clear();
        }else{
            Log.i(TAG, "id input Field is empty!");
        }

    }

    public void sendMessage(String message) {
        for (int i = 0; i < connectedNode.size(); i++) {
            byte[] bytes = message.getBytes();
            Wearable.MessageApi.sendMessage(client, connectedNode.get(i).getId(), "/meal", bytes);
            Log.i("Message sent", message);
        }
    }
}
