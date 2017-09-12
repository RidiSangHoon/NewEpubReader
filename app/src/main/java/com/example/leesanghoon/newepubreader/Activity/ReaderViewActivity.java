package com.example.leesanghoon.newepubreader.Activity;

import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.leesanghoon.newepubreader.Model.BookItem;
import com.example.leesanghoon.newepubreader.R;
import com.example.leesanghoon.newepubreader.Tools.ZipTool;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.channels.FileChannel;
import java.util.ArrayList;

/**
 * Created by leesanghoon on 2017. 9. 7..
 */

public class ReaderViewActivity extends RootActivity {

    BookItem currentBook;
    TextView titleText;
    ImageView backBtn;
    WebView webView;
    String contentLoc,folderPath,oebpsFilePath,opfFilePath,ncxFilePath,fullHtml = "";

    // 목차대로 저장하는 파일 리스트
    ArrayList<String> fileSeqList = new ArrayList<>();

    //cover페이지를 찾았는지 못찾았는지
    boolean coverFlag = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_readerview);

        titleText = (TextView)findViewById(R.id.bookTitleTv);
        backBtn = (ImageView)findViewById(R.id.backBtn);
        webView = (WebView)findViewById(R.id.webView);

        webView.getSettings().setJavaScriptEnabled(true);

        currentBook = (BookItem) getIntent().getParcelableExtra("bookItem");

        if(currentBook == null){
            Toast.makeText(ReaderViewActivity.this, "선택하신 책이 없습니다.", Toast.LENGTH_SHORT).show();
            finish();
        }

        //NewEpubReader 폴더 만들기
        String dirPath = Environment.getExternalStorageDirectory().toString() + "/NewEpubReader";
        File tempFile = new File(dirPath);
        if(!tempFile.exists()) {
            tempFile.mkdirs();
        }

        titleText.setText(currentBook.name);
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        if(ZipTool.unzip(currentBook.path+"/")) {
            Log.e("ReaderViewActivity","unzip Success");
        } else {
            Log.e("ReaderViewActivity","unzip Fail");
            Toast.makeText(ReaderViewActivity.this,"ePub 파일 열기에 실패하였습니다.",Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        //.epub 이기 때문에 -5
        folderPath = currentBook.path.substring(0,currentBook.path.length()-5);

        File containerXmlFile = new File(folderPath+"/META-INF/container.xml");
        if (containerXmlFile.exists()) {
            Log.e("ReaderViewActivity","file exists");
        } else {
            Log.e("ReaderViewActivity","file not exists");
            Toast.makeText(ReaderViewActivity.this,"ePub 파일 열기에 실패하였습니다.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        //container xml Parser

        try{
            FileInputStream is = new FileInputStream(containerXmlFile);

            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            XmlPullParser parser = factory.newPullParser();
            parser.setInput(new InputStreamReader(is,"UTF-8"));

            int eventType = parser.getEventType();

            while(eventType != XmlPullParser.END_DOCUMENT){
                switch(eventType){
                    case XmlPullParser.START_TAG:
                        if(parser.getName().equals("rootfile")){
                            contentLoc = parser.getAttributeValue("","full-path");
                            Log.e("ReaderViewActivity","contentLoc=>"+contentLoc);
                        }
                        break;
                    case XmlPullParser.END_TAG:
                        break;
                }
                eventType = parser.next();
            }
        }catch(Exception e){
            e.printStackTrace();
        }

        //oebps폴더 파일위치
        if(!contentLoc.contains("/")){
            oebpsFilePath = folderPath + "/";
        } else {
            oebpsFilePath = contentLoc.substring(0, contentLoc.indexOf("/"));
            oebpsFilePath = folderPath + "/" + oebpsFilePath + "/";
        }

        File[] list = new File(oebpsFilePath).listFiles();

        for(File listItem: list) {
            if(listItem.getName().endsWith(".opf")){
                opfFilePath = listItem.getAbsolutePath();
            }
            if(listItem.getName().endsWith(".ncx")){
                ncxFilePath = listItem.getAbsolutePath();
            }
        }

        //ncx File Parser
        try{
            FileInputStream is = new FileInputStream(ncxFilePath);

            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            XmlPullParser parser = factory.newPullParser();
            parser.setInput(new InputStreamReader(is,"UTF-8"));

            int eventType = parser.getEventType();

            while(eventType != XmlPullParser.END_DOCUMENT){
                switch(eventType){
                    case XmlPullParser.START_TAG:
                        if(parser.getName().equals("content")){
                            fileSeqList.add(parser.getAttributeValue("","src"));
                        }
                        break;
                    case XmlPullParser.END_TAG:
                        break;
                }
                eventType = parser.next();
            }
        }catch(Exception e){
            e.printStackTrace();
        }

        for(String f: fileSeqList){
            Log.e("ReaderViewActivity","file list => "+f);
            addHtml(f,false);
        }


        try{
            FileInputStream is = new FileInputStream(opfFilePath);

            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            XmlPullParser parser = factory.newPullParser();
            parser.setInput(new InputStreamReader(is,"UTF-8"));

            int eventType = parser.getEventType();

            //커버 페이지
            while(eventType != XmlPullParser.END_DOCUMENT) {
                switch (eventType) {
                    case XmlPullParser.START_TAG:
                        for (int i = 0; i < parser.getAttributeCount(); i++) {
                            //커버 페이지
                            if (parser.getAttributeValue("", parser.getAttributeName(i)).endsWith("cover.xhtml") && !coverFlag) {
                                coverFlag = true;
                                addHtml(parser.getAttributeValue("", parser.getAttributeName(i)), true);
                            }
                            //이미지나 css
                            if(parser.getAttributeValue("",parser.getAttributeName(i)).endsWith(".jpg") ||
                                    parser.getAttributeValue("",parser.getAttributeName(i)).endsWith(".png") ||
                                    parser.getAttributeValue("",parser.getAttributeName(i)).endsWith(".gif") ||
                                    parser.getAttributeValue("",parser.getAttributeName(i)).endsWith(".css")) {
                                createDirectory(parser.getAttributeValue("",parser.getAttributeName(i)));
                                String filename = "/NewEpubReader/"+parser.getAttributeValue("",parser.getAttributeName(i));
                                File srcFile = new File(oebpsFilePath+parser.getAttributeValue("",parser.getAttributeName(i)));
                                File destFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath()+filename);
                                try{
                                    copyFile(srcFile,destFile);
                                }catch(Exception e){
                                    e.printStackTrace();
                                }
                                fullHtml = fullHtml.replaceAll("href=\"" + parser.getAttributeValue("", parser.getAttributeName(i)) + "\"", "href=\"file://" + Environment.getExternalStorageDirectory().getAbsolutePath() + filename + "\"");
                                fullHtml = fullHtml.replaceAll("href=\"../" + parser.getAttributeValue("", parser.getAttributeName(i)) + "\"", "href=\"file://" + Environment.getExternalStorageDirectory().getAbsolutePath() + filename + "\"");
                                fullHtml = fullHtml.replaceAll("src=\"" + parser.getAttributeValue("", parser.getAttributeName(i)) + "\"", "src=\"file://" + Environment.getExternalStorageDirectory().getAbsolutePath() + filename + "\"");
                                fullHtml = fullHtml.replaceAll("src=\"../" + parser.getAttributeValue("", parser.getAttributeName(i)) + "\"", "src=\"file://" + Environment.getExternalStorageDirectory().getAbsolutePath() + filename + "\"");

                            }
                        }
                        break;
                    case XmlPullParser.END_TAG:
                        break;
                }
                eventType = parser.next();
            }
        }catch(Exception e){
            e.printStackTrace();
        }

        webView.loadDataWithBaseURL("file:///android_asset/",fullHtml,"text/html","utf-8",null);
    }

    // 링크에 폴더를 포함하는 경우 처리
    public void createDirectory(String path) {
        Log.e("ReaderViewActivity","path => "+path);
        String currentPath = Environment.getExternalStorageDirectory().getAbsolutePath()+"/NewEpubReader";
        if(path.contains("/")) {
            String str[] = path.split("/");
            for(int i = 0;i<str.length-1;i++){ // 마지막은 파일이므로 -1
                currentPath = currentPath + "/"+str[i];
                File tempFile = new File(currentPath);
                if(!tempFile.exists()) {
                    tempFile.mkdirs();
                }
            }
        } else {
            return;
        }
    }

    //파일 복사하기
    void copyFile(File src, File dst) throws IOException {
        if(!dst.getParentFile().exists()){
            dst.getParentFile().mkdirs();
        }
        if(!dst.exists()){
            dst.createNewFile();
        }
        FileChannel inChannel = new FileInputStream(src).getChannel();
        FileChannel outChannel = new FileOutputStream(dst).getChannel();
        try {
            inChannel.transferTo(0, inChannel.size(), outChannel);
        } finally {
            if (inChannel != null)
                inChannel.close();
            if (outChannel != null)
                outChannel.close();
        }
    }

    //cover페이지인지 여부에 따라 html에 추가해주는 함수
    public void addHtml(String filename, boolean cover) {
        File htmlFile = new File(oebpsFilePath+filename);
        if(htmlFile.exists()){
            try {
                FileInputStream fileInputStream = new FileInputStream(htmlFile);
                StringBuffer buffer = new StringBuffer();
                byte[] b = new byte[4096];

                int i;
                while((i = fileInputStream.read(b)) != -1) {
                    buffer.append(new String(b,0,i));
                }
                if(cover) {
                    fullHtml = buffer.toString()+fullHtml;
                } else {
                    fullHtml = fullHtml + buffer.toString();
                }
            }catch(Exception e){
                e.printStackTrace();
            }
        }
    }
}
