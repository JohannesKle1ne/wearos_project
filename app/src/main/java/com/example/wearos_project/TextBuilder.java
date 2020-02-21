package com.example.wearos_project;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.SystemClock;
import android.util.Log;

public class TextBuilder {

    private WatchLogger logger;
    private Bitmap resultBitmap;
    private final static String TAG = "TextBuilder";
    private int lastLetterWidth = 296;
    private int lastLetterHeight = 416;


    public void addLetter(Bitmap bm) {
        //Log.i(TAG,String.valueOf(SystemClock.uptimeMillis()));
        //Log.i(TAG,String.valueOf(SystemClock.elapsedRealtime()/1000));

        Bitmap bitmap = applyCrop(bm, 60,0,60,0);
        if(resultBitmap==null){
            resultBitmap = bitmap.copy(Bitmap.Config.ARGB_8888,true);

        }else{
            resultBitmap = combineImages(resultBitmap,bitmap);
        }
        lastLetterWidth = bitmap.getWidth();
        lastLetterHeight = bitmap.getHeight();
        logger.log(WatchLogger.LETTER);
    }

    public void removeLetter(){
        if(resultBitmap!=null){
            resultBitmap = applyCrop(resultBitmap,0,0, lastLetterWidth,0);
            logger.log(WatchLogger.REMOVE);
        }
    }



    public Bitmap applyCrop(Bitmap bitmap, int leftCrop, int topCrop, int rightCrop, int bottomCrop) {
        if(bitmap==null){
            Log.i(TAG, "can't crop empty bitmap");
            return bitmap;
        }
        int cropWidth = bitmap.getWidth() - rightCrop - leftCrop;
        int cropHeight = bitmap.getHeight() - bottomCrop - topCrop;
        if(cropHeight<=0 || cropWidth <=0){
            Log.i(TAG, "Crop made bitmap empty");
            return null;
        }
        return Bitmap.createBitmap(bitmap, leftCrop, topCrop, cropWidth, cropHeight);
    }



    public void addSpace(){
        Bitmap blackBitmap = Bitmap.createBitmap(lastLetterWidth, lastLetterHeight, Bitmap.Config.ARGB_8888);
        Canvas blackCanvas = new Canvas(blackBitmap);
        blackCanvas.drawColor(Color.WHITE);
        if(resultBitmap==null){
            resultBitmap = blackBitmap.copy(Bitmap.Config.ARGB_8888,true);

        }else{
            resultBitmap = combineImages(resultBitmap,blackBitmap);
        }
        logger.log(WatchLogger.SPACE);
    }


    private Bitmap combineImages(Bitmap c, Bitmap s) {
        Bitmap cs;

        int width, height;

        if (c.getHeight() > s.getHeight()) {
            width = c.getWidth() + s.getWidth();
            height = c.getHeight();
        } else {
            width = c.getWidth() + s.getWidth();
            height = s.getHeight();
        }

        cs = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

        Canvas comboImage = new Canvas(cs);

        comboImage.drawBitmap(c, 0f, 0f, null);
        comboImage.drawBitmap(s, c.getWidth(), 0f, null);

        return cs;
    }

    public void resetResult(){
        resultBitmap=null;
        logger.log(WatchLogger.RESET);
    }

    public Bitmap getResult(){
        return resultBitmap;
    }

    public void setLogger(WatchLogger logger) {
        this.logger = logger;
    }


}
