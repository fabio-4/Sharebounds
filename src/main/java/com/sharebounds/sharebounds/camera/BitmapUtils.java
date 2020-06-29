package com.sharebounds.sharebounds.camera;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;

import com.sharebounds.sharebounds.AppSettings;
import com.sharebounds.sharebounds.R;

import java.util.List;

class BitmapUtils {

    static Bitmap convertToBitmap(byte[] bytes, ImageData imageData) {
        final Bitmap convertedBitmap = BitmapUtils.convertToBitmap(bytes);
        return BitmapUtils.rotateScale(convertedBitmap, imageData);
    }

    private static Bitmap convertToBitmap(byte[] bytes) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inMutable = true;
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length, options);
    }

    private static Bitmap rotateScale(Bitmap bitmap, ImageData imageData) {
        Float rotation = imageData.rotation + imageData.fixRotation;
        float[] matrixValues = imageData.matrixValues;
        boolean portrait = imageData.portrait;

        Matrix rotMatrix = new Matrix();
        rotMatrix.postRotate(rotation);
        int newW, newH;
        if (rotation % 180 != 0 || portrait) {
            newW = (int) (bitmap.getWidth() / matrixValues[4]);
            newH = (int) (bitmap.getHeight() / matrixValues[0]);
        } else {
            newW = (int) (bitmap.getWidth() / matrixValues[0]);
            newH = (int) (bitmap.getHeight() / matrixValues[4]);
        }
        int x = (bitmap.getWidth() - newW) / 2;
        int y = (bitmap.getHeight() - newH) / 2;

        Bitmap rotatedBitmap = Bitmap.createBitmap(bitmap, x, y, newW, newH, rotMatrix, true);
        bitmap.recycle();
        return rotatedBitmap;
    }

    static Bitmap mutableCopy(Bitmap bitmap) {
        return bitmap.copy(Bitmap.Config.ARGB_8888, true);
    }

    private Context mContext;
    private Paint mPaint = new Paint();

    BitmapUtils(Context context) {
        mContext = context;
        int theme = AppSettings.getInstance(context.getApplicationContext()).getTheme();
        setTheme(theme);
    }

    void setTheme(int theme) {
        int color;
        switch (theme) {
            case 1:
                color = R.color.colorDarkAccent;
                break;
            default:
                color = R.color.colorAccent;
        }
        mPaint.setColor(mContext.getResources().getColor(color));
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setAlpha(150);
        mPaint.setAntiAlias(true);
        mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_OVER));
    }

    void drawImageRect(Bitmap imageBitmap, List<RectF> lines) {
        Canvas canvas = new Canvas(imageBitmap);
        for (RectF line: lines) {
            float round = line.height() / 8;
            canvas.drawRoundRect(line, round, round, mPaint);
        }
    }
}
