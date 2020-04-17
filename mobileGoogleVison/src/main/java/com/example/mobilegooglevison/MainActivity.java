package com.example.mobilegooglevison;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Calendar;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import android.os.Bundle;

import android.os.Environment;
import android.text.method.ScrollingMovementMethod;
import android.util.Base64;
import android.util.Log;

import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.example.shared.MessageDict;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.MessageClient;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;

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
    LogView logView;
    BitmapView bitmapView;
    EditText idInput_Starting;
    EditText idInput_Loading;

    private GoogleApiClient client;
    private List<Node> connectedNode;
    private static final String TAG = "PHONE_MAIN";


    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();

        setContentView(R.layout.activity_main);
        bitmapView = findViewById(R.id.bitmapView);
        logView = findViewById(R.id.log);
        logView.setMovementMethod(new ScrollingMovementMethod());
        userId = findViewById(R.id.userId);
        idInput_Loading = findViewById(R.id.idInputForLoading);
        idInput_Starting = findViewById(R.id.idInputForStarting);

        client = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addApi(Wearable.API)
                .build();
        client.connect();
    }

    @Override
    public void onBackPressed() {
        if (bitmapView.getVisibility() == View.VISIBLE) {
            bitmapView.setVisibility(View.INVISIBLE);
        }else{
            super.onBackPressed();
        }

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

                    saveBitmap(bitmap,userId);
                }
                break;
            case (MessageDict.LOG):
                JSONArray messageArray = jsonObject.getJSONArray(MessageDict.MESSAGE);

                for (int i = 0; i < messageArray.length(); i++) {
                    JSONObject currentObject = messageArray.getJSONObject(i);
                    String log = currentObject.getString(MessageDict.LOG);
                    String fileName = currentObject.getString(MessageDict.FILE_NAME);

                    saveLog(log, fileName);
                    logView.setText("Log: \n\n"+log);
                }

                break;
            default:
                Log.i(TAG, "could not handle message");
        }
    }

    private void saveLog(String log, String fileName) {
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
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                Log.i(TAG, "directory does not exist");
            }
        } else {
            Log.i(TAG, "write permission not granted!");
        }
    }

    public void saveBitmap(Bitmap bitmap, int userId){

        //write External
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
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                Log.i(TAG, "directory does not exist");
            }
        } else {
            Log.i(TAG, "write permission not granted!");
        }
        saveInternal(bitmap,userId);
    }

    private void saveInternal(Bitmap bitmap, int userId){
        //write internal
        File directory = new File(getFilesDir(), "userSession_"+userId);
        if(!directory.exists()){
            directory.mkdir();
        }
        String fileName = Calendar.getInstance().getTime().toString()
                .substring(4, 19).replaceAll("\\s+", "_")
                .replaceAll(":", "-")
                + "_id:_" + userId + ".png";

        FileOutputStream fos;
        try {
            fos = new FileOutputStream(new File(directory, fileName));
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);

            fos.flush();
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        bitmapView.addBitmap(bitmap);
        savedSuccessfully();
    }

    public void savedSuccessfully(){
        String jsonString = null;
        try {
            jsonString = new JSONObject()
                    .put(MessageDict.MESSAGE_TYPE, MessageDict.ACK)
                    .put(MessageDict.MESSAGE, MessageDict.BITMAP_SAVED)
                    .toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        sendMessage(jsonString);
    }

    public void handleButtonClick(View v){
        switch (v.getId()) {

            case R.id.startUserSession:
                String idS = idInput_Starting.getText().toString();
                if (idS != null && !idS.isEmpty()) {
                    sendUserInformation(idS);
                } else {
                    Log.i(TAG, "id input Field is empty!");
                }
                break;
            case R.id.showBitmaps:
                bitmapView.show(BitmapView.INCOMING);
                break;
            case R.id.loadBitmaps:
                String idL = idInput_Loading.getText().toString();
                if (idL != null && !idL.isEmpty()) {
                    loadBitmaps(idL);
                } else {
                    Log.i(TAG, "id input Field is empty!");
                }
                break;

        }

    }

    public void loadBitmaps(String idString){

        int userId = Integer.parseInt(idString);

        File[] fileNames;

        File directory = new File(getFilesDir(), "userSession_"+userId);

        if (directory.exists()) {

            fileNames = directory.listFiles();
            Log.i(TAG,"Files: "+fileNames);
            FileInputStream fis;
            ArrayList<Bitmap> bitmaps = new ArrayList<>();

            for (int i = 0; i < fileNames.length; i++) {
                try {
                    Log.i(TAG,"fileName: "+fileNames[i].getName());
                    fis = new FileInputStream(fileNames[i]);
                    Bitmap bitmap = BitmapFactory.decodeStream(fis);
                    bitmaps.add(bitmap);
                    fis.close();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            Log.i(TAG,String.valueOf(bitmaps.size()));
            bitmapView.setLoadedBitmaps(bitmaps);
            bitmapView.show(BitmapView.LOADED);
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

    public void sendUserInformation(String id) {
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
        userId.setText(idInput_Starting.getText());
        idInput_Starting.getText().clear();
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
