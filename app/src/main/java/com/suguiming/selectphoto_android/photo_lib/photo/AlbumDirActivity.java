package com.suguiming.selectphoto_android.photo_lib.photo;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;


import com.suguiming.selectphoto_android.R;
import com.suguiming.selectphoto_android.photo_lib.base.BaseSheetActivity;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class AlbumDirActivity extends BaseSheetActivity {

    private static AlbumResultListener alphaSheetListener;
    private ImageView backImage;

    private ListView listView;
    private HashMap<String, List<String>> dirMap = new HashMap<>();
    private List<AlbumDirModel> folderList = new ArrayList<>();
    private AlbumDirAdapter adapter;

    public static PhotoType photoType;
    public static int selectNum;

    private final static int SCAN_OK = 1;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case SCAN_OK:
                    initListView();
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        setContentView(R.layout.activity_album_dir);

        TextView titleTv = (TextView) findViewById(R.id.title_tv);
        titleTv.setText("相册");

        backImage = (ImageView) findViewById(R.id.back_image);

        getLocalImages();
    }

    public static void startCropImageActivity(Activity activity, AlbumResultListener listener) {
        alphaSheetListener= null;
        alphaSheetListener = listener;
        photoType = PhotoType.SQUARE_IMAGE;
        selectNum = 1;

        Intent intent = new Intent(activity, AlbumDirActivity.class);
        activity.startActivity(intent);
    }

    public static void startFullImageActivity(Activity activity,int num,AlbumResultListener listener) {
        alphaSheetListener= null;
        alphaSheetListener = listener;
        photoType = PhotoType.FULL_IMAGE;
        selectNum = num;

        Intent intent = new Intent(activity, AlbumDirActivity.class);
        activity.startActivity(intent);
    }


    private void initListView() {
        Iterator<Map.Entry<String, List<String>>> item = dirMap.entrySet().iterator();

        while (item.hasNext()) {
            Map.Entry<String, List<String>> entry = item.next();
            String key = entry.getKey();
            List<String> value = entry.getValue();

            AlbumDirModel mImageBean = new AlbumDirModel();
            mImageBean.setFolderName(key);
            mImageBean.setImageCount(value.size());
            mImageBean.setFolderImagePath(value.get(0));

            folderList.add(mImageBean);
        }

        listView = (ListView) findViewById(R.id.album_dir_list);
        adapter = new AlbumDirAdapter(this, R.layout.album_dir_item, folderList);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                List<String> imagePathList = dirMap.get(folderList.get(position).getFolderName());
                AlbumPhotoActivity.startActivity(AlbumDirActivity.this, imagePathList, photoType,selectNum,alphaSheetListener);
            }
        });
    }

    public void getLocalImages() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Uri mImageUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                ContentResolver mContentResolver = AlbumDirActivity.this.getContentResolver();

                Cursor mCursor = mContentResolver.query(
                        mImageUri,
                        null,
                        MediaStore.Images.Media.MIME_TYPE + "=? or " + MediaStore.Images.Media.MIME_TYPE + "=? or " + MediaStore.Images.Media.MIME_TYPE + "=?",
                        new String[]{"image/jpeg", "image/png", "image/jpg"},
                        MediaStore.Images.Media.DATE_MODIFIED);

                while (mCursor.moveToNext()) {
                    String path = mCursor.getString(mCursor.getColumnIndex(MediaStore.Images.Media.DATA));
                    String parentName = new File(path).getParentFile().getName();

                    if (!dirMap.containsKey(parentName)) {
                        List<String> chileList = new ArrayList<>();
                        chileList.add(path);
                        dirMap.put(parentName, chileList);
                    } else {
                        dirMap.get(parentName).add(path);
                    }
                }
                mCursor.close();
                mHandler.sendEmptyMessage(SCAN_OK);
            }
        }).start();
    }


    public void backImageTap(View v) {
        finish();
        if (alphaSheetListener != null) {
            alphaSheetListener.complete(backImage, null);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (alphaSheetListener != null) {
            alphaSheetListener.complete(backImage, null);
        }
    }

    public  int getPxFromDp(float dpValue) {
        final float scale = getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    class AlbumDirAdapter extends ArrayAdapter<AlbumDirModel> {
        private int layoutId;

        private int imageWidthPx = getPxFromDp(60);
        private Point mPoint = new Point(imageWidthPx, imageWidthPx);
        private LocalImageCache cache;

        public AlbumDirAdapter(Context context, int resourceId, List<AlbumDirModel> objects) {
            super(context, resourceId, objects);
            layoutId = resourceId;
            cache = LocalImageCache.getInstance();
        }

        @Override
        public int getCount() {
            return folderList.size();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            AlbumDirModel model = getItem(position);
            ViewHolder viewHolder;
            View layoutView;

            if (convertView == null) {
                layoutView = LayoutInflater.from(getContext()).inflate(layoutId, null);
                viewHolder = new ViewHolder();

                viewHolder.name = (TextView) layoutView.findViewById(R.id.dir_name);
                viewHolder.image = (ImageView) layoutView.findViewById(R.id.dir_image);

                layoutView.setTag(viewHolder);
            } else {
                layoutView = convertView;
                viewHolder = (ViewHolder) layoutView.getTag();
            }
            //--------在下面赋值 ----------------
            String path = model.getFolderImagePath();
            viewHolder.name.setText(model.getFolderName() + " (" + model.getImageCount() + ")");
            cache.displayBmp(viewHolder.image,path,mPoint);

            return layoutView;
        }

        class ViewHolder {
            ImageView image;
            TextView name;
        }
    }

    @Override
    protected void onDestroy() {
        dirMap.clear();
        folderList.clear();
        super.onDestroy();
    }
}
