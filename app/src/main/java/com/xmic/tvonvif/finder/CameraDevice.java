package com.xmic.tvonvif.finder;

import android.view.SurfaceView;

import com.googlecode.javacv.FFmpegFrameGrabber;
import com.googlecode.javacv.FrameGrabber.Exception;
import com.googlecode.javacv.cpp.opencv_core.IplImage;
import com.xmic.tvonvif.IPCamManager.IPCam;

import java.util.UUID;

public class CameraDevice implements IPCam {
	public UUID uuid;
	public String serviceURL;
	private int id;
	private String name;
	private String ipAddr;
	private boolean isOnline = false;
	private String rtspUri = "";
	private FFmpegFrameGrabber mGrabber;
	private SurfaceView mSurfaceView;
	private OnSoapDoneListener mListener;
	
	public int width;
	public int height;
	public int rate;

	public String username;
	public String password;

	public CameraDevice(UUID uuid, String serviceURL) {
		this.uuid = uuid;
		this.serviceURL = serviceURL;
	}

	public void setSecurity(String username, String password) {
		this.username = username;
		this.password = password;
	}

	public void setProperties(int width, int height, int rate) {
		this.width = width;
		this.height = height;
		this.rate = rate;
	}

	public void setId(int id) {
		this.id = id;
	}

	public void setIpAddr(String ipAddr) {
		this.ipAddr = ipAddr;
	}

	public void setOnline(boolean isOnline) {
		this.isOnline = isOnline;
	}

	@Override
	public void IPCamInit() {
		if (this.isOnline) {
			HttpSoap soap = new HttpSoap(this);
			soap.setOnHttpSoapListener(listener);
			soap.start();
		}
	}
	
	public void setOnSoapDoneListener(OnSoapDoneListener listener) {
		mListener = listener;
	}

	private OnHttpSoapListener listener = new OnHttpSoapListener() {
		@Override
		public void OnHttpSoapDone(CameraDevice camera, String uri, boolean success) {
			if (success) {
				rtspUri = uri.substring(0, uri.indexOf("//") + 2) + camera.username
						+ ":" + camera.password + "@"
						+ uri.substring(uri.indexOf("//") + 2);
				mGrabber = new FFmpegFrameGrabber(rtspUri);
				mGrabber.setImageWidth(width);
				mGrabber.setImageHeight(height);
				try {
					mGrabber.start();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			if (mListener != null) {
				mListener.onSoapDone(CameraDevice.this, success);
			}
		}
	};

	public String getRtspUri() {
		return rtspUri;
	}

	@Override
	public int getId() {
		return this.id;
	}

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public String getIpAddress() {
		return this.ipAddr;
	}

	@Override
	public boolean isOnline() {
		return this.isOnline;
	}

	@Override
	public void setSurfaceView(SurfaceView surfaceView) {
		// TODO what to do ?
		mSurfaceView = surfaceView;
		
//		IplImage src = grabber.grab();
//		if (src == null) {
//			runGrabberThread = false;
//			return;
//		}
//		// Log.e("PlayerActivity", src.toString());
//		IplImage dst = cvCreateImage(new CvSize(width, height),
//				src.depth(), 4);
//		cvCvtColor(src, dst, CV_BGR2RGBA);
//		Bitmap bitmap = Bitmap.createBitmap(width, height,
//				Bitmap.Config.ARGB_8888);
//		bitmap.copyPixelsFromBuffer(dst.getByteBuffer());
//		src.release();
//		dst.release();
	}

	@Override
	public IplImage grab() {
		if (mGrabber != null) {
			try {
				return mGrabber.grab();
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		} else {
			return null;
		}
	}

	@Override
	public void IPCamRelease() {
		try {
			mGrabber.stop();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public String toString() {
		return "CameraDevice{" +
				"uuid=" + uuid +
				", serviceURL='" + serviceURL + '\'' +
				", id=" + id +
				", name='" + name + '\'' +
				", ipAddr='" + ipAddr + '\'' +
				", isOnline=" + isOnline +
				", rtspUri='" + rtspUri + '\'' +
				", mGrabber=" + mGrabber +
				", mSurfaceView=" + mSurfaceView +
				", mListener=" + mListener +
				", width=" + width +
				", height=" + height +
				", rate=" + rate +
				", username='" + username + '\'' +
				", password='" + password + '\'' +
				", listener=" + listener +
				'}';
	}
}
