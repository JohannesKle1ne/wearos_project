package com.example.wearos_project;

import android.util.Log;

public class PickRec {

    private int[] first;
    private int[] second;



    public void setFirst(float x, float y) {
        this.first = new int[]{(int)x,(int)y};
        //Log.d("First",first[0]+" | "+first[1]);
    }


    public void setSecond(float x, float y) {
        this.second = new int[]{(int)x,(int)y};
    }

    public int getRec(){
        int deltaX = first[0]-second[0];
        int deltaY = first[1]-second[1];
        int deltaXAbs = Math.abs(deltaX);
        int deltaYAbs = Math.abs(deltaY);
        if(deltaXAbs<deltaYAbs){
            if(deltaY<0){
                return 11;
            }else{
                return 9;
            }
        }
        if(deltaXAbs>deltaYAbs){
            if(deltaX<0){
                return 10;
            }else{
                return 12;
            }
        }
        return -1;
    }
}
