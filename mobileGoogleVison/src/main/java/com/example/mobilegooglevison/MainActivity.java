package com.example.mobilegooglevison;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.Calendar;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
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


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;


public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks {

    TextView userId;
    TextView watchLogCount;
    LogView logView;
    Button startUserSession;
    Button getWatchLogs;
    EditText idInput;

    private GoogleApiClient client;
    private List<Node> connectedNode;
    private static final String TAG = "PHONE_MAIN";
    private static final String RESULT_VIEW_DEFAULT = "Result: ";
    private static final String RECEIVED_VIEW_DEFAULT = "Received: ";


    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();

        setContentView(R.layout.activity_main);
        logView = findViewById(R.id.log);
        logView.setMovementMethod(new ScrollingMovementMethod());
        userId = findViewById(R.id.userId);
        watchLogCount = findViewById(R.id.watchLogCount);
        idInput = findViewById(R.id.idInput);
        startUserSession = findViewById(R.id.button);
        getWatchLogs = findViewById(R.id.getWatchLogs);



        client = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addApi(Wearable.API)
                .build();
        client.connect();
    }

    public void openLandscapeActivity(View v){
        Intent intent = new Intent(this, LandscapeActivity.class);
        startActivity(intent);
    }

    public void makeTextFromImage(Bitmap bm){

        //other branch
        //bitmapView.setImageBitmap(bm);

        Bitmap bitmap = bm;

        //bitmapView.setImageBitmap(bitmap);

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
                sendHey();
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
    }

    @Override
    public void onConnectionSuspended(int i) {
        connectedNode = null;
    }

    private void handleMessage(byte[] bytes) throws JSONException {
        Log.i(TAG, "received: "+new String(bytes));
        JSONObject jsonObject = new JSONObject(new String(bytes));
        String messageType = jsonObject.getString(MessageDict.MESSAGE_TYPE);

        switch(messageType){
            case (MessageDict.BITMAP):
                JSONObject messageObject = jsonObject.getJSONObject(MessageDict.MESSAGE);
                String bitmapString = messageObject.getString(MessageDict.BITMAP);
                int userId = messageObject.getJSONObject(MessageDict.USER).getInt(MessageDict.ID);
                if (bitmapString.equals(MessageDict.EMPTY)) {
                    //bitmapView.setImageResource(0);
                    //resultView.setText(RESULT_VIEW_DEFAULT);
                }else {
                    byte[] encodeByte = Base64.decode(bitmapString, Base64.DEFAULT);
                    Bitmap bitmap = BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.length);

                    //bitmapView.setImageBitmap(bitmap);
                    BitmapStorage.getInstance().addBitmap(bitmap);

                    if(saveBitmap(bitmap, userId)){
                        String jsonString = new JSONObject()
                                .put(MessageDict.MESSAGE_TYPE, MessageDict.ACK)
                                .put(MessageDict.MESSAGE, MessageDict.BITMAP_SAVED)
                                .toString();
                        sendMessage(jsonString);
                    }
                }
                break;
            case (MessageDict.LOG):
                JSONArray messageArray = jsonObject.getJSONArray(MessageDict.MESSAGE);
                boolean successfulSaved = true;

                for (int i = 0; i < messageArray.length(); i++) {
                    JSONObject currentObject = messageArray.getJSONObject(i);
                    String log = currentObject.getString(MessageDict.LOG);
                    String fileName = currentObject.getString(MessageDict.FILE_NAME);

                    successfulSaved = saveLog(log, fileName)&&successfulSaved;
                    logView.setText("Log: \n\n"+log);
                }
                if(successfulSaved){
                    //watchLogCount.setText(messageArray.length()+" log files saved");
                }else{
                    //watchLogCount.setText("SAVE ERROR");
                }

                break;
            default:
                Log.i(TAG, "could not handle message");
        }
    }

    private boolean saveLog(String log, String fileName) {
        if (checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                && isExternalStorageWritable()) {

            File filePath = new File(Environment.getExternalStorageDirectory()
                    .toString() + "/#Logs");

            if (filePath.exists()) {

                File textFile = new File(filePath, fileName);
                FileOutputStream fos;

                try {
                    fos = new FileOutputStream(textFile);
                    fos.write(log.getBytes());
                    fos.close();
                    return true;
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    return false;
                } catch (IOException e) {
                    e.printStackTrace();
                    return false;
                }
            } else {
                Log.i(TAG, "directory does not exist");
                return false;
            }
        } else {
            Log.i(TAG, "write permission not granted!");
            return false;
        }
    }

    public boolean saveBitmap(Bitmap bitmap, int userId){
        if (checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                && isExternalStorageWritable()) {

            File filePath = new File(Environment.getExternalStorageDirectory()
                    .toString() + "/#Bitmaps");
            String fileName = Calendar.getInstance().getTime().toString()
                    .substring(4,19).replaceAll("\\s+","_")
                    .replaceAll(":","-")
                    +"_id:_"+userId+".png";

            if (filePath.exists()) {

                File bitmapFile = new File(filePath, fileName);
                FileOutputStream fos;

                try {
                    fos = new FileOutputStream(bitmapFile);
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);

                    fos.flush();
                    fos.close();
                    return true;
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    return false;
                } catch (IOException e) {
                    e.printStackTrace();
                    return false;
                }
            } else {
                Log.i(TAG, "directory does not exist");
                return false;
            }
        } else {
            Log.i(TAG, "write permission not granted!");
            return false;
        }
    }



    public boolean isExternalStorageWritable(){
        if(Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())){
            return true;
        }else{
            return false;
        }
    }

    public boolean checkPermission(String permission){
        int check = ContextCompat.checkSelfPermission(this, permission);
        return (check == PackageManager.PERMISSION_GRANTED);
    }

    public void sendUserInformation(View v){
        String id = idInput.getText().toString();
        if(id != null && !id.isEmpty()) {
            try {
                String jsonString = new JSONObject()
                        .put(MessageDict.MESSAGE_TYPE, MessageDict.USER)
                        .put(MessageDict.MESSAGE, new JSONObject()
                                .put(MessageDict.USER, new JSONObject()
                                        .put(MessageDict.ID, id)))
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

    private void sendHey(){
        try {
            String jsonString = new JSONObject()
                    .put(MessageDict.MESSAGE_TYPE,MessageDict.HEY)
                    .put(MessageDict.MESSAGE,null)
                    .toString();
            sendMessage(jsonString);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void requestWatchLogs(View v){
        /*try {
            String jsonString = new JSONObject()
                    .put(MessageDict.MESSAGE_TYPE,MessageDict.LOG_REQUEST)
                    .put(MessageDict.MESSAGE,null)
                    .toString();
            sendMessage(jsonString);
        } catch (JSONException e) {
            e.printStackTrace();
        }*/
        try {
            String jsonString = new JSONObject()
                    .put(MessageDict.MESSAGE_TYPE,MessageDict.END_USER_SESSION)
                    .put(MessageDict.MESSAGE,null)
                    .toString();
            sendMessage(jsonString);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void sendMessage(String message) {
        for (int i = 0; i < connectedNode.size(); i++) {
            byte[] bytes = message.getBytes();
            Wearable.MessageApi.sendMessage(client, connectedNode.get(i).getId(), "/meal", bytes);
            Log.i("sent: ", message);
        }
    }
}
