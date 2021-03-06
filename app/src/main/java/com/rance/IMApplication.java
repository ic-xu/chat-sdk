package com.rance;

import android.app.Application;
import android.content.Context;
import android.util.DisplayMetrics;

import androidx.multidex.MultiDex;

/**
 * 作者：Rance on 2016/12/20 16:49
 * 邮箱：rance935@163.com
 */
public class IMApplication extends Application {
    private static IMApplication mInstance;
    public static Context mContext;
    /**
     * 屏幕宽度
     */
    public static int screenWidth;
    /**
     * 屏幕高度
     */
    public static int screenHeight;
    /**
     * 屏幕密度
     */
    public static float screenDensity;

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = getApplicationContext();
        mInstance = this;
        initScreenSize();
        MultiDex.install(this);
    }

    public static Context getInstance() {
        return mInstance;
    }

    /**
     * 初始化当前设备屏幕宽高
     */
    private void initScreenSize() {
        DisplayMetrics curMetrics = getApplicationContext().getResources().getDisplayMetrics();
        screenWidth = curMetrics.widthPixels;
        screenHeight = curMetrics.heightPixels;
        screenDensity = curMetrics.density;
    }
}
