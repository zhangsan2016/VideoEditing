package com.xmic.tvonvif.finder;

import java.util.List;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import com.xmic.tvonvif.database.Database;

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
	
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		return super.onStartCommand(intent, flags, startId);
	}
	
}
