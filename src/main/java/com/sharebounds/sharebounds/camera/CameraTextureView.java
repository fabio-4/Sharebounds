package com.sharebounds.sharebounds.camera;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.util.AttributeSet;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;

public class CameraTextureView extends TextureView {

    public CameraTextureView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    void reset() {
        if (isAvailable()) {
            Surface surface = null;
            try {
                surface = new Surface(getSurfaceTexture());
                if (surface.isValid()) {
                    Canvas canvas = surface.lockCanvas(null);
                    if (canvas != null) {
                        canvas.drawColor(Color.BLACK);
                        surface.unlockCanvasAndPost(canvas);
                    }
                }
            } catch (Surface.OutOfResourcesException e) { //
            } finally {
                if (surface != null) surface.release();
            }
        }
    }

    void visible(boolean bool) {
        if (bool) {
            this.animate().cancel();
            if (this.getAlpha() == 0.0f) this.animate().alpha(1.0f).setDuration(0)
                    .setStartDelay(300).withStartAction(new Runnable() {
                @Override
                public void run() {
                    CameraTextureView.this.setVisibility(View.VISIBLE);
                }
            });
        }
        else if (this.getAlpha() == 1.0f) this.animate().alpha(0.0f).setDuration(0).setStartDelay(300)
                .withEndAction(new Runnable() {
            @Override
            public void run() {
                CameraTextureView.this.reset();
                CameraTextureView.this.setVisibility(View.INVISIBLE);
            }
        });
    }

    void scalePreview(double aspectRatio, int rotation) {
        Matrix matrix = scaleMatrix(aspectRatio, rotation);
        setTransform(matrix);
    }

    Matrix scaleMatrix(double aspectRatio, int rotation) {
        Matrix matrix = new Matrix();
        int textureWidth = getWidth();
        int textureHeight = getHeight();

        if (rotation % 180 != 0) {
            int tmp = textureWidth;
            textureWidth = textureHeight;
            textureHeight = tmp;
        }
        int newW, newH;
        if (textureHeight > (int)(textureWidth * aspectRatio)) {
            newW = (int)(textureHeight / aspectRatio);
            newH = textureHeight;
        } else {
            newW = textureWidth;
            newH = (int)(textureWidth * aspectRatio);
        }
        float scaleY = (float) newW / (float) textureWidth;
        float scaleX = (float) newH / (float) textureHeight;
        int transX = (textureHeight - (int) (textureHeight * scaleX)) / 2;
        int transY = (textureWidth - (int) (textureWidth * scaleY)) / 2;

        if (rotation % 180 != 0) {
            matrix.setScale(scaleX, scaleY);
            matrix.postTranslate(transX, transY);
        } else {
            matrix.setScale(scaleY, scaleX);
            matrix.postTranslate(transY, transX);
        }
        return matrix;
    }

    @Override
    public boolean performClick() {
        return super.performClick();
    }
}
