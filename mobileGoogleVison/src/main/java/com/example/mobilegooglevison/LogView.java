package com.example.mobilegooglevison;

import android.content.Context;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatTextView;


public class LogView extends AppCompatTextView {

    public LogView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void addLine(String line){
        this.setText(this.getText()+"\n"+line);
    }
}
