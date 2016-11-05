package com.suguiming.selectphoto_android.photo_lib.photo;

/**
 * Created by suguiming on 16/9/23.
 */
public class AlbumDirModel {

    private String folderImagePath;
    private String folderName;
    private int imageCount;

    public String getFolderImagePath() {
        return folderImagePath;
    }

    public void setFolderImagePath(String folderImagePath) {
        this.folderImagePath = folderImagePath;
    }

    public String getFolderName() {
        return folderName;
    }

    public void setFolderName(String folderName) {
        this.folderName = folderName;
    }

    public int getImageCount() {
        return imageCount;
    }

    public void setImageCount(int imageCount) {
        this.imageCount = imageCount;
    }
}
