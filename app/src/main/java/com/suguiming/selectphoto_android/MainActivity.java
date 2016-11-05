package com.suguiming.selectphoto_android;

import android.content.Context;
import android.graphics.Point;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.suguiming.selectphoto_android.photo_lib.photo.AlbumResultListener;
import com.suguiming.selectphoto_android.photo_lib.photo.LocalImageCache;
import com.suguiming.selectphoto_android.photo_lib.photo.SelectPhotoSheet;
import com.suguiming.selectphoto_android.photo_lib.util.FileUtil;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private LocalImageCache cache;

    private ImageView headImageView;
    public GridView gridView;

    private List<String> localPathList = new ArrayList<>();
    private GridAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        cache = LocalImageCache.getInstance();
        headImageView = (ImageView) findViewById(R.id.head_image);

        gridView = (GridView) findViewById(R.id.grid_view);
        adapter = new GridAdapter(this, R.layout.gird_itme_layout, localPathList);
        gridView.setAdapter(adapter);
    }

    public void oneTaped(View v) {
        //在适当的时候清除本地临时图片和缓存
        FileUtil.deleteAllFile();
        LocalImageCache.clearCache();

        SelectPhotoSheet.showCropImageSheet(this, new AlbumResultListener() {
            @Override
            public void complete(View tapedView, List<String> pathList) {
                cache.displayBmp(headImageView, pathList.get(0), new Point(100, 100));
            }
        });
    }

    public void twoTaped(View v) {
        //在适当的时候清除本地临时图片和缓存
        FileUtil.deleteAllFile();
        LocalImageCache.clearCache();

        SelectPhotoSheet.showFullImageSheet(this, 8, new AlbumResultListener() {
            @Override
            public void complete(View tapedView, List<String> pathList) {
                localPathList.clear();
                localPathList.addAll(pathList);
                adapter.notifyDataSetChanged();
            }
        });
    }

    //---------adapter------------
    class GridAdapter extends ArrayAdapter<String> {
        private int layoutId;
        private Point point;

        public GridAdapter(Context context, int resourceId, List<String> objects) {
            super(context, resourceId, objects);
            layoutId = resourceId;
            point = new Point(100, 100);
        }

        @Override
        public int getCount() {
            return localPathList.size();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            GridAdapter.ViewHolder viewHolder;
            View layoutView;

            if (convertView == null) {
                layoutView = LayoutInflater.from(getContext()).inflate(layoutId, null);
                viewHolder = new GridAdapter.ViewHolder();

                viewHolder.image = (ImageView) layoutView.findViewById(R.id.grid_photo);

                layoutView.setTag(viewHolder);
            } else {
                layoutView = convertView;
                viewHolder = (GridAdapter.ViewHolder) layoutView.getTag();
            }
            //--------在下面赋值 ----------------
            cache.displayBmp(viewHolder.image, localPathList.get(position), point);

            return layoutView;
        }

        class ViewHolder {
            ImageView image;
        }
    }

}
