package com.example.mobilegooglevison;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.view.MotionEvent;

import androidx.appcompat.widget.AppCompatImageView;

import java.util.ArrayList;


public class BitmapView extends AppCompatImageView {

    private ArrayList<Bitmap> bitmapList;
    private Bitmap currentBitmap;
    private int currentId;

    private int down;
    private int up;

    public BitmapView(Context context, AttributeSet attrs) {
        super(context, attrs);
        bitmapList = new ArrayList<>();
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
                    if (currentId > 0) {
                        currentBitmap = bitmapList.get(currentId - 1);
                        currentId--;
                    }
                }
                if(down > up){
                    if (currentId+1 < bitmapList.size()) {
                        currentBitmap = bitmapList.get(currentId + 1);
                        currentId++;
                    }
                }
                updateBitmap();
                return true;
        }
        return false;
    }

    private void updateBitmap(){
        this.setImageBitmap(currentBitmap);
    }

    public void addBitmap(Bitmap b){
        bitmapList.add(b);
        currentBitmap = b;
        currentId = bitmapList.size()-1;
        updateBitmap();
    }
}
