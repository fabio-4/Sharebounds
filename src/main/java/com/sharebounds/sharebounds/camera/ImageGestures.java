package com.sharebounds.sharebounds.camera;

import android.content.Context;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.util.Pair;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

class ImageGestures {

    interface GesturesCallBack {
        void gestureValues(Matrix matrix);
        void imageTouchPoint(float[] touchPoint, GestureType type);
    }

    enum GestureType {
        SINGLE_TAP, DOUBLE_TAP, LONG_PRESS
    }

    private GesturesCallBack mImageGesturesCallBack;
    private float mImgW, mImgH, mImgVW, mImgVH;
    private Matrix mMatrix = new Matrix();
    private Matrix mFixRot = new Matrix();
    private Matrix mFixRotInv = new Matrix();
    private float[] mValues = new float[9];
    private float mSc, mCurrentSc;

    private ScaleGestureDetector mScaleGestureDetector;
    private GestureDetector mGestureDetector;

    ImageGestures(Context context, GesturesCallBack callBack) {
        mImageGesturesCallBack = callBack;
        mScaleGestureDetector = new ScaleGestureDetector(context, new ScaleGestureListener());
        mGestureDetector = new GestureDetector(context, new DragGestureListener());
    }

    void onTouch(View view, MotionEvent motionEvent) {
        mGestureDetector.onTouchEvent(motionEvent);
        mScaleGestureDetector.onTouchEvent(motionEvent);
        if (motionEvent.getAction() == MotionEvent.ACTION_UP) view.performClick();
    }

    RectF invertBoundingBox(RectF rect) {
        Matrix matrix = new Matrix(mFixRot);
        matrix.postConcat(mMatrix);
        matrix.mapRect(rect);
        return rect;
    }

    private float[] convertTouchPoint(MotionEvent event) {
        Matrix inverse = new Matrix();
        mMatrix.invert(inverse);
        inverse.postConcat(mFixRotInv);

        float[] touchPoint = new float[] {event.getX(), event.getY()};
        inverse.mapPoints(touchPoint);
        return touchPoint;
    }

    void setOriginValues(Matrix matrix, int imgW, int imgH, Pair<Integer, Integer> sizeValues,
                         float fixRotation) {
        mImgW = imgW; mImgH = imgH; mImgVW = sizeValues.first; mImgVH = sizeValues.second;
        mMatrix = matrix;
        matrix.getValues(mValues);
        mSc = Math.min(mValues[Matrix.MSCALE_X], mValues[Matrix.MSCALE_Y]);
        mCurrentSc = mSc;

        mFixRot = CameraUtils.fixRotationMatrix(fixRotation, imgW, imgH);
        mFixRot.invert(mFixRotInv);
    }

    private void translate(float dX, float dY, boolean scale) {
        mMatrix.getValues(mValues);
        float trX = mValues[Matrix.MTRANS_X];
        float trY = mValues[Matrix.MTRANS_Y];
        mCurrentSc = Math.min(mValues[Matrix.MSCALE_X], mValues[Matrix.MSCALE_Y]);

        float imgW = mCurrentSc * mImgW;
        float imgH = mCurrentSc * mImgH;

        if (imgW < mImgVW) {
            if (scale) {
                dX = (mImgVW - imgW) / 2 - trX;
            } else {
                dX = 0;
            }
        }
        else if (trX + dX > 0) dX = -trX;
        else if (trX + imgW + dX < mImgVW) dX = mImgVW - (trX + imgW);

        if (imgH < mImgVH) {
            if (scale) {
                dY = (mImgVH - imgH) / 2 - trY;
            } else {
                dY = 0;
            }
        }
        else if (trY + dY > 0) dY = -trY;
        else if (trY + imgH + dY < mImgVH) dY = mImgVH - (trY + imgH);

        if (dX != 0 || dY != 0 || scale) {
            mMatrix.postTranslate(dX, dY);
            mImageGesturesCallBack.gestureValues(mMatrix);
        }
    }

    private class ScaleGestureListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        private float mLastFocusX;
        private float mLastFocusY;
        private float maxZoom(){
            return 4.0f * mSc;
        }

        @Override
        public boolean onScaleBegin(ScaleGestureDetector detector) {
            mLastFocusX = detector.getFocusX();
            mLastFocusY = detector.getFocusY();
            return true;
        }

        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            float scale = Math.max(0.1f, Math.min(detector.getScaleFactor(), 5.0f));

            if (scale < 1.0 && mCurrentSc == mSc) return true;
            else if (scale * mCurrentSc - mSc < 0) scale = mSc / mCurrentSc;
            else if (mCurrentSc * scale > maxZoom()) scale = maxZoom() / mCurrentSc;

            float focusX = detector.getFocusX();
            float focusY = detector.getFocusY();

            mMatrix.postScale(scale, scale, focusX, focusY);
            translate(focusX - mLastFocusX, focusY - mLastFocusY, true);

            mLastFocusX = focusX;
            mLastFocusY = focusY;
            return true;
        }
    }

    private class DragGestureListener extends GestureDetector.SimpleOnGestureListener {
        private MotionEvent doubleTap;

        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            mImageGesturesCallBack.imageTouchPoint(convertTouchPoint(e), GestureType.SINGLE_TAP);
            return true;
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            doubleTap = e;
            return true;
        }

        @Override
        public boolean onDoubleTapEvent(MotionEvent e) {
            if (e.getActionMasked() == MotionEvent.ACTION_UP && doubleTap != null){
                float[] firstTouch = convertTouchPoint(doubleTap);
                float[] secondTouch = convertTouchPoint(e);
                float[] touchPoints = new float[] {firstTouch[0], firstTouch[1],
                        secondTouch[0], secondTouch[1]};
                mImageGesturesCallBack.imageTouchPoint(touchPoints, GestureType.DOUBLE_TAP);
                doubleTap = null;
            }
            return true;
        }

        @Override
        public void onLongPress(MotionEvent e) {
            if (doubleTap == null) mImageGesturesCallBack.imageTouchPoint(convertTouchPoint(e),
                    GestureType.LONG_PRESS);
            super.onLongPress(e);
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            if (!mScaleGestureDetector.isInProgress() && mCurrentSc > mSc) {
                translate(-distanceX, -distanceY, false);
            }
            return true;
        }
    }
}
