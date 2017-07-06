package com.example.tswords.livewallpaper.Wallpaper;

import android.app.WallpaperInfo;
import android.app.WallpaperManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.media.MediaPlayer;
import android.service.wallpaper.WallpaperService;
import android.view.SurfaceHolder;

import com.example.tswords.livewallpaper.Util.GlobalContext;
import com.example.tswords.livewallpaper.Util.L;
import java.io.IOException;

/**
 * Created by Tswords on 2017/5/16.
 */

public class VideoLiveWallpaper extends WallpaperService {

    public Engine onCreateEngine() {

        return new VideoEngine();
       // return new VideoEngine(filePath,mfileType);
    }
    private static String filePath;
    private static int mfileType;
//    private static String filePath="test12.mp4";
//    private static int mfileType=1;
    public static final String VIDEO_PARAMS_CONTROL_ACTION = "com.example.livewallpaper";
    public static final String KEY_ACTION = "action";
    public static final String FILE_PATH="filePath";
    public static final int ACTION_VOICE_SILENCE = 110;
    public static final int ACTION_VOICE_NORMAL = 111;
    public static final int CHANGE_MEDIA=1;

    public static void voiceSilence(Context context) {
        Intent intent = new Intent(VideoLiveWallpaper.VIDEO_PARAMS_CONTROL_ACTION);
        intent.putExtra(VideoLiveWallpaper.KEY_ACTION, VideoLiveWallpaper.ACTION_VOICE_SILENCE);
        context.sendBroadcast(intent);
    }

    public static void voiceNormal(Context context) {
        Intent intent = new Intent(VideoLiveWallpaper.VIDEO_PARAMS_CONTROL_ACTION);
        intent.putExtra(VideoLiveWallpaper.KEY_ACTION, VideoLiveWallpaper.ACTION_VOICE_NORMAL);
        context.sendBroadcast(intent);
    }

    /**
     *
     * @param context
     * @param fileURI       //assets中的文件名,或本地文件路径
     * @param VideoType     //视频文件类型，1 assets中的示例，2 本地内存中的资源
     */
    public static void setToVideoWallPaper(Context context,String fileURI,int VideoType){    //
        filePath=fileURI;
        mfileType=VideoType;
        SharedPreferences.Editor editor= GlobalContext.getContext().getSharedPreferences("contentShared",Context.MODE_MULTI_PROCESS | Context.MODE_PRIVATE).edit();
        editor.putString("filePath", filePath);
        editor.putInt("mfileType",mfileType);
        editor.apply();
        setToWallPaper(context,VideoLiveWallpaper.class);
    }
    public static void setToWallPaper(Context context,Class target) {
        final Intent intent = new Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER);
        WallpaperInfo wallpaperInfo=DefaultWallPaperManager.getCurrentWallPaper(context);   //简单处理，如果当前是动态壁纸直接清除
        if(wallpaperInfo!=null){
            WallpaperManager wallpaperManager=WallpaperManager.getInstance(context);
            try {
                wallpaperManager.clear();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        intent.putExtra(WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT,
                new ComponentName(context, target));
        context.startActivity(intent);
    }


    class VideoEngine extends Engine {
        private MediaPlayer mMediaPlayer;
        private String VideoPath;
        private int type;

        private BroadcastReceiver mVideoParamsControlReceiver;
        @Override
        public void onCreate(SurfaceHolder surfaceHolder) {
            super.onCreate(surfaceHolder);

            IntentFilter intentFilter = new IntentFilter(VIDEO_PARAMS_CONTROL_ACTION);  //具体接受的action
            registerReceiver(mVideoParamsControlReceiver = new BroadcastReceiver() {    //动态建立一个广播接收器
                @Override
                public void onReceive(Context context, Intent intent) {
                    L.d("onReceive");
                    int action = intent.getIntExtra(KEY_ACTION, -1);
                    switch (action) {
                        case ACTION_VOICE_NORMAL:
                            mMediaPlayer.setVolume(1.0f, 1.0f);
                            break;
                        case ACTION_VOICE_SILENCE:
                            mMediaPlayer.setVolume(0, 0);
                            break;

                    }
                }
            }, intentFilter);


        }

        @Override
        public void onDestroy() {
            unregisterReceiver(mVideoParamsControlReceiver);    //关闭接收器
            super.onDestroy();

        }

        @Override
        public void onVisibilityChanged(boolean visible) {
            L.d("VideoEngine#onVisibilityChanged visible = " + visible);
            if (visible) {
                mMediaPlayer.start();
            } else {
                mMediaPlayer.pause();   //不可见暂停视频流
            }
        }


        @Override
        public void onSurfaceCreated(SurfaceHolder holder) {
            L.d("VideoEngine#onSurfaceCreated ");
            super.onSurfaceCreated(holder);
            SharedPreferences prefs= GlobalContext.getContext().getSharedPreferences("contentShared",Context.MODE_MULTI_PROCESS | Context.MODE_PRIVATE);
            VideoPath = prefs.getString("filePath", "test12.mp4");  //获得路径
            type=prefs.getInt("mfileType",1);

            L.d(VideoPath+"--------------"+type);
//            L.d( prefs.getString("filePath", "test12.mp4")+prefs.getInt("mfileType",1));
            mMediaPlayer = new MediaPlayer();
            mMediaPlayer.setSurface(holder.getSurface());
         // L.d(filePath);
            try {
                switch (type){
                    case 1:
                        AssetManager assetMg = getApplicationContext().getAssets();
                        AssetFileDescriptor fileDescriptor = assetMg.openFd(VideoPath);
                        mMediaPlayer.setDataSource(fileDescriptor.getFileDescriptor(),fileDescriptor.getStartOffset(), fileDescriptor.getLength());
                        break;
                    case 2:
                        mMediaPlayer.setDataSource(VideoPath);
                        break;
                }

                mMediaPlayer.setLooping(true);
                mMediaPlayer.setVolume(0, 0);
                mMediaPlayer.prepare();
                mMediaPlayer.start();

            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        @Override
        public void onSurfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            L.d("VideoEngine#onSurfaceChanged ");
            super.onSurfaceChanged(holder, format, width, height);
        }

        @Override
        public void onSurfaceDestroyed(SurfaceHolder holder) {
            L.d("VideoEngine#onSurfaceDestroyed ");
            super.onSurfaceDestroyed(holder);
            mMediaPlayer.release();
            mMediaPlayer = null;

        }
    }


}