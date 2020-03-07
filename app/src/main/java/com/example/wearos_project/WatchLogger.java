package com.example.wearos_project;

import android.nfc.Tag;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Calendar;

public class WatchLogger {

    public static final int SESSION_ENDED = 0;
    public static final int SPACE = 1;
    public static final int REMOVE = 2;
    public static final int RESET = 3;
    public static final int SESSION_STARTED = 4;
    public static final int LETTER_START = 5;
    public static final int LETTER_END = 6;
    public static final int SEND = 7;
    public static final int WORD_END = 8;

    private long initTime;
    private final String TAG = "WatchLogger";
    private int id;
    private String fileName;
    private ArrayList<LogEntry> logs;
    private ArrayList<Letter> letters;
    private ArrayList<Word> words;
    private Letter currentLetter;
    private Word currentWord;



    public WatchLogger(int id){
        this.id = id;
        logs = new ArrayList<>();
        letters = new ArrayList<>();
        words = new ArrayList<>();
        currentLetter = null;
        currentWord = null;

        initTime = SystemClock.elapsedRealtime();
        logs.add(new LogEntry(SESSION_STARTED,null));
        fileName = Calendar.getInstance().getTime().toString()
                .substring(4,19).replaceAll("\\s+","_")
                .replaceAll(":","-")
                +"_id:_"+id+".txt";
    }

    public void log(int type) {
        switch(type) {
            case (LETTER_START):
                currentLetter = new Letter(getCurrentTimestamp());
                break;
            case (SEND):
                if(currentWord!=null){
                    currentWord.setFinishTime(getCurrentTimestamp());
                    logs.add(new LogEntry(type, logs.get(logs.size() - 1)));
                    words.add(currentWord);
                    currentWord = null;
                }
                break;
            default:
                logs.add(new LogEntry(type, logs.get(logs.size() - 1)));
        }
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

    public String getFilename(){
        return fileName;
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
                case (SPACE):
                    if(currentWord!=null){
                        currentWord.setFinishTime(getCurrentTimestamp());
                        this.logString = "added space, word finished in: "
                                +((currentWord.getFinishTime()-currentWord.getStartTime())/1000)+"sec";
                        words.add(currentWord);
                        currentWord = null;
                    }else{
                        this.logString = "added space, no word finished";
                    }
                    for (int i = 0; i < words.size(); i++) {
                        Log.d(TAG, "WORDS1: "+words.get(i));
                    }
                    break;
                case (LETTER_END):
                    currentLetter.setFinishTime(getCurrentTimestamp());
                    this.logString = "added letter with duration: "
                            +((currentLetter.getFinishTime()-currentLetter.getStartTime())/1000)+"sec";
                    letters.add(currentLetter);
                    if(currentWord==null){
                        currentWord = new Word(currentLetter.getStartTime());
                    }
                    break;
                case (WORD_END): this.logString = "added word";
                    break;
                case (SESSION_ENDED):
                    if(currentWord!=null){
                        currentWord.setFinishTime(getCurrentTimestamp());
                        this.logString = "word finished in: "
                                +((currentWord.getFinishTime()-currentWord.getStartTime())/1000)+"sec";
                        words.add(currentWord);
                        currentWord = null;
                    }
                    Log.d(TAG,words.size()+"");
                    for (int i = 0; i < words.size(); i++) {
                        Log.d(TAG, "WORDS2: "+words.get(i));
                    }
                    double duration = words.get(words.size()-1).getFinishTime()-words.get(0).getStartTime()/60000;
                    double wpm = Math.round((words.size()/duration)*100.0)/100.0;
                    this.logString = "user session ended. speed: "+wpm+" wpm";
                    break;
                case (REMOVE): this.logString = "removed Letter";
                    break;
                case (RESET): this.logString = "reset all letters";
                    break;
                case (SEND):
                    if(currentWord!=null){
                        currentWord.setFinishTime(getCurrentTimestamp());
                        this.logString = "word finished in: "
                                +((currentWord.getFinishTime()-currentWord.getStartTime())/1000)+"sec";
                        words.add(currentWord);
                        currentWord = null;
                    }
                    for (int i = 0; i < words.size(); i++) {
                        Log.d(TAG, "WORDS3: "+words.get(i));
                    }
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
                return logString; //+" (sec "+ timestamp / 1000+")";
            }else {
                return logString; //+" (sec "+ timestamp / 1000+")";
            }
        }

    }
    private class Letter{
        double startTime;
        double finishTime;

        public Letter(double startTime){
            this.startTime = startTime;
        }

        public void setFinishTime(double finishTime) {
            this.finishTime = finishTime;
        }

        public double getStartTime() {
            return startTime;
        }

        public double getFinishTime() {
            return finishTime;
        }
    }
    private class Word{
        double startTime;
        double finishTime;

        public Word(double startTime){
            this.startTime = startTime;
        }

        public void setStartTime(double startTime) {
            this.startTime = startTime;
        }

        public void setFinishTime(double finishTime) {
            this.finishTime = finishTime;
        }
        public double getStartTime() {
            return startTime;
        }

        public double getFinishTime() {
            return finishTime;
        }
    }
}

