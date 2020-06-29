package com.sharebounds.sharebounds.camera;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.RectF;
import android.os.AsyncTask;

import java.lang.ref.WeakReference;
import java.util.List;

class OcrTask {

    private final Object mDetectorLock = new Object();
    private OcrDetector mOcrDetector;
    private BitmapUtils mBitmapUtils;
    private OcrCameraController mOcrCameraController;
    private OcrDetectorTask mOcrDetectorTask;

    OcrTask(Context context, OcrCameraController cameraController) {
        mOcrDetector = new OcrDetector(context);
        mBitmapUtils = new BitmapUtils(context);
        mOcrCameraController = cameraController;
    }

    void setup() {
        mOcrDetector.setup();
    }

    void release() {
        synchronized (mDetectorLock) {
            cancelTask();
            mOcrDetector.release();
        }
    }

    void setTheme(int theme) {
        mBitmapUtils.setTheme(theme);
    }

    void startTask(ImageModel imageModel) {
        mOcrDetectorTask = new OcrDetectorTask(this, imageModel);
        mOcrDetectorTask.execute();
    }

    private boolean isRunning() {
        return (mOcrDetectorTask != null && mOcrDetectorTask.getStatus() != AsyncTask.Status.FINISHED);
    }

    void cancelTask() {
        if (isRunning()) {
            mOcrDetectorTask.cancel(true);
            mOcrDetectorTask = null;
        }
    }

    private void setControllerModel(final ImageModel model) {
        mOcrCameraController.setImageModel(model);
        mOcrDetectorTask = null;
    }

    private static class OcrDetectorTask extends AsyncTask<Float, Void, ImageModel> {

        private final WeakReference<OcrTask> mOcrTaskRef;
        private final ImageModel mImageModel;

        OcrDetectorTask(OcrTask ocrTask, ImageModel imageModel) {
            this.mOcrTaskRef = new WeakReference<>(ocrTask);
            this.mImageModel = imageModel;
        }

        @Override
        protected ImageModel doInBackground(Float... floats) {
            OcrTask ocrTask = mOcrTaskRef.get();
            if (ocrTask != null) {
                byte[] bytes = mImageModel.bytes;
                Bitmap bitmap = mImageModel.bitmap;
                float fixRotation = mImageModel.imageData.fixRotation;

                if (bytes != null) {
                    bitmap = BitmapUtils.convertToBitmap(bytes, mImageModel.imageData);
                } else if (!bitmap.isMutable()) {
                    bitmap = BitmapUtils.mutableCopy(bitmap);
                }
                mImageModel.bitmap = bitmap;

                synchronized (ocrTask.mDetectorLock) {
                    OcrDetector.Status detectorStatus = ocrTask.mOcrDetector.getStatus();
                    if (isCancelled() || detectorStatus == OcrDetector.Status.Released) return null;
                    else if (detectorStatus == OcrDetector.Status.Available)
                        mImageModel.setTextBlocks(ocrTask.mOcrDetector.detect(bitmap, -fixRotation));
                }
                List<RectF> lines = mImageModel.getTextLines();
                if (lines != null && lines.size() != 0) {
                    ocrTask.mBitmapUtils.drawImageRect(bitmap, lines);
                }
                return mImageModel;
            }
            return null;
        }

        @Override
        protected void onPostExecute(ImageModel imageModel) {
            super.onPostExecute(imageModel);
            OcrTask ocrTask = mOcrTaskRef.get();
            if (ocrTask != null && imageModel != null && !isCancelled()) {
                ocrTask.setControllerModel(imageModel);
            }
        }
    }
}
