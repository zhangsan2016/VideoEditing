package com.xmic.tvonvif.finder;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import com.example.ldgd.videoediting.util.LogUtil;
import com.xmic.tvonvif.database.Database;

import java.util.List;

public class CameraService extends Service {

	private CameraFinder mFinder;
	private CameraBinder mBinder = new CameraBinder();
	private Database mDb;
	private List<CameraDevice> mDevices;
	
	public CameraFinder getFinder() {
		return mFinder;
	}

	public class CameraBinder extends Binder {
		public CameraService getService() {
			return CameraService.this;
		}
	}
	
	public void sendBroadcast() {
		if (mFinder != null) {
			mFinder.sendProbe();
		}
	}
	
	public Database getDb() {
		return mDb;
	}
	
	public List<CameraDevice> getDevices() {
		return mDevices;
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		LogUtil.e(" xxx onBind 执行");
		mDb = new Database(getApplicationContext());
		mDevices = mDb.getCameraDevices();
		return mBinder;
	}

	@Override
	public void onCreate() {
		mFinder = new CameraFinder(getApplicationContext());
		mFinder.setOnCameraFinderListener(finderListener);
		super.onCreate();
	}


	private OnCameraFinderListener finderListener = new OnCameraFinderListener() {
		@Override
		public void OnCameraListUpdated() {
			List<CameraDevice> aa = 	mFinder.getCameraList();
			if (mDevices != null && mDevices.size() > 0) {
				for (CameraDevice cd1 : mDevices) {
					for (CameraDevice cd2 : mFinder.getCameraList()) {
						if (cd1.uuid.equals(cd2.uuid)) {
							cd1.setOnline(true);
							break; 
						}
					}
				}
			}
		}
	};
	

	
}
