package com.suguiming.selectphoto_android.photo_lib.photo;

import android.os.Bundle;
import android.view.View;

import com.suguiming.selectphoto_android.R;
import com.suguiming.selectphoto_android.photo_lib.base.BaseSheetActivity;


/**
 * Created by suguiming on 15/11/28.
 */
public class PhotoSheet extends BaseSheetActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.action_photo_sheet);

    }

    public void itemTap(View view){
        dismiss();
        if (itemTapListener != null){
            itemTapListener.itemTap(view,"");
        }
    }

    @Override
    public void onBackPressed() {
        dismiss();
        if (itemTapListener != null){
            itemTapListener.itemTap(new View(this),"");
        }
    }
}
