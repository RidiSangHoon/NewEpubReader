package com.example.leesanghoon.newepubreader.Tools;

import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;

/**
 * Created by leesanghoon on 2017. 9. 20..
 */

public class JSProcessChromeClient extends WebChromeClient {

    private int dataY;

    @JavascriptInterface
    public void saveData(int y) {
        Log.e("JSProcessChromeClient", "save Data => " + y);
        dataY = y;
    }

    public int getScrollY(){
        return dataY;
    }
}
