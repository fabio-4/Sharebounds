package com.sharebounds.sharebounds.camera;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.support.constraint.ConstraintLayout;
import android.util.Pair;
import android.view.MotionEvent;
import android.view.View;

import com.sharebounds.sharebounds.R;

public class OcrCameraController implements View.OnClickListener, View.OnTouchListener,
        ImageGestures.GesturesCallBack, CameraGestures.GesturesCallBack, OcrCamera.PictureCallback {

    public interface OcrCameraListener {
        void onNewOcrText(String text);
        void onCameraEvent(boolean on);
    }

    private boolean mRotateImage = true;
    private int currentRotation = 0;
    private OcrCameraListener mListener;
    private OcrCamera mOcrCamera;
    private OcrTask mOcrTask;
    private ImageGestures mImageGestures;
    private CameraGestures mCameraGestures;
    private BitmapImageView mBitmapImageView;
    private CameraTextureView mCameraTextureView;
    private CaptureButton mCaptureButton;

    private boolean cameraIsRunning = true;
    private void setCameraIsRunning(boolean bool) {
        if (bool != cameraIsRunning) {
            mCaptureButton.nextMode(bool);
            mListener.onCameraEvent(bool);
            cameraIsRunning = bool;
        }
    }

    private ImageModel currentImageModel;
    public ImageData getParcelableData() {
        if (currentImageModel == null) return null;
        return currentImageModel.imageData;
    }
    public byte[] getBytes() {
        if (currentImageModel == null) return null;
        return currentImageModel.bytes;
    }
    public Pair<Integer, Integer> getImageViewSize() {
        int width = (mBitmapImageView.getLayoutParams().width == -1
                || mBitmapImageView.getLayoutParams().width == 0)
                ? mBitmapImageView.getWidth() : mBitmapImageView.getLayoutParams().width;
        int height = (mBitmapImageView.getLayoutParams().height == -1
                || mBitmapImageView.getLayoutParams().height == 0)
                ? mBitmapImageView.getHeight() : mBitmapImageView.getLayoutParams().height;
        return new Pair<>(width, height);
    }

    public OcrCameraController(Context context, ConstraintLayout constraintLayout) {
        this(context, constraintLayout, true);
    }

    public OcrCameraController(Context context, ConstraintLayout constraintLayout, boolean rotateImage) {
        if (context instanceof OcrCameraListener) {
            mListener = (OcrCameraListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OcrCameraTextListener");
        }
        mRotateImage = rotateImage;
        mBitmapImageView = constraintLayout.findViewById(R.id.camera_image_view);
        mCaptureButton = constraintLayout.findViewById(R.id.capture_button);
        mCaptureButton.setOnClickListener(this);
        constraintLayout.findViewById(R.id.flash_button).setOnClickListener(this);

        mImageGestures = new ImageGestures(context, this);
        mBitmapImageView.setOnTouchListener(this);

        mCameraGestures = new CameraGestures(context, this);
        mCameraTextureView = constraintLayout.findViewById(R.id.camera_texture);
        mCameraTextureView.setOnTouchListener(this);

        CameraAnimationView cameraAnimationView = constraintLayout.findViewById(R.id.camera_animation);
        mOcrCamera = new OcrCamera(context, this, mCameraTextureView, cameraAnimationView);
        mOcrTask = new OcrTask(context, this);
    }

    public void setup() {
        mOcrTask.setup();
    }

    public void release() {
        mOcrTask.release();
    }

    public void destroy() {
        mListener = null;
    }

    public void setTheme(int theme) {
        mOcrTask.setTheme(theme);
        mBitmapImageView.setTheme(theme);
    }

    public void onRotation(final int newRotation) {
        currentRotation = newRotation;
        if (mRotateImage && !cameraIsRunning) {
            mBitmapImageView.endAnimation();
            if (mCameraTextureView.getAlpha() == 1.0f) mCameraTextureView.reset();
            if (currentImageModel != null) setImageModel(currentImageModel);
        }
    }

    public void resumeCamera(boolean start) {
        if (cameraIsRunning || start) {
            boolean cameraStarted = mOcrCamera.start();
            setCameraIsRunning(cameraStarted);
        }
    }

    public void pauseCamera(boolean stop) {
        if (cameraIsRunning || stop) {
            mOcrCamera.stop();
            if (stop) setCameraIsRunning(false);
        }
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        if (view == mCameraTextureView && cameraIsRunning)
            mCameraGestures.onTouch(view, motionEvent);
        else if (view == mBitmapImageView && !cameraIsRunning)
            mImageGestures.onTouch(view, motionEvent);
        else if (motionEvent.getAction() == MotionEvent.ACTION_UP) view.performClick();
        return true;
    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.flash_button:
                mOcrCamera.setFlashMode(((FlashButton) view).nextMode());
                break;
            case R.id.capture_button:
                mOcrTask.cancelTask();
                currentImageModel = null;
                if (cameraIsRunning) {
                    mOcrCamera.takePicture();
                } else {
                    mBitmapImageView.reset();
                    mCameraTextureView.visible(true);
                    resumeCamera(true);
                }
                break;
        }
    }

    public void onPictureTaken(byte[] bytes, int rotation, float[] matrixValues) {
        pauseCamera(true);
        final float rot = -currentRotation + rotation;
        ImageData imageData = new ImageData(null, rot, matrixValues);
        if (!mRotateImage) {
            imageData.fixRotation = currentRotation;
            imageData.portrait = false;
        }
        processImage(null, bytes, imageData);
    }

    public void processImage(final Bitmap bitmap, final byte[] bytes, final ImageData imageData) {
        mOcrTask.cancelTask();

        if (bitmap != null || bytes != null) {
            ImageModel model = new ImageModel(bitmap, bytes, imageData);
            mOcrTask.startTask(model);
        } else {
            resumeCamera(true);
        }
    }

    void setImageModel(ImageModel model) {
        Bitmap bitmap = model.bitmap;
        float fixRot = model.imageData.fixRotation;
        if (!cameraIsRunning && bitmap != null) {
            currentImageModel = model;
            Pair<Integer, Integer> sizeValues = getImageViewSize();
            Matrix rotM = mBitmapImageView.setImageViewBitmap(bitmap, sizeValues);
            mImageGestures.setOriginValues(rotM, bitmap.getWidth(), bitmap.getHeight(), sizeValues, fixRot);
            mCameraTextureView.visible(false);
        }
    }

    @Override
    public void gestureValues(Matrix matrix) {
        mBitmapImageView.endAnimation();
        mBitmapImageView.setImageMatrix(matrix);
        mBitmapImageView.invalidate();
    }

    @Override
    public void imageTouchPoint(float[] touchPoint, ImageGestures.GestureType type) {
        if (mBitmapImageView.animatorSet.isRunning()) return;
        OcrData textData = currentImageModel.getGestureText(touchPoint, type);
        if (textData != null && mListener != null) {
            mBitmapImageView.animate(mImageGestures.invertBoundingBox(textData.boundingBox));
            mListener.onNewOcrText(CameraUtils.editString(textData.text, type));
        }
    }

    @Override
    public void cameraZoom(float scale) {
        mOcrCamera.zoom(scale);
    }

    @Override
    public void cameraTap() {
        mOcrCamera.resetAutoFocus();
    }
}
