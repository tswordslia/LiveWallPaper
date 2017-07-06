package com.example.tswords.livewallpaper.Adaper;

import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.example.tswords.livewallpaper.R;

import java.util.List;

/**
 * Created by Tswords on 2017/6/4.
 */

public class ThumbnailAdapter extends RecyclerView.Adapter<ThumbnailAdapter.ViewHolder> {

    private List<Thumbnail> mFruitList;
    public void addPhoto(Bitmap image, int index){
        mFruitList.get(index).setBitmap(image);
    }
    static class ViewHolder extends RecyclerView.ViewHolder {
        View fruitView;
        ImageView fruitImage;

        public ViewHolder(View view) {
            super(view);
            fruitView = view;
            fruitImage = (ImageView) view.findViewById(R.id.fruit_image);
        }
    }

    public ThumbnailAdapter(List<Thumbnail> fruitList) {
        mFruitList = fruitList;
    }

    @Override
    public ThumbnailAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.fruit_item, parent, false);
        final ViewHolder holder = new ViewHolder(view);
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
