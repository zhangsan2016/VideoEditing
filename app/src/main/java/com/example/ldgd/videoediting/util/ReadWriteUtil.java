package com.example.ldgd.videoediting.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by ldgd on 2019/7/12.
 * 功能：当前应用读写工具类
 * 说明：
 */

public class ReadWriteUtil {


    /**
     *  写入json本地文件
     * @param json
     * @param filePath
     */
    public static void writeStringToFile(String json, String filePath) {
        File txt = new File(filePath);
        if (!txt.exists()) {
            try {
                txt.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        byte[] bytes = json.getBytes(); //新加的
        int b = json.length(); //改
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(txt);
            fos.write(bytes);
            fos.flush();
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }





}
