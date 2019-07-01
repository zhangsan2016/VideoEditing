package com.example.ldgd.videoediting.act;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnShowListener;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ldgd.videoediting.R;
import com.example.ldgd.videoediting.appliction.MyApplication;
import com.example.ldgd.videoediting.util.LogUtil;
import com.googlecode.javacv.cpp.opencv_core.CvSize;
import com.googlecode.javacv.cpp.opencv_core.IplImage;
import com.xmic.tvonvif.IPCamManager.IPCam;
import com.xmic.tvonvif.IPCamManager.IPCamManger;
import com.xmic.tvonvif.finder.CameraDevice;
import com.xmic.tvonvif.finder.CameraFinder;
import com.xmic.tvonvif.finder.CameraService;
import com.xmic.tvonvif.finder.CameraService.CameraBinder;
import com.xmic.tvonvif.finder.OnCameraFinderListener;
import com.xmic.tvonvif.finder.OnSoapDoneListener;

import java.io.ByteArrayOutputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import static com.googlecode.javacv.cpp.opencv_core.cvCreateImage;
import static com.googlecode.javacv.cpp.opencv_core.cvReleaseImage;
import static com.googlecode.javacv.cpp.opencv_imgproc.CV_BGR2RGBA;
import static com.googlecode.javacv.cpp.opencv_imgproc.cvCvtColor;

public class MainVideoActivity extends Activity {

    public static final String TAG = "VideoEditing";
    private ListView mListView;
    private Button mButton;
    private MyAdapter mAdapter;
    private SurfaceView mSurfaceView;
    private SurfaceHolder mHolder;
    private ProgressDialog mProgressDialog;
    private MyHandle mHandler;
    private AlertDialog mLoginDialog;
    private View mLoginView;
    private boolean runGrabberThread = false;
    private static CameraService mService;
    private  ServiceConnection mServiceConnection;
    private int mNowIndex = -1;

    static class MyHandle extends Handler {

        WeakReference<MainVideoActivity> mActivity;

        public  MyHandle(MainVideoActivity mActivity) {
            super();
            this.mActivity = new WeakReference<MainVideoActivity>(mActivity);
        }

        @Override
        public void handleMessage(Message msg) {
            mActivity.get().mProgressDialog.hide();
            switch (msg.what) {
                case 1:
                    try {
                        Bitmap bitmap = (Bitmap) msg.obj;

                        /*Bitmap bitmap2 = Bitmap.createBitmap(bitmap, 0, 0, width, height,matrix, true);
                        bitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height,matrix, true);*/

                        MainVideoActivity activity = mActivity.get();
                        Canvas canvas = activity.mHolder.lockCanvas();
                        canvas.drawBitmap(bitmap, 0, 0, null);
                        activity.mHolder.unlockCanvasAndPost(canvas);


                        if (!bitmap.isRecycled()) {
                            bitmap.recycle();
                            bitmap = null;
                            System.gc();
                        }

                    } catch (Exception e) {
                        // TODO: handle exception
                        e.printStackTrace();
                    }
                    break;
                case 2:
                    mActivity.get().showToast("登录失败");
                default:
                    break;
            }

            super.handleMessage(msg);
        }
    }

    private static byte[] Bitmap2Bytes(Bitmap bm) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.JPEG, 75, baos);
        return baos.toByteArray();
    }


    public void showToast(String str) {
        Toast.makeText(MainVideoActivity.this, str, Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_main);
        findView();

        // 设置矩形绘制（用于框选）
      /*  GameView gameView = new GameView(this);
        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.MATCH_PARENT);
        addContentView(gameView,layoutParams);*/


        if (mServiceConnection == null) {
            mServiceConnection = new ServiceConnection() {

                @Override
                public void onServiceDisconnected(ComponentName name) {
                    mService = null;
                    //   mAdapter = null;
                }

                @Override
                public void onServiceConnected(ComponentName name,
                                               IBinder service) {
                    CameraBinder cb = (CameraBinder) service;
                    mService = cb.getService();
                    mAdapter = new MyAdapter(MainVideoActivity.this,
                            mService.getFinder());
                    mListView.setAdapter(mAdapter);
                    mService.getFinder().setOnCameraFinderListener(
                            new OnCameraFinderListener() {
                                @Override
                                public void OnCameraListUpdated() {


                                    // 重新获取Fragment的强引用，并且判断是否存活
                                    MainVideoActivity act = mHandler.mActivity.get();
                                    if (act == null ) {
                                        // Fragment死亡了，不再做任何的事情
                                        LogUtil.e("弱引用已经死亡！");
                                        return;
                                    }

                                    mHandler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            List<CameraDevice>  list =   mService.getFinder().getCameraList();
                                            mAdapter.setCameraDevices(list);

                                        }
                                    });
                                }
                            });

                }
            };
        }
        bindService(new Intent(getApplicationContext(), CameraService.class),
                mServiceConnection, Service.BIND_AUTO_CREATE);

        mHandler = new MyHandle(this);

        mListView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                mNowIndex = position;
                mLoginDialog.show();
            }

        });




    }

    private void findView() {
        mListView = (ListView) findViewById(R.id.listView1);
        mButton = (Button) findViewById(R.id.button1);
        mButton.setOnClickListener(mButtonClickListener);
        mSurfaceView = (SurfaceView) findViewById(R.id.surfaceView1);
        mHolder = mSurfaceView.getHolder();
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setTitle("请稍等...");
        mProgressDialog.setMessage("正在连接...");
        mProgressDialog.hide();
        LayoutInflater inflater = getLayoutInflater();
        mLoginView = inflater.inflate(R.layout.login_layout, null);
        mLoginDialog = new AlertDialog.Builder(this).setTitle("请先登录")
                .setView(mLoginView)
                .setPositiveButton("登录", loginDialogListener)
                .setNegativeButton("取消", loginDialogListener)
                .setNeutralButton("忘记", loginDialogListener).create();
        mLoginDialog.setCancelable(false);
        mLoginDialog.setOnShowListener(new OnShowListener() {

            @Override
            public void onShow(DialogInterface dialog) {
                // TODO Auto-generated method stub
                EditText etUser = (EditText) mLoginView
                        .findViewById(R.id.etuserName);
                EditText etPwd = (EditText) mLoginView.findViewById(R.id.etPWD);
                CameraDevice cd = mService.getFinder().getCameraList()
                        .get(mNowIndex);
                CameraDevice cd2 = mService.getDb().getCameraByUUID(cd.uuid);
                if (cd2 != null) {
                    etUser.setText(cd2.username);
                    etPwd.setText(cd2.password);
                } else {
                /*	etUser.setText("");
                    etPwd.setText("");*/
                }
            }
        });
    }

    private View.OnClickListener mButtonClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            if (mService != null) {
                mService.sendBroadcast();
            }
        }
    };

    public OnClickListener loginDialogListener = new OnClickListener() {

        @Override
        public void onClick(DialogInterface dialog, int which) {
            // TODO Auto-generated method stub
            CameraDevice cd = mService.getFinder().getCameraList()
                    .get(mNowIndex);
            switch (which) {
                case AlertDialog.BUTTON_POSITIVE:
                    runGrabberThread = false;
                    mProgressDialog.show();
                    EditText etUser = (EditText) mLoginView
                            .findViewById(R.id.etuserName);
                    EditText etPwd = (EditText) mLoginView.findViewById(R.id.etPWD);
                    cd.setSecurity(etUser.getText().toString().trim(), etPwd
                            .getText().toString().trim());
                    cd.setOnSoapDoneListener(new OnSoapDoneListener() {

                        @Override
                        public void onSoapDone(final CameraDevice device, boolean success) {
                            // TODO Auto-generated method stub
                            if (success) {

                                /*mService.getDb().addCamera(device);
                                new Thread(new VideoPlayer(device)).start();*/

                              runOnUiThread(new Runnable() {
                                   @Override
                                   public void run() {

                                       // 关闭提示
                                       MainVideoActivity.this.mProgressDialog.hide();
                                       // 设置当前播放对象到appliction
                                       MyApplication myApplication = (MyApplication) MainVideoActivity.this.getApplication();
                                       myApplication.setAppointCameraDevice(device);
                                       // 启动播放界面
                                       Intent intent = new Intent(MainVideoActivity.this, VideoPlayerActivity.class);
                                       MainVideoActivity.this.startActivity(intent);
                                   }
                               });


                            } else {
                                Message msgStr = mHandler.obtainMessage(2, null);
                                mHandler.sendMessage(msgStr);
                            }
                        }
                    });
                    cd.IPCamInit();
                    break;
                case AlertDialog.BUTTON_NEGATIVE:

                    break;
                case AlertDialog.BUTTON_NEUTRAL:
                    mService.getDb().deleteCamera(cd);
                    break;
                default:
                    break;
            }
        }
    };

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
                IplImage src = mDevice.grab();
                if (src == null) {
                    runGrabberThread = false;
                    return;
                }
                IplImage dst = cvCreateImage(new CvSize(mDevice.width,
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
                mbitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        if (!mbitmap.isRecycled()) {
                            MainVideoActivity.this.mProgressDialog.hide();
                            Canvas canvas = MainVideoActivity.this.mHolder.lockCanvas();
                            canvas.drawBitmap(mbitmap, 0, 0, null);
                            MainVideoActivity.this.mHolder.unlockCanvasAndPost(canvas);
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

    private class MyAdapter extends BaseAdapter {

        private Context mContext;
     //   private CameraFinder mFinder;
        private List<CameraDevice> cameraDevices;

        public MyAdapter(Context context, CameraFinder finder) {
            mContext = context;
       //     mFinder = finder;
            cameraDevices = new ArrayList<CameraDevice>();
        }

        public void setCameraDevices(List<CameraDevice> list) {
          //    notifyDataSetChanged();
           if (list != null) {
                cameraDevices.clear();
                cameraDevices.addAll(list);
                notifyDataSetChanged();
            }

        }

        @Override
        public int getCount() {
            return   cameraDevices.size();
        }

        @Override
        public Object getItem(int position) {
            return  cameraDevices.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = View.inflate(mContext,
                        android.R.layout.simple_expandable_list_item_2, null);
            }
            CameraDevice device =  cameraDevices.get(position);
            TextView title = (TextView) convertView
                    .findViewById(android.R.id.text1);
            title.setTextColor(Color.BLACK);
            title.setText(device.uuid.toString());
            TextView subTitle = (TextView) convertView
                    .findViewById(android.R.id.text2);
            subTitle.setTextColor(Color.BLACK);
            subTitle.setText(device.serviceURL.toString());
            return convertView;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // TODO Auto-generated method stub
        if (item.getItemId() == R.id.menu_settings) {
            IPCamManger.connectToIPCamService(this);
        } else if (item.getItemId() == R.id.menu_settings2) {
            IPCam[] b = IPCamManger.getAllConnectedIPCam();
            int i = 100;
            // CameraDevice ipcams[] =
            // (CameraDevice[])IPCamManger.getAllConnectedIPCam();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        Log.e("onvif ", "onDestroy被执行");
        unbindService(mServiceConnection);
        super.onDestroy();
    }

}
