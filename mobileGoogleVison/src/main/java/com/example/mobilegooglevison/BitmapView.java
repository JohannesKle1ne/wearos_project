package com.example.mobilegooglevison;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.view.MotionEvent;

import androidx.appcompat.widget.AppCompatImageView;

import java.util.ArrayList;


public class BitmapView extends AppCompatImageView {


    private int down;
    private int up;

    public BitmapView(Context context, AttributeSet attrs) {
        super(context, attrs);
        BitmapStorage.getInstance().delegate = this;
        updateBitmap();
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
                    BitmapStorage.getInstance().goLeft();
                }
                if(down > up){
                    BitmapStorage.getInstance().goRight();
                }
                return true;
        }
        return false;
    }

    public void updateBitmap(){
        this.setImageBitmap(BitmapStorage.getInstance().getCurrentBitmap());
    }
}
