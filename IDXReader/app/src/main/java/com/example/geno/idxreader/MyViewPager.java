package com.example.geno.idxreader;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * Created by geno on 17/05/18.
 */

public class MyViewPager extends ViewPager {
    public MyViewPager(@NonNull Context context) {
        super(context);
    }

    public MyViewPager(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }
//    public Boolean isCanScroll = true;
//
//    public MyViewPager(@NonNull Context context, Boolean isCanScroll) {
//        super(context);
//        this.isCanScroll = isCanScroll;
//    }
//
//    public MyViewPager(@NonNull Context context, @Nullable AttributeSet attrs, Boolean isCanScroll) {
//        super(context, attrs);
//        this.isCanScroll = isCanScroll;
//    }
//
//    public void setCanScroll(Boolean canScroll) {
//        isCanScroll = canScroll;
//    }
//
//    @Override
//    public boolean onTouchEvent(MotionEvent ev) {
//        if (!isCanScroll)return false;
//        return super.onTouchEvent(ev);
//    }
//
//    @Override
//    public boolean onInterceptTouchEvent(MotionEvent ev) {
//        if (!isCanScroll)return false;
//        return super.onInterceptTouchEvent(ev);
//    }
}
