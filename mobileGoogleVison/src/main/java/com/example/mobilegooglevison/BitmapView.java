package com.example.mobilegooglevison;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;

import androidx.appcompat.widget.AppCompatImageView;

import java.util.ArrayList;


public class BitmapView extends AppCompatImageView {

    private ArrayList<Bitmap> fetchedBitmaps;
    private ArrayList<Bitmap> dynamicBitmaps;
    private ArrayList<Bitmap> currentBitmaps;

    private Bitmap currentBitmap;
    private int currentId;

    private int down;
    private int up;

    public BitmapView(Context context, AttributeSet attrs) {
        super(context, attrs);
        currentBitmaps = new ArrayList<>();
        dynamicBitmaps = new ArrayList<>();
        fetchedBitmaps = new ArrayList<>();
        BitmapStorage.getInstance().delegate = this;
        getDynamicBitmaps();
        showDynamic();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        super.onTouchEvent(event);

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                this.down = (int)event.getX();
                return true;

            case MotionEvent.ACTION_UP:
                this.up = (int)event.getX();
                if (down < up) {
                    goLeft();
                }
                if(down > up){
                    goRight();
                }
                return true;
        }
        return false;
    }

    private void goRight(){
        if (currentId+1 < currentBitmaps.size()) {
            currentBitmap = currentBitmaps.get(currentId + 1);
            currentId++;
            updateBitmap();
        }
    }

    private void goLeft(){
        if (currentId > 0) {
            currentBitmap = currentBitmaps.get(currentId - 1);
            currentId--;
            updateBitmap();
        }
    }

    private void updateBitmap(){
        currentBitmap = currentBitmaps.get(currentId);
        this.setImageBitmap(currentBitmap);
    }

    public void setFetchedBitmaps(ArrayList<Bitmap> fetchedBitmaps){
        this.fetchedBitmaps = fetchedBitmaps;
    }

    private void getDynamicBitmaps(){
        dynamicBitmaps = BitmapStorage.getInstance().getDynamicBitmaps();
        Log.i("TAG View",dynamicBitmaps.size()+"");
    }

    public void showFetched(){
        currentBitmaps = fetchedBitmaps;
        currentId = 0;
        updateBitmap();
    }
    public void showDynamic(){
        currentBitmaps = dynamicBitmaps;
        currentId = currentBitmaps.size()-1;
        updateBitmap();
    }
}
