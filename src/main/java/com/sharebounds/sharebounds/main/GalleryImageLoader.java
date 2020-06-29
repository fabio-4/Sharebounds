package com.sharebounds.sharebounds.main;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Pair;
import android.widget.Toast;

import com.sharebounds.sharebounds.R;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.lang.ref.WeakReference;

class GalleryImageLoader {

    private MainActivity mMainActivity;
    private static String FILE_NAME = "sb-RestoredStateImage-00";

    RestoreBytesTask restoreBytesTask;

    GalleryImageLoader(MainActivity activity) {
        mMainActivity = activity;
    }

    void cancelRequest() {
        Picasso.get().cancelRequest(mImageTarget);
        cancelRestoreTask();
    }

    private void cancelRestoreTask() {
        if (restoreBytesTask != null && restoreBytesTask.getStatus() != AsyncTask.Status.FINISHED) {
            restoreBytesTask.cancel(false);
            restoreBytesTask = null;
        }
    }

    private void errorToast(String error) {
        Toast.makeText(mMainActivity, error, Toast.LENGTH_LONG).show();
    }

    void loadSetImage(Uri uri, Pair<Integer, Integer> imageViewSize) {
        cancelRequest();
        int maxSize = (imageViewSize.first == 0) ? 1920 :
                (Math.max(imageViewSize.first, imageViewSize.second));
        Picasso.get().load(uri).memoryPolicy(MemoryPolicy.NO_CACHE)
                .resize(maxSize, maxSize).centerInside().onlyScaleDown().into(mImageTarget);
    }

    private Target mImageTarget = new Target() {
        @Override
        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
            mMainActivity.setImage(bitmap, null);
        }

        @Override
        public void onBitmapFailed(Exception e, Drawable errorDrawable) {
            errorToast(mMainActivity.getResources().getString(R.string.img_loading_error));
            if (!mMainActivity.isFinishing()) {
                mMainActivity.setImage(null, null);
            }
        }

        @Override
        public void onPrepareLoad(Drawable placeHolderDrawable) {}
    };

    void saveImage(final byte[] bytes) {
        final Context appContext = mMainActivity.getApplicationContext();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    FileOutputStream outputStream = appContext.openFileOutput(FILE_NAME, Context.MODE_PRIVATE);
                    outputStream.write(bytes);
                    outputStream.flush();
                    outputStream.close();
                } catch (Exception e) {
                    //
                }
            }
        }).start();
    }

    void restoreImage() {
        cancelRequest();
        restoreBytesTask = new RestoreBytesTask(mMainActivity);
        restoreBytesTask.execute();
    }

    private static class RestoreBytesTask extends AsyncTask<Void, Void, byte[]>{

        private final WeakReference<MainActivity> mMainActivityRef;

        RestoreBytesTask(MainActivity mainActivity) {
            mMainActivityRef = new WeakReference<>(mainActivity);
        }

        @Override
        protected byte[] doInBackground(Void... voids) {
            try {
                MainActivity activityRef = mMainActivityRef.get();
                if (activityRef != null && !activityRef.isFinishing()) {
                    FileInputStream inputStream = activityRef.openFileInput(FILE_NAME);
                    byte[] bytes = new byte[(int) inputStream.getChannel().size()];
                    inputStream.read(bytes);
                    inputStream.close();
                    activityRef.deleteFile(FILE_NAME);
                    return bytes;
                }
            } catch (Exception e) {
                //
            }
            return null;
        }

        @Override
        protected void onPostExecute(byte[] bytes) {
            super.onPostExecute(bytes);
            MainActivity activityRef = mMainActivityRef.get();
            if (activityRef != null && !activityRef.isFinishing() && !isCancelled())
                activityRef.setImage(null, bytes);
        }
    }
}
