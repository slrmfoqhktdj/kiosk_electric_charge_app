package com.example.kiosk_electric_charger.adapter;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.MotionEventCompat;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import org.w3c.dom.Attr;

import java.util.ArrayList;
import java.util.List;


public class SwipeDisabledViewPager extends ViewPager {


    public SwipeDisabledViewPager(@NonNull Context context) {
        super(context);
    }

    public SwipeDisabledViewPager(@NonNull Context context, AttributeSet attr) {
        super(context,attr);
    }


    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {



        switch (ev.getAction()){
            case MotionEvent.ACTION_DOWN:
//                Log.d("ViewPager","InterceptTouch Event Action_Down");
                break;
            case MotionEvent.ACTION_UP:
//                Log.d("ViewPager","InterceptTouch Event Action_Down");
                break;
            case MotionEvent.ACTION_CANCEL:
//                Log.d("ViewPager","InterceptTouch Event Action_Down");
                break;
        }


        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        Log.d("ViewPager","Touch Event");
        return false;
    }

}
