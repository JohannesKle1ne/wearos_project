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

import java.util.HashMap;
import java.util.List;


public class MainActivity extends WearableActivity implements
        GoogleApiClient.ConnectionCallbacks{


    private HashMap<Integer,WatchLogger> watchLoggers;
    private WatchLogger currentLogger;
    private Vibrator vibrator;
    private PaintView paintView;
    private GoogleApiClient client;
    private List<Node> connectedNode;
    private CountDownTimer letterTimer;
    private CountDownTimer doubleTapTimer;
    CountDownTimer vibrationTimer;
    private static final String TAG = "WATCH_MAIN";
    private boolean waitingForDoubleTap = false;

    private boolean letterTimerRunning = false;
    private State state;

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
        state = State.NO_SESSION;


        client = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addApi(Wearable.API)
                .build();
        client.connect();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

        getPhoneNode();

        Wearable.MessageApi.addListener(client, new MessageClient.OnMessageReceivedListener() {
            @Override
            public void onMessageReceived(@NonNull MessageEvent messageEvent) {
                String message = new String(messageEvent.getData());
                Log.i("Received: ", message);
                try {
                    handleMessage(message);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void getPhoneNode(){
        if(connectedNode==null || connectedNode.size()==0) {
            Wearable.NodeApi.getConnectedNodes(client).setResultCallback(new ResultCallback<NodeApi.GetConnectedNodesResult>() {
                @Override
                public void onResult(NodeApi.GetConnectedNodesResult getConnectedNodesResult) {
                    connectedNode = getConnectedNodesResult.getNodes();
                }
            });
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (event.getRepeatCount() == 0) {
            if (state == State.ENTER_LETTERS) {
                if (keyCode == KeyEvent.KEYCODE_STEM_1) {
                    try {
                        abridgeLetterTimer();
                        sendBitmap(textbuilder.getResult());
                        currentLogger.log(WatchLogger.SEND);
                        sendLogs();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    return true;
                } else if (keyCode == KeyEvent.KEYCODE_STEM_2) {
                    abridgeLetterTimer();
                    textbuilder.removeLetter();
                    return true;
                }
            } else {
                Toast.makeText(this, "no user session started", Toast.LENGTH_SHORT).show();
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    public void startUserSession(int userId){
        /*if(watchLoggers.containsKey(userId)){
            currentLogger = watchLoggers.get(userId);
            Toast.makeText(this, "user session reactivated", Toast.LENGTH_SHORT).show();
        } else {
            currentLogger = new WatchLogger(userId);
            watchLoggers.put(userId, currentLogger);
            Toast.makeText(this, "user session started", Toast.LENGTH_SHORT).show();
        }*/
        Toast.makeText(this, "user session started", Toast.LENGTH_SHORT).show();
        currentLogger = new WatchLogger(userId);
        textbuilder.setLogger(currentLogger);
        state = State.ENTER_LETTERS;    ///here change State
        vibrate();
        //vibrateEndless();
    }



    private void handleMessage(String message) throws JSONException {
        JSONObject messageObject = new JSONObject(message);
        String messageType = messageObject.getString(MessageDict.MESSAGE_TYPE);
        switch(messageType){
            case (MessageDict.ACK):
                String ackType = messageObject.getString(MessageDict.MESSAGE);
                switch (ackType) {
                    case (MessageDict.BITMAP_SAVED):
                        textbuilder.resetResult();
                        break;
                    default:
                        Toast.makeText(this, "received unknown ack type", Toast.LENGTH_SHORT).show();
                }
                break;
            case (MessageDict.USER):
                int userID = messageObject.getJSONObject(MessageDict.MESSAGE)
                        .getJSONObject(MessageDict.USER).getInt(MessageDict.ID);
                startUserSession(userID);
                //vibrateLong();
                break;
            case(MessageDict.LOG_REQUEST):
                //sendLogs();
                break;
            case(MessageDict.END_USER_SESSION):
                state = State.NO_SESSION;
                currentLogger.log(WatchLogger.SESSION_ENDED);
                sendLogs();
                break;
            case(MessageDict.HEY):
                getPhoneNode();
                break;
            default:
                Toast.makeText(this, "received unknown message", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i("Wear", "Google Api Client connection suspended!");

    }

    public void sendBitmap(Bitmap bitmap) throws JSONException {

        JSONObject json = new JSONObject()
                .put(MessageDict.MESSAGE_TYPE,MessageDict.BITMAP)
                .put(MessageDict.MESSAGE,new JSONObject()
                        .put(MessageDict.USER,new JSONObject()
                                .put(MessageDict.ID,currentLogger.getId())));
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

        logArray.put(
                new JSONObject()
                        .put(MessageDict.USER, new JSONObject()
                                .put(MessageDict.ID, currentLogger.getId()))
                        .put(MessageDict.LOG, currentLogger.getLogs())
                        .put(MessageDict.FILE_NAME, currentLogger.getFilename()));
        /*for (Map.Entry<Integer, WatchLogger> entry : watchLoggers.entrySet()) {

            String logs = entry.getValue().getLogs();
            logArray.put(
                    new JSONObject()
                            .put(MessageDict.USER, new JSONObject()
                                    .put(MessageDict.ID, entry.getValue().getId()))
                            .put(MessageDict.LOG, logs));

        }*/
        send(jsonObject.toString().getBytes());
    }

    public void send(byte[] byteArray){ //changed meal!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
        for (int i = 0; i < connectedNode.size(); i++) {
            Wearable.MessageApi.sendMessage(client, connectedNode.get(i).getId(), "/1", byteArray);
            Log.d("sent: ", new String(byteArray));
        }
    }


    public void vibrate(){
        long milliseconds = 100;
        int amplitude = 50;
        vibrator.vibrate(VibrationEffect.createOneShot(milliseconds,amplitude));
    }

    public void vibrateSoft(){
        long milliseconds = 100;
        int amplitude = 10;
        vibrator.vibrate(VibrationEffect.createOneShot(milliseconds,amplitude));
    }

    public void vibrateLong(){
        long milliseconds = 500;
        int amplitude = 50;
        vibrator.vibrate(VibrationEffect.createOneShot(milliseconds,amplitude));
    }


    public void vibrateEndless(){
        vibrateSoft();
        vibrationTimer = new CountDownTimer(600, 100) {
            public void onTick(long millisUntilFinished) {
            }
            public void onFinish() {
                if(state == State.PICK_RECIPIENT){
                    vibrateEndless();
                }
            }
        };
        vibrationTimer.start();
    }


    public void startLetterTimer() {
        if(letterTimer !=null) {
            letterTimer.cancel();
        }
        letterTimerRunning = true;
        letterTimer = new CountDownTimer(400, 300) {
            public void onTick(long millisUntilFinished) {
            }
            public void onFinish() {
                Bitmap bitmap = paintView.getBitmap();
                textbuilder.addLetter(bitmap);
                currentLogger.log(WatchLogger.LETTER_END);
                vibrate();
                paintView.clear();
                letterTimerRunning = false;
            }
        };
        letterTimer.start();
    }

    public void abridgeLetterTimer() {
        if (letterTimerRunning) {
            cancelLetterTimer();
            Bitmap bitmap = paintView.getBitmap();
            textbuilder.addLetter(bitmap);
            currentLogger.log(WatchLogger.LETTER_END);
            paintView.clear();
        }
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
            }
        };
        doubleTapTimer.start();
    }


    public void cancelLetterTimer() {
        if(letterTimer !=null) {
            letterTimerRunning = false;
            letterTimer.cancel();
        }
    }

    public void cancelDoubleTapTimer() {
        if(doubleTapTimer!=null)
            doubleTapTimer.cancel();
    }

    public int getConnectedNodeSize() {
        return connectedNode.size();
    }

    public WatchLogger getCurrentLogger() {
        return currentLogger;
    }

    public TextBuilder getTextBuilder() {
        return textbuilder;
    }

    public boolean isWaitingForDoubleTap(){
        return waitingForDoubleTap;
    }

    public boolean isLetterTimerRunning(){
        return letterTimerRunning;
    }

    public State getState(){
        return state;
    }
    public void setState(State state){
        this.state = state;
    }
}
