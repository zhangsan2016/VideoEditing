package com.xmic.tvonvif.IPCamManager;

import android.view.SurfaceView;
import com.googlecode.javacv.cpp.opencv_core.IplImage;

public interface IPCam {
	
	//Initial the network connection of the IP camera.
	public void IPCamInit();
	
	//Get the unique identification for this IP camera.
	public int getId();
	
	//Get the IP camera name set by user in smart tv system ui.
	public String getName();
	
	//Get the IP address of this IP camera with XXX.XXX.XXX.XXX format.
	public String getIpAddress();
	
	//Get the connect status of this IP camera.
	public boolean isOnline();
	
	//If surface is not null, draw the surface when a new frame arrived.
	public void setSurfaceView(SurfaceView surfaceView);
	
	//Get the frame from IP camera. I will call this method on non-ui thread.
	public IplImage grab();
	
	//Release the network connection of the IP camera.
	public void IPCamRelease();
}
