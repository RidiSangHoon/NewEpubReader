package com.example.leesanghoon.newepubreader.Activity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.leesanghoon.newepubreader.Model.BookItem;
import com.example.leesanghoon.newepubreader.R;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.List;

import nl.siegmann.epublib.domain.Book;
import nl.siegmann.epublib.domain.MediaType;
import nl.siegmann.epublib.domain.Resource;
import nl.siegmann.epublib.epub.EpubReader;
import nl.siegmann.epublib.service.MediatypeService;

/**
 * Created by leesanghoon on 2017. 9. 7..
 */

public class ReaderViewActivity extends RootActivity {

    BookItem currentBook;
    TextView titleText;
    ImageView backBtn;
    ViewPager viewPager;
    WebView webView;

    int currentPosition = 0 ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_readerview);

        backBtn = (ImageView)findViewById(R.id.backBtn);
        titleText = (TextView)findViewById(R.id.bookTitleTv);

        webView = (WebView)findViewById(R.id.webView);
        webView.getSettings().setJavaScriptEnabled(true);


        currentBook = (BookItem)getIntent().getParcelableExtra("bookItem");
        if(currentBook == null) {
            Toast.makeText(ReaderViewActivity.this,"선택하신 책이 없습니다.",Toast.LENGTH_SHORT).show();
            finish();
        }

        Log.e("ReaderViewActivity","Book name => "+currentBook.name);
        Log.e("ReaderViewActivity","Book path => "+currentBook.path);

        titleText.setText(currentBook.name);
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        File file = new File(currentBook.path);
        if(!file.exists()){
            Toast.makeText(ReaderViewActivity.this,"파일이 존재하지 않습니다.",Toast.LENGTH_SHORT).show();
            finish();
        }
        try {
            EpubReader epubReader = new EpubReader();
            Book book = epubReader.readEpub(new FileInputStream(currentBook.path));

            Log.e("ReaderViewActivity","book Content Size => "+book.getContents().size());
            String fullHtml = "";

            for(int j=0;j<book.getContents().size();j++) {
                InputStream inputStream = book.getContents().get(j).getInputStream();


                StringBuffer buffer = new StringBuffer();
                byte[] b = new byte[4096];

                int i;
                while ((i = inputStream.read(b)) != -1) {
                    buffer.append(new String(b, 0, i));
                }
                fullHtml = fullHtml + buffer.toString();
            }

            // 웹뷰에서 이미지 안나오는것들 처리
            MediaType[] bitmapTypes = {MediatypeService.PNG, MediatypeService.GIF, MediatypeService.JPG};
            List<Resource> bitmapResources = book.getResources().getResourcesByMediaTypes(bitmapTypes);

            for(Resource r: bitmapResources) {
                Bitmap bm = BitmapFactory.decodeByteArray(r.getData(),0,r.getData().length);
                FileOutputStream out = null;
                try{
                    String dirPath = Environment.getExternalStorageDirectory().toString() + "/NewEpubReader";
                    File tempFile = new File(dirPath);
                    if(!tempFile.exists()) {
                        tempFile.mkdirs();
                    }
                    String filename = "/NewEpubReader/"+r.getHref();
                    FileOutputStream fos = new FileOutputStream(Environment.getExternalStorageDirectory().toString()+filename);
                    bm.compress(Bitmap.CompressFormat.JPEG,100,fos);
                    fullHtml = fullHtml.replace("<img src=\""+r.getHref()+"\"/>","<img src=\"file://"+Environment.getExternalStorageDirectory().getAbsolutePath()+filename+"\"/>");

                } catch(Exception e){
                    e.printStackTrace();
                }
            }

            Log.e("ReaderViewActivity","After Html => "+fullHtml);
//            webView.loadData(fullHtml,"text/html; charset=UTF-8",null);
            webView.loadDataWithBaseURL("file:///android_asset/",fullHtml,"text/html","utf-8",null);
        } catch (Exception e) {
            Toast.makeText(ReaderViewActivity.this,"정상적인 ePub 파일이 아닙니다.",Toast.LENGTH_SHORT).show();
            e.printStackTrace();
            finish();
        }
    }

}
