package com.xmic.tvonvif.IPCamManager;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import com.xmic.tvonvif.finder.CameraDevice;
import com.xmic.tvonvif.finder.CameraService;
import com.xmic.tvonvif.finder.CameraService.CameraBinder;

public class IPCamManger {
	
	private static CameraService mService;
	private static Context mContext;
	private static ServiceConnection mServiceConnection;
	
	// Connect to the IPcam service in the smartTV system.
	// This method will be called first before calling other method.
	public static void connectToIPCamService(Context context) {
		mContext = context;
		if (mServiceConnection == null) {
			mServiceConnection = new ServiceConnection() {
				@Override
				public void onServiceDisconnected(ComponentName name) {
					// TODO Auto-generated method stub
					mService = null;
				}
				
				@Override
				public void onServiceConnected(ComponentName name, IBinder service) {
					// TODO Auto-generated method stub
					CameraBinder cb = (CameraBinder) service;
					mService = cb.getService();
				}
			};
		}
		mContext.bindService(new Intent(mContext, CameraService.class), mServiceConnection, Service.BIND_AUTO_CREATE);
	}
	
	// Call disconnect when IPcam service is not need.
	// This method will be called after all operations had finished.
	public static void disconnectIPCamService() {
		mContext.unbindService(mServiceConnection);
	}
	
	// Get all the IPCam which had connect to tv.
	public static IPCam[] getAllConnectedIPCam() {
		if (mService != null) {
			IPCam[] res = new IPCam[mService.getDevices().size()];
			for (int i = 0; i < res.length; i++) {
				res[i] = (IPCam)mService.getDevices().get(i);
			}
			return res;
		} else {
			return null;
		}
		
	}
	
	// Get the specific IPCam by id.
	public static IPCam getIPCamById(int id) {
		for (CameraDevice cd : mService.getFinder().getCameraList()) {
			if (cd.getId() == id) {
				return cd;
			}
		}
		return null;
	}
}
