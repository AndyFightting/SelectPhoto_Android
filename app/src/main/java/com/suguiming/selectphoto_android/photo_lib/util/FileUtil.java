package com.suguiming.selectphoto_android.photo_lib.util;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;


import com.suguiming.selectphoto_android.photo_lib.base.MyApplication;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by suguiming on 16/10/20.
 */

public class FileUtil {

    public static final MyApplication application = MyApplication.getInstance();
    public static String DIR_PATH = Environment.getExternalStorageDirectory() + "/paiqi/";

    public static void createDir() {
        File dirFile = new File(DIR_PATH);
        dirFile.mkdirs();//有了就不会再创建了
    }

    public static Bitmap getBitmapFromUri(Uri uri) {
        try {
            return MediaStore.Images.Media.getBitmap(application.getContentResolver(), uri);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    //name 不要斜杠/,不要.jpg
    public static void saveBitmap(Bitmap bitmap, String name) {
        createDir();

        FileOutputStream b = null;
        String filePath = DIR_PATH + name + ".jpg";
        try {
            b = new FileOutputStream(filePath);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, b);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            try {
                b.flush();
                b.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static String getBitmapPath(String name) {
        createDir();
        return DIR_PATH + name + ".jpg";
    }

    public static void deleteAllFile() {
        File dirFile = new File(DIR_PATH);
        deleteFile(dirFile);
    }

    private static void deleteFile(File file) {
        if (file.isFile()) {
            file.delete();
            return;
        }
        if (file.isDirectory()) {
            File[] childFile = file.listFiles();
            if (childFile == null || childFile.length == 0) {//文件夹里没东西
                file.delete();
                return;
            }
            for (File f : childFile) {
                deleteFile(f);
            }
            file.delete();
        }
    }

    //压缩图片
    public static Bitmap getCompressedBitmap(String originImagePath, int maxPx) {
        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(originImagePath, options);
            options.inSampleSize = computeScale(options, maxPx, maxPx);
            options.inJustDecodeBounds = false;

            return BitmapFactory.decodeFile(originImagePath, options);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static int computeScale(BitmapFactory.Options options, int viewWidth, int viewHeight) {
        int inSampleSize = 1;
        if (viewWidth == 0 || viewHeight == 0) {
            return inSampleSize;
        }
        int bitmapWidth = options.outWidth;
        int bitmapHeight = options.outHeight;

        if (bitmapWidth > viewWidth || bitmapHeight > viewHeight) {
            int widthScale = Math.round((float) bitmapWidth / (float) viewWidth);
            int heightScale = Math.round((float) bitmapHeight / (float) viewHeight);

            inSampleSize = (widthScale + heightScale) / 2;
        }
        return inSampleSize;
    }

    //压缩图片和得到路径
    public static String getCompressedPath(String originImagePath, String imageName, int maxPx) {
        createDir();
        Bitmap compressedBitmap = getCompressedBitmap(originImagePath, maxPx);
        if (compressedBitmap != null) {
            saveBitmap(compressedBitmap, imageName);
            return getBitmapPath(imageName);
        }
        return "";
    }

    public static void refreshSystemPhoto(String localImagePath) {
        try {
            Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            File file = new File(localImagePath);

            Uri contentUri = Uri.fromFile(file);
            mediaScanIntent.setData(contentUri);
            application.sendBroadcast(mediaScanIntent);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

}
