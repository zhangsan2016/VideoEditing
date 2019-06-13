package com.example.ldgd.videoediting.act;


import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.example.ldgd.videoediting.R;
import com.example.ldgd.videoediting.appliction.MyApplication;
import com.example.ldgd.videoediting.util.LogUtil;
import com.googlecode.javacv.cpp.opencv_core;
import com.xmic.tvonvif.finder.CameraDevice;

import static com.googlecode.javacv.cpp.opencv_core.cvCreateImage;
import static com.googlecode.javacv.cpp.opencv_core.cvReleaseImage;
import static com.googlecode.javacv.cpp.opencv_imgproc.CV_BGR2RGBA;
import static com.googlecode.javacv.cpp.opencv_imgproc.cvCvtColor;

public class VideoPlayerActivity extends Activity {

    // 当前要播放的设备
    private CameraDevice device;
    private SurfaceHolder mHolder;
    private SurfaceView mSurfaceView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_player);

        initView();

        MyApplication myApplication = (MyApplication) this.getApplication();
        device   = myApplication.getAppointCameraDevice();
        if (device != null) {
         //   mService.getDb().addCamera(device);
            new Thread(new VideoPlayer(device)).start();
        }
    }

    private void initView() {

        mSurfaceView = (SurfaceView) this.findViewById(R.id.surfaceView1);

        mHolder = mSurfaceView.getHolder();
    }


    private boolean runGrabberThread = false;
    private class VideoPlayer implements Runnable {

        private CameraDevice mDevice;
        //    private Bitmap bitmap = null;
        private Bitmap mbitmap;

        public VideoPlayer(CameraDevice cd) {
            mDevice = cd;

        }

        @Override
        public void run() {
            runGrabberThread = true;
            while (runGrabberThread) {
                opencv_core.IplImage src = mDevice.grab();
                if (src == null) {
                    runGrabberThread = false;
                    return;
                }
                opencv_core.IplImage dst = cvCreateImage(new opencv_core.CvSize(mDevice.width,
                        mDevice.height), src.depth(), 4);
                cvCvtColor(src, dst, CV_BGR2RGBA);

                final Bitmap bitmap = Bitmap.createBitmap(mDevice.width, mDevice.height, Bitmap.Config.ARGB_8888);
                bitmap.copyPixelsFromBuffer(dst.getByteBuffer());
                // 释放 IplImage (关键代码)
                dst.position(0);
                cvReleaseImage(dst);

                LogUtil.e("bitmap.getWidth() =  " + bitmap.getWidth() + "    mDevice.width  = " + mDevice.width + "   :  " + "  ");

                int width = bitmap.getWidth();
                int height = bitmap.getHeight();
                // 设置想要的大小
                int newWidth = mSurfaceView.getWidth();
                int newHeight = mSurfaceView.getHeight();
                // 计算缩放比例
                float scaleWidth = ((float) newWidth) / width;
                float scaleHeight = ((float) newHeight) / height;
                Matrix matrix = new Matrix();
                matrix.postScale(scaleWidth, scaleHeight);
                mbitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        if (!mbitmap.isRecycled()) {
                            Canvas canvas = VideoPlayerActivity.this.mHolder.lockCanvas();
                            canvas.drawBitmap(mbitmap, 0, 0, null);
                            VideoPlayerActivity.this.mHolder.unlockCanvasAndPost(canvas);
                            // 回收
                            mbitmap.recycle();
                            System.gc();
                        }
                    }
                });

                if (!bitmap.isRecycled()) {
                    bitmap.recycle();
                    System.gc();
                }

            }
        }
    }



}
