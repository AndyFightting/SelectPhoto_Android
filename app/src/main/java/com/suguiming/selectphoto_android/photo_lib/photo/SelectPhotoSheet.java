package com.suguiming.selectphoto_android.photo_lib.photo;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;


import com.suguiming.selectphoto_android.R;
import com.suguiming.selectphoto_android.photo_lib.base.ItemTapListener;
import com.suguiming.selectphoto_android.photo_lib.util.FileUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

//这是一个透明activity,处理相机裁剪,相机全尺寸
public class SelectPhotoSheet extends AppCompatActivity {

    public static final int TAKE_PHOTO_CODE = 1;
    public static final int CROP_PHOTO_CODE = 3;

    public static final int RESULT_OK = -1;

    public static final String CAMERA_ACTION = "android.media.action.IMAGE_CAPTURE";
    public static final String CROP_ACTION = "com.android.camera.action.CROP";

    public static Uri tmpUri;
    public static AlbumResultListener sheetListener;
    public static PhotoType photoType;
    public static int selectNum;
    public static List<String> sheetPathList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

        showSheet();
    }

    //选择裁剪正方形图片
    public static void showCropImageSheet(Activity activity, AlbumResultListener listener) {
        sheetListener = listener;
        photoType = PhotoType.SQUARE_IMAGE;
        selectNum = 1;
        sheetPathList.clear();

        Intent intent = new Intent(activity, SelectPhotoSheet.class);
        activity.startActivity(intent);

    }

    //选择全尺寸图片
    public static void showFullImageSheet(Activity activity, int num, AlbumResultListener listener) {
        sheetListener = listener;
        photoType = PhotoType.FULL_IMAGE;
        selectNum = num;
        sheetPathList.clear();

        Intent intent = new Intent(activity, SelectPhotoSheet.class);
        activity.startActivity(intent);
    }


    public void showSheet() {
        tmpUri = Uri.fromFile(new File(FileUtil.getBitmapPath("camera_tmp_photo")));

        PhotoSheet.show(this, PhotoSheet.class, new ItemTapListener() {
            @Override
            public void itemTap(View view, String result) {
                switch (view.getId()) {
                    case R.id.camera_tv:
                        cameraTap();
                        break;
                    case R.id.phone_tv:
                        photoTap();
                        break;
                    default://点击sheet背景
                        finish();
                        break;
                }
            }
        });
    }

    private void cameraTap() {
        if (photoType == PhotoType.FULL_IMAGE) {//全尺寸
            Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, tmpUri);//必须
            startActivityForResult(cameraIntent, CROP_PHOTO_CODE);//全尺寸即当是最后裁剪的了
        } else if (photoType == PhotoType.SQUARE_IMAGE) {//正方形图片
            Intent cameraIntent = new Intent(CAMERA_ACTION);
            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, tmpUri);//必须
            startActivityForResult(cameraIntent, TAKE_PHOTO_CODE);
        }
    }

    private void photoTap() {
        if (photoType == PhotoType.SQUARE_IMAGE) {//正方形图片
            AlbumDirActivity.startCropImageActivity(this, new AlbumResultListener() {
                @Override
                public void complete(View tapedView, List<String> pathList) {
                    if (pathList != null) {//得到从相册来的裁剪图片
                        sheetListener.complete(new View(SelectPhotoSheet.this), pathList);
                    }
                    finish();
                }
            });

        } else if (photoType == PhotoType.FULL_IMAGE) {//全尺寸
            AlbumDirActivity.startFullImageActivity(this, selectNum, new AlbumResultListener() {
                @Override
                public void complete(View tapedView, List<String> pathList) {
                    if (pathList != null) {//得到从相册来的图片
                        sheetListener.complete(new View(SelectPhotoSheet.this), pathList);
                    }
                    finish();
                }
            });
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case TAKE_PHOTO_CODE://开相机
                    startCameraCrop();
                    break;
                case CROP_PHOTO_CODE://相机裁剪,相机全尺寸
                    getResultImage(tmpUri);
                    break;
                default:
                    break;
            }
        } else {
            finish();
        }
    }

    private void startCameraCrop() {
        Intent intent = new Intent(CROP_ACTION);
        intent.setDataAndType(tmpUri, "image/*");
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

    private void getResultImage(Uri uri) {
        sheetPathList.add(uri.getPath());
        sheetListener.complete(new View(this), sheetPathList);
        finish();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        finish();
        return super.onTouchEvent(event);
    }

    @Override
    protected void onDestroy() {
        sheetPathList.clear();
        super.onDestroy();
    }
}
