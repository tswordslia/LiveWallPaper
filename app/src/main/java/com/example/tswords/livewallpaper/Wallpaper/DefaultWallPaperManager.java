package com.example.tswords.livewallpaper.Wallpaper;

import android.app.WallpaperInfo;
import android.app.WallpaperManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.widget.Toast;

import java.io.IOException;

/**
 * Created by Tswords on 2017/6/2.
 */

public class DefaultWallPaperManager {
    private static Bitmap bitmap=null;
    private static WallpaperInfo wallPaperInfo;

    /**
     * 在修改壁纸后调用，可以设置成修改壁纸前的样子，如果没有修改直接调用，将清除壁纸，返回系统默认壁纸
     * @param context
     */
    public static void setBeforeWallpaper(Context context){
        final Intent intent = new Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER);
        WallpaperManager wallPaperManager=WallpaperManager.getInstance(context);
        if(wallPaperInfo!=null){        //原来是动态壁纸的
            intent.putExtra(WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT,
                    wallPaperInfo.getComponent());
            context.startActivity(intent);
        }else{
            try {
                if(bitmap==null) {
                    wallPaperManager.clear();   //如果获取位图出错为空的话则直接清除，返回系统默认壁纸
                }else {
                    wallPaperManager.setBitmap(bitmap);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        wallPaperInfo=null;
        bitmap=null;    //清空数据
    }
    /**
     * 获取当前的壁纸
     * @param context
     * @return 返回WallpaperInfo 为null时，是静态壁纸
     */
    public static WallpaperInfo getCurrentWallPaper(Context context){

        WallpaperManager wallPaperManager=WallpaperManager.getInstance(context);
        //如何获得当前的壁纸，并且最后设置回去
        wallPaperInfo = wallPaperManager.getWallpaperInfo();
        if( wallPaperInfo==null){       //静态壁纸则获取静态壁纸位图并保存
            Drawable a=wallPaperManager.getDrawable();
            bitmap  =((BitmapDrawable)a).getBitmap();
            //Toast.makeText(context,"dynamic ",Toast.LENGTH_SHORT).show();
        }
        return wallPaperInfo;
    }
}
