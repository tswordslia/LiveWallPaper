package com.example.tswords.livewallpaper.Util;

import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Tswords on 2017/6/2.
 */
//getExternalStoragePublicDirectory
public class OutPutFilePath {
    public static final int MEDIA_TYPE_IMAGE = 1;
    public static final int MEDIA_TYPE_VIDEO = 2;
    /**
     * 在公共照片文件夹下创建以本程序名为名字的文件夹，并返回此路径
     * @return null 表示失败
     */
    public static File getOutPutPublicMediaDirectory(){
        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "LiveWallPaper");
        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
                Log.d("LiveWallPaper", "failed to create directory");
                return null;
            }
        }
        return mediaStorageDir;
    }

    /**
     * 建立一个file对象，根据不同的类型生成不同格式的文件名
     * @param storageDir    文件保存的路径
     * @param type  类型 MEDIA_TYPE_IMAGE = 1; MEDIA_TYPE_VIDEO = 2;
     * @return  返回文件名
     */
    public static File createMediaFileName(File storageDir,int type){
        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE){
            mediaFile = new File(storageDir.getPath() + File.separator +
                    "IMG_"+ timeStamp + ".jpg");
        } else if(type == MEDIA_TYPE_VIDEO) {
            mediaFile = new File(storageDir.getPath() + File.separator +
                    "VID_"+ timeStamp + ".mp4");
        } else {
            return null;
        }
        return mediaFile;
    }

}
