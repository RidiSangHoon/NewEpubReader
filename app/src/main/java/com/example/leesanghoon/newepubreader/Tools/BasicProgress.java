package com.example.leesanghoon.newepubreader.Tools;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.leesanghoon.newepubreader.R;

/**
 * Created by leesanghoon on 2017. 9. 6..
 */

public class BasicProgress extends Dialog {

    public BasicProgress(Context context, int theme) {
        super(context, theme);
    }

    public BasicProgress(Context context) {
        super(context);
    }

    public void setMsg(String msg) {
        TextView progressText = (TextView) findViewById(R.id.progress_text);
        progressText.setText(msg);
    }

    public static class Builder {

        private Context mContext;

        public Builder(Context context) {
            mContext = context;
        }

        public BasicProgress create() {
            final BasicProgress dialog = new BasicProgress(mContext, R.style.DialogStyle);
            LayoutInflater inflater = LayoutInflater.from(mContext);
            View layout = inflater.inflate(R.layout.view_base_progress, null);
            dialog.addContentView(layout, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            return dialog;
        }
    }

}