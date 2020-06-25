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
    public static final int REP_A = 9;
    public static final int REP_B = 10;
    public static final int REP_C = 11;
    public static final int REP_D = 12;


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
        logs.add(new LogEntry(SESSION_STARTED));
        fileName = Calendar.getInstance().getTime().toString()
                .substring(4,19).replaceAll("\\s+","_")
                .replaceAll(":","-")
                +"_id:_"+id+".txt";
    }

    //TODO move all cases here
    public void log(int type) {
        switch(type) {
            case (LETTER_START):
                //more letters are started than finished
                currentLetter = new Letter(getCurrentTimestamp());
                break;
            case (SEND):
                if(currentWord!=null){
                    currentWord.setFinishTime(getCurrentTimestamp());
                    logs.add(new LogEntry(WORD_END));
                    words.add(currentWord);
                    currentWord = null;
                }
                logs.add(new LogEntry(type));
                break;
            default:
                logs.add(new LogEntry(type));
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


    private double calculateWpm(){
        if (words.size()<1){
            return 0;
        }else {
            double wordsStart = words.get(0).getStartTime();
            double wordsEnd = words.get(words.size() - 1).getFinishTime();
            double duration = (wordsEnd - wordsStart) / 60000;
            double wpm = Math.round((words.size() / duration) * 100.0) / 100.0;
            return Math.round(wpm * 100.0) / 100.0;
        }
    }

    private double calculateDuration() {
        if (words.size() < 1) {
            return 0;
        } else {
            double wordsStart = words.get(0).getStartTime();
            double wordsEnd = words.get(words.size() - 1).getFinishTime();
            double duration = (wordsEnd - wordsStart) / 60000;
            return Math.round(duration * 100.0) / 100.0;
        }
    }



    private class LogEntry{

        private String logString;
        private double timestamp;
        private int entryType;

        public LogEntry(int entryType) {
            this.timestamp = getCurrentTimestamp();
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
                    break;
                case (LETTER_END):
                    currentLetter.setFinishTime(getCurrentTimestamp());
                    this.logString = "added letter with duration: "
                            +((currentLetter.getFinishTime()-currentLetter.getStartTime())/1000)+"sec";
                    letters.add(currentLetter);
                    if(currentWord==null){
                        //A word is started right after the first letter has ended.
                        // ..It gets passed the letters start time
                        currentWord = new Word(currentLetter.getStartTime());
                    }
                    break;
                case (WORD_END):
                    this.logString = "word finished in: "
                            +((currentWord.getFinishTime()-currentWord.getStartTime())/1000)+"sec";
                    break;
                case (SESSION_ENDED):
                    if(currentWord!=null){
                        currentWord.setFinishTime(getCurrentTimestamp());
                        this.logString = "word finished in: "
                                +((currentWord.getFinishTime()-currentWord.getStartTime())/1000)+"sec";
                        words.add(currentWord);
                        currentWord = null;
                    }
                    //wpm refers to the time interval from the touch of the first letter(also the word start time),
                    //until the end of the last Word
                    this.logString = "user session ended. speed: "+calculateWpm()+" wpm";
                    words.clear();
                    break;
                case (REMOVE): this.logString = "removed Letter";
                    break;
                case (RESET): this.logString = "reset all letters";
                    break;
                case (SEND):
                        this.logString = "message sent. entry speed: "+calculateWpm()+" wpm, duration: "+calculateDuration()+ " min";
                        words.clear();
                    break;
                case(REP_A):
                    this.logString = "recipient: A ";
                    break;
                case(REP_B):
                    this.logString = "recipient: B ";
                    break;
                case(REP_C):
                    this.logString = "recipient: C ";
                    break;
                case(REP_D):
                    this.logString = "recipient: D ";
                    break;
                default:
                    Log.d(TAG, "there's no such entryType");
                    break;
            }
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

