package com.example.ldgd.videoediting.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

/**
 * Created by ldgd on 2019/6/6.
 * 功能：框选视屏中的区域
 * 说明：
 */

public class EditView extends FrameLayout {


    //  声明Paint对象
    private Paint mPaint = null;
    private int StrokeWidth = 5;
    //手动绘制矩形
    private Rect rect = new Rect(0, 0, 0, 0);
    // 按钮
    private LinearLayout layout;
    // 框选的状态 - 是否完成编辑
    private boolean finish = false;
    // 按钮监听事件
    private EditViewOnClickListener listener;


    public EditView(Context context) {
        super(context);
        //构建对象
        mPaint = new Paint();
        mPaint.setColor(Color.RED);
        //开启线程
        // new Thread(this).start();

        initView(context);

        // View继承FrameLayout后，设置后才会执行绘制
        setWillNotDraw(false);

    }

    private void initView(Context context) {

        layout = new LinearLayout(context); // 线性布局方式
        layout.setOrientation(LinearLayout.HORIZONTAL); //
        layout.setBackgroundColor(0xff00ffff);
        LinearLayout.LayoutParams LP_MM = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        layout.setVisibility(GONE);
        layout.setLayoutParams(LP_MM);

        LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        Button bt_save = new Button(context);
        bt_save.setLayoutParams(params);
        bt_save.setPadding(10, 10, 10, 10);
        bt_save.setTextColor(Color.WHITE);
        bt_save.setText("保存");
        bt_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(listener != null){
                    listener.saveButtonOnClick(rect);
                }
            }
        });


        Button bt_cancel = new Button(context);
        bt_cancel.setLayoutParams(params);
        bt_cancel.setPadding(10, 10, 10, 10);
        bt_cancel.setTextColor(Color.WHITE);
        bt_cancel.setText("取消");
        bt_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(listener != null){
                    listener.cancelButtonOnClick(rect);
                }

            }
        });

        layout.addView(bt_save);
        layout.addView(bt_cancel);

        addView(layout);

    }


 /*   @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //设置无锯齿
        mPaint.setAntiAlias(true);
        //   canvas.drawARGB(50, 255, 227, 0);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(StrokeWidth);
        mPaint.setAlpha(100);
        mPaint.setStrokeWidth(2);

        mPaint.setColor(Color.RED);
        canvas.drawRect(rect, mPaint);

    }*/


    @Override
    protected void dispatchDraw(Canvas canvas) {
        //设置无锯齿
        mPaint.setAntiAlias(true);
        //   canvas.drawARGB(50, 255, 227, 0);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(StrokeWidth);
        mPaint.setAlpha(100);
        mPaint.setStrokeWidth(2);

        mPaint.setColor(Color.RED);

        canvas.drawRect(rect, mPaint);
        if (finish) {
            layout.setVisibility(VISIBLE);
            //   layout.layout(layout.getLeft(),layout.getTop(),layout.getRight(),layout.getBottom());
            float x = rect.right - layout.getWidth() - StrokeWidth;
            float y = rect.bottom - layout.getHeight() - StrokeWidth;
            layout.setX(x);
            layout.setY(y);
        } else {
            layout.setVisibility(GONE);
        }
        super.dispatchDraw(canvas);
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int x = (int) event.getX();
        int y = (int) event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                finish = false;
                rect.right += StrokeWidth;
                rect.bottom += StrokeWidth;
                invalidate(rect);
                rect.left = x;
                rect.top = y;
                rect.right = rect.left;
                rect.bottom = rect.top;
            case MotionEvent.ACTION_MOVE:
                Rect old = new Rect(rect.left, rect.top, rect.right + StrokeWidth, rect.bottom + StrokeWidth);
                rect.right = x;
                rect.bottom = y;
                old.union(x, y);
                invalidate(old);
                break;
            case MotionEvent.ACTION_UP:
                if ((rect.right - rect.left) > 100) {
                    finish = true;

                } else {
                    finish = false;
                }

                break;
            default:
                break;
        }
        return true;//处理了触摸信息，消息不再传递
    }

    public void setListener(EditViewOnClickListener listener) {
        this.listener = listener;
    }

    public interface EditViewOnClickListener {
        public void saveButtonOnClick(Rect rect);

        public void cancelButtonOnClick(Rect rect);
    }

}
