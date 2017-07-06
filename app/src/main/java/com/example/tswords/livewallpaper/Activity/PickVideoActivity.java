package com.example.tswords.livewallpaper.Activity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.example.tswords.livewallpaper.Adaper.Thumbnail;
import com.example.tswords.livewallpaper.Adaper.VideoRecyclerAdapter;
import com.example.tswords.livewallpaper.R;
import com.example.tswords.livewallpaper.Util.GetVideoInfoUtils;
import com.example.tswords.livewallpaper.Util.LoadedImage;
import com.example.tswords.livewallpaper.Video.VideoInfo;

import java.util.ArrayList;
import java.util.List;

public class PickVideoActivity extends AppCompatActivity {
    private static boolean isPermission=false;
    private List<VideoInfo> listVideo;
    private GetVideoInfoUtils getVideoInfoUtils;
    private int videoSize;
    private boolean selectedLeft=true;
    private Button localVideo;
    private Button exampleVideo;
    private VideoRecyclerAdapter mAdapter;
    private RecyclerView recyclerView;
    private List<Thumbnail> exampleVideoBitmap;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(Build.VERSION.SDK_INT>=21){
            View decorView=getWindow().getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN|View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
        exampleVideoBitmap=new ArrayList<>();
        setContentView(R.layout.activity_pick_video);

        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        GridLayoutManager layoutManager = new GridLayoutManager(this, 3);
        recyclerView.setLayoutManager(layoutManager);
        if(selectedLeft){       //初始打开pickVideoActivity时加载示例视频缩略图
            LoadExampleVideoBitmap();
        }
//        List<Thumbnail>  exampleList=new ArrayList<>();   //示例资源
        localVideo = (Button) findViewById(R.id.local_video);    //本地视频按钮
        localVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(selectedLeft){  //当前是从示例切过来的才再开启一个线程
                    selectedLeft=false;
                    setSelectedButton();
                    LoadLocalVideo(recyclerView);    //加载本地视频
                }
            }
        });
        exampleVideo = (Button) findViewById(R.id.example_button);
        exampleVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {               //示例视频
                if(!selectedLeft){
                    selectedLeft=true;
                    setSelectedButton();
                    LoadExampleVideoBitmap();       //加载示例视频的缩略图
                }

            }
        });

        ImageView imageView= (ImageView) findViewById(R.id.finish_activity);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();   //销毁本activity
            }
        });
    }


    /**
     * 为recyclerView添加本地视频的adper
     * @param recyclerView
     */
    private void LoadLocalVideo(RecyclerView recyclerView) {
        getVideoInfoUtils=new GetVideoInfoUtils(this);
        List<Thumbnail>  fruitList=new ArrayList<>();
        checkSelfPermission();  //读取内存卡时的运行时权限，并且返回video列表

        if(listVideo!=null){
            videoSize=listVideo.size(); //视频数量
            for(int i=0;i<listVideo.size();i++){
                if(selectedLeft){   //当突然切换到示例时结束这个函数
                    return;
                }
                VideoInfo videoInfoTmp=listVideo.get(i);    //循环建立recyclerView的数据
                Thumbnail tmp=new Thumbnail(videoInfoTmp.getTitle(),videoInfoTmp.getBitmap(),2,videoInfoTmp.getPath());
                fruitList.add(tmp);
            }
            mAdapter = new VideoRecyclerAdapter(fruitList);
            recyclerView.setAdapter(mAdapter);
        }
        LoadImagesFromSDCard loadImage=new LoadImagesFromSDCard();
        loadImage.execute();
    }

    void checkSelfPermission() {
        if (ContextCompat.checkSelfPermission(PickVideoActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    222);
        }else{
            listVideo=getVideoInfoUtils.getList();
        }
    }


    private void setSelectedButton(){
        if(selectedLeft) {   //左侧按钮选中
            exampleVideo.setBackgroundResource(R.drawable.button_oval_corner);
            localVideo.setBackgroundResource(R.drawable.button_oval_transparent_corner);
        }else{
            localVideo.setBackgroundResource(R.drawable.button_oval_corner);
            exampleVideo.setBackgroundResource(R.drawable.button_oval_transparent_corner);
        }
    }
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 222: {
                listVideo=getVideoInfoUtils.getList();
                return;
            }
        }
    }
    private void addImage(LoadedImage... value) {
        for (LoadedImage image : value) {
            mAdapter.addPhoto(image.getBitmap(),image.getIndex());
            mAdapter.notifyDataSetChanged();
        }
    }

    private void LoadExampleVideoBitmap() {
        new LoadImagesFromAssets().execute();
    }

    /**
     * 加载示例
     */
    class LoadImagesFromAssets extends AsyncTask<Object, Thumbnail, Object>{
        int i=0;
        List<String> list=new ArrayList<>();
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            list.add("test2.mp4");
            list.add("test3.mp4");
            list.add("test12.mp4");
            exampleVideoBitmap.clear();     //防止重复清除数据
            mAdapter = new VideoRecyclerAdapter(exampleVideoBitmap);
            recyclerView.setAdapter(mAdapter);
        }

        @Override
        protected Object doInBackground(Object... params) {
            for(int i=0;i<list.size();i++){

                    Thumbnail tmp=new Thumbnail("test", GetVideoInfoUtils.getExampleVideoBitmap(list.get(i)),1,list.get(i));
                    if(!selectedLeft) {   //当切换到本地视频时，结束这个线程
                        return null;
                    }
                    publishProgress(tmp);
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Thumbnail... values) {
            if(selectedLeft) {   //当不是示例资源时跳过这个循环
                for (Thumbnail image : values) {
                    mAdapter.mFruitList.add(image);
                    mAdapter.notifyDataSetChanged();
                }
            }

        }

        @Override
        protected void onPostExecute(Object result) {

        }
    }
    class LoadImagesFromSDCard extends AsyncTask<Object, LoadedImage, Object> {

        @Override
        protected void onPreExecute() {
//             alert_progress.show();
        }
        @Override
        protected Object doInBackground(Object... params) {
            Bitmap bitmap = null;
            for (int i = 0; i < videoSize; i++) {   //获取litVieo中的视频路径
                    bitmap = GetVideoInfoUtils.getVideoThumbnail(listVideo.get(i).getPath(), 360, 500, MediaStore.Video.Thumbnails.MINI_KIND);
                    if (bitmap != null) {
                        publishProgress(new LoadedImage(bitmap, i));
                    }
                if(selectedLeft) {   //当切换到示例时，结束这个线程
                    return null;
                }
            }
            return null;
        }

        @Override
        public void onProgressUpdate(LoadedImage... value) {
            if(!selectedLeft) {   //当不是本地资源时，不做更新操作
                addImage(value);
            }
        }
        @Override
        protected void onPostExecute(Object result) {
          //  alert_progress.cancel();
        }
    }


}


