package com.example.mobilegooglevison;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import androidx.appcompat.widget.AppCompatImageView;

import java.util.ArrayList;
import java.util.HashMap;


public class BitmapView extends AppCompatImageView {

    private ArrayList<Bitmap> fetchedBitmaps;
    private ArrayList<Bitmap> visibleBitmaps;
    private ArrayList<Bitmap> currentBitmaps;
    private HashMap<Integer,ArrayList> userHashMap;

    private Bitmap currentBitmap;
    private int currentId;

    private int down;
    private int up;

    public BitmapView(Context context, AttributeSet attrs) {
        super(context, attrs);
        userHashMap = new HashMap<>();
        currentBitmaps = new ArrayList<>();
        visibleBitmaps = new ArrayList<>();
        fetchedBitmaps = new ArrayList<>();
        this.setVisibility(View.INVISIBLE);
        /*BitmapStorage.getInstance().delegate = this;
        getDynamicBitmaps();
        showDynamic();*/
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
            currentId++;
            updateBitmap();
        }
    }

    private void goLeft(){
        if (currentId > 0) {
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

    public void addBitmap(Bitmap b, int userId, String filename){
        if(userHashMap.containsKey(userId) == false){
            userHashMap.put(userId,new ArrayList<String>());
            userHashMap.get(userId).add(filename);
        }else{
            userHashMap.get(userId).add(filename);
        }
        currentBitmaps.add(b);
        currentId = currentBitmaps.size()-1;
        updateBitmap();
    }


    public ArrayList<String> getFileNames(String id){
        return userHashMap.get(id);
    }



    public void showFetched(){
        currentBitmaps = fetchedBitmaps;
        currentId = 0;
        updateBitmap();
    }
    public void showDynamic(){
        this.setVisibility(View.VISIBLE);
    }
}
