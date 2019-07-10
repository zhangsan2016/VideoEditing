package com.example.ldgd.videoediting.util;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;

/**
 * Created by ldgd on 2019/6/25.
 * 功能：
 * 说明：
 */

public class FipUtil {

    //ftp对象
    private FTPClient ftp;
    //需要连接到的ftp端的ip
    private String ip;
    //连接端口，默认21
    private int port;
    //要连接到的ftp端的名字
    private String name;
    //要连接到的ftp端的对应得密码
    private String pwd;

    //调用此方法，输入对应得ip，端口，要连接到的ftp端的名字，要连接到的ftp端的对应得密码。连接到ftp对象，并验证登录进入fto
    public FipUtil(String ip, int port, String name, String pwd) {
        ftp = new FTPClient();
        this.ip = ip;
        this.port = port;
        this.name = name;
        this.pwd = pwd;

        //验证登录
        try {
            ftp.connect(ip, port);
            ftp.login(name, pwd);
            int replyCode = ftp.getReplyCode();


            if (!FTPReply.isPositiveCompletion(replyCode)) {
                ftp.disconnect();
                System.out.println("FTP连接失败");
            } else {
                System.out.println("FTP连接成功");
            }

            ftp.setCharset(Charset.forName("UTF-8"));
            ftp.setControlEncoding("UTF-8");
            // 设置文件类型（二进制）
            ftp.setFileType(FTPClient.BINARY_FILE_TYPE);
            ftp.setBufferSize(1024);


        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    //获取ftp某一文件（路径）下的文件名字,用于查看文件列表
    public void getFilesName() {
        try {
            //获取ftp里面，“Windows”文件夹里面的文件名字，存入数组中
            FTPFile[] files = ftp.listFiles("/Windows");
            //打印出ftp里面，“Windows”文件夹里面的文件名字
            for (int i = 0; i < files.length; i++) {
                System.out.println(files[i].getName());
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }


    public void putFile() {
        try {

            OutputStream os = ftp.storeFileStream("configs/configs.json");
            FileInputStream fis = new FileInputStream(new File("F:/configs.json"));

            byte[] b = new byte[1024];
            int len = 0;
            while ((len = fis.read(b)) != -1) {
                os.write(b, 0, len);
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }


    /**
     * 下载文件,优化了传输速度
     * @param ftpPath  服务器中的目录地址
     * @param savePath  保存在app中的目录地址
     */
    public void getFile(String ftpPath,String savePath) {
        try {
            InputStream is = ftp.retrieveFileStream(ftpPath);
            FileOutputStream fos = new FileOutputStream(new File(savePath));

            if (is == null) {
                is.close();
                return;
            }

            byte[] b = new byte[1024];
            int len = 0;
            while ((len = is.read(b)) != -1) {
                fos.write(b, 0, len);
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }


    /**
     * 清除登录状态
     */
    public void clear() {
        try {
            if (!ftp.completePendingCommand()) {
                ftp.logout();
                ftp.disconnect();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String args[]) throws ClassNotFoundException, IOException {
/*        FipUtil m = new FipUtil("192.168.1.111", 21, "chenquan", "ch0070165194");

        // m.putFile();
        // m.putFile2();
        //m.getFile();
        m.getFile("configs/configs.json");
        //    m.putFile();*/




     /*   FTPClient ftpClient = new FTPClient();
        ftpClient.setControlEncoding("UTF-8");
        ftpClient.connect("192.168.1.111",21);
        ftpClient.login("chenquan", "ch0070165194");
        int replyCode = ftpClient.getReplyCode();
        ftpClient.setDataTimeout(120000);
        ftpClient.setFileType(FTP.BINARY_FILE_TYPE);//设置为二进制文件

        if (!FTPReply.isPositiveCompletion(replyCode)) {
            ftpClient.disconnect();
            System.out.println("FTP连接失败");
        }else {
            System.out.println("FTP连接成功");
        }
        //同理，假如指定不存在的路径，会去根路径下查找
//        ftpClient.changeWorkingDirectory("test2");
        File file=new File("F://abc4.txt");
        FileOutputStream fos=new FileOutputStream(file);
        boolean result = ftpClient.retrieveFile("configs/201905140001.json",fos);
        if(result) {
            System.out.println("下载成功!");
        }else {
            System.out.println("下载失败!");
        }
        //关闭文件流
        fos.close();
        //关闭连接
        if (ftpClient != null) {
            ftpClient.logout();
            ftpClient.disconnect();
        }*/

     String data = "{\"user\":\"chenquan\",\"pwd\":\"ch0070165194\",\"dir\":\"configs\",\"ipaddr\":\"192.168.1.111\",\"port\":21,\"uuid\":\"201905140001\"}�������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������������";

        String data2 = data.substring(0,data.lastIndexOf("}")+1);

        System.out.println(data2);


    }

}
