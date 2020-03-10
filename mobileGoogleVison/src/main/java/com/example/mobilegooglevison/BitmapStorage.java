package com.example.mobilegooglevison;

import android.graphics.Bitmap;

import java.util.ArrayList;

public class BitmapStorage {

    private static BitmapStorage singleInstance;

    private ArrayList<Bitmap> bitmapList;
    private Bitmap currentBitmap;
    private int currentId;
    public BitmapView delegate;

    private BitmapStorage(){
        bitmapList = new ArrayList<>();
    }

    public static BitmapStorage getInstance(){
        if(singleInstance == null){
            singleInstance = new BitmapStorage();
        }
        return singleInstance;
    }

    public void addBitmap(Bitmap b){
        bitmapList.add(b);
        currentBitmap = b;
        currentId = bitmapList.size()-1;
        updateDelegate();
    }

    public void goRight(){
        if (currentId+1 < bitmapList.size()) {
            currentBitmap = bitmapList.get(currentId + 1);
            currentId++;
            updateDelegate();
        }
    }

    public void goLeft(){
        if (currentId > 0) {
            currentBitmap = bitmapList.get(currentId - 1);
            currentId--;
            updateDelegate();
        }
    }
    public Bitmap getCurrentBitmap(){
        return currentBitmap;
    }

    private void updateDelegate(){
        if(delegate!=null){
            delegate.updateBitmap();
        }
    }
}
