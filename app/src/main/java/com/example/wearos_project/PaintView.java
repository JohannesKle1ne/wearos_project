package com.example.wearos_project;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;

public class PaintView extends View implements GestureDetector.OnGestureListener,
        GestureDetector.OnDoubleTapListener {

    public static final int DEFAULT_COLOR = Color.BLACK;
    public static final int DEFAULT_BG_COLOR = Color.BLACK;
    private static final float TOUCH_TOLERANCE = 4;
    public static final int DEFAULT_STROKE_WIDTH = 15;


    private float mX, mY;
    private Path currentPath;
    private Paint paint;
    private int backgroundColor = DEFAULT_BG_COLOR;

    private Bitmap bitmap;
    private Bitmap blackBitmap;
    private Canvas mCanvas;
    private Canvas blackCanvas;
    private Paint mBitmapPaint = new Paint(Paint.DITHER_FLAG);

    private ArrayList<Path> paths = new ArrayList<>();
    private ArrayList<Path> undo = new ArrayList<>();

    private MainActivity mainActivity;
    private static final String TAG = "PaintView";
    private GestureDetector gestureDetector;
    private boolean doubleTapped;





    public PaintView(Context context) {

        super(context, null);

    }

    public PaintView(Context context, AttributeSet attrs) {

        super(context, attrs);

        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setDither(true);
        paint.setColor(DEFAULT_COLOR);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStrokeWidth(DEFAULT_STROKE_WIDTH);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setXfermode(null);
        paint.setAlpha(0xff);

        gestureDetector = new GestureDetector(context, this);
        doubleTapped =  false;



    }

    public void initialise (DisplayMetrics displayMetrics, Activity activity) {

        mainActivity = (MainActivity) activity;

        int height = displayMetrics.heightPixels;
        int width = displayMetrics.widthPixels;

        bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        blackBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        mCanvas = new Canvas(bitmap);
        blackCanvas = new Canvas(blackBitmap);
        invalidate();

    }

    private void updateBitmap(){
        mCanvas.drawColor(Color.WHITE);
        int i = 0;
        for (Path path : paths) {
            i++;
            mCanvas.drawPath(path, paint);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {

        canvas.save();
        blackCanvas.drawColor(backgroundColor);
        /*mCanvas.drawColor(Color.WHITE);
        blackCanvas.drawColor(backgroundColor);

        int i = 0;
        for (Path path : paths) {
            i++;
            mCanvas.drawPath(path, paint);
            Log.d(TAG,"draw path "+i);

        }*/

        canvas.drawBitmap(blackBitmap, 0, 0, mBitmapPaint); //change this to black to Hide
        canvas.restore();

    }

    public void touchDown(){
        if(mainActivity.isWaitingForDoubleTap()){
            handleDoubleTap();
        }else{
            mainActivity.startDoubleTapTimer();
        }
        mainActivity.cancelLetterTimer(); //this is the normal letter delay reset
    }

    public void handleDoubleTap() {
        Log.d(TAG,""+paths.size());
        if (paths.size() > 2){
            undo(2);
            mainActivity.abridgeLetterTimer();
        }
        mainActivity.vibrate();
        doubleTapped = true;
        mainActivity.getTextBuilder().addSpace();
    }

    private void pathStart(float x, float y) {

        currentPath = new Path();

        paths.add(currentPath);

        currentPath.reset();
        currentPath.moveTo(x, y);

        mX = x;
        mY = y;

    }

    private void pathMove(float x, float y) {

        float dx = Math.abs(x - mX);
        float dy = Math.abs(y - mY);

        if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
            currentPath.quadTo(mX, mY, (x + mX) / 2, (y + mY) / 2);
            mX = x;
            mY = y;
        }
    }

    private void pathEnd(){
        currentPath.lineTo(mX, mY);
    }

    private void touchUp () {

        if(doubleTapped){
            clear();
            doubleTapped = false;
        }else{
            mainActivity.startLetterTimer();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(mainActivity.isUserSessionRunning()) {

            float x = event.getX();
            float y = event.getY();

            gestureDetector.onTouchEvent(event);

            switch (event.getAction()) {

                case MotionEvent.ACTION_DOWN:
                    pathStart(x, y);
                    touchDown();
                    break;
                case MotionEvent.ACTION_UP:
                    pathEnd();
                    touchUp();
                    break;
                case MotionEvent.ACTION_POINTER_DOWN:
                    break;
                case MotionEvent.ACTION_MOVE:
                    pathMove(x, y);
                    break;

            }
            return true;

        }else{
            Toast.makeText(getContext(), "no user session started", Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    public void clear () {

        backgroundColor = DEFAULT_BG_COLOR;

        paths.clear();
        //invalidate();

    }

    public void undo(int number) {

        if (paths.size() >= number) {

            for(int i = 1; i<=number;i++){
                undo.add(paths.remove(paths.size() - 1));
            }
        } else {

            Toast.makeText(getContext(), "Nothing to undo", Toast.LENGTH_LONG).show();

        }

    }

    public void redo () {

        if (undo.size() > 0) {

            paths.add(undo.remove(undo.size() - 1));
            //invalidate(); // add

        } else {

            Toast.makeText(getContext(), "Nothing to undo", Toast.LENGTH_LONG).show();
        }
    }



    public Bitmap getBitmap() {
        updateBitmap();
        return bitmap;
    }









    //Touch functions

    @Override
    public boolean onSingleTapConfirmed(MotionEvent e) {
        //Log.d(TAG, "onSingleTapConfirmed");

        return false;
    }

    @Override
    public boolean onDoubleTap(MotionEvent e) {
        /*Log.d(TAG, "double tap");
        //mainActivity.sendSpace();
        mainActivity.getTextbuilder().addSpace();
        mainActivity.vibrate();
        doubleTapped = true;
        mainActivity.cancelLetterTimer();*/
        return false;
    }

    @Override
    public boolean onDoubleTapEvent(MotionEvent e) {
        //mainActivity.cancelLetterTimer();
        //Log.d(TAG, "double tap event");
        return false;
    }

    @Override
    public boolean onDown(MotionEvent e) {
        return false;
    }

    @Override
    public void onShowPress(MotionEvent e) {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        //Log.d(TAG, "onSingleTapUp");
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {
        //Log.d(TAG, "onLongPress");

    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        //Log.d(TAG, "onFling");
        return false;
    }
}
