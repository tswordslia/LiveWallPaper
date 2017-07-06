package com.example.tswords.livewallpaper.Adaper;

import android.graphics.Bitmap;

public class Thumbnail {

    private String name;

    public int getType() {
        return Type;
    }

    public String getFilePath() {
        return filePath;
    }

    private int Type;
    private String filePath;
    public Bitmap getBitmap() {
        return bitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    private Bitmap bitmap=null;

    /**
     * 缩略图
     * @param name
     * @param bitmap
     * @param VideoType 1位accets下的视频 2 手机内存中的本地视频
     * @param path  assets下文件传入文件名即可，手机本地视频传入路径
     */
    public Thumbnail(String name, Bitmap bitmap,int VideoType,String path) {
        this.name = name;
        this.bitmap=bitmap;
        this.filePath=path;
        this.Type=VideoType;
    }

    public String getName() {
        return name;
    }


}
