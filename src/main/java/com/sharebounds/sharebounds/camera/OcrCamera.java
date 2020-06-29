package com.sharebounds.sharebounds.camera;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.view.TextureView;
import android.widget.Toast;

import com.sharebounds.sharebounds.AppSettings;
import com.sharebounds.sharebounds.OrientationListener;
import com.sharebounds.sharebounds.PermissionUtils;

import java.io.IOException;
import java.util.List;

class OcrCamera implements Camera.ShutterCallback, Camera.PictureCallback {

    interface PictureCallback {
        void onPictureTaken(byte[] bytes, int rotation, float[] matrixValues);
    }

    private enum FocusMode { Auto, Continuous, None }
    private FocusMode mFocusMode = FocusMode.None;

    private final Object mCameraLock = new Object();
    private Handler mMainHandler = new Handler(Looper.getMainLooper());
    private Thread mCameraStartThread;

    private Context mContext;
    private Camera mCamera;
    private boolean mCameraRunning = false;
    private double mAspectRatio = 1.0;
    private double mPictureAspectRatio = 1.0;
    private int mRotation = 0;
    private int mFlashMode;
    private float[] mPictureMatrixValues;
    private OcrCamera.PictureCallback mPictureCallback;
    private CameraTextureView mTextureView;
    private CameraAnimationView mCameraAnimationView;
    private boolean mWaitingForPicture = false;

    private TextureView.SurfaceTextureListener mSurfaceTextureListener =
            new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int i, int i1) {
            startCameraPreview(surfaceTexture);
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int i, int i1) {
            setScaleValues();
        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
            stop();
            return true;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {
        }
    };

    OcrCamera(Context context, OcrCamera.PictureCallback pictureCallback, CameraTextureView textureView,
              CameraAnimationView cameraAnimationView) {
        mFlashMode = AppSettings.getInstance(context.getApplicationContext()).getFlashMode();
        mContext = context;
        mPictureCallback = pictureCallback;
        mTextureView = textureView;
        mCameraAnimationView = cameraAnimationView;
    }

    boolean start() {
        if (mCamera != null || mCameraStartThread != null) {
            return true;
        }
        if (mContext.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA) &&
                PermissionUtils.getPermission(mContext, PermissionUtils.Type.Camera)) {
            synchronized (mCameraLock) {
                mCameraStartThread = new Thread(new CameraLoader());
                mCameraStartThread.start();
                return true;
            }
        } else {
            PermissionUtils.errorToast(mContext, PermissionUtils.Type.Camera);
        }
        return false;
    }

    private void startCameraPreview(SurfaceTexture surfaceTexture) {
        synchronized (mCameraLock) {
            if (mCameraRunning) return;
            try {
                if (mCamera != null) {
                    mCamera.setPreviewTexture(surfaceTexture);
                    mCamera.startPreview();
                    resetAutoFocus();
                    setScaleValues();
                    mCameraRunning = true;
                    return;
                }
            } catch (final IOException e) {
                mMainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(mContext, e.toString(), Toast.LENGTH_LONG).show();
                    }
                });
            }
            mCameraRunning = false;
        }
    }

    private void setScaleValues() {
        synchronized (mCameraLock) {
            mTextureView.scalePreview(mAspectRatio, mRotation);
            mPictureMatrixValues = new float[9];
            mTextureView.scaleMatrix(mPictureAspectRatio, mRotation).getValues(mPictureMatrixValues);
        }
    }

    void stop() {
        synchronized (mCameraLock) {
            if (mCameraStartThread != null && !mCameraStartThread.isInterrupted()) {
                mCameraStartThread.interrupt();
                mCameraStartThread = null;
            }
            releaseCamera();
        }
    }

    private void releaseCamera() {
        if (mCamera != null) {
            mCamera.stopPreview();
            mCameraRunning = false;
            mTextureView.setSurfaceTextureListener(null);
            mCamera.release();
            mCamera = null;
        }
    }

    void resetAutoFocus() {
        synchronized (mCameraLock) {
            if (mCamera == null || !mCameraRunning) return;
            switch (mFocusMode) {
                case Continuous:
                    mCamera.cancelAutoFocus();
                    break;
                case Auto:
                    mCamera.autoFocus(null);
                    break;
            }
        }
    }

    void setFlashMode(int value) {
        synchronized (mCameraLock) {
            mFlashMode = value;
            String flashMode = FlashButton.FlashMode.convertToString(value);
            if (mCamera == null) return;
            Camera.Parameters params = mCamera.getParameters();
            List<String> flashList = params.getSupportedFlashModes();
            if (flashList == null) return;
            if (flashList.contains(flashMode)) {
                params.setFlashMode(flashMode);
                mCamera.setParameters(params);
            }
        }
    }

    void zoom(float scale) {
        synchronized (mCameraLock) {
            if (mCamera == null) return;
            Camera.Parameters params = mCamera.getParameters();
            if (params.isZoomSupported()) {
                int maxZoom = params.getMaxZoom();
                int currentZoom = params.getZoom();
                currentZoom += (scale > 1.0) ? 1 : -1;
                if (currentZoom > 0 && currentZoom < maxZoom) {
                    params.setZoom(currentZoom);
                    mCamera.cancelAutoFocus();
                    mCamera.setParameters(params);
                }
            }
        }
    }

    void takePicture() {
        synchronized (mCameraLock) {
            if (mCamera == null || !mCameraRunning || mWaitingForPicture) return;
            mWaitingForPicture = true;
            if (mFocusMode == FocusMode.Auto) {
                mCamera.autoFocus(mAutoFocusCallback);
            } else {
                mCamera.takePicture(this, null, this);
            }
            mCameraAnimationView.shutter();
        }
    }

    private Camera.AutoFocusCallback mAutoFocusCallback = new Camera.AutoFocusCallback() {
        @Override
        public void onAutoFocus(boolean b, Camera camera) {
            synchronized (mCameraLock) {
                if (camera != null && mCameraRunning)
                    camera.takePicture(null, null, OcrCamera.this);
            }
        }
    };

    @Override
    public void onPictureTaken(byte[] bytes, Camera camera) {
        synchronized (mCameraLock) {
            mPictureCallback.onPictureTaken(bytes, mRotation, mPictureMatrixValues);
            mWaitingForPicture = false;
        }
    }

    @Override
    public void onShutter() {}

    private class CameraLoader implements Runnable {
        private boolean mMuteShutter = false;

        @Override
        public void run() {
            synchronized (mCameraLock) {
                if (Thread.currentThread().isInterrupted()) return;
                final Exception e = openCamera();
                mMainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (e == null) setupTexture();
                        else Toast.makeText(mContext, e.toString(), Toast.LENGTH_LONG).show();
                    }
                });
                mCameraStartThread = null;
            }
        }

        private void setupTexture() {
            if (mTextureView.isAvailable()) startCameraPreview(mTextureView.getSurfaceTexture());
            mTextureView.setSurfaceTextureListener(mSurfaceTextureListener);
        }

        private Exception openCamera() {
            try {
                int id = findCamera();
                if (id != -1) mCamera = Camera.open(id);
                if (mCamera != null) {
                    setupCamera();
                }
                return null;
            } catch (Exception e) {
                return e;
            }
        }

        private int findCamera() {
            int currentRotation = OrientationListener.getDisplayRotation(mContext);
            mRotation = 90;

            int numberOfCameras = Camera.getNumberOfCameras();
            for (int i = 0; i < numberOfCameras; i++) {
                Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
                Camera.getCameraInfo(i, cameraInfo);
                if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                    if (Build.VERSION.SDK_INT >= 17) mMuteShutter = cameraInfo.canDisableShutterSound;
                    mRotation = (cameraInfo.orientation - currentRotation + 360) % 360;
                    return i;
                }
            }
            return -1;
        }

        private void setupCamera() {
            mCamera.setDisplayOrientation(mRotation);
            if (Build.VERSION.SDK_INT >= 17 && mMuteShutter) mCamera.enableShutterSound(false);
            Camera.Parameters params = mCamera.getParameters();
            setCameraSizes(params);
            setFocusMode(params);
            mCamera.setParameters(params);
            setFlashMode(mFlashMode);
            mWaitingForPicture = false;
        }

        private void setCameraSizes(Camera.Parameters params) {
            int width = mTextureView.getWidth();
            if (width == 0) width = Resources.getSystem().getDisplayMetrics().widthPixels;
            int height = mTextureView.getLayoutParams().height;
            if (height == 0) height = mTextureView.getHeight();
            if (height == 0) height = Resources.getSystem().getDisplayMetrics().heightPixels;

            if (mRotation == 0 || mRotation == 180) {
                int tmp = width;
                width = height;
                height = tmp;
            }
            List<Camera.Size> previewSizes = params.getSupportedPreviewSizes();
            List<Camera.Size> pictureSizes = params.getSupportedPictureSizes();
            List<Camera.Size> intersection = CameraUtils.intersection(previewSizes, pictureSizes);

            Camera.Size previewSize;
            Camera.Size pictureSize;
            if (intersection.size() > 2) {
                previewSize = CameraUtils.chooseOptimalSize(intersection, width, height);
                pictureSize = previewSize;
            } else {
                previewSize = CameraUtils.chooseOptimalSize(previewSizes, width, height);

                if (pictureSizes.contains(previewSize)) {
                    pictureSize = previewSize;
                } else {
                    pictureSize = CameraUtils.chooseOptimalSize(pictureSizes, previewSize.width, previewSize.height);
                }
            }
            params.setPreviewSize(previewSize.width, previewSize.height);
            params.setPictureSize(pictureSize.width, pictureSize.height);
            mAspectRatio = (double) previewSize.height / (double) previewSize.width;
            mPictureAspectRatio = (double) pictureSize.height / (double) pictureSize.width;
        }

        private void setFocusMode(Camera.Parameters params) {
            List<String> focusModes = params.getSupportedFocusModes();
            if (focusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
                params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
                mFocusMode = FocusMode.Continuous;
            } else if (focusModes.contains(Camera.Parameters.FOCUS_MODE_AUTO)) {
                params.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
                mFocusMode = FocusMode.Auto;
            }
        }
    }
}
