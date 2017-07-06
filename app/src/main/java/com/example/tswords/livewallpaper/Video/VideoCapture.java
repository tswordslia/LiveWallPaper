package com.example.tswords.livewallpaper.Video;

import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.util.Log;
import android.view.Surface;

import com.example.tswords.livewallpaper.Util.OutPutFilePath;

import java.io.File;
import java.io.IOException;

import static android.content.ContentValues.TAG;

/**
 * Created by Tswords on 2017/5/31.
 */

public class VideoCapture {
    public static final int MEDIA_TYPE_IMAGE = 1;
    public static final int MEDIA_TYPE_VIDEO = 2;
    public static MediaRecorder mediaRecorder;
    public static Camera mCamera;
    private static boolean isPreparing=false;
    public static boolean PreparingVideoRecorder(Camera camera, Surface surface){
        mCamera=camera;
        mediaRecorder=new MediaRecorder();
        mCamera.unlock();
        mediaRecorder.setCamera(mCamera);
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
        mediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
        mediaRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_720P));
        mediaRecorder.setOutputFile(getOutputMediaFile(MEDIA_TYPE_VIDEO).toString());
        mediaRecorder.setPreviewDisplay(surface);   //设置预览界面
        mediaRecorder.setOrientationHint(90);   //视频旋转90度
        try {
            mediaRecorder.prepare();
        } catch (IllegalStateException e) {
            Log.d(TAG, "IllegalStateException preparing MediaRecorder: " + e.getMessage());
            releaseMediaRecorder();
            return false;
        } catch (IOException e) {
            Log.d(TAG, "IOException preparing MediaRecorder: " + e.getMessage());
            releaseMediaRecorder();
            return false;
        }
        Log.d("=================视频捕捉","videoRecorder准备完毕");
        isPreparing=true;   //准备完毕
        return true;
    }
    public static void startRecording(){
        if(isPreparing){
            mediaRecorder.start();
        }
    }
    public static void releaseMediaRecorder(){  //释放资源
        if (mediaRecorder != null) {
            mediaRecorder.reset();   // clear recorder configuration
            mediaRecorder.release(); // release the recorder object
            mediaRecorder = null;
            mCamera.lock();           // lock camera for later use
        }
    }
    /** Create a File for saving an image or video */
    private static File getOutputMediaFile(int type){
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.

        File mediaStorageDir = OutPutFilePath.getOutPutPublicMediaDirectory();
        File mediaFile;
        mediaFile=OutPutFilePath.createMediaFileName(mediaStorageDir,type);
        return mediaFile;
    }
    public static void stopRecording(){
        if(mediaRecorder!=null){
            mediaRecorder.stop();   //停止录制，释放资源
            mediaRecorder.release();
            mediaRecorder=null;
            mCamera.lock();
        }
    }
}
