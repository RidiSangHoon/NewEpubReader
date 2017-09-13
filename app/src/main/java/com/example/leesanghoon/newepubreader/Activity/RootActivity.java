package com.example.leesanghoon.newepubreader.Activity;

import android.app.Activity;
import android.content.Context;

import com.example.leesanghoon.newepubreader.Tools.BasicProgress;

public class RootActivity extends Activity {

    BasicProgress basicProgress;

    /**
     * 시간이 걸릴때 progressBar
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
                    if (basicProgress.isShowing())
                        basicProgress.dismiss();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
