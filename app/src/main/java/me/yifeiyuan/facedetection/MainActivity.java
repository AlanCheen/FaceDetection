package me.yifeiyuan.facedetection;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;

import java.io.ByteArrayOutputStream;

/**
 *
 *
 * todo  SurfaceView 卡 慢，可能要换成 SurfaceTexture
 *  1. SurfaceView 需要在 PreviewCallback 中去获取帧的数据 byte[] data
 *  2. 然后把data转成 Bitmap 去处理，再处理完后生成最后的 Bitmap （找嘴唇，上色，旋转角度）
 *  3. 再在另外一个 ImageView 中去展示。
 *
 * SurfaceTexture
 */
public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    CameraSurfaceView surfaceView = null;
    ImageButton shutterBtn;
    ImageButton switchBtn;
    FaceView faceView;
    float previewRate = -1f;
    private MainHandler mMainHandler = null;
    GoogleFaceDetect googleFaceDetect = null;

    ImageView faker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
//        setContentView(R.layout.activity_main);
        faker = (ImageView) findViewById(R.id.faker);

        surfaceView = (CameraSurfaceView) findViewById(R.id.camera_surfaceview);
        shutterBtn = (ImageButton) findViewById(R.id.btn_shutter);
        switchBtn = (ImageButton) findViewById(R.id.btn_switch);
        faceView = (FaceView) findViewById(R.id.face_view);

        ViewGroup.LayoutParams params = surfaceView.getLayoutParams();
        Point p = DisplayUtil.getScreenMetrics(this);
        params.width = p.x;
        params.height = p.y;

        previewRate = DisplayUtil.getScreenRate(this);

        Log.d(TAG, "onCreate: previewRate:" + previewRate);

        surfaceView.setLayoutParams(params);

        mMainHandler = new MainHandler();
        googleFaceDetect = new GoogleFaceDetect(getApplicationContext(), mMainHandler);

        shutterBtn.setOnClickListener(new BtnListeners());
        switchBtn.setOnClickListener(new BtnListeners());
        mMainHandler.sendEmptyMessageDelayed(EventUtil.CAMERA_HAS_STARTED_PREVIEW, 1500);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.camera, menu);
        return true;
    }

    private class BtnListeners implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            // TODO Auto-generated method stub
            switch (v.getId()) {
                case R.id.btn_shutter:
                    takePicture();
                    break;
                case R.id.btn_switch:
                    switchCamera();
                    break;
                default:
                    break;
            }
        }

    }

    private class MainHandler extends Handler {

        @Override
        public void handleMessage(Message msg) {
            // TODO Auto-generated method stub
            switch (msg.what) {
                case EventUtil.UPDATE_FACE_RECT:
                    Camera.Face[] faces = (Camera.Face[]) msg.obj;
                    faceView.setFaces(faces);
                    surfaceView.dosth();
                    break;
                case EventUtil.CAMERA_HAS_STARTED_PREVIEW:
                    startGoogleFaceDetect();
                    break;
            }
            super.handleMessage(msg);
        }

    }

    private void takePicture() {
        CameraInterface.getInstance().doTakePicture();
        mMainHandler.sendEmptyMessageDelayed(EventUtil.CAMERA_HAS_STARTED_PREVIEW, 1500);
    }

    private void switchCamera() {
        stopGoogleFaceDetect();
        int newId = (CameraInterface.getInstance().getCameraId() + 1) % 2;
        CameraInterface.getInstance().doStopCamera();
        CameraInterface.getInstance().doOpenCamera(null, newId);
        CameraInterface.getInstance().doStartPreview(surfaceView.getSurfaceHolder(), previewRate);
        mMainHandler.sendEmptyMessageDelayed(EventUtil.CAMERA_HAS_STARTED_PREVIEW, 1500);
//		startGoogleFaceDetect();

    }

    private void startGoogleFaceDetect() {
        Camera.Parameters params = CameraInterface.getInstance().getCameraParams();
        if (params.getMaxNumDetectedFaces() > 0) {
            if (faceView != null) {
                faceView.clearFaces();
                faceView.setVisibility(View.VISIBLE);
            }
            CameraInterface.getInstance().getCameraDevice().setFaceDetectionListener(googleFaceDetect);
            CameraInterface.getInstance().getCameraDevice().setPreviewCallback(mPreviewCallback);
            CameraInterface.getInstance().getCameraDevice().startFaceDetection();
        }
    }

    private void stopGoogleFaceDetect() {
        Camera.Parameters params = CameraInterface.getInstance().getCameraParams();
        if (params.getMaxNumDetectedFaces() > 0) {
            CameraInterface.getInstance().getCameraDevice().setFaceDetectionListener(null);
            CameraInterface.getInstance().getCameraDevice().stopFaceDetection();
            faceView.clearFaces();
        }
    }

    private Camera.PreviewCallback mPreviewCallback = new Camera.PreviewCallback() {
        @Override
        public void onPreviewFrame(byte[] data, Camera camera) {
//            Log.d(TAG, "onPreviewFrame() called with: data = [" + data + "], camera = [" + camera + "]");
            Camera.Size localSize = camera.getParameters().getPreviewSize();  //获得预览分辨率
            YuvImage localYuvImage = new YuvImage(data, 17, localSize.width, localSize.height, null);
            ByteArrayOutputStream localByteArrayOutputStream = new ByteArrayOutputStream();
            //把摄像头回调数据转成YUV，再按图像尺寸压缩成JPEG，从输出流中转成数组
            localYuvImage.compressToJpeg(new Rect(0, 0, localSize.width, localSize.height), 100, localByteArrayOutputStream);
            byte[] mParamArrayOfByte = localByteArrayOutputStream.toByteArray();
            //生成Bitmap
            BitmapFactory.Options localOptions = new BitmapFactory.Options();
            localOptions.inMutable = true;
            localOptions.inPreferredConfig = Bitmap.Config.RGB_565;  //构造位图生成的参数，必须为565。类名+enum
            Bitmap src = BitmapFactory.decodeByteArray(mParamArrayOfByte, 0, mParamArrayOfByte.length, localOptions);

            src = ImageUtil.getRotateBitmap(src, -90.0f);

            int width = src.getWidth();
            int height = src.getHeight();
            int[] srcData = new int[width * height];
            src.getPixels(srcData, 0, width, 0, 0, width, height);

            int r = 0, g = 0, b = 0, offset = width - width * 4;
            double Y = 0, I = 0, Q = 0;
            double k = 0;

            for (int j = 0; j < height; j++) {

                for (int i = 0; i < width; i++) {

//                    int argb = src.getPixel(i, j);
                    int argb = srcData[width*j+i];

                    b = Color.blue(argb);
                    g = Color.green(argb);
                    r = Color.red(argb);

                    ////////////////Process image...
                    //算法1
                    Y = 0.299 * r + 0.587 * g + 0.114 * b;
                    I = 0.596 * r - 0.275 * g - 0.321 * b;
                    Q = 0.212 * r - 0.523 * g + 0.311 * b;
                    if ((Y >= 80 && Y <= 220 && I >= 12 && I <= 78 && Q >= 7 && Q <= 25)) {
                        Log.d(TAG, "LipsDetectBmp: 检测到嘴唇颜色：" + r + " " + g + " " + b);
//                    src.setPixel(i,j, Color.parseColor("#00ff00"));
//                        src.setPixel(i, j, 123);
                        src.setPixel(i, j, 123);
                    }

                    //算法2 不准啊
//                    k = Math.log((double) g / (Math.pow((double) b, 0.391) * Math.pow((double) r, 0.609)));//使用算法                    ////2时把算法1注释掉即可
//                    if (k < -0.15) {
//                        Log.d(TAG, "LipsDetectBmp: 检测到嘴唇颜色：" + r + " " + g + " " + b);
//                        src.setPixel(i, j, 123);
//                    }
//                p += 4;
                }
//            p += offset;
            }

//            configBtp(mCurrentBitmap);
//            faker.setImageBitmap(mCurrentBitmap);
//            faker.setImageBitmap(LipsDetectBmp(src));
            faker.setImageBitmap(src);

        }
    };

    private void configBtp(Bitmap mCurrentBitmap) {
        int width = mCurrentBitmap.getWidth();
        int height = mCurrentBitmap.getHeight();
        int[] src = new int[width * height];

        mCurrentBitmap.getPixels(src, 0, width, 0, 0, width, height);

    }


    ///////////////////////////////////////////////////////////////////////////
// http://blog.csdn.net/trent1985/article/details/46330847
///////////////////////////////////////////////////////////////////////////
    public Bitmap LipsDetectBmp(Bitmap src) {

        int width = src.getWidth();

        int height = src.getHeight();

//        Bitmap bcccc = Bitmap.createBitmap(src, 0, 0, width, height);
        Bitmap a = src.copy(src.getConfig(), true);

//        Log.d(TAG, "LipsDetectBmp: "+bcccc.isMutable()+" "+a.isMutable());

        int[] srcData = new int[width * height];

        src.getPixels(srcData, 0, width, 0, 0, width, height);

//        BitmapData srcData = a.LockBits(new Rectangle(0, 0, a.Width, a.Height), ImageLockMode.ReadWrite, PixelFormat.Format32bppArgb);
//        byte p = (byte)srcData.Scan0;
//        int[] p = new int[3];

        int r = 0, g = 0, b = 0, offset = width - width * 4;
        double Y = 0, I = 0, Q = 0;
        double k = 0;

        Log.d(TAG, "LipsDetectBmp: width:" + width + ";height:" + height);
        for (int j = 0; j < height; j++) {

            for (int i = 0; i < width; i++) {

                int argb = a.getPixel(i, j);

                b = Color.blue(argb);
                g = Color.green(argb);
                r = Color.red(argb);

                ////////////////Process image...
                //算法1
                Y = 0.299 * r + 0.587 * g + 0.114 * b;
                I = 0.596 * r - 0.275 * g - 0.321 * b;
                Q = 0.212 * r - 0.523 * g + 0.311 * b;
                if ((Y >= 80 && Y <= 220 && I >= 12 && I <= 78 && Q >= 7 && Q <= 25)) {
                    Log.d(TAG, "LipsDetectBmp: 检测到嘴唇颜色：" + r + " " + g + " " + b);
                    Log.d(TAG, "LipsDetectBmp: i:" + i + ";j:" + j);
//                    a.setPixel(i,j, Color.parseColor("#00ff00"));
                    a.setPixel(i, j, 123);
                }
                //算法2
                //k = Math.Log((double)g / (Math.Pow((double)b, 0.391) * Math.Pow((double)r, 0.609)));//使用算法                    ////2时把算法1注释掉即可
                //if (k < -0.15)
                //{
                //    p[0] = (byte)255;
                //    p[1] = 0;
                //    p[2] = (byte)255;
                //}
//                p += 4;
            }
//            p += offset;
        }
//        a.UnlockBits(srcData);
//        try {
//            a.setPixels(srcData, 0, w, 0, 0, w, h);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
        return a;
    }

}
