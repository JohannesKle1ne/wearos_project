package com.example.wearos_project;

import android.util.Log;

import java.util.ArrayList;

public class WatchLogger {

    public static final int LETTER = 0;
    public static final int SPACE = 1;
    public static final int REMOVE = 2;
    public static final int RESET = 3;



    private final String TAG = "WatchLogger";
    private int id;

    private ArrayList<String> logs;



    public WatchLogger(int id){
        this.id = id;
        logs = new ArrayList<>();
        logs.add("new session started with id: "+id);
    }

    public void log(int type) {
        switch(type){
            case (LETTER):
                logs.add("added letter");
                break;
            case (SPACE):
                logs.add("added space");
                break;
            case (REMOVE):
                logs.add("removed letter");
                break;
            case (RESET):
                logs.add("reset all letters");
                break;
            default:
                Log.d(TAG, "there's no such logger");
                break;
        }
    }

    public ArrayList<String> getLogs() {
        return logs;
    }


}
