package com.suguiming.selectphoto_android.photo_lib.photo;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.os.Handler;
import android.support.v4.util.LruCache;
import android.text.TextUtils;
import android.widget.ImageView;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class LocalImageCache {

    private static LocalImageCache mInstance;

    private Handler mHander = new Handler();
    private LruCache<String, Bitmap> mMemoryCache;
    private CacheImageCallBack mCallBack;
    private ExecutorService mImageThreadPool = Executors.newFixedThreadPool(25);

    private LocalImageCache() {
        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
        final int cacheSize = maxMemory / 5;

        mMemoryCache = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                return bitmap.getByteCount()/1024;
            }
        };

        mCallBack = new CacheImageCallBack() {
            @Override
            public void onImageLoader(ImageView imageView, Bitmap bitmap) {
                if (bitmap != null && imageView != null) {
                    imageView.setImageBitmap(bitmap);
                }
            }
        };
    }

    public static LocalImageCache getInstance() {
        synchronized (LocalImageCache.class) {
            if (mInstance == null) {
                mInstance = new LocalImageCache();
            }
            return mInstance;
        }
    }

    public static void clearCache() {
        LocalImageCache cache = getInstance();
        cache.mMemoryCache.evictAll();
    }

    public void displayBmp(final ImageView imageView, final String path, final Point mPoint) {
        if (TextUtils.isEmpty(path) || imageView == null) {
            return;
        }

        Bitmap bitmap = getBitmapFromMemCache(path);
        if (bitmap == null) {
            imageView.setImageBitmap(null);

            mImageThreadPool.execute(new Runnable() {
                Bitmap mBitmap;

                @Override
                public void run() {
                    mBitmap = decodeThumbBitmapForFile(path, mPoint == null ? 0 : mPoint.x, mPoint == null ? 0 : mPoint.y);
                    mHander.post(new Runnable() {
                        @Override
                        public void run() {
                            mCallBack.onImageLoader(imageView, mBitmap);
                        }
                    });
                    addBitmapToMemoryCache(path, mBitmap);
                }
            });
        } else {
            imageView.setImageBitmap(bitmap);
        }
    }

    private void addBitmapToMemoryCache(String key, Bitmap bitmap) {
        if (getBitmapFromMemCache(key) == null && bitmap != null) {
            mMemoryCache.put(key, bitmap);
        }
    }

    private Bitmap getBitmapFromMemCache(String key) {
        return mMemoryCache.get(key);
    }

    private Bitmap decodeThumbBitmapForFile(String path, int viewWidth, int viewHeight) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);
        options.inSampleSize = computeScale(options, viewWidth, viewHeight);
        options.inJustDecodeBounds = false;

        return BitmapFactory.decodeFile(path, options);
    }

    private int computeScale(BitmapFactory.Options options, int viewWidth, int viewHeight) {
        int inSampleSize = 1;
        if (viewWidth == 0 || viewHeight == 0) {
            return inSampleSize;
        }
        int bitmapWidth = options.outWidth;
        int bitmapHeight = options.outHeight;

        if (bitmapWidth > viewWidth || bitmapHeight > viewHeight) {
            int widthScale = Math.round((float) bitmapWidth / (float) viewWidth);
            int heightScale = Math.round((float) bitmapHeight / (float) viewHeight);

            inSampleSize = (widthScale+heightScale)/2;
        }
        return inSampleSize;
    }


    private interface CacheImageCallBack {
        void onImageLoader(ImageView imageView, Bitmap bitmap);
    }
}
