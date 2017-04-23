package com.example.d062589.buddy.Models;

import android.databinding.BaseObservable;
import android.databinding.BindingAdapter;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.example.d062589.buddy.R;

/**
 * Created by D062589 on 10.03.2017.
 */

public class Drop extends BaseObservable {
    private String id;
    private String comment;
    private String previewImg;
    private String author;
    private String authorImgUrl;
    private double latitude;
    private double longitude;
    private String dropType;
    private String contentUrl;
    private boolean infoWindowOpened = false;
    private boolean hideable;


    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getid() {
        return id;
    }

    public void setPreviewImg(String previewImg) {
        this.previewImg = previewImg;
    }

    public String getPreviewImg() {
        return previewImg;
    }

    // Binding for ImageViews
    @BindingAdapter("android:src")
    public static void setImageUri(ImageView view, String imageUri) {
        Glide.with(view.getContext())
                .load(imageUri)
                .placeholder(R.drawable.image_placeholder)
                .crossFade()
                .into(view);
    }

    public String getDropType() {
        return dropType;
    }

    public void setDropType(String dropType) {
        this.dropType = dropType;
    }


    public boolean isInfoWindowOpened() {
        return infoWindowOpened;
    }

    public void setInfoWindowOpened(boolean infoWindowOpened) {
        this.infoWindowOpened = infoWindowOpened;
    }



    public String getContentUrl() {
        return contentUrl;
    }

    public void setContentUrl(String contentUrl) {
        this.contentUrl = contentUrl;
    }



    public double getLongitude() {
        return longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public boolean isHideable() {
        return hideable;
    }

    public void setHideable(boolean hideable) {
        this.hideable = hideable;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
}

