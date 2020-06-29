package com.sharebounds.sharebounds.camera;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.SparseArray;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;
import com.sharebounds.sharebounds.R;

class OcrDetector {

    enum Status { Available, Released, Failed }

    private Status mDetectorStatus = Status.Failed;

    Status getStatus() {
        return mDetectorStatus;
    }

    private Context mContext;
    private TextRecognizer mTextRecognizer;

    static private int convertRotation(float rotation) {
        return Math.round(((rotation + 360) % 360) / 90);
    }

    OcrDetector(Context context) {
        mContext = context;
        setup();
    }

    void setup() {
        if (mTextRecognizer != null) return;

        Context context = mContext.getApplicationContext();
        int code = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(context);
        if (code == ConnectionResult.SUCCESS || code == ConnectionResult.SERVICE_UPDATING ||
                code == ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED)
            mTextRecognizer = new TextRecognizer.Builder(context).build();
        if (mTextRecognizer == null || !mTextRecognizer.isOperational()) {
            Toast.makeText(mContext,
                    R.string.ocr_loading_error,
                    Toast.LENGTH_LONG).show();
            release();
            mDetectorStatus = Status.Failed;
        } else {
            mDetectorStatus = Status.Available;
        }
    }

    void release(){
        if (mTextRecognizer != null) {
            mDetectorStatus = Status.Released;
            mTextRecognizer.release();
            mTextRecognizer = null;
        }
    }

    SparseArray<TextBlock> detect(Bitmap imageBitmap, float rotation) {
        if (mTextRecognizer != null) {
            Frame imageFrame = new Frame.Builder().setBitmap(imageBitmap)
                    .setRotation(OcrDetector.convertRotation(rotation)).build();
            return mTextRecognizer.detect(imageFrame);
        }
        return null;
    }
}
