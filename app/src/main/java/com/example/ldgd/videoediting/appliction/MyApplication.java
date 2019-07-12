package com.example.ldgd.videoediting.appliction;

import android.app.Application;

import com.xmic.tvonvif.finder.CameraDevice;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ldgd on 2019/6/13.
 * 功能：
 * 说明：
 */

public class MyApplication extends Application {

    /**
     *  当前需要播放的摄像头设备
     */
    public CameraDevice appointCameraDevice;
    /**
     *  保存过的设备
     */
    public List<String> saveDevice = new ArrayList<>();



    public CameraDevice getAppointCameraDevice() {
        return appointCameraDevice;
    }

    public void setAppointCameraDevice(CameraDevice appointCameraDevice) {
        this.appointCameraDevice = appointCameraDevice;
    }

    public List<String> getSaveDevice() {
        return saveDevice;
    }

    /**
     *  记录保存过的设备地址
     * @param devicePath
     */
    public void recordDevice(String devicePath) {
        saveDevice.add(devicePath);
    }
}
