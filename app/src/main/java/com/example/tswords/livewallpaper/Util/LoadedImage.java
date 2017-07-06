package com.example.tswords.livewallpaper.Util;

import android.graphics.Bitmap;

public class LoadedImage {
    Bitmap mBitmap;
    int index;
    public LoadedImage(Bitmap bitmap,int index) {
        mBitmap = bitmap;
        this.index=index;
    }

    public int getIndex(){
        return this.index;
    }
    public Bitmap getBitmap() {
        return mBitmap;
    }
}