package com.example.ldgd.videoediting.entity;

import java.io.Serializable;

/**
 * Created by ldgd on 2019/7/10.
 * 功能：存储NFC中读取的信息
 * 说明：
 */

public class FtpConfig implements Serializable {

    /**
     * user : chenquan
     * pwd : ch0070165194
     * dir : configs
     * ipaddr : 192.168.1.111
     * port : 21
     * uuid : 201905140001
     */

    private String user;
    private String pwd;
    private String dir;
    private String ipaddr;
    private int port;
    private String uuid;

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
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

    public String getIpaddr() {
        return ipaddr;
    }

    public void setIpaddr(String ipaddr) {
        this.ipaddr = ipaddr;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    @Override
    public String toString() {
        return "NfcConfiguration{" +
                "user='" + user + '\'' +
                ", pwd='" + pwd + '\'' +
                ", dir='" + dir + '\'' +
                ", ipaddr='" + ipaddr + '\'' +
                ", port=" + port +
                ", uuid='" + uuid + '\'' +
                '}';
    }
}
