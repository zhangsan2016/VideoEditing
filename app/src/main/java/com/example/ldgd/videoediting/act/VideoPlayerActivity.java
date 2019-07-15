package com.example.ldgd.videoediting.act;


import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.example.ldgd.videoediting.R;
import com.example.ldgd.videoediting.appliction.MyApplication;
import com.example.ldgd.videoediting.entity.FtpConfig;
import com.example.ldgd.videoediting.entity.VideoConfig;
import com.example.ldgd.videoediting.util.FtpUtil;
import com.example.ldgd.videoediting.util.LogUtil;
import com.example.ldgd.videoediting.util.ReadWriteUtil;
import com.example.ldgd.videoediting.view.EditView;
import com.google.gson.Gson;
import com.googlecode.javacv.cpp.opencv_core;
import com.xmic.tvonvif.finder.CameraDevice;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.math.BigDecimal;

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
    // 自定义View 编辑框
    private EditView editView;

    // Ftp 服务器配置信息
    private FtpConfig ftpConfig = null;

    // 加载框
    private ProgressDialog mProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 去掉窗口标题
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        // 隐藏顶部的状态栏
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_video_player);


        // 获取 ftp 服务器配置文件
        ftpConfig = (FtpConfig) getIntent().getSerializableExtra("ftpconfig");


        // 初始化 View
        initView();

        // 播放视屏选项
        MyApplication myApplication = (MyApplication) this.getApplication();
        device = myApplication.getAppointCameraDevice();
        if (device != null && device.width > 0 && device.height > 0) {
            //   mService.getDb().addCamera(device);
            new Thread(new VideoPlayer(device)).start();
        }

        LogUtil.e("VideoPlayerActivity device = " + device.toString());


        // 设置矩形绘制（用于框选）
        editView = new EditView(this);
        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        addContentView(editView, layoutParams);
        editView.setListener(this);

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
    public void saveButtonOnClick(final Rect rect) {
       /* Rect surRect = new Rect();
        mSurfaceView.getDrawingRect(surRect);*/

     new  Thread(new Runnable() {
         @Override
         public void run() {
             showProgress();

             // 读取下载在本地的配置文件
             String jsonPath = VideoPlayerActivity.this.getFilesDir() + "/" + ftpConfig.getUuid() + ".json";
             String json = readTextFile(jsonPath);
             // json转换成类
             Gson gson = new Gson();
             VideoConfig config = gson.fromJson(json, VideoConfig.class);
             // 更新配置文件到 PTF 服务器
             if (device != null && device.width > 0 && device.height > 0) {

                 // 获取当前屏幕的宽高
                 DisplayMetrics dm = new DisplayMetrics();
                 //获取屏幕信息
                 getWindowManager().getDefaultDisplay().getMetrics(dm);
                 int screenWidth = dm.widthPixels;
                 int screenHeigh = dm.heightPixels;

                 // 配置信息
                 VideoConfig.RtspinfoBean rtspinfoBean = new VideoConfig.RtspinfoBean();
                 rtspinfoBean.setUrl(device.getRtspUri());

                 LogUtil.e("VideoPlayerActivity rect =  " + rect.left + "  " + rect.top + "   " + rect.right + "    " + rect.bottom);
                 //    screenWidth = 800  screenHeigh = 480
                 rtspinfoBean.setX(new BigDecimal((float) rect.left / screenWidth).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
                 rtspinfoBean.setY(new BigDecimal((float) rect.top / screenHeigh).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
                 rtspinfoBean.setW(new BigDecimal((float) rect.right / screenWidth).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
                 rtspinfoBean.setH(new BigDecimal((float) rect.bottom / screenHeigh).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
                 config.getRtspinfo().add(rtspinfoBean);
                 LogUtil.e("VideoPlayerActivity rtspinfoBean =  " + rtspinfoBean.toString());

                 String newJson = gson.toJson(config);
                 ReadWriteUtil.writeStringToFile(newJson, jsonPath);

                 // 登录 ftp  更新配置文件到服务器
                 FtpUtil fipUtil = new FtpUtil(ftpConfig.getIpaddr(), ftpConfig.getPort(), ftpConfig.getUser(), ftpConfig.getPwd());
                 String ftpPath = "configs/" + ftpConfig.getUuid() + ".json";
                 fipUtil.putFile(ftpPath, jsonPath);

                 // 记录保存过的设备地址
                 MyApplication myApplication = (MyApplication) VideoPlayerActivity.this.getApplication();
                 myApplication.recordDevice(device.getRtspUri());

                 showToast("保存成功！",Toast.LENGTH_SHORT);

                 // 关闭选框
                 editView.clear();

             }
             stopProgress();
         }
     }).start();


    }

    @Override
    public void cancelButtonOnClick(Rect rect) {
        LogUtil.e("cancelButtonOnClick 被点击" + rect.toString());
        editView.clear();
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

                // 获取视屏图片存放到 Bitmap 中
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
                if (newWidth <= 0 || newHeight <= 0) {
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


    /**
     * 从本地读取json
     *
     * @param filePath
     * @return
     */
    private String readTextFile(String filePath) {
        StringBuilder sb = new StringBuilder();
        try {
            File file = new File(filePath);
            InputStream in = null;
            in = new FileInputStream(file);
            int tempbyte;
            while ((tempbyte = in.read()) != -1) {
                sb.append((char) tempbyte);
            }
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sb.toString();
    }

    private void showToast(final CharSequence context, final int length) {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplication(), context, length).show();
            }
        });
    }

    private void showProgress() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mProgress = ProgressDialog.show(VideoPlayerActivity.this, "", "Save...");
            }
        });
    }

    private void stopProgress() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mProgress != null) {
                    mProgress.cancel();
                }
            }
        });
    }


}
