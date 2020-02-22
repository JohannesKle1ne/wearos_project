package com.example.wearos_project;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.support.wearable.activity.WearableActivity;
import android.util.Base64;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class MainActivity extends WearableActivity implements
        GoogleApiClient.ConnectionCallbacks{


    private HashMap<Integer,WatchLogger> watchLoggers;
    private WatchLogger currentLogger;
    private Vibrator vibrator;
    private PaintView paintView;
    private GoogleApiClient client;
    private List<Node> connectedNode;
    private CountDownTimer letterAndResetTimer;
    private CountDownTimer doubleTapTimer;
    private static final String TAG = "WATCH_MAIN";
    private boolean waitingForReset = false;
    private boolean waitingForDoubleTap = false;

    private boolean userSessionRunning = false;

    private TextBuilder textbuilder;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        paintView = findViewById(R.id.paintView);
        textbuilder = new TextBuilder();
        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        watchLoggers = new HashMap();

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
            if (userSessionRunning) {
                if (keyCode == KeyEvent.KEYCODE_STEM_1) {
                    if (waitingForReset) {
                        textbuilder.resetResult();
                        waitingForReset = false;
                        Log.i(TAG, "RESET");
                    } else {
                        textbuilder.removeLetter();
                        startResetTimer();
                        Log.i(TAG, "REMOVE");
                    }
                    return true;
                } else if (keyCode == KeyEvent.KEYCODE_STEM_2) {
                    try {
                        sendBitmap(textbuilder.getResult());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    return true;
                }
            } else {
                Toast.makeText(this, "no user session started", Toast.LENGTH_SHORT).show();
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
                try {
                    handleMessage(message);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

    }

    public void startUserSession(int userId){
        if(watchLoggers.containsKey(userId)){
            currentLogger = watchLoggers.get(userId);
            Toast.makeText(this, "user session reactivated", Toast.LENGTH_SHORT).show();
        } else {
            currentLogger = new WatchLogger(userId);
            watchLoggers.put(userId, currentLogger);
            Toast.makeText(this, "user session started", Toast.LENGTH_SHORT).show();
        }
        textbuilder.setLogger(currentLogger);
        userSessionRunning = true;
    }



    private void handleMessage(String message) throws JSONException {
        JSONObject messageObject = new JSONObject(message);
        String messageType = messageObject.getString(MessageDict.MESSAGE_TYPE);
        switch(messageType){
            case (MessageDict.ACK):
                vibrate();
                break;
            case (MessageDict.USER):
                int userID = messageObject.getJSONObject(MessageDict.MESSAGE)
                        .getJSONObject(MessageDict.USER).getInt(MessageDict.ID);
                startUserSession(userID);
                vibrateLong();
                break;
            case(MessageDict.LOG_REQUEST):
                sendLogs();
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

    public void sendBitmap(Bitmap bitmap) throws JSONException {
        JSONObject json = new JSONObject()
                .put(MessageDict.MESSAGE_TYPE,MessageDict.BITMAP)
                .put(MessageDict.MESSAGE,new JSONObject());
        if(bitmap==null){
            json.getJSONObject(MessageDict.MESSAGE)
                    .put(MessageDict.BITMAP,MessageDict.EMPTY).toString();
            send(json.toString().getBytes());
        }else {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
            byte[] byteArray = stream.toByteArray();
            String bmString = Base64.encodeToString(byteArray, Base64.DEFAULT);

            json.getJSONObject(MessageDict.MESSAGE)
                    .put(MessageDict.BITMAP,bmString).toString();
            try {
                stream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            send(json.toString().getBytes());
        }
    }

    public void sendLogs() throws JSONException {
        JSONObject jsonObject = new JSONObject()
                .put(MessageDict.MESSAGE_TYPE, MessageDict.LOG)
                .put(MessageDict.MESSAGE, new JSONArray());
        JSONArray logArray = jsonObject.getJSONArray(MessageDict.MESSAGE);

        for (Map.Entry<Integer, WatchLogger> entry : watchLoggers.entrySet()) {

            String logs = entry.getValue().getLogs();
            logArray.put(
                    new JSONObject()
                            .put(MessageDict.USER, new JSONObject()
                                    .put(MessageDict.ID, entry.getValue().getId()))
                            .put(MessageDict.LOG, logs));

        }
        send(jsonObject.toString().getBytes());
    }

    public void send(byte[] byteArray){
        for (int i = 0; i < connectedNode.size(); i++) {
            Wearable.MessageApi.sendMessage(client, connectedNode.get(i).getId(), "/meal", byteArray);
            Log.i("MessageDict sent", "ByteArray");
        }
    }


    public void vibrate(){
        vibrator.vibrate(VibrationEffect.createOneShot(100,VibrationEffect.DEFAULT_AMPLITUDE));
    }
    public void vibrateLong(){
        vibrator.vibrate(VibrationEffect.createOneShot(500,VibrationEffect.DEFAULT_AMPLITUDE));
    }


    public void startLetterTimer() {
        if(letterAndResetTimer !=null) {
            letterAndResetTimer.cancel();
        }
        letterAndResetTimer = new CountDownTimer(400, 100) {
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
        letterAndResetTimer.start();
    }

    public void startResetTimer() {
        if(letterAndResetTimer !=null) {
            letterAndResetTimer.cancel();
        }
        waitingForReset = true;
        letterAndResetTimer = new CountDownTimer(200, 100) {
            public void onTick(long millisUntilFinished) {
            }
            public void onFinish() {
                waitingForReset =false;
                Log.i("onFinish","done!");
            }
        };
        letterAndResetTimer.start();
    }

    public void startDoubleTapTimer() {
        if(doubleTapTimer!=null) {
            doubleTapTimer.cancel();
        }
        waitingForDoubleTap = true;
        doubleTapTimer = new CountDownTimer(200, 100) {
            public void onTick(long millisUntilFinished) {
            }
            public void onFinish() {
                waitingForDoubleTap = false;
                Log.i("onFinish","done!");
            }
        };
        doubleTapTimer.start();
    }


    public void cancelLetterTimer() {
        if(letterAndResetTimer !=null)
            letterAndResetTimer.cancel();
    }

    public void cancelDoubleTapTimer() {
        if(doubleTapTimer!=null)
            doubleTapTimer.cancel();
    }


    public TextBuilder getTextBuilder() {
        return textbuilder;
    }

    public boolean isWaitingForDoubleTap(){
        return waitingForDoubleTap;
    }

    public boolean isUserSessionRunning() {
        return userSessionRunning;
    }

}
