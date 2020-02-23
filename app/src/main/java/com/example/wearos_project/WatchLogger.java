package com.example.wearos_project;

import android.os.SystemClock;
import android.util.Log;

import androidx.annotation.NonNull;

import java.util.ArrayList;

public class WatchLogger {

    public static final int LETTER = 0;
    public static final int SPACE = 1;
    public static final int REMOVE = 2;
    public static final int RESET = 3;
    public static final int SESSION_STARTED = 4;

    private long initTime;
    private final String TAG = "WatchLogger";
    private int id;
    private ArrayList<LogEntry> logs;



    public WatchLogger(int id){
        this.id = id;
        logs = new ArrayList<>();
        initTime = SystemClock.elapsedRealtime();
        logs.add(new LogEntry(SESSION_STARTED,null));
    }

    public void log(int type) {
        logs.add(new LogEntry(type,logs.get(logs.size() - 1)));
    }

    private double getCurrentTimestamp(){
        return SystemClock.elapsedRealtime()-initTime;
    }

    public String getLogs() {
        String fullString = "";
        for(int i= 0; i<logs.size();i++) {
            fullString = fullString + logs.get(i) + "\n";
        }
        return fullString;
    }

    public int getId() {
        return id;
    }


    private class LogEntry{

        private String logString;
        private double timestamp;
        private LogEntry referenceEntry;
        private int entryType;

        public LogEntry(int entryType, LogEntry referenceEntry) {
            this.timestamp = getCurrentTimestamp();
            this.referenceEntry = referenceEntry;
            this.entryType = entryType;

            switch(entryType){
                case (SESSION_STARTED): this.logString = "user session started with id: "+id;
                    break;
                case (LETTER): this.logString = "added letter";
                    break;
                case (SPACE): this.logString = "added space";
                    break;
                case (REMOVE): this.logString = "removed Letter";
                    break;
                case (RESET): this.logString = "reset all letters";
                    break;
                default:
                    Log.d(TAG, "there's no such entryType");
                    break;
            }
        }

        public double getTimestamp() {
            return timestamp;
        }

        @NonNull
        @Override
        public String toString() {
            if(entryType == SESSION_STARTED){
                return logString = logString + " | time passed: "
                        + timestamp / 1000 + "s (absolute)";
            }else {
                return logString + " | time passed: " + timestamp / 1000 + "s (absolute), "
                        + (timestamp - referenceEntry.getTimestamp())/1000 + "s (relative)";
            }
        }
    }
}

