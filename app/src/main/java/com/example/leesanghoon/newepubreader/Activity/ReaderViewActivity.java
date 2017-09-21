package com.example.leesanghoon.newepubreader.Activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
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
import java.util.HashMap;

public class ReaderViewActivity extends RootActivity {

    private String contentLoc, oebpsFilePath, opfFilePath;

    // 목차대로 저장하는 파일 리스트
    private ArrayList<String> fileSeqList = new ArrayList<>();
    private ArrayList<String> htmlList = new ArrayList<>();
    private LinearLayout readerView;
    private BookItem currentBook;
    private String folderPath, dirPath = "";
    private File containerXmlFile;
    private HashMap<String, String> idHrefMap = new HashMap<>();
    private int finishWebViewCnt = 0;
    private ScrollView scrollView;
    private WebView[] webViews;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_readerview);

        init();
        openEpub();
        getPaths();
        getSpine();

        for (String f : fileSeqList) {
            addHtml(f);
        }

        showProgress(ReaderViewActivity.this, "ePub 파일을 읽는 중입니다.");

        BackThread thread = new BackThread();
        thread.start();
    }

    //스레드를 만들어 다 읽어올때까지 progressBar를 띄운다.
    private class BackThread extends Thread {
        @Override
        public void run() {
            saveImgCssFile();
            webViews = new WebView[htmlList.size()];
            for (final String htmlItem : htmlList) {
                new Handler(getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        final WebView webView = new WebView(ReaderViewActivity.this);

                        webView.getSettings().setJavaScriptEnabled(true);

                        webView.loadDataWithBaseURL("file:///android_asset/", htmlItem, "text/html",
                                "utf-8",
                                null);
                        webView.setLayoutParams(new LinearLayout.LayoutParams(
                                FrameLayout.LayoutParams.WRAP_CONTENT,
                                FrameLayout.LayoutParams.WRAP_CONTENT));
                        webView.setWebViewClient(new WebViewClient() {
                            @Override
                            public void onPageFinished(WebView view, String url) {
                                if (finishWebViewCnt < htmlList.size()) {
                                    webViews[finishWebViewCnt++] = webView;
                                }
                                if (finishWebViewCnt == htmlList.size()) {
                                    // 다 읽어온 것이므로
                                    dismissProgress();
                                }
                            }

                            @Override
                            public boolean shouldOverrideUrlLoading(WebView view,
                                                                    WebResourceRequest request) {
                                Log.e("ReaderViewActivity", "should override url loading");
                                final String url = request.getUrl().toString();
                                int scrollHeight = 0;
                                if (url.startsWith("http")) {
                                    //외부 웹 실행
                                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                                    startActivity(intent);
                                } else {
                                    //내부 링크 이동 시도 (목차 기능)
                                    Log.e("ReaderViewActivity", "request url => " + url);
                                    int sharpIndex = url.indexOf("#");
                                    if (sharpIndex == -1) {
                                        for (int i = 0; i < fileSeqList.size(); i++) {
                                            if (("file://" + dirPath + "/" + fileSeqList.get(
                                                    i)).equals(url)) {
                                                scrollHeight = (int) webViews[i].getY();
                                                break;
                                            }
                                        }
                                        Log.e("ReaderViewActivity",
                                                "scroll Height => " + scrollHeight);
                                        scrollView.smoothScrollTo(0, scrollHeight);
                                    } else {
                                        final String link =
                                                url.substring(sharpIndex + 1, url.length());
                                        final String exceptLink = url.substring(0, sharpIndex);
                                        int i;
                                        for (i = 0; i < fileSeqList.size(); i++) {
                                            if (("file://" + dirPath + "/" + fileSeqList.get(
                                                    i)).equals(exceptLink)) {
                                                scrollHeight = (int) webViews[i].getY();
                                                break;
                                            }
                                        }
                                        scrollView.smoothScrollTo(0, scrollHeight);
                                    }

                                }
                                return true;
                            }
                        });
                        readerView.addView(webView);
                    }
                });
            }
        }
    }

    private void init() {
        TextView titleText = findViewById(R.id.book_title);
        ImageView backBtn = findViewById(R.id.back_button);

        currentBook = getIntent().getParcelableExtra("bookItem");
        readerView = findViewById(R.id.reader_view);
        scrollView = findViewById(R.id.scroll_view);

        if (currentBook == null) {
            Toast.makeText(ReaderViewActivity.this, "선택하신 책이 없습니다.", Toast.LENGTH_SHORT).show();
            finish();
        }

        //NewEpubReader 폴더 만들기
        dirPath = Environment.getExternalStorageDirectory().toString() + "/NewEpubReader";
        File tempFile = new File(dirPath);
        if (!tempFile.exists()) {
            tempFile.mkdirs();
        }

        titleText.setText(currentBook.name);
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    // 링크에 폴더를 포함하는 경우 처리
    private void createDirectory(String path) {
        String currentPath =
                Environment.getExternalStorageDirectory().getAbsolutePath() + "/NewEpubReader";
        if (path.contains("/")) {
            String str[] = path.split("/");
            for (int i = 0; i < str.length - 1; i++) { // 마지막은 파일이므로 -1
                currentPath = currentPath + "/" + str[i];
                File tempFile = new File(currentPath);
                if (!tempFile.exists()) {
                    tempFile.mkdirs();
                }
            }
        } else {
            return;
        }
    }

    //파일 복사하기
    private void copyFile(File src, File dst) throws IOException {
        if (!dst.getParentFile().exists()) {
            dst.getParentFile().mkdirs();
        }
        if (!dst.exists()) {
            dst.createNewFile();
        }
        FileChannel inChannel = new FileInputStream(src).getChannel();
        FileChannel outChannel = new FileOutputStream(dst).getChannel();
        try {
            inChannel.transferTo(0, inChannel.size(), outChannel);
        } finally {
            if (inChannel != null) {
                inChannel.close();
            }
            if (outChannel != null) {
                outChannel.close();
            }
        }
    }

    //html에 추가해주는 함수
    private void addHtml(String filename) {
        File htmlFile = new File(oebpsFilePath + filename);
        String fullHtml = "";
        if (htmlFile.exists()) {
            try {
                FileInputStream fileInputStream = new FileInputStream(htmlFile);
                StringBuffer buffer = new StringBuffer();
                byte[] b = new byte[4096];

                int i;
                while ((i = fileInputStream.read(b)) != -1) {
                    buffer.append(new String(b, 0, i));
                }
                fullHtml = fullHtml + buffer.toString();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        htmlList.add(fullHtml);
    }

    // epub 파일 열기
    private void openEpub() {
        if (!ZipTool.unzip(currentBook.path + "/")) {
            Log.e("ReaderViewActivity", "unzip Fail");
            Toast.makeText(ReaderViewActivity.this, "ePub 파일 열기에 실패하였습니다.", Toast.LENGTH_SHORT)
                    .show();
            finish();
            return;
        }

        //.epub 이기 때문에 -5
        folderPath = currentBook.path.substring(0, currentBook.path.length() - 5);

        containerXmlFile = new File(folderPath + "/META-INF/container.xml");
        if (!containerXmlFile.exists()) {
            Log.e("ReaderViewActivity", "file not exists");
            Toast.makeText(ReaderViewActivity.this, "ePub 파일 열기에 실패하였습니다.", Toast.LENGTH_SHORT)
                    .show();
            finish();
            return;
        }
    }

    //oebsFilePath, opfFilePath 구하기
    private void getPaths() {
        try {
            FileInputStream is = new FileInputStream(containerXmlFile);
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            XmlPullParser parser = factory.newPullParser();
            parser.setInput(new InputStreamReader(is, "UTF-8"));

            int eventType = parser.getEventType();

            while (eventType != XmlPullParser.END_DOCUMENT) {
                switch (eventType) {
                    case XmlPullParser.START_TAG:
                        if (parser.getName().equals("rootfile")) {
                            contentLoc = parser.getAttributeValue("", "full-path");
                        }
                        break;
                    case XmlPullParser.END_TAG:
                        break;
                }
                eventType = parser.next();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        //oebps폴더 파일위치
        if (!contentLoc.contains("/")) {
            oebpsFilePath = folderPath + "/";
        } else {
            oebpsFilePath = contentLoc.substring(0, contentLoc.indexOf("/"));
            oebpsFilePath = folderPath + "/" + oebpsFilePath + "/";
        }
        Log.e("ReaderViewAcitivity", "oebpsFilePath => " + oebpsFilePath);

        File[] list = new File(oebpsFilePath).listFiles();

        for (File listItem : list) {
            if (listItem.getName().endsWith(".opf")) {
                opfFilePath = listItem.getAbsolutePath();
            }
        }
    }

    // Img 나 Css File 저장하기
    private void saveImgCssFile() {
        try {
            FileInputStream is = new FileInputStream(opfFilePath);
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            XmlPullParser parser = factory.newPullParser();
            parser.setInput(new InputStreamReader(is, "UTF-8"));

            int eventType = parser.getEventType();

            while (eventType != XmlPullParser.END_DOCUMENT) {
                switch (eventType) {
                    case XmlPullParser.START_TAG:
                        for (int i = 0; i < parser.getAttributeCount(); i++) {
                            //이미지나 css
                            if (parser.getAttributeValue("", parser.getAttributeName(i))
                                    .endsWith(".jpg") ||
                                    parser.getAttributeValue("", parser.getAttributeName(i))
                                            .endsWith(".png") ||
                                    parser.getAttributeValue("", parser.getAttributeName(i))
                                            .endsWith(".gif") ||
                                    parser.getAttributeValue("", parser.getAttributeName(i))
                                            .endsWith(".css") ||
                                    parser.getAttributeValue("", parser.getAttributeName(i))
                                            .endsWith(".html") ||
                                    parser.getAttributeValue("", parser.getAttributeName(i))
                                            .endsWith(".xhtml")) {
                                createDirectory(
                                        parser.getAttributeValue("", parser.getAttributeName(i)));
                                String filename = "/NewEpubReader/" + parser.getAttributeValue("",
                                        parser.getAttributeName(i));
                                File srcFile = new File(oebpsFilePath + parser.getAttributeValue("",
                                        parser.getAttributeName(i)));
                                File destFile = new File(
                                        Environment.getExternalStorageDirectory().getAbsolutePath()
                                                + filename);
                                try {
                                    copyFile(srcFile, destFile);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                for (String htmlItem : htmlList) {
                                    int index = htmlList.indexOf(htmlItem);
                                    htmlItem = htmlItem.replaceAll(
                                            "href=\"" + parser.getAttributeValue("",
                                                    parser.getAttributeName(i)),
                                            "href=\"file://"
                                                    + Environment.getExternalStorageDirectory()
                                                    .getAbsolutePath() + filename);
                                    htmlItem = htmlItem.replaceAll(
                                            "href=\"../" + parser.getAttributeValue("",
                                                    parser.getAttributeName(i)),
                                            "href=\"file://"
                                                    + Environment.getExternalStorageDirectory()
                                                    .getAbsolutePath() + filename);
                                    htmlItem = htmlItem.replaceAll(
                                            "src=\"" + parser.getAttributeValue("",
                                                    parser.getAttributeName(i)),
                                            "src=\"file://"
                                                    + Environment.getExternalStorageDirectory()
                                                    .getAbsolutePath() + filename);
                                    htmlItem = htmlItem.replaceAll(
                                            "src=\"../" + parser.getAttributeValue("",
                                                    parser.getAttributeName(i)),
                                            "src=\"file://"
                                                    + Environment.getExternalStorageDirectory()
                                                    .getAbsolutePath() + filename);
                                    htmlList.set(index, htmlItem);
                                }
                            }
                        }
                        break;
                    case XmlPullParser.END_TAG:
                        break;
                }
                eventType = parser.next();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //Spine을 가져와서 순서대로 fileSeqList에 추가해주는 함수
    private void getSpine() {
        try {
            FileInputStream is = new FileInputStream(opfFilePath);
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            XmlPullParser parser = factory.newPullParser();
            parser.setInput(new InputStreamReader(is, "UTF-8"));

            int eventType = parser.getEventType();
            int spineFlag = 0, itemFlag = 0;

            while (eventType != XmlPullParser.END_DOCUMENT) {
                switch (eventType) {
                    case XmlPullParser.START_TAG:
                        if (parser.getName().equalsIgnoreCase("spine") || spineFlag == 1) {
                            spineFlag = 1;
                            if (parser.getAttributeValue("", "idref") != null) {
                                fileSeqList.add(
                                        idHrefMap.get(parser.getAttributeValue("", "idref")));
                            }
                        }
                        if (parser.getName().equalsIgnoreCase("item") || itemFlag == 1) {
                            itemFlag = 1;
                            idHrefMap.put(parser.getAttributeValue("", "id"),
                                    parser.getAttributeValue("", "href"));
                        }
                        break;
                    case XmlPullParser.END_TAG:
                        if (parser.getName().equalsIgnoreCase("spine")) {
                            spineFlag = 0;
                        }
                        if (parser.getName().equalsIgnoreCase("item")) {
                            itemFlag = 0;
                        }
                        break;
                }
                eventType = parser.next();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
