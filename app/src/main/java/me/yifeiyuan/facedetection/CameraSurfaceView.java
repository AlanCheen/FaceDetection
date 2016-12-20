package me.yifeiyuan.facedetection;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/**
 *
 * http://www.bkjia.com/Androidjc/1047831.html
 *
 */
public class CameraSurfaceView extends SurfaceView implements SurfaceHolder.Callback {

    private static final String TAG = "CameraSurfaceView";
    Context mContext;
    SurfaceHolder mSurfaceHolder;

    public CameraSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        // TODO Auto-generated constructor stub
        mContext = context;
        mSurfaceHolder = getHolder();
        mSurfaceHolder.setFormat(PixelFormat.TRANSPARENT);//translucent��͸�� transparent͸��
        mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        mSurfaceHolder.addCallback(this);
        mPaint.setColor(Color.BLUE);
        mPaint.setStrokeWidth(10);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        // TODO Auto-generated method stub
        Log.i(TAG, "surfaceCreated..."+(mSurfaceHolder==holder));
//        CameraInterface.getInstance().doOpenCamera(null, CameraInfo.CAMERA_FACING_BACK);
        CameraInterface.getInstance().doOpenCamera(null, Camera.CameraInfo.CAMERA_FACING_FRONT);
        dosth();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width,
                               int height) {
        // TODO Auto-generated method stub
        Log.i(TAG, "surfaceChanged..."+(mSurfaceHolder==holder));
        CameraInterface.getInstance().doStartPreview(mSurfaceHolder, 1.333f);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        // TODO Auto-generated method stub
        Log.i(TAG, "surfaceDestroyed..."+(mSurfaceHolder==holder));
        CameraInterface.getInstance().doStopCamera();
    }

    public SurfaceHolder getSurfaceHolder() {
        return mSurfaceHolder;
    }

    Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    /**
     * SurfaceView 用来实时显示 Camera 的时候是不能获得 Canvas 的。
     */
    public void dosth() {
//        Canvas canvas = mSurfaceHolder.lockCanvas();
//        if (null != canvas) {
//            canvas.drawLine(0,0,100,400,mPaint);
//            mSurfaceHolder.unlockCanvasAndPost(canvas);
//        }else {
//            Log.d(TAG, "dosth() called : canvas is null");
//        }
    }

}
