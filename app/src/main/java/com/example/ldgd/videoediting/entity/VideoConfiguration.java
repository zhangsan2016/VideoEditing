package com.example.ldgd.videoediting.entity;

import java.util.List;

/**
 * Created by ldgd on 2019/6/28.
 * 功能：摄像头配置
 * 说明：
 */

public class VideoConfiguration {

    /**
     * username : slvier
     * pwd : aa
     * dir : ftp
     * ipaddress : 192.168.133.208
     * port : 21
     * graph : graph
     * graphMD5 : graphMD5.txt
     * percentage : 70
     * label : ["background","aeroplane","bicycle","bird","boat","bottle","bus","car","cat","chair","cow","diningtable","dog","horse","motorbike","person","pottedplant","sheep","sofa","train","tvmonitor"]
     * detect : [15]
     * rtspinfo : [{"url":"rtsp://192.168.133.51:554/stream0","x":0,"y":0,"w":1,"h":1},{"url":"rtsp://192.168.133.52:554/stream0","x":0,"y":0,"w":1,"h":1},{"url":"rtsp://192.168.133.53:554/stream0","x":0,"y":0,"w":1,"h":1},{"url":"rtsp://192.168.133.54:554/stream0","x":0,"y":0,"w":1,"h":1},{"url":"rtsp://192.168.133.70:554/1/h264major","x":0,"y":0,"w":1,"h":1},{"url":"rtsp://192.168.133.71:554/1/h264major","x":0,"y":0,"w":1,"h":1},{"url":"rtsp://192.168.133.72:554/1/h264major","x":0,"y":0,"w":1,"h":1},{"url":"rtsp://192.168.133.185:8554/live/0","x":0,"y":0,"w":1,"h":1}]
     */

    private String username;
    private String pwd;
    private String dir;
    private String ipaddress;
    private int port;
    private String graph;
    private String graphMD5;
    private int percentage;
    private List<String> label;
    private List<Integer> detect;
    private List<RtspinfoBean> rtspinfo;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPwd() {
        return pwd;
    }

    public void setPwd(String pwd) {
        this.pwd = pwd;
    }

    public String getDir() {
        return dir;
    }

    public void setDir(String dir) {
        this.dir = dir;
    }

    public String getIpaddress() {
        return ipaddress;
    }

    public void setIpaddress(String ipaddress) {
        this.ipaddress = ipaddress;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getGraph() {
        return graph;
    }

    public void setGraph(String graph) {
        this.graph = graph;
    }

    public String getGraphMD5() {
        return graphMD5;
    }

    public void setGraphMD5(String graphMD5) {
        this.graphMD5 = graphMD5;
    }

    public int getPercentage() {
        return percentage;
    }

    public void setPercentage(int percentage) {
        this.percentage = percentage;
    }

    public List<String> getLabel() {
        return label;
    }

    public void setLabel(List<String> label) {
        this.label = label;
    }

    public List<Integer> getDetect() {
        return detect;
    }

    public void setDetect(List<Integer> detect) {
        this.detect = detect;
    }

    public List<RtspinfoBean> getRtspinfo() {
        return rtspinfo;
    }

    public void setRtspinfo(List<RtspinfoBean> rtspinfo) {
        this.rtspinfo = rtspinfo;
    }

    public static class RtspinfoBean {
        /**
         * url : rtsp://192.168.133.51:554/stream0
         * x : 0.0
         * y : 0.0
         * w : 1.0
         * h : 1.0
         */

        private String url;
        private double x;
        private double y;
        private double w;
        private double h;

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public double getX() {
            return x;
        }

        public void setX(double x) {
            this.x = x;
        }

        public double getY() {
            return y;
        }

        public void setY(double y) {
            this.y = y;
        }

        public double getW() {
            return w;
        }

        public void setW(double w) {
            this.w = w;
        }

        public double getH() {
            return h;
        }

        public void setH(double h) {
            this.h = h;
        }
    }
}
