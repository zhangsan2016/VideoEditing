package com.example.ldgd.videoediting.act;


import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import com.example.ldgd.videoediting.R;
import com.example.ldgd.videoediting.appliction.MyApplication;
import com.example.ldgd.videoediting.util.LogUtil;
import com.example.ldgd.videoediting.view.EditView;
import com.googlecode.javacv.cpp.opencv_core;
import com.xmic.tvonvif.finder.CameraDevice;

import static com.googlecode.javacv.cpp.opencv_core.cvCreateImage;
import static com.googlecode.javacv.cpp.opencv_core.cvReleaseImage;
import static com.googlecode.javacv.cpp.opencv_imgproc.CV_BGR2RGBA;
import static com.googlecode.javacv.cpp.opencv_imgproc.cvCvtColor;

public class VideoPlayerActivity extends Activity implements EditView.EditViewOnClickListener {

    // 当前要播放的设备
    private CameraDevice device;
    private SurfaceHolder mHolder;
    private SurfaceView mSurfaceView;
    private PaintFlagsDrawFilter pfd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 去掉窗口标题
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        // 隐藏顶部的状态栏
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_video_player);

        // 初始化 View
        initView();

        // 播放视屏选项
        MyApplication myApplication = (MyApplication) this.getApplication();
        device = myApplication.getAppointCameraDevice();
        if (device != null && device.width > 0 && device.height > 0) {
            //   mService.getDb().addCamera(device);
            new Thread(new VideoPlayer(device)).start();
        }

        // 设置矩形绘制（用于框选）
        EditView gameView = new EditView(this);
        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        addContentView(gameView, layoutParams);
        gameView.setListener(this);

    }

    private void initView() {

        mSurfaceView = (SurfaceView) this.findViewById(R.id.surfaceView1);
        pfd = new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
        mHolder = mSurfaceView.getHolder();

        // 设置播放状态监听
        mSurfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder surfaceHolder) {

            }

            @Override
            public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
                synchronized (this) {
                    runGrabberThread = false;
                }
            }
        });
    }


    private boolean runGrabberThread = false;


    @Override
    public void saveButtonOnClick(Rect rect) {
        Rect surRect = new Rect();
        mSurfaceView.getDrawingRect(surRect);

    }

    @Override
    public void cancelButtonOnClick(Rect rect) {
        LogUtil.e("cancelButtonOnClick 被点击" + rect.toString());
    }

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
                if(newWidth <= 0 || newHeight <= 0){
                    continue;
                }
                mbitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        synchronized (this) {
                            if (!mbitmap.isRecycled() && runGrabberThread == true) {
                                Canvas canvas = VideoPlayerActivity.this.mHolder.lockCanvas();
                                //  对canvas设置抗锯齿的滤镜，防止变化canvas引起画质降低
                                canvas.setDrawFilter(pfd);
                                canvas.drawBitmap(mbitmap, 0, 0, null);
                                VideoPlayerActivity.this.mHolder.unlockCanvasAndPost(canvas);
                                // 回收
                                mbitmap.recycle();
                                System.gc();
                            }
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



    @Override
    protected void onDestroy() {
        runGrabberThread = false;
        super.onDestroy();
    }


}
