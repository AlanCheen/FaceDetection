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
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.hardware.Camera;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * Created by 程序亦非猿 on 16/12/2.
 */
public class FaceView extends ImageView {

    private Context mContext;
    private Camera.Face[] mFaces;
    private Matrix mMatrix = new Matrix();
    private RectF mRect = new RectF();
    private Drawable mFaceIndicator = null;
    Paint mPaint;
    public FaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        mFaceIndicator = getResources().getDrawable(R.drawable.ic_face_find_1);
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setColor(Color.BLUE);

    }

    public void setFaces(Camera.Face[] faces) {
        this.mFaces = faces;
        invalidate();
    }

    public void clearFaces() {
        mFaces = null;
        invalidate();
    }


    @Override
    protected void onDraw(Canvas canvas) {
        if (mFaces == null || mFaces.length < 1) {
            return;
        }
        boolean isMirror = false;
        int Id = CameraInterface.getInstance().getCameraId();
        if (Id == Camera.CameraInfo.CAMERA_FACING_BACK) {
            isMirror = false; //后置Camera无需mirror
        } else if (Id == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            isMirror = true;  //前置Camera需要mirror
        }
        Util.prepareMatrix(mMatrix, isMirror, 90, getWidth(), getHeight());
        canvas.save();
        mMatrix.postRotate(0); //Matrix.postRotate默认是顺时针
        canvas.rotate(-0);   //Canvas.rotate()默认是逆时针

        for (int i = 0; i < mFaces.length; i++) {
            mRect.set(mFaces[i].rect);
            mMatrix.mapRect(mRect);
            mFaceIndicator.setBounds(Math.round(mRect.left), Math.round(mRect.top),
                    Math.round(mRect.right), Math.round(mRect.bottom));
            mFaceIndicator.draw(canvas);
            if (null != mFaces[i].mouth) {
                canvas.drawPoint(mFaces[i].mouth.x,mFaces[i].mouth.y,mPaint);
            }
        }
        canvas.restore();
        super.onDraw(canvas);
    }

}