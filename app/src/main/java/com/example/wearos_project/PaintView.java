package com.example.wearos_project;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Environment;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class PaintView extends View {

    public static final int DEFAULT_COLOR = Color.WHITE;
    public static final int DEFAULT_BG_COLOR = Color.BLACK;
    private static final float TOUCH_TOLERANCE = 4;
    public static final int DEFAULT_STROKE_WIDTH = 10;


    private float mX, mY;
    private Path currentPath;
    private Paint paint;
    private int backgroundColor = DEFAULT_BG_COLOR;

    private Bitmap bitmap;
    private Canvas mCanvas;
    private Paint mBitmapPaint = new Paint(Paint.DITHER_FLAG);

    private ArrayList<Path> paths = new ArrayList<>();
    private ArrayList<Path> undo = new ArrayList<>();

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

    }

    public void initialise (DisplayMetrics displayMetrics) {

        int height = displayMetrics.heightPixels;
        int width = displayMetrics.widthPixels;

        bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        mCanvas = new Canvas(bitmap);



    }

    @Override

    protected void onDraw(Canvas canvas) {

        canvas.save();
        mCanvas.drawColor(backgroundColor); // WRONG

        for (Path path : paths) {

            mCanvas.drawPath(path, paint);

        }

        canvas.drawBitmap(bitmap, 0, 0, mBitmapPaint);
        canvas.restore();

    }

    private void touchStart (float x, float y) {

        currentPath = new Path();

        paths.add(currentPath);

        currentPath.reset();
        currentPath.moveTo(x, y);

        mX = x;
        mY = y;

    }

    private void touchMove (float x, float y) {

        float dx = Math.abs(x - mX);
        float dy = Math.abs(y - mY);

        if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {

            currentPath.quadTo(mX, mY, (x + mX) / 2, (y + mY) / 2);

            mX = x;
            mY = y;

        }

    }

    private void touchUp () {

        currentPath.lineTo(mX, mY);

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {

            case MotionEvent.ACTION_DOWN:
                touchStart(x, y);
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                touchUp();
                invalidate();
                break;
            case MotionEvent.ACTION_MOVE:
                touchMove(x, y);
                invalidate();
                break;

        }

        return true;

    }

    public void clear () {

        backgroundColor = DEFAULT_BG_COLOR;

        paths.clear();
        invalidate();

    }

    public void undo () {

        if (paths.size() > 0) {

            undo.add(paths.remove(paths.size() - 1));
            invalidate(); // add

        } else {

            Toast.makeText(getContext(), "Nothing to undo", Toast.LENGTH_LONG).show();

        }

    }

    public void redo () {

        if (undo.size() > 0) {

            paths.add(undo.remove(undo.size() - 1));
            invalidate(); // add

        } else {

            Toast.makeText(getContext(), "Nothing to undo", Toast.LENGTH_LONG).show();

        }

    }

    public void saveImage () {

        int count = 0;

        File sdDirectory = Environment.getExternalStorageDirectory();
        File subDirectory = new File(sdDirectory.toString() + "/Pictures/Paint");

        if (subDirectory.exists()) {

            File[] existing = subDirectory.listFiles();

            for (File file : existing) {

                if (file.getName().endsWith(".jpg") || file.getName().endsWith(".png")) {

                    count++;

                }

            }

        } else {

            subDirectory.mkdir();

        }

        if (subDirectory.exists()) {

            File image = new File(subDirectory, "/drawing_" + (count + 1) + ".png");
            FileOutputStream fileOutputStream;

            try {

                fileOutputStream = new FileOutputStream(image);

                //bitmap.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream);

                fileOutputStream.flush();
                fileOutputStream.close();

                Toast.makeText(getContext(), "saved", Toast.LENGTH_LONG).show();

            } catch (FileNotFoundException e) {


            } catch (IOException e) {


            }

        }

    }

    public Bitmap getBitmap() {
        return bitmap;
    }



}
