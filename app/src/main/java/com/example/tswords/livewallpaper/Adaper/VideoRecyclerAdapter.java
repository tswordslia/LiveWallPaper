package com.example.tswords.livewallpaper.Adaper;

import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.tswords.livewallpaper.R;
import com.example.tswords.livewallpaper.Util.GlobalContext;
import com.example.tswords.livewallpaper.Wallpaper.VideoLiveWallpaper;

import java.util.List;

public class VideoRecyclerAdapter extends RecyclerView.Adapter<VideoRecyclerAdapter.ViewHolder>{

    public List<Thumbnail> mFruitList;

    static class ViewHolder extends RecyclerView.ViewHolder {
        View fruitView;
        ImageView fruitImage;

        public ViewHolder(View view) {
            super(view);
            fruitView = view;
            fruitImage = (ImageView) view.findViewById(R.id.fruit_image);
        }
    }

    public VideoRecyclerAdapter(List<Thumbnail> fruitList) {
        mFruitList = fruitList;
    }

    public void addPhoto(Bitmap image, int index){
        mFruitList.get(index).setBitmap(image);
    }
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.fruit_item, parent, false);
        final ViewHolder holder = new ViewHolder(view);
        holder.fruitView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = holder.getAdapterPosition();
                Thumbnail fruit = mFruitList.get(position);
                    //启动壁纸，传入路径，当他是assets文件时是文件名，否则是本地视频路径
                    //getType返回类型
                VideoLiveWallpaper.setToVideoWallPaper(GlobalContext.getMainActivity(),fruit.getFilePath(),fruit.getType());
                Toast.makeText(v.getContext(), "you clicked view " + fruit.getName(), Toast.LENGTH_SHORT).show();
            }
        });
        return holder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Thumbnail fruit = mFruitList.get(position);
        if(fruit.getBitmap()!=null){    //图片有数据时加载
            holder.fruitImage.setImageBitmap(fruit.getBitmap());
        }
    }

    @Override
    public int getItemCount() {
        return mFruitList.size();
    }

}