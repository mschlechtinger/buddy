package com.example.d062589.partylive.Models;

import android.databinding.BaseObservable;
import android.databinding.BindingAdapter;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.example.d062589.partylive.R;

/**
 * Created by D062589 on 10.03.2017.
 */

public class Party extends BaseObservable {
    private String _id;
    private boolean publicParty;
    private String title;
    private String imgUrl;
    private String[] guestImgs;
    private int guestCount;
    private Organizer organizer;
    private Location location;
    private String description;

    public String getTitle() {
        return title;
    }

    public String get_id() {
        return _id;
    }

    public void setTitle(String title) {
        this.title = title;
    }


    public String getImgUrl() {
        return imgUrl;
    }

    // Binding for ImageViews
    @BindingAdapter("android:src")
    public static void setImageUri(ImageView view, String imageUri) {
        //view.setImageURI(Uri.parse(imageUri));
        // TODO: change to use backend information
        //String resource = "android.resource://com.example.d062589.partylive/drawable/" + imageUri;
        Glide.with(view.getContext())
                .load(imageUri)
                .thumbnail(Glide.with(view.getContext())
                        .load(R.drawable.loading_spinner)) //load gif as thumbnail
                .placeholder(R.drawable.image_placeholder)
                .crossFade()
                .into(view);
    }


    public int getGuestCount() {
        return guestCount;
    }

    public String[] getGuestImgs() {
        return guestImgs;
    }

    public boolean isPublicParty() {
        return publicParty;
    }


    // Subclass Organizer
    public Organizer getOrganizer() {
        return organizer;
    }

    public String getDescription() {
        return description;
    }

    public class Organizer {
        private String id;
        private String name;
        private String imgUrl;

        public String getName() {
            return name;
        }

        public String getImgUrl() {
            return imgUrl;
        }

        public String getId() {
            return id;
        }
    }


    // Subclass Location
    public Location getLocation() {
        return location;
    }

    public class Location {
        private double latitude;
        private double longitude;

        public double getLongitude() {
            return longitude;
        }

        public double getLatitude() {
            return latitude;
        }
    }
}

