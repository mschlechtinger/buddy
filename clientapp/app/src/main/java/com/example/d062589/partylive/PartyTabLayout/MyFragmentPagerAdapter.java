package com.example.d062589.partylive.PartyTabLayout;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ImageSpan;

import com.example.d062589.partylive.R;

/**
 * Created by D062589 on 21.03.2017.
 */

public class MyFragmentPagerAdapter extends FragmentPagerAdapter {
    final int PAGE_COUNT = 3;
    private String tabTitles[] = new String[] { "Guestlist", "Bringalong", "Info" };
    private int[] imageResId = {
            R.drawable.ic_people_white_24dp,
            R.drawable.ic_shopping_cart_white_24dp,
            R.drawable.ic_info_white_24dp
    };


    private Context context;

    public MyFragmentPagerAdapter(FragmentManager fm, Context context) {
        super(fm);
        this.context = context;
    }

    @Override
    public int getCount() {
        return PAGE_COUNT;
    }


    @Override
    public Fragment getItem(int position) {
        switch(position) {
            case 0:
                return GuestListFragment.newInstance();
            case 1:
                return BringalongListFragment.newInstance();
            case 3:
                return InfoListFragment.newInstance();
            default:
                return InfoListFragment.newInstance();
        }
    }

    /**
     * Render TabLayout headers with images
     * @param position position in array
     * @return SpannableString containing the image and description
     */
    @Override
    public CharSequence getPageTitle(int position) {
        // Generate title based on item position
        Drawable image = context.getResources().getDrawable(imageResId[position]);
        image.setBounds(0, 0, image.getIntrinsicWidth(), image.getIntrinsicHeight());
        // Replace blank spaces with image icon
        SpannableString sb = new SpannableString("   " + tabTitles[position]);
        ImageSpan imageSpan = new ImageSpan(image, ImageSpan.ALIGN_BOTTOM);
        sb.setSpan(imageSpan, 0, 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        return sb;
    }

}