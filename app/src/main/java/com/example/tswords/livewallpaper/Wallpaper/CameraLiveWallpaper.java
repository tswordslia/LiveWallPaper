package com.example.tswords.livewallpaper.Wallpaper;

import android.app.WallpaperManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.os.Handler;
import android.os.HandlerThread;
import android.service.wallpaper.WallpaperService;
import android.support.v4.view.MotionEventCompat;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.WindowManager;
import android.widget.Toast;

import com.example.tswords.livewallpaper.Util.GlobalContext;
import com.example.tswords.livewallpaper.Util.OutPutFilePath;
import com.example.tswords.livewallpaper.Video.VideoCapture;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import static com.example.tswords.livewallpaper.Util.GlobalContext.getContext;

public class CameraLiveWallpaper extends WallpaperService {

    public static void setToTransparentWallPaper(Context context) {
        DefaultWallPaperManager.getCurrentWallPaper(context);
        setToTransparentWallPaper(context, CameraLiveWallpaper.class);
    }

    public static void setToTransparentWallPaper(Context context, Class target) {
        final Intent intent = new Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER);
        intent.putExtra(WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT,
                new ComponentName(context, target));
        context.startActivity(intent);
    }

    @Override
    public Engine onCreateEngine() {
        return new CameraEngine();
    }

    class CameraEngine extends Engine implements GestureDetector.OnGestureListener, GestureDetector.OnDoubleTapListener {
        private final static String TAG="LiveWallPaper";
        private WindowManager wm= (WindowManager) getSystemService(WINDOW_SERVICE);
        private GestureDetector mGestureDetector;
        private File mFile;
        private Camera mCamera;
        private HandlerThread mBackgroundThread;
        private Handler mBackgroundHandler;
        private SurfaceHolder msurfaceHolder=getSurfaceHolder();
        private Camera.PictureCallback mPicture = new Camera.PictureCallback() {

            @Override
            public void onPictureTaken(byte[] data, Camera camera) {
                mFile= OutPutFilePath.createMediaFileName(OutPutFilePath.getOutPutPublicMediaDirectory(),1);
                File pictureFile = mFile;
                if (pictureFile == null){
                    Log.d(TAG, "Error creating media file, check storage permissions: " );
                    return;
                }
                try {
                    FileOutputStream fos = new FileOutputStream(pictureFile);
                    fos.write(data);       //写入数据
                    fos.close();
                    Toast.makeText(GlobalContext.getMainActivity(),"saved"+mFile.getAbsolutePath(),Toast.LENGTH_SHORT).show();
                } catch (FileNotFoundException e) {
                    Log.d(TAG, "File not found: " + e.getMessage());
                } catch (IOException e) {
                    Log.d(TAG, "Error accessing file: " + e.getMessage());
                }
            }
        };
        private Camera.Size mBestPictureSize;

        public int getPointerNumber() {
            return pointerNumber;
        }

        public void setPointerNumber(int pointerNumber) {
            this.pointerNumber = pointerNumber;
        }


        private int pointerNumber = 0;  //手指数量
        private Thread thread = null;
        @Override
        public void onCreate(SurfaceHolder surfaceHolder) {
            super.onCreate(surfaceHolder);
           // mFile = new File(GlobalContext.getContext().getExternalFilesDir(null), "pic.jpg");   //打开一个File对象
//            Log.d("LiveWallPaper",mFile.getAbsolutePath()); //调试信息
            mGestureDetector = new GestureDetector(getApplicationContext(), this);  //手势
            setTouchEventsEnabled(true);
        }

        @Override
        public void onSurfaceCreated(SurfaceHolder holder) {
            super.onSurfaceCreated(holder);

        }

        @Override
        public void onTouchEvent(MotionEvent event) {
            super.onTouchEvent(event);
            // 时间处理:双击拍照，两指长按
            int action = MotionEventCompat.getActionMasked(event);
            switch (action) {
                case MotionEvent.ACTION_POINTER_DOWN:
                    //有其他手指按下的时候，触发这个动作，记录手指数量，如果大于等于2，就开启一个计时器
                    Log.d("touchEvent", "另外有手指按下");
                    if (event.getPointerCount() < pointerNumber || pointerNumber < 2) {  //减少对pointerNumber的写入
                        pointerNumber = event.getPointerCount();
                    }
                    Log.d("touchEvent", "当前手指数量" + pointerNumber);
                    if (pointerNumber >= 2 && thread == null) { //还没有启动线程，并且有两根以上手指按下了
                        //开启计时
                        thread = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                long c1, c2;
                                c1 = System.currentTimeMillis() + 1000;//一秒

                                while (pointerNumber >= 2) { //小于2时，结束计时
                                    c2 = System.currentTimeMillis();
                                    if (c2 > c1) {  //一秒计时结束
                                        //开启录制的子线程
                                        Log.d("拍摄视频", "-------------------------------------------");
                                            //更新ui需要一个handler
                                        mBackgroundHandler.post(new Runnable() {
                                            @Override
                                            public void run() {
                                                Toast.makeText(getApplicationContext(),"拍摄视频",Toast.LENGTH_SHORT).show();
                                                VideoCapture.PreparingVideoRecorder(mCamera,msurfaceHolder.getSurface());
                                                VideoCapture.startRecording();

                                            }
                                        });

                                        break;
                                    }
                                }
                                if(pointerNumber<2){
                                    Log.d("touchEvent", "未达到长按时间");
                                }
                                thread = null;    //退出线程的时候删除引用
                            }
                        });
                        thread.start();
                    }
                    mGestureDetector.onTouchEvent(event);
                    break;
                case MotionEvent.ACTION_POINTER_UP:
                    mBackgroundHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(),"拍摄结束",Toast.LENGTH_SHORT).show();
                            VideoCapture.stopRecording();

                        }
                    });
                    Log.d("touchEvent", "有手指放开");
                    pointerNumber--;  //减少手指数量
                    Log.d("touchEvent", "当前手指数量" + pointerNumber);
                    mGestureDetector.onTouchEvent(event);
                    break;
                default:
                    mGestureDetector.onTouchEvent(event);
                    break;
            }
        }

        @Override
        public void onDestroy() {
            super.onDestroy();
//            stopPreview();
        }
        private void startBackgroundThread() {
            mBackgroundThread = new HandlerThread("CameraBackground");
            mBackgroundThread.start();
            mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
        }
        private void stopBackgroundThread() {
            if(mBackgroundThread!=null) {
                mBackgroundThread.quitSafely();
                try {
                    mBackgroundThread.join();
                    mBackgroundThread = null;
                    mBackgroundHandler = null;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        @Override
        public void onVisibilityChanged(boolean visible) {
            if (visible) {
                SurfaceHolder surfaceHolder = getSurfaceHolder();
                startBackgroundThread();
             //   openCamera(surfaceHolder.getSurfaceFrame().width(), surfaceHolder.getSurfaceFrame().height());
                startPreview(); //进入预览模式
            } else {
              //  closeCamera();
                stopBackgroundThread();
                stopPreview();

            }
        }

        /**
         * 开始预览
         */
        public void startPreview() {
            mCamera = Camera.open();
            mCamera.setDisplayOrientation(90);

            try {
                mCamera.setPreviewDisplay(getSurfaceHolder());
            } catch (IOException e) {
                e.printStackTrace();
            }

            Camera.Parameters param= mCamera.getParameters();
            //param.setPictureSize(1080,1920);
            WindowManager windowManager = (WindowManager) getContext().getSystemService(WINDOW_SERVICE);
             float ratio = (float) windowManager.getDefaultDisplay().getWidth() / windowManager.getDefaultDisplay().getHeight();
            List<Camera.Size> pictureSizes = param.getSupportedPictureSizes();
            if (mBestPictureSize == null) {
                mBestPictureSize =findBestPictureSize(pictureSizes, param.getPictureSize(), ratio);
            }
            //param.setPictureSize(1920,1080);
            param.setPictureSize(mBestPictureSize.width, mBestPictureSize.height);
            param.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
            mCamera.setParameters(param);
            mCamera.startPreview();
//            mCamera.cancelAutoFocus();

        }
        /**
         * 找到短边比长边大于于所接受的最小比例的最大尺寸
         *
         * @param sizes       支持的尺寸列表
         * @param defaultSize 默认大小
         * @param minRatio    相机图片短边比长边所接受的最小比例
         * @return 返回计算之后的尺寸
         */
        private Camera.Size findBestPictureSize(List<Camera.Size> sizes, Camera.Size defaultSize, float minRatio) {
            final int MIN_PIXELS = 320 * 480;

        //    sortSizes(sizes);

            Iterator<Camera.Size> it = sizes.iterator();
            while (it.hasNext()) {
                Camera.Size size = it.next();
                //移除不满足比例的尺寸
                if ((float) size.height / size.width <= minRatio) {
                    it.remove();
                    continue;
                }
                //移除太小的尺寸
                if (size.width * size.height < MIN_PIXELS) {
                    it.remove();
                }
            }

            // 返回符合条件中最大尺寸的一个
            if (!sizes.isEmpty()) {
                return sizes.get(0);
            }
            return defaultSize;
        }
        /**
         * 停止预览
         */
        public void stopPreview() {
            if (mCamera != null) {
                try {
                    mCamera.stopPreview();
                    mCamera.setPreviewCallback(null);
                    // mCamera.lock();
                    mCamera.release();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                mCamera = null;
            }
        }

        @Override
        public boolean onDown(MotionEvent e) {
            return false;
        }

        @Override
        public void onShowPress(MotionEvent e) {

        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {

            return false;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            return false;
        }

        @Override
        public void onLongPress(MotionEvent e) {
            //长按
            Toast.makeText(getContext(), "长按", Toast.LENGTH_SHORT).show();

        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            return false;
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            return false;
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            final Camera.Parameters cameraParams = mCamera.getParameters();
            cameraParams.setPictureFormat(ImageFormat.JPEG);
            cameraParams.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
            cameraParams.setRotation(90);
            mCamera.setParameters(cameraParams);
            mCamera.takePicture(null, null, mPicture);

            Toast.makeText(getContext(), "双击", Toast.LENGTH_SHORT).show();
            return true;
        }
        @Override
        public boolean onDoubleTapEvent(MotionEvent e) {
            return false;
        }
    }
}  