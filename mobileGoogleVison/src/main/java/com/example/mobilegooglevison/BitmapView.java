package com.example.mobilegooglevison;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import androidx.appcompat.widget.AppCompatImageView;

import java.util.ArrayList;
import android.widget.LinearLayout;

import java.util.HashMap;


public class BitmapView extends AppCompatImageView {

    private ArrayList<Bitmap> loadedBitmaps;
    private ArrayList<Bitmap> visibleBitmaps;
    private ArrayList<Bitmap> incomingBitmaps;
    public final static int LOADED = 0;
    public final static int INCOMING = 1;


    private final String TAG = "BitmapView";

    private Bitmap visibleBitmap;
    private int visibleId;

    private int down;
    private int up;

    public BitmapView(Context context, AttributeSet attrs) {
        super(context, attrs);

        incomingBitmaps = new ArrayList<>();
        visibleBitmaps = new ArrayList<>();
        loadedBitmaps = new ArrayList<>();
        visibleBitmaps = incomingBitmaps;

        this.setVisibility(View.INVISIBLE);
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
        if (visibleId +1 < visibleBitmaps.size()) {
            visibleId++;
            updateBitmap();
        }
    }

    private void goLeft(){
        if (visibleId > 0) {
            visibleId--;
            updateBitmap();
        }
    }

    private void updateBitmap(){
        if(!visibleBitmaps.isEmpty()) {
            visibleBitmap = visibleBitmaps.get(visibleId);
            this.setImageBitmap(visibleBitmap);
        }
    }

    public void setLoadedBitmaps(ArrayList<Bitmap> fetchedBitmaps){
        this.loadedBitmaps = fetchedBitmaps;
    }

    public void addBitmap(Bitmap b){
        incomingBitmaps.add(b);
        if(visibleBitmaps==incomingBitmaps){
            visibleId = visibleBitmaps.size()-1;
            updateBitmap();
        }
    }

    public void show(int type){
        if((type==LOADED) && !loadedBitmaps.isEmpty()){
            visibleBitmaps = loadedBitmaps;
            visibleId = 0;
            this.setBackgroundColor(Color.parseColor("#FFFFFF"));
            updateBitmap();
            this.setVisibility(View.VISIBLE);
            ((LinearLayout) this.getParent()).setVisibility(VISIBLE);
        }
        if((type==INCOMING)) {
            visibleBitmaps = incomingBitmaps;
            this.setBackgroundColor(Color.parseColor("#FFFFFF"));
            this.setVisibility(View.VISIBLE);
            ((LinearLayout) this.getParent()).setVisibility(VISIBLE);
            if(!incomingBitmaps.isEmpty()){
                visibleId = visibleBitmaps.size() - 1;
                updateBitmap();
            }
        }
    }
}
