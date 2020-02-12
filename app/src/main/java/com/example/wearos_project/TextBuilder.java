package com.example.wearos_project;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.util.Log;

public class TextBuilder {
    private Bitmap resultBitmap;
    private final static String TAG = "TextBuilder";
    private int lastLetterWidth;
    private int lastLetterHeight;


    public void addLetter(Bitmap bm) {
        Bitmap bitmap = applyCrop(bm, 50,1,50,1);
        if(resultBitmap==null){
            resultBitmap = bitmap.copy(Bitmap.Config.ARGB_8888,true);

        }else{
            resultBitmap = combineImages(resultBitmap,bitmap);
        }
        lastLetterWidth = bitmap.getWidth();
        lastLetterHeight = bitmap.getHeight();
    }


    public Bitmap applyCrop(Bitmap bitmap, int leftCrop, int topCrop, int rightCrop, int bottomCrop) {
        int cropWidth = bitmap.getWidth() - rightCrop - leftCrop;
        int cropHeight = bitmap.getHeight() - bottomCrop - topCrop;
        if(cropHeight<=0 || cropWidth <=0){
            Log.i(TAG, "negativ values for Bitmap");
            return bitmap;
        }
        return Bitmap.createBitmap(bitmap, leftCrop, topCrop, cropWidth, cropHeight);
    }



    public void addSpace(){
        Bitmap blackBitmap = Bitmap.createBitmap(lastLetterWidth, lastLetterHeight, Bitmap.Config.ARGB_8888);
        Canvas blackCanvas = new Canvas(blackBitmap);
        blackCanvas.drawColor(Color.BLACK);
        addLetter(blackBitmap);

    }


    private Bitmap combineImages(Bitmap c, Bitmap s) {
        Bitmap cs = null;

        int width, height = 0;

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
    }

    public Bitmap getResult(){
        return resultBitmap;
    }



}
