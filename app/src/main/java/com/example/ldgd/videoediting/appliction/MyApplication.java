package com.example.ldgd.videoediting.appliction;

import android.app.Application;

import com.xmic.tvonvif.finder.CameraDevice;

/**
 * Created by ldgd on 2019/6/13.
 * 功能：
 * 说明：
 */

public class MyApplication extends Application {

    public CameraDevice appointCameraDevice;



    public CameraDevice getAppointCameraDevice() {
        return appointCameraDevice;
    }

    public void setAppointCameraDevice(CameraDevice appointCameraDevice) {
        this.appointCameraDevice = appointCameraDevice;
    }
}
