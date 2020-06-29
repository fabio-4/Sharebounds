package com.sharebounds.sharebounds.camera;

import android.content.Context;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

class CameraGestures {

    interface GesturesCallBack {
        void cameraZoom(float scale);
        void cameraTap();
    }

    private GesturesCallBack mGesturesCallBack;
    private ScaleGestureDetector mScaleGestureDetector;
    private GestureDetector mTapGestureListener;

    CameraGestures(Context context, GesturesCallBack callBack) {
        mGesturesCallBack = callBack;
        mScaleGestureDetector = new ScaleGestureDetector(context, new ScaleGestureListener());
        mTapGestureListener = new GestureDetector(context, new TapGestureListener());
    }

    void onTouch(View view, MotionEvent motionEvent) {
        mScaleGestureDetector.onTouchEvent(motionEvent);
        mTapGestureListener.onTouchEvent(motionEvent);
        if (motionEvent.getAction() == MotionEvent.ACTION_UP) view.performClick();
    }

    private class ScaleGestureListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            float scale = Math.max(0.1f, Math.min(detector.getScaleFactor(), 5.0f));
            mGesturesCallBack.cameraZoom(scale);
            return true;
        }
    }

    private class TapGestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            mGesturesCallBack.cameraTap();
            return super.onSingleTapConfirmed(e);
        }
    }
}
