package com.suguiming.selectphoto_android.photo_lib.photo;

import android.view.View;

import java.util.List;

/**
 * Created by suguiming on 16/9/23.
 */
public interface AlbumResultListener {
    void complete(View tapedView, List<String> pathList);
}
