package com.example.tswords.livewallpaper.Util;

import android.app.Activity;
import android.app.Application;
import android.content.Context;

/**
 * Created by Tswords on 2017/5/15.
 */

public class GlobalContext extends Application {
    private static Context context;
    public static String file;
    public static int type;
    public static void setMainActivity(Activity mainActivity) {
        GlobalContext.mainActivity = mainActivity;
    }

    public static Activity getMainActivity() {
        return mainActivity;
    }

    private static Activity mainActivity;
    public void onCreate(){
        super.onCreate();
        context=getApplicationContext();

    }
    public static Context getContext(){
        return context;
    }
}
