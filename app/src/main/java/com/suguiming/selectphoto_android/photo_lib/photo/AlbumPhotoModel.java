package com.suguiming.selectphoto_android.photo_lib.photo;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by suguiming on 16/9/22.
 */
public class AlbumPhotoModel implements Parcelable{
    public String imagePath;
    public int isSelected;

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public int getIsSelected() {
        return isSelected;
    }

    public void setIsSelected(int isSelected) {
        this.isSelected = isSelected;
    }


    //-------parcelbale-------
    public static final Creator<AlbumPhotoModel> CREATOR = new Creator<AlbumPhotoModel>() {
        @Override
        public AlbumPhotoModel createFromParcel(Parcel source) {
            AlbumPhotoModel police = new AlbumPhotoModel();
            police.imagePath = source.readString();
            police.isSelected = source.readInt();
            return police;
        }

        @Override
        public AlbumPhotoModel[] newArray(int size) {
            return new AlbumPhotoModel[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeString(imagePath);
        parcel.writeInt(isSelected);
    }
}
