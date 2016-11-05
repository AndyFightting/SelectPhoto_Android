package com.suguiming.selectphoto_android.photo_lib.photo;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;


import com.suguiming.selectphoto_android.R;
import com.suguiming.selectphoto_android.photo_lib.base.BaseSwipeActivity;
import com.suguiming.selectphoto_android.photo_lib.util.FileUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

//相册全尺寸多选,或者相册裁剪
public class AlbumPhotoActivity extends BaseSwipeActivity {

    private static AlbumResultListener alphaSheetListener;
    public static PhotoType photoType;
    public static int limitNum;
    private int selectedNum;

    private static Activity dirActivity;

    public GridView gridView;
    private List<AlbumPhotoModel> modelList = new ArrayList<>();
    private GridAdapter adapter;

    private TextView previewTv;
    private TextView sureTv;
    private TextView numTv;

    private RelativeLayout bottomLayout;
    public Uri tmpUri;
    public static final int RESULT_OK = -1;
    public static final int CROP_PHOTO_CODE = 3;
    public static final String CROP_ACTION = "com.android.camera.action.CROP";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        setMainView(R.layout.activity_album);
        TextView titleTv = (TextView) findViewById(R.id.title_tv);
        titleTv.setText("选择照片");

        bottomLayout = (RelativeLayout) findViewById(R.id.bottom_bar);
        previewTv = (TextView) findViewById(R.id.pre_tv);
        sureTv = (TextView) findViewById(R.id.sure_tv);
        numTv = (TextView) findViewById(R.id.num_tv);

        if (photoType == PhotoType.SQUARE_IMAGE) {
            bottomLayout.setVisibility(View.GONE);
        }

        initData();

        gridView = (GridView) findViewById(R.id.photo_grid);
        adapter = new GridAdapter(this, R.layout.album_photo_item, modelList);
        gridView.setAdapter(adapter);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //-----照片点击-------
                if (photoType == PhotoType.SQUARE_IMAGE) {
                    startCropImage(position);
                } else {
                    switchImage(position);
                }
            }
        });

        checkSelectedNum();
    }

    //查看照片
    private void switchImage(int index) {
        AlbumPhotoSwitchActivity.startActivity(this, index, limitNum, modelList, false, new AlbumPhotoSwitchActivity.SwitchListener() {
            @Override
            public void switchComplete(List<AlbumPhotoModel> selectModelList, String string, boolean isPreview) {
                switchBacked(selectModelList, string, isPreview);
            }
        });
    }

    private void switchBacked(List<AlbumPhotoModel> list, String string, boolean isPreview) {
        if (isPreview) {
            for (AlbumPhotoModel preMd : list) {
                for (AlbumPhotoModel nowMd : modelList) {
                    if (preMd.getImagePath().equals(nowMd.getImagePath())) {
                        nowMd.isSelected = preMd.isSelected;
                        break;
                    }
                }
            }
        } else {
            modelList.clear();
            modelList.addAll(list);
        }

        if ("sure".equals(string)) {
            sureTap(new View(this));
        } else {
            adapter.notifyDataSetChanged();
            checkSelectedNum();
        }
    }

    //相册全尺寸-------
    public void sureTap(View v) {
        List<String> resultList = new ArrayList<>();
        for (AlbumPhotoModel model : modelList) {
            if (model.isSelected == 1) {
                resultList.add(model.getImagePath());
            }
        }
        alphaSheetListener.complete(v, resultList);

        finish();
        dirActivity.finish();
        dirActivity = null;
    }

    public void previewTap(View v) {
        List<AlbumPhotoModel> selectList = new ArrayList<>();
        for (AlbumPhotoModel model : modelList) {
            if (model.isSelected == 1) {
                selectList.add(model);
            }
        }
        AlbumPhotoSwitchActivity.startActivity(this, 0, limitNum, selectList, true, new AlbumPhotoSwitchActivity.SwitchListener() {
            @Override
            public void switchComplete(List<AlbumPhotoModel> selectModelList, String string, boolean isPreview) {
                switchBacked(selectModelList, string, isPreview);
            }
        });
    }

    public void startCropImage(int index) {
        String photoPath = modelList.get(index).getImagePath();

        Intent intent = new Intent();
        intent.setData(Uri.fromFile(new File(photoPath)));
        startAlbumCrop(intent);
    }

    private void startAlbumCrop(Intent data) {
        tmpUri = Uri.fromFile(new File(FileUtil.getBitmapPath("album_tmp_photo")));

        Intent intent = new Intent(CROP_ACTION);
        intent.setDataAndType(data.getData(), "image/*"); //区别 data.getData()
        intent.putExtra(MediaStore.EXTRA_OUTPUT, tmpUri);
        intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
        intent.putExtra("crop", "true");
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        intent.putExtra("outputX", 600);
        intent.putExtra("outputY", 600);
        intent.putExtra("scale", true);
        intent.putExtra("return-data", false);
        intent.putExtra("noFaceDetection", true);
        startActivityForResult(intent, CROP_PHOTO_CODE);
    }


    public static void startActivity(Activity activity, List<String> list, PhotoType type, int num, AlbumResultListener listener) {
        alphaSheetListener = null;
        alphaSheetListener = listener;

        dirActivity = activity;
        photoType = type;
        limitNum = num;

        Intent intent = new Intent(activity, AlbumPhotoActivity.class);
        intent.putStringArrayListExtra("imageList", (ArrayList<String>) list);
        activity.startActivity(intent);
    }

    private void initData() {
        List<String> imagePathList = getIntent().getStringArrayListExtra("imageList");
        if (imagePathList != null && imagePathList.size() > 0) {
            for (int i = 0; i < imagePathList.size(); i++) {
                AlbumPhotoModel model = new AlbumPhotoModel();
                model.imagePath = imagePathList.get(i);
                modelList.add(model);
            }
        }
    }

    private void checkSelectedNum() {
        int num = 0;
        for (AlbumPhotoModel model : modelList) {
            if (model.isSelected == 1) {
                num++;
            }
        }
        selectedNum = num;

        if (num > 0) {
            previewTv.setTextColor(ContextCompat.getColor(this, R.color.black));
            previewTv.setClickable(true);

            numTv.setVisibility(View.VISIBLE);
            numTv.setText(num + "");

            sureTv.setTextColor(ContextCompat.getColor(this, R.color.black));
            sureTv.setClickable(true);
        } else {
            previewTv.setTextColor(ContextCompat.getColor(this, R.color.gray));
            previewTv.setClickable(false);

            numTv.setVisibility(View.GONE);

            sureTv.setTextColor(ContextCompat.getColor(this, R.color.gray));
            sureTv.setClickable(false);
        }
    }

    public  int getPxFromDp(float dpValue) {
        final float scale = getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    //---------adapter------------
    class GridAdapter extends ArrayAdapter<AlbumPhotoModel> {
        private int layoutId;

        private int imageWidthPx = getPxFromDp(100);
        private Point mPoint = new Point(imageWidthPx, imageWidthPx);
        private LocalImageCache cache;

        public GridAdapter(Context context, int resourceId, List<AlbumPhotoModel> objects) {
            super(context, resourceId, objects);
            layoutId = resourceId;
            cache = LocalImageCache.getInstance();
        }

        @Override
        public int getCount() {
            return modelList.size();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final AlbumPhotoModel model = getItem(position);

            ViewHolder viewHolder;
            View layoutView;

            if (convertView == null) {
                layoutView = LayoutInflater.from(getContext()).inflate(layoutId, null);
                viewHolder = new ViewHolder();

                viewHolder.checkImage = (ImageView) layoutView.findViewById(R.id.check_image);
                viewHolder.image = (ImageView) layoutView.findViewById(R.id.grid_photo);

                layoutView.setTag(viewHolder);
            } else {
                layoutView = convertView;
                viewHolder = (ViewHolder) layoutView.getTag();
            }
            //--------在下面赋值 ----------------
            cache.displayBmp(viewHolder.image, model.getImagePath(), mPoint);

            //-----check image 处理------
            if (photoType == PhotoType.SQUARE_IMAGE) {
                viewHolder.checkImage.setVisibility(View.GONE);
            } else {
                if (model.isSelected == 1) {
                    viewHolder.checkImage.setImageResource(R.mipmap.selected);
                } else {
                    viewHolder.checkImage.setImageResource(R.mipmap.select_no);
                }
            }

            //----checkImage 点击----
            viewHolder.checkImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (selectedNum < limitNum) {
                        if (model.isSelected == 1) {
                            model.isSelected = 0;
                            ImageView imageView = (ImageView) v;
                            imageView.setImageResource(R.mipmap.select_no);
                        } else {
                            model.isSelected = 1;
                            ImageView imageView = (ImageView) v;
                            imageView.setImageResource(R.mipmap.selected);
                        }
                    } else {
                        if (model.isSelected == 1) {
                            model.isSelected = 0;
                            ImageView imageView = (ImageView) v;
                            imageView.setImageResource(R.mipmap.select_no);
                        } else {
                            Toast.makeText(AlbumPhotoActivity.this, "限定选择" + limitNum + "张", Toast.LENGTH_SHORT).show();
                        }
                    }

                    checkSelectedNum();
                }
            });

            return layoutView;
        }

        class ViewHolder {
            ImageView image;
            ImageView checkImage;
        }
    }

    //相册裁剪-------
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && photoType == PhotoType.SQUARE_IMAGE && requestCode == CROP_PHOTO_CODE) {

            List<String> tmpList = new ArrayList<>();
            tmpList.add(tmpUri.getPath());
            alphaSheetListener.complete(new View(this), tmpList);

            finish();
            dirActivity.finish();
            dirActivity = null;
        }
    }

    @Override
    protected void onDestroy() {
        dirActivity = null;
        modelList.clear();
        super.onDestroy();
    }

}
