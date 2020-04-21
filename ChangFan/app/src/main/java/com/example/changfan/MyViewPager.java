package com.example.changfan;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.viewpager.widget.ViewPager;

public class MyViewPager extends ViewPager{
    //暴露boolean变量给外界禁止滑动
    public boolean canScroll=true;
    public MyViewPager(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if(!canScroll){
            return false;
        }
        return super.onTouchEvent(ev);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if(!canScroll){
            return false;
        }
        return super.onInterceptTouchEvent(ev);
    }

    //ViewPager与DrawerLayout的冲突，通过此监听器控制
    public static class MyDrawerListener implements DrawerLayout.DrawerListener{
        private MyViewPager myViewPager;
        public MyDrawerListener(MyViewPager myViewPager){
            this.myViewPager=myViewPager;
        }
        @Override
        public void onDrawerSlide(@NonNull View drawerView, float slideOffset) {
            myViewPager.canScroll=false;
        }

        @Override
        public void onDrawerOpened(@NonNull View drawerView) {
            myViewPager.canScroll=true;
        }

        @Override
        public void onDrawerClosed(@NonNull View drawerView) {
            myViewPager.canScroll=true;
        }

        @Override
        public void onDrawerStateChanged(int newState) {

        }
    }
}