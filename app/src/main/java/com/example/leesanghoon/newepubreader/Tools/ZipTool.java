package com.example.leesanghoon.newepubreader.Tools;

import android.util.Log;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class ZipTool {

    //압축 해제 하는 함수
    public static boolean unzip(String path) {
        InputStream is;
        ZipInputStream zis;
        try {
            String filename;
            is = new FileInputStream(path);
            zis = new ZipInputStream(new BufferedInputStream(is));
            ZipEntry ze;
            byte[] buffer = new byte[1024];
            int count;

            path = path.substring(0,path.length()-6);

            while ((ze = zis.getNextEntry()) != null) {
                filename = ze.getName();

                Log.e("ZipTool","ze.getName() => "+ze.getName());

                if (ze.isDirectory()) {
                    File fmd = new File(path, filename);
                    fmd.mkdirs();
                    continue;
                } else {
                    File fmd = new File(path, filename);
                    Log.d("Unzipping", fmd.getParentFile().getPath());
                    String parent = fmd.getParentFile().getPath();

                    File fmd_1 = new File(parent);
                    fmd_1.mkdirs();
                }

                FileOutputStream fout = new FileOutputStream(path+ "/" +filename);

                while ((count = zis.read(buffer)) != -1) {
                    fout.write(buffer, 0, count);
                }

                fout.close();
                zis.closeEntry();
            }

            zis.close();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
}
