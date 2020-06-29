package com.sharebounds.sharebounds.camera;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.util.SparseArray;
import com.google.android.gms.vision.text.Text;
import com.google.android.gms.vision.text.TextBlock;
import java.util.ArrayList;
import java.util.List;

class ImageModel {

    Bitmap bitmap;
    byte[] bytes;
    ImageData imageData;
    private SparseArray<TextBlock> mTextBlocks;

    ImageModel(Bitmap bitmap, byte[] bytes, ImageData imageData) {
        this.bitmap = bitmap;
        this.bytes = bytes;
        this.imageData = imageData;
    }

    void setTextBlocks(SparseArray<TextBlock> textBlocks) {
        this.mTextBlocks = textBlocks;
    }

    List<RectF> getTextLines() {
        float fixRotation = imageData.fixRotation;
        Matrix matrix = CameraUtils.fixRotationMatrix(fixRotation, bitmap.getWidth(), bitmap.getHeight());

        List<RectF> lines = new ArrayList<>();
        if (mTextBlocks != null) {
            for (int i = 0; i < mTextBlocks.size(); i++) {
                for (Text currentLine : mTextBlocks.valueAt(i).getComponents()) {
                    RectF boundingBox = new RectF(currentLine.getBoundingBox());
                    if (fixRotation != 0) {
                        matrix.mapRect(boundingBox);
                    }
                    lines.add(boundingBox);
                }
            }
        }
        return lines;
    }

    OcrData getGestureText(float[] touchPoint, ImageGestures.GestureType type) {
        if (mTextBlocks != null) for (int i = 0; i < mTextBlocks.size(); i++) {
            TextBlock textBlock = mTextBlocks.valueAt(i);
            if (textBlock.getBoundingBox().contains((int) touchPoint[0], (int) touchPoint[1])){
                if (type == ImageGestures.GestureType.LONG_PRESS){
                    return new OcrData(textBlock.getValue(), textBlock.getBoundingBox());
                }
                for (Text currentLine : textBlock.getComponents()) {
                    if (currentLine.getBoundingBox().contains((int) touchPoint[0], (int) touchPoint[1])) {
                        if (type == ImageGestures.GestureType.DOUBLE_TAP &&
                                currentLine.getBoundingBox().contains((int) touchPoint[2],
                                        (int) touchPoint[3])) {
                            return new OcrData(currentLine.getValue(), currentLine.getBoundingBox());
                        }
                        for (Text currentWord : currentLine.getComponents()) {
                            if (currentWord.getBoundingBox().contains((int) touchPoint[0],
                                    (int) touchPoint[1])) {
                                return new OcrData(currentWord.getValue(), currentWord.getBoundingBox());
                            }
                        }
                    }
                }
            }
        }
        return null;
    }
}
