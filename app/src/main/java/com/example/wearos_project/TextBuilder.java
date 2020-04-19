package com.example.wearos_project;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.util.Log;

public class TextBuilder {

    private WatchLogger logger;
    private Bitmap resultBitmap;
    private final static String TAG = "TextBuilder";
    private final int letterWidth = 296;
    private final int letterHeight = 416;
    private final int lineLetterLimit = 15;


    public void addLetter(Bitmap bm) {

        Bitmap bitmap = applyCrop(bm, 60, 0, 60, 0);
        if (resultBitmap == null) {
            resultBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);

        } else {
            resultBitmap = combineImages(resultBitmap, bitmap);
        }
    }

    public void removeLetter() {
        if (resultBitmap != null) {
            resultBitmap = applyCrop(resultBitmap, 0, 0, letterWidth, 0);
            logger.log(WatchLogger.REMOVE);
        }
    }


    public Bitmap applyCrop(Bitmap bitmap, int leftCrop, int topCrop, int rightCrop, int bottomCrop) {
        if (bitmap == null) {
            Log.i(TAG, "can't crop empty bitmap");
            return bitmap;
        }
        int cropWidth = bitmap.getWidth() - rightCrop - leftCrop;
        int cropHeight = bitmap.getHeight() - bottomCrop - topCrop;
        if (cropHeight <= 0 || cropWidth <= 0) {
            Log.i(TAG, "Crop made bitmap empty");
            return null;
        }
        return Bitmap.createBitmap(bitmap, leftCrop, topCrop, cropWidth, cropHeight);
    }


    public void addSpace() {
        Bitmap blackBitmap = Bitmap.createBitmap(letterWidth, letterHeight, Bitmap.Config.ARGB_8888);
        Canvas blackCanvas = new Canvas(blackBitmap);
        blackCanvas.drawColor(Color.WHITE);
        if (resultBitmap == null) {
            resultBitmap = blackBitmap.copy(Bitmap.Config.ARGB_8888, true);

        } else {
            resultBitmap = combineImages(resultBitmap, blackBitmap);
        }
        logger.log(WatchLogger.SPACE);
    }


    private Bitmap combineImages(Bitmap c, Bitmap s) {
        Bitmap cs;

        int width, height;

        width = c.getWidth() + s.getWidth();
        if (c.getHeight() > s.getHeight()) {

            height = c.getHeight();
        } else {
            height = s.getHeight();
        }

        cs = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

        Canvas comboImage = new Canvas(cs);

        comboImage.drawBitmap(c, 0f, 0f, null);
        comboImage.drawBitmap(s, c.getWidth(), 0f, null);

        return cs;
    }

    public Bitmap shapeBitmap(Bitmap bitmap) {
        int lineWidth = letterWidth * lineLetterLimit;
        double numberOfLetters = bitmap.getWidth() / letterWidth;
        int numberOfLines = (int) Math.ceil(numberOfLetters / lineLetterLimit);
        Bitmap[] lines = new Bitmap[numberOfLines];

        for (int i = 0; i < lines.length; i++) {
            if (lineWidth*i+lineWidth>bitmap.getWidth()) {
                lines[i] = Bitmap.createBitmap(bitmap, lineWidth * i,
                        0, bitmap.getWidth() % lineWidth, letterHeight);
            }else {
                lines[i] = Bitmap.createBitmap(bitmap, lineWidth * i,
                        0, lineWidth, letterHeight);
            }
        }
        Bitmap result = null;
        for (int i = 0; i < lines.length; i++) {

            if (i == 0) {
                result = lines[i];
            } else {
                Bitmap upper = result;
                Bitmap lower = lines[i];

                result = Bitmap.createBitmap(lineWidth, upper.getHeight()+lower.getHeight(),
                        Bitmap.Config.ARGB_8888);

                Canvas comboImage = new Canvas(result);

                comboImage.drawBitmap(upper, 0f, 0f, null);
                comboImage.drawBitmap(lower, 0f, upper.getHeight(), null);
            }
        }
        //scale the Bitmap
        int newWidth = result.getWidth()/2;
        int newHeight = result.getHeight()/2;
        Bitmap scaledBitmap = Bitmap.createScaledBitmap(result, newWidth, newHeight, false);
        return scaledBitmap;
    }


    public void resetResult(){
        resultBitmap=null;
    }



    public Bitmap getResult(){
        if(resultBitmap!=null){
            return shapeBitmap(resultBitmap);
        }else{
            return null;
        }
    }

    public void setLogger(WatchLogger logger) {
        this.logger = logger;
    }


}
