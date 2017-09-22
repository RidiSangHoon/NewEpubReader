package com.example.leesanghoon.newepubreader.Activity;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.util.DisplayMetrics;

import com.example.leesanghoon.newepubreader.Tools.BasicProgress;

public class RootActivity extends Activity {

    BasicProgress basicProgress;

    /**
     * 시간이 걸릴때 progressBar
     *
     * @param context
     */
    protected void showProgress(Context context, String msg) {
        try {
            if (this != null) {
                if (basicProgress != null) {
                    if (basicProgress.isShowing()) {
                        basicProgress.dismiss();
                    }
                }
                BasicProgress.Builder progress = new BasicProgress.Builder(context);
                basicProgress = progress.create();
                basicProgress.setCancelable(false);
                basicProgress.setMsg(msg);
                basicProgress.show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void dismissProgress() {
        try {
            if (this != null) {
                if (basicProgress != null) {
                    if (basicProgress.isShowing()) {
                        basicProgress.dismiss();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static float convertDpToPixel(float dp, Context context){
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        float px = dp * ((float)metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT);
        return px;
    }
}
