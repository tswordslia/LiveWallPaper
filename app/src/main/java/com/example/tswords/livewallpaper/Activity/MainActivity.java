package com.example.tswords.livewallpaper.Activity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.media.ThumbnailUtils;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.tswords.livewallpaper.Adaper.Thumbnail;
import com.example.tswords.livewallpaper.Adaper.ThumbnailAdapter;
import com.example.tswords.livewallpaper.Adaper.VideoRecyclerAdapter;
import com.example.tswords.livewallpaper.R;
import com.example.tswords.livewallpaper.Util.FastBlurUtil;
import com.example.tswords.livewallpaper.Util.GetVideoInfoUtils;
import com.example.tswords.livewallpaper.Util.GlobalContext;
import com.example.tswords.livewallpaper.Util.LoadedImage;
import com.example.tswords.livewallpaper.Util.OutPutFilePath;
import com.example.tswords.livewallpaper.Wallpaper.CameraLiveWallpaper;
import com.example.tswords.livewallpaper.Wallpaper.DefaultWallPaperManager;
import com.example.tswords.livewallpaper.Wallpaper.VideoLiveWallpaper;


import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;


public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener  {
    static final String PERMISSION_CAMERA = Manifest.permission.CAMERA;
    static final String PERMISSION_AUDIO= Manifest.permission.RECORD_AUDIO;
    private static final int PERMISSIONS_REQUEST_CAMERA = 454;
    private static final int PERMISSIONS_REQUEST_AUDIO=455;
    private boolean isScience=false;        //默认静音
    private Context mContext;
    private NavigationView navigationView;
    private DrawerLayout drawerLayout;
    private Bitmap overlay;
    private Bitmap bmp;
    private RecyclerView recyclerView;
    private List<Thumbnail> thumbnailList;  //recyclerView的数据列表
    private ThumbnailAdapter thumbnailAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(Build.VERSION.SDK_INT>=21){
            View decorView=getWindow().getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN|View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
        setContentView(R.layout.activity_main);
        //获取公共picture下拍摄的照片和视频，分类获取缩略图展示在recyclerView上
        recyclerView= (RecyclerView) findViewById(R.id.recycler_view);
        GridLayoutManager layoutManager = new GridLayoutManager(this, 3);
        recyclerView.setLayoutManager(layoutManager);
        thumbnailList=new ArrayList<>();
        GetVideoFileName(thumbnailList);    //获得文件夹下的所有文件信息
        thumbnailAdapter=new ThumbnailAdapter(thumbnailList);   //将含有文件信息的list赋值给adapter
        recyclerView.setAdapter(thumbnailAdapter);

        LoadImagesFromPictureDirect ayscTask=new LoadImagesFromPictureDirect();
        ayscTask.execute(); //异步加载信息
        GlobalContext.setMainActivity(this);
      //  bingPicImg= (ImageView) findViewById(R.id.bing_pic_img);    //主界面背景图
        ImageView imageVoice = (ImageView) findViewById(R.id.imageVoice);
        imageVoice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!isScience) {  //如果当前是静音的

                    ((ImageView)v).setImageResource(R.drawable.ic_volume_up_black_24dp);
                    VideoLiveWallpaper.voiceNormal(getApplicationContext());    //打开音量
                    isScience=true;
                }else{
                    ((ImageView)v).setImageResource(R.drawable.ic_volume_off_black_24dp);
                    VideoLiveWallpaper.voiceSilence(getApplicationContext());    //打开音量
                    isScience=false;
                }
            }
        });
        mContext = this;

        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            public boolean onPreDraw() {
                if (bmp == null) {
                    bmp = getRes("timg");
                    //bmp = drawerLayout.getDrawingCache(); //根据下层的主activity的darwable来打磨毛玻璃效果
                }
                blur(bmp, navigationView);//只传x坐标
                return true;
            }

        });
       // blur(bmp,backgroundImageView);
        ImageView imageView= (ImageView) findViewById(R.id.imageMenu);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });
    }


    private Bitmap blur(Bitmap bkg, View view)
    {

        float scaleFactor = 4;//缩放图片，缩放之后模糊效果更好
        int radius = 20;
        if(overlay==null){
            overlay = Bitmap.createBitmap((int) (view.getMeasuredWidth() / scaleFactor),
                    (int) (view.getMeasuredHeight() / scaleFactor), Bitmap.Config.ARGB_8888);

        }
        Canvas canvas = new Canvas(overlay);
        canvas.translate(-view.getLeft() / scaleFactor, 0);
        canvas.scale(1 / scaleFactor, 1 / scaleFactor);
        Paint paint = new Paint();
        paint.setFlags(Paint.FILTER_BITMAP_FLAG);
        canvas.drawBitmap(bkg, 0, 0, paint);
        overlay = FastBlurUtil.Blur(overlay, radius, true);//进行高斯模糊操作
        view.setBackground(new BitmapDrawable(getResources(), overlay));
        return overlay;
    }

    /**
     * 获取存在于drawable文件夹图片的bitmap
     * @param name  文件名无需后缀
     * @return
     */
    public Bitmap getRes(String name) {
        ApplicationInfo appInfo = getApplicationInfo();
        int resID = getResources().getIdentifier(name, "drawable", appInfo.packageName);
        return BitmapFactory.decodeResource(getResources(), resID);
    }
    /**
     * 检查权限
     */
    void checkSelfPermission() {
        if (ContextCompat.checkSelfPermission(mContext, PERMISSION_CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{PERMISSION_CAMERA},
                    PERMISSIONS_REQUEST_CAMERA);
           // checkRedioPermission();
        }else{
            if(ContextCompat.checkSelfPermission(mContext,PERMISSION_AUDIO)!=PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(this,new String[]{PERMISSION_AUDIO},PERMISSIONS_REQUEST_AUDIO);
            }else {
                CameraLiveWallpaper.setToTransparentWallPaper(MainActivity.this);
            }
        }
    }
    void checkRedioPermission(){
        if(ContextCompat.checkSelfPermission(mContext,PERMISSION_AUDIO)!=PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,new String[]{PERMISSION_AUDIO},PERMISSIONS_REQUEST_AUDIO);
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PERMISSIONS_REQUEST_AUDIO: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    CameraLiveWallpaper.setToTransparentWallPaper(MainActivity.this);
                } else {
                    Toast.makeText(mContext, "Please open Recording redio permissions", Toast.LENGTH_SHORT).show();
                }
                return;
            }
            case PERMISSIONS_REQUEST_CAMERA: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                    setTransparentWallpaper();
                    //checkRedioPermission();
                    /// / CameraLiveWallpaper.setToTransparentWallPaper(MainActivity.this);
                    checkRedioPermission();
                } else {
                    Toast.makeText(mContext, "Please open permissions", Toast.LENGTH_SHORT).show();
                }
                return;
            }
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.transparentWallpaper) {   //透明壁纸
            checkSelfPermission();  //检查权限后会调用intent启动摄像头设置壁纸
        } else if (id == R.id.videoWallpaper) { //动态视频壁纸
            Intent intent=new Intent(MainActivity.this,PickVideoActivity.class);    //启动选择视频的activity
            startActivity(intent);
          //  VideoLiveWallpaper.setToVideoWallPaper(this,"test12.mp4",1);
        } else if (id == R.id.beforeWallpaper) {    //恢复壁纸
            DefaultWallPaperManager.setBeforeWallpaper(MainActivity.this);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
    /**
     * 获取应用目录下的照片和视频文件的绝对路径
     *
     * @return
     */
    public static void GetVideoFileName(List<Thumbnail> list) {
        File[] subFile=null;
        File file = OutPutFilePath.getOutPutPublicMediaDirectory(); //获得本应用存放照片视频的位置
        if(file!=null){
            subFile = file.listFiles(); //获取列表
        }

        for (int iFileLength = 0; iFileLength < subFile.length; iFileLength++) {
            // 判断是否为文件夹
            if (!subFile[iFileLength].isDirectory()) {
                String filename = subFile[iFileLength].getName();
                // 判断是否为MP4结尾
                if (filename.trim().toLowerCase().endsWith(".mp4")||filename.trim().toLowerCase().endsWith(".jpg")) {
                    Thumbnail tmp=new Thumbnail(filename,null,2,subFile[iFileLength].getPath());    //暂时不传入缩略图，缩略图异步加载
                    list.add(tmp);
                }
            }
        }
    }

    class LoadImagesFromPictureDirect extends AsyncTask<Object, LoadedImage, Object> {
        Bitmap bitmap = null;
        String tmpFilePath;
        @Override
        protected Object doInBackground(Object... params) {
            for (int i = 0; i < thumbnailList.size(); i++) {   //获取缩略图
                tmpFilePath=thumbnailList.get(i).getFilePath();
                if (tmpFilePath.trim().toLowerCase().endsWith(".mp4")) {
                    bitmap=GetVideoInfoUtils.getVideoThumbnail(tmpFilePath,360,500, MediaStore.Video.Thumbnails.MINI_KIND);
                }else if(tmpFilePath.trim().toLowerCase().endsWith(".jpg")){
                    Bitmap tmpBitmap=BitmapFactory.decodeFile(tmpFilePath);
                    bitmap = ThumbnailUtils.extractThumbnail(tmpBitmap,360,500);
                }
                //bitmap = GetVideoInfoUtils.getVideoThumbnail(listVideo.get(i).getPath(), 360, 500, MediaStore.Video.Thumbnails.MINI_KIND);
                if (bitmap != null) {
                    publishProgress(new LoadedImage(bitmap,i));
                }
            }
            return null;
        }
        @Override
        public void onProgressUpdate(LoadedImage... value) {
            addImage(value);
        }
        @Override
        protected void onPostExecute(Object result) {

//            mAdapter = new VideoRecyclerAdapter(exampleVideoBitmap);
//            recyclerView.setAdapter(mAdapter);
        }
    }
    private void addImage(LoadedImage... value) {
        for (LoadedImage image : value) {
            thumbnailAdapter.addPhoto(image.getBitmap(),image.getIndex());
            thumbnailAdapter.notifyDataSetChanged();
        }
    }
}
