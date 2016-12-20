/*
 * Copyright (C) 2016, 程序亦非猿
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package me.yifeiyuan.facedetection;

import android.content.Context;
import android.hardware.Camera;
import android.hardware.Camera.FaceDetectionListener;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

/**
 * Created by 程序亦非猿 on 16/12/2.
 */
public class FaceDectListener implements FaceDetectionListener {

    private static final String TAG = "FaceDectListener";

    private Context mContext;
    private Handler mHander;

    public FaceDectListener(Context context, Handler hander) {
        mContext = context;
        mHander = hander;
    }

    @Override
    public void onFaceDetection(Camera.Face[] faces, Camera camera) {
        //检测结果集合大小为0,表示未检测到人脸
        Log.v(TAG, "人脸数量："+faces.length);
        //发送结果消息
        Message m = mHander.obtainMessage();
        m.what = 1;
        m.obj = faces;
        m.sendToTarget();
    }
}
