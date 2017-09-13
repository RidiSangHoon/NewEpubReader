package com.example.leesanghoon.newepubreader.Activity;

import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.example.leesanghoon.newepubreader.Adapter.MainAdapter;
import com.example.leesanghoon.newepubreader.Model.BookItem;
import com.example.leesanghoon.newepubreader.R;

import java.io.File;
import java.util.ArrayList;

public class MainActivity extends RootActivity {

    private ArrayList<BookItem> bookList;
    private RecyclerView bookListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        showProgress(MainActivity.this, "폰에서 모든 ePub파일들을 가져오고 있습니다.\n잠시만 기다려주세요.");

        bookList = new ArrayList<>();
        bookListView = findViewById(R.id.book_list_view);

        BackThread thread = new BackThread();
        thread.start();
    }

    /**
     * Thread를 만들어 폰에 있는 모든 epub파일을 가져온다.
     */

    private class BackThread extends Thread {
        @Override
        public void run() {
            searchAllFiles(Environment.getExternalStorageDirectory());
        }
    }

    private void searchAllFiles(File fileList) {
        File[] list = fileList.listFiles();
        if (list == null) {
            return;
        }

        for (File file : list) {
            if (file.isDirectory()) {
                searchAllFiles(file);
            }
            if (file.getName().endsWith(".epub")) {
                bookList.add(new BookItem(file.getName(), file.getAbsolutePath()));
                Log.e("MainActivity", file.getName());
            }
        }

        if (fileList.getName().equals(Environment.getExternalStorageDirectory().getName())) {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    dismissProgress();
                    bookListView.setAdapter(new MainAdapter(bookList));
                    bookListView.setLayoutManager(new LinearLayoutManager(MainActivity.this));
                }
            });
        }
    }
}