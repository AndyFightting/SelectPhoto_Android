package com.suguiming.selectphoto_android.photo_lib.photo;

import android.app.Activity;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Point;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import com.suguiming.selectphoto_android.R;
import com.suguiming.selectphoto_android.photo_lib.base.BaseSwipeActivity;
import com.suguiming.selectphoto_android.photo_lib.customer.PhotoViewPager;

import java.util.ArrayList;
import java.util.List;

import uk.co.senab.photoview.PhotoView;

public class AlbumPhotoSwitchActivity extends BaseSwipeActivity implements View.OnClickListener{

    private PhotoViewPager viewPager;
    private SimplePagerAdapter adapter;

    protected int activityCloseEnterAnimation;
    protected int activityCloseExitAnimation;

    private int currentIndex;
    private List<AlbumPhotoModel> modelList = new ArrayList<>();

    private ImageView selectImage;
    private TextView titleTv;
    private TextView sureTv;
    private  static int limitNum;
    private int selectedNum;
    private static  SwitchListener switchListener;
    private boolean isSureTap = false;
    private static boolean isPreview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album_photo_switch);
        initAnimation();

        titleTv = (TextView) findViewById(R.id.title_tv);
        selectImage = (ImageView)findViewById(R.id.selected_img);
        sureTv = (TextView)findViewById(R.id.sure_tv);
        selectImage.setOnClickListener(this);
        sureTv.setOnClickListener(this);

        currentIndex = getIntent().getIntExtra("index",0);
        Bundle extras = getIntent().getExtras();
        ArrayList<AlbumPhotoModel> arrayList  = extras.getParcelableArrayList("modelList");
        modelList.addAll(arrayList);

        viewPager = (PhotoViewPager) findViewById(R.id.view_pager);
        adapter = new SimplePagerAdapter();
        viewPager.setAdapter(adapter);
        viewPager.setCurrentItem(currentIndex);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                currentIndex = position;
                refreshTitle();
                refreshSelectImage();
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        refreshTitle();
        refreshSelectImage();
        refreshSureTv();
    }

    @Override
    protected void onDestroy() {
        limitNum = 0;
        switchListener = null;
        isPreview = false;
        super.onDestroy();
    }

    public static void startActivity(Activity activity, int index, int limitN, List<AlbumPhotoModel> models,boolean preview, SwitchListener listener){
        limitNum = limitN;
        switchListener = listener;
        isPreview = preview;

        ArrayList<AlbumPhotoModel> arrayList;
        if (models instanceof ArrayList){
            arrayList = (ArrayList<AlbumPhotoModel>) models;
        }else {
            arrayList = new ArrayList<>();
            arrayList.addAll(models);
        }

        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList("modelList", arrayList);

        Intent intent = new Intent(activity,AlbumPhotoSwitchActivity.class);
        intent.putExtra("index",index);
        intent.putExtras(bundle);
        activity.startActivity(intent);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
           case  R.id.selected_img:
               selectImageTaped();
            break;
            case  R.id.sure_tv:
                isSureTap = true;
                completeBack("sure");
                break;
        }
    }

    private void selectImageTaped(){
        AlbumPhotoModel model = modelList.get(currentIndex);
        if (selectedNum < limitNum){
            if (model.isSelected == 1){
                model.isSelected = 0;
            }else {
                model.isSelected = 1;
            }
        }else {
            if (model.isSelected == 1){
                model.isSelected = 0;
            }else {
                Toast.makeText(this, "限制选择"+limitNum+"张", Toast.LENGTH_SHORT).show();
            }
        }
        refreshSelectImage();
        refreshSureTv();
    }

    private void completeBack(String name){
        switchListener.switchComplete(modelList,name,isPreview);
        finish();
    }

    @Override
    public void backImageTap(View v) {
        completeBack("back");
        super.backImageTap(v);
    }

    @Override
    public void onBackPressed() {
        completeBack("back");
        super.onBackPressed();
    }

    private void refreshTitle(){
        titleTv.setText(currentIndex+1+"/"+modelList.size());
    }

    private void refreshSelectImage(){
        AlbumPhotoModel model = modelList.get(currentIndex);
        if (model.isSelected == 1){
            selectImage.setImageResource(R.mipmap.selected);
        }else {
            selectImage.setImageResource(R.mipmap.select_no);
        }
    }

    private void refreshSureTv(){
        int num = 0;
        for (AlbumPhotoModel model:modelList){
            if (model.isSelected == 1){
                num++;
            }
        }
        selectedNum = num;

        if (num>0){
            sureTv.setText("确定("+num+")");
            sureTv.setVisibility(View.VISIBLE);
        }else {
            sureTv.setText("确定");
            sureTv.setVisibility(View.GONE);
        }
    }

    private void initAnimation() {
        //自定义退出动画要用的，不然退出的效果不行(1)
        TypedArray activityStyle = getTheme().obtainStyledAttributes(new int[]{android.R.attr.windowAnimationStyle});
        int windowAnimationStyleResId = activityStyle.getResourceId(0, 0);
        activityStyle.recycle();
        activityStyle = getTheme().obtainStyledAttributes(windowAnimationStyleResId, new int[]{android.R.attr.activityCloseEnterAnimation, android.R.attr.activityCloseExitAnimation});
        activityCloseEnterAnimation = activityStyle.getResourceId(0, 0);
        activityCloseExitAnimation = activityStyle.getResourceId(1, 0);
        activityStyle.recycle();
    }

    @Override
    public void finish() {
        super.finish();
        if (!isSureTap){
            //自定义退出动画要用的，不然退出的效果不行(2)
            overridePendingTransition(activityCloseEnterAnimation, activityCloseExitAnimation);
        }
    }

    //---------adapter--------
    private class SimplePagerAdapter extends PagerAdapter {
        private LocalImageCache cache;
        private Point point;

        public SimplePagerAdapter(){
            cache = LocalImageCache.getInstance();
            point = new Point(300,300);
        }

        @Override
        public int getCount() {
            return modelList.size();
        }

        @Override
        public View instantiateItem(ViewGroup container, int position) {
            AlbumPhotoModel model = modelList.get(position);

            PhotoView photoView = new PhotoView(container.getContext());
            cache.displayBmp(photoView,model.getImagePath(),point);
            container.addView(photoView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

            return photoView;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }
    }

    public interface SwitchListener{
        void switchComplete(List<AlbumPhotoModel> list, String string, boolean isPreview);
    }
}
