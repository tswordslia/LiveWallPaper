package com.example.tswords.livewallpaper.Util;

/**
 * Created by Tswords on 2017/6/3.
 */

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.media.MediaMetadataRetriever;
import android.media.ThumbnailUtils;
import android.provider.MediaStore;

import com.example.tswords.livewallpaper.Video.VideoInfo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 获取视频的各种信息 ，标题、 地址等等
 * @author chao
 *
 */
public class GetVideoInfoUtils {
    private Context mContext;

    public GetVideoInfoUtils(Context context) {
        this.mContext = context;
    }
    public  static Bitmap getVideoThumbnail(String videoPath, int width , int height, int kind){
        Bitmap bitmap = null;
        bitmap = ThumbnailUtils.createVideoThumbnail(videoPath, kind);
        bitmap = ThumbnailUtils.extractThumbnail(bitmap, width, height, ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
        return bitmap;
    }

    /**
     * 获得assets目录下的视频文件的截图
     * 如果出错返回Null
     * @param VideoName
     * @return
     */
    public static Bitmap getExampleVideoBitmap(String VideoName){
        Bitmap bitmap=null;
        AssetManager assetMg = GlobalContext.getContext().getAssets();
        try {
            AssetFileDescriptor fileDescriptor = assetMg.openFd(VideoName);
            MediaMetadataRetriever mediaMetadataRetriever=new MediaMetadataRetriever();
            mediaMetadataRetriever.setDataSource(fileDescriptor.getFileDescriptor(),fileDescriptor.getStartOffset(), fileDescriptor.getLength());
            Bitmap orignBitmap = mediaMetadataRetriever.getFrameAtTime(1,
                    MediaMetadataRetriever.OPTION_CLOSEST_SYNC);
            bitmap = ThumbnailUtils.extractThumbnail(orignBitmap,360,500);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    /**
     * 获取本地视频信息
     * @return
     */
    public List<VideoInfo> getList() {
        List<VideoInfo> list = null;
        if (mContext != null) {
            Cursor cursor = mContext.getContentResolver().query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, null, null,null, null);
            if (cursor != null) {
                list = new ArrayList<VideoInfo>();
                while (cursor.moveToNext()) {
                    int id = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID));
                    String title = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.TITLE));  //视频文件的标题内容
                    String album = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.ALBUM));
                    String artist = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.ARTIST));
                    String displayName = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME));
                    String mimeType = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.MIME_TYPE));
                    String path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA));  //
                    long duration = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION));
                    long size = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE));

                   // Bitmap bitmap = getVideoThumbnail(path, 360, 720, MediaStore.Video.Thumbnails.MINI_KIND);      //获取缩略图
                    VideoInfo videoinfo = new VideoInfo(id, title, album, artist, displayName, mimeType, path, size, duration,null);
                    list.add(videoinfo);
                }
                cursor.close();
            }
        }
        return list;
    }

}