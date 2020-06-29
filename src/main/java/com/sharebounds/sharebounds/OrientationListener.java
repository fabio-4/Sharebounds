package com.sharebounds.sharebounds;

import android.content.Context;
import android.view.OrientationEventListener;
import android.view.Surface;
import android.view.WindowManager;

public class OrientationListener extends OrientationEventListener {

    public interface OrientationCallBack {
        void onRotation(int oldRotation, int newRotation);
    }

    private int mOldRotation = 0;
    private OrientationCallBack mCallBack;

    private int mROT_0 = 0;
    private int mROT_90 = 90;
    private int mROT_180 = 180;
    private int mROT_270 = 270;

    public OrientationListener(Context context, int rate,
                        OrientationCallBack callBack) {
        super(context, rate);
        mCallBack = callBack;
        setup(context);
    }

    public void setup(Context context) {
        int origin = getDisplayRotation(context);
        if (origin == 270) {
            mROT_0 = 90; mROT_90 = 180; mROT_180 = 270; mROT_270 = 0;
        } else if (origin == 90) {
            mROT_0 = 270; mROT_90 = 0; mROT_180 = 90; mROT_270 = 180;
        } else if (origin == 180) {
            mROT_0 = 180; mROT_90 = 270; mROT_180 = 0; mROT_270 = 90;
        } else {
            mROT_0 = 0; mROT_90 = 90; mROT_180 = 180; mROT_270 = 270;
        }
        mOldRotation = 0;
    }

    public void release() {
        mCallBack = null;
    }

    @Override
    public void onOrientationChanged(int i) {
        newOrientation(i);
    }

    private void newOrientation(int i) {
        int newRotation = mOldRotation;

        if (i == OrientationEventListener.ORIENTATION_UNKNOWN) return;

        if((i < 25 || i > 335)){
            newRotation = mROT_0;
        }
        else if( i > 155 && i < 205 ){
            newRotation = mROT_180;
        }
        else if(i > 65 && i < 115){
            newRotation = mROT_270;
        }
        else if(i > 245 && i < 295){
            newRotation = mROT_90;
        }

        if (newRotation != mOldRotation) {
            int animRotation = newRotation;
            if ((newRotation - mOldRotation) == 270) {
                animRotation = -90;
            } else if ((newRotation - mOldRotation) == -270) {
                animRotation = 360;
            }

            if (mCallBack != null) mCallBack.onRotation(mOldRotation, animRotation);
            mOldRotation = newRotation;
        }
    }

    public static int getDisplayRotation(Context context) {
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        int rotation = Surface.ROTATION_0;
        if (windowManager != null){
            rotation = windowManager.getDefaultDisplay().getRotation();
        }
        switch (rotation) {
            case Surface.ROTATION_0:
                return 0;
            case Surface.ROTATION_90:
                return 90;
            case Surface.ROTATION_180:
                return 180;
            case Surface.ROTATION_270:
                return 270;
        }
        return 0;
    }
}
