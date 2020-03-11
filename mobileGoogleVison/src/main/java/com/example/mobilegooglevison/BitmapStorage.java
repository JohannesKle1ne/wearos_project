package com.example.mobilegooglevison;

import android.graphics.Bitmap;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;

public class BitmapStorage {

    private static BitmapStorage singleInstance;

    public BitmapView delegate;
    private HashMap<Integer,ArrayList> userHashMap;
    private ArrayList<Bitmap> dynamicBitmaps;

    private BitmapStorage(){
        userHashMap = new HashMap<>();
        dynamicBitmaps = new ArrayList<>();
    }

    public static BitmapStorage getInstance(){
        if(singleInstance == null){
            singleInstance = new BitmapStorage();
        }
        return singleInstance;
    }

    public void addBitmap(Bitmap b, int userId, String filename){
        if(userHashMap.containsKey(userId) == false){
            userHashMap.put(userId,new ArrayList<String>());
            userHashMap.get(userId).add(filename);
        }else{
            userHashMap.get(userId).add(filename);
        }
        dynamicBitmaps.add(b);
        if(delegate!=null){
            delegate.showDynamic();
        }
        Log.i("TAG Storage",dynamicBitmaps.size()+"");

    }


    public ArrayList<String> getFileNames(int id){
        return userHashMap.get(id);
    }

    public ArrayList<Bitmap> getDynamicBitmaps(){
        return dynamicBitmaps;
    }

}
